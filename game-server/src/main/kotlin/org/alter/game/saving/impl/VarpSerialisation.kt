package org.alter.game.saving.impl

import dev.openrune.cache.CacheManager.getVarbitOrDefault
import org.alter.game.model.entity.Client
import org.alter.game.saving.DocumentHandler
import org.bson.Document

class VarpSerialisation(override val name: String = "varps") : DocumentHandler {

    // Cache the keybinding varp IDs on first use
    private val keybindingVarpIds: Set<Int> by lazy {
        // Keybinding varbit IDs from Hotkey.kt
        val keybindingVarbits = listOf(
            4675, // COMBAT (ATTACK_KEYBIND)
            4676, // SKILLS
            4677, // QUESTS
            4678, // INVENTORY
            4679, // EQUIPMENT
            4680, // PRAYERS
            4681, // ESC_CLOSES_INTERFACES
            4682, // MAGIC
            4683, // CLAN_CHAT
            4684, // SOCIAL (FRIENDS)
            4686, // SETTINGS
            4687, // EMOTES
            4688, // MUSIC
            4689, // LOG_OUT
            4690, // FOCUSED_HOTKEY_VARBIT
            6517  // ACCOUNT_MANAGEMENT (PROFILE)
        )

        // Get the varp IDs that these varbits map to
        keybindingVarbits.mapNotNull { varbitId ->
            try {
                val varbitDef = getVarbitOrDefault(varbitId)
                if (varbitDef.id != -1) varbitDef.varp else null
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    override fun fromDocument(client: Client, doc: Document) = doc.forEach { idKey, stateValue ->
        idKey.toIntOrNull()?.let { id ->
            stateValue?.toString()?.toIntOrNull()?.let { state ->
                client.varps.setState(id, state)
            }
        }
    }

    override fun asDocument(client: Client): Document {
        return Document().apply {
            putAll(client.varps.getAll()
                .filter { varp ->
                    // Always save F key binding varps, even if they're 0
                    // These are the varps that the keybinding varbits map to
                    varp.state != 0 || varp.id in keybindingVarpIds
                }
                .associate { it.id.toString() to it.state.toString() })
        }
    }

}