package org.alter.game.saving

import io.github.oshai.kotlinlogging.KotlinLogging
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import org.alter.game.GameContext
import org.alter.game.model.PlayerUID
import org.alter.game.model.attr.APPEARANCE_SET_ATTR
import org.alter.game.model.attr.NEW_ACCOUNT_ATTR
import org.alter.game.model.entity.Client
import org.alter.game.saving.impl.*
import org.alter.game.saving.formats.FormatHandler
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt

object PlayerSaving {

    private val logger = KotlinLogging.logger {}

    lateinit var serialization : FormatHandler

    private val documents = linkedSetOf(
        DetailSerialisation(),
        AppearanceSerialisation(),
        SkillSerialisation(),
        AttributeSerialisation(),
        TimerSerialisation(),
        ContainersSerialisation(),
        VarpSerialisation(),
    )

    fun init(gameContext: GameContext) {
        serialization = gameContext.saveFormat.createInstance("details")

        logger.info { "Player Save Format : ${gameContext.saveFormat.name}" }

        serialization.init()

    }

    fun savePlayer(player: Client) {
        try {
            val currentPosition = player.tile
            logger.debug { "Saving player ${player.username} at position (${currentPosition.x}, ${currentPosition.z}, ${currentPosition.height})" }
            
            val doc = Document().apply {
                append("loginUsername", player.loginUsername)
                append("passwordHash", player.passwordHash)
                append("previousXteas", player.currentXteaKeys.asList())

                append("attributes", Document().also { attrs ->
                    documents.forEach { encoder ->
                        try {
                            val encoderDoc = encoder.asDocument(player)
                            attrs.append(encoder.name, encoderDoc)
                            // Verify position is being saved
                            if (encoder.name == "details") {
                                try {
                                    // BSON may store integers as Long or Number, so we need to handle type conversion
                                    val tileList = encoderDoc.getList("tile", Any::class.java)
                                    if (tileList == null || tileList.size < 3) {
                                        logger.error { "CRITICAL: Position not properly saved for player ${player.username} - tile data is missing or invalid!" }
                                    } else {
                                        // Convert to integers safely
                                        val x = (tileList[0] as? Number)?.toInt() ?: tileList[0] as Int
                                        val z = (tileList[1] as? Number)?.toInt() ?: tileList[1] as Int
                                        val h = (tileList[2] as? Number)?.toInt() ?: tileList[2] as Int
                                        logger.debug { "Position saved for player ${player.username}: ($x, $z, $h)" }
                                    }
                                } catch (e: Exception) {
                                    logger.error(e) { "Failed to verify position data for player ${player.username}" }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to serialize ${encoder.name} for player ${player.username}" }
                            throw e
                        }
                    }
                })
            }
            serialization.saveDocument(player, doc)
            logger.debug { "Successfully saved player ${player.username} to file" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save player ${player.username}: ${e.message}" }
            throw e
        }
    }

    fun loadPlayer(client: Client, block: LoginBlock<*>): PlayerLoadResult {
        if (!PlayerDetails.playerExists(client)) {
            val registered = PlayerDetails.registerAccount(client)
            if (!registered) {
                return PlayerLoadResult.INVALID_CREDENTIALS
            }
            configureNewPlayer(client, block)
            client.uid = PlayerUID(client.loginUsername)
            savePlayer(client)
            return PlayerLoadResult.NEW_ACCOUNT
        }

        return try {

            if (PlayerDetails.getDisplayName(client.loginUsername) == null) {
                return PlayerLoadResult.INVALID_CREDENTIALS
            }

            val document = serialization.parseDocument(client)
            client.loginUsername = document.getString("loginUsername")
            client.passwordHash = document.getString("passwordHash")
            val previousXteas = document.getList("previousXteas", Any::class.java).map { it as Int }.toIntArray()

            val authentication = validateAuthentication(previousXteas, client, block)
            if (authentication != PlayerLoadResult.LOAD_ACCOUNT) {
                return authentication
            }

            client.username = PlayerDetails.getDisplayName(client.loginUsername)?.currentDisplayName?: client.loginUsername

            client.uid = PlayerUID(client.username)

            // Load attributes - this includes position data
            val attributes = document.get("attributes", Document::class.java)
            if (attributes == null) {
                logger.error { "No attributes found in save file for player ${client.loginUsername} - save file may be corrupted" }
                return PlayerLoadResult.MALFORMED
            }
            
            // Log position before loading to verify it's in the save file
            val detailsDoc = attributes.get("details", Document::class.java)
            if (detailsDoc != null) {
                val tileData = try {
                    // BSON may store integers as Long or Number, so handle type conversion
                    val tileList = detailsDoc.getList("tile", Any::class.java)
                    if (tileList != null && tileList.size >= 3) {
                        listOf(
                            (tileList[0] as? Number)?.toInt() ?: tileList[0] as Int,
                            (tileList[1] as? Number)?.toInt() ?: tileList[1] as Int,
                            (tileList[2] as? Number)?.toInt() ?: tileList[2] as Int
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to parse tile data for player ${client.loginUsername}: ${e.message}" }
                    null
                }
                if (tileData != null && tileData.size >= 3) {
                    logger.info { "Loading player ${client.loginUsername} from position (${tileData[0]}, ${tileData[1]}, ${tileData[2]})" }
                } else {
                    logger.warn { "Player ${client.loginUsername} save file missing position data - will spawn at home" }
                }
            } else {
                logger.warn { "Player ${client.loginUsername} save file missing 'details' document - will spawn at home" }
            }
            
            if (!loadAttributes(client, attributes)) {
                logger.error { "Failed to load attributes for player ${client.loginUsername}" }
                return PlayerLoadResult.MALFORMED
            }
            
            // Log position after loading to verify it was set correctly
            logger.info { "Player ${client.loginUsername} loaded at position (${client.tile.x}, ${client.tile.z}, ${client.tile.height})" }

            PlayerLoadResult.LOAD_ACCOUNT
        } catch (e: Exception) {
            logger.error(e) { "Error when loading player: ${client.loginUsername}" }
            PlayerLoadResult.MALFORMED
        }
    }

    private fun validateAuthentication(previousXteas : IntArray, client: Client, block: LoginBlock<*>): PlayerLoadResult {
        when (val auth = block.authentication) {
            is AuthenticationType.PasswordAuthentication<*> -> {
                if (!BCrypt.checkpw(auth.password.asString(), client.passwordHash)) {
                    return PlayerLoadResult.INVALID_CREDENTIALS
                }
            }
            is XteaKey -> {
                if (!previousXteas.contentEquals(auth.key)) {
                    return PlayerLoadResult.INVALID_RECONNECTION
                }
            }
        }
        return PlayerLoadResult.LOAD_ACCOUNT
    }

    private fun loadAttributes(client: Client, attributes: Document?): Boolean {
        return try {
            attributes?.let {
                documents.forEach { decoder ->
                    val attrDoc = attributes.get(decoder.name, Document::class.java)
                    if (attrDoc != null) {
                        try {
                            decoder.fromDocument(client, attrDoc)
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to decode ${decoder.name} for player ${client.loginUsername}: ${e.message}" }
                            // For critical attributes like "details" (position), we should fail
                            // For non-critical attributes, we could continue but log the error
                            if (decoder.name == "details") {
                                logger.error { "Critical attribute 'details' failed to load for ${client.loginUsername} - position will be set to home" }
                                throw e // Re-throw for critical attributes
                            }
                        }
                    } else {
                        // Missing attribute document - log warning but continue for non-critical attributes
                        logger.warn { "Missing attribute document '${decoder.name}' for player ${client.loginUsername}" }
                        if (decoder.name == "details") {
                            // Details (position) is critical - fail if missing
                            logger.error { "Critical attribute 'details' is missing for ${client.loginUsername} - cannot load player" }
                            return false
                        }
                    }
                }
            } ?: run {
                logger.error { "Attributes document is null for player ${client.loginUsername}" }
                return false
            }
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode attributes for client: ${client.loginUsername}" }
            false
        }
    }
    private fun configureNewPlayer(client: Client, block: LoginBlock<*>) {
        client.attr.put(NEW_ACCOUNT_ATTR, true)
        client.attr.put(APPEARANCE_SET_ATTR, false)

        if (block.authentication is AuthenticationType.PasswordAuthentication<*>) {
            val passwordAuth = block.authentication as AuthenticationType.PasswordAuthentication<*>
            client.passwordHash = BCrypt.hashpw(passwordAuth.password.asString(), BCrypt.gensalt(16))
        }
        client.tile = client.world.gameContext.home
    }
}
