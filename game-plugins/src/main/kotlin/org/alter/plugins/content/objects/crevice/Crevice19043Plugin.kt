package org.alter.plugins.content.objects.crevice

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.TeleportType
import org.alter.plugins.content.magic.canTeleport
import org.alter.plugins.content.magic.teleport

/**
 * Crevice 19043 Teleport Plugin
 *
 * This plugin allows object 19043 (crevice_19043) to teleport the player
 * to coordinates 3048, 10339.
 *
 * Usage:
 * Click on the crevice to teleport to the specified location.
 */
class Crevice19043Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Teleport destination coordinates
         */
        private val TELEPORT_DESTINATION = Tile(x = 3048, z = 10339, height = 0)
        
        /**
         * Crevice object ID
         */
        private const val CREVICE_ID = 19043
        private const val CREVICE_OBJECT = "object.crevice_19043"
    }

    init {
        // Function to teleport player
        val teleportPlayer: Plugin.() -> Unit = {
            player.queue {
                if (!player.canTeleport(TeleportType.MODERN)) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                player.message("You squeeze through the crevice and find yourself in a new location.")
                player.teleport(TELEPORT_DESTINATION, TeleportType.MODERN)
            }
        }

        // Track registered options to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Try string options from object definition first (more specific)
        try {
            val creviceDef = getObject(CREVICE_ID)
            val creviceOptions: List<String> = creviceDef.actions.filterNotNull().map { it.toLowerCase() }

            creviceOptions.forEach { option: String ->
                if ((option == "enter" || option == "use" || option == "operate" || option == "squeeze-through" || 
                     option == "climb-through" || option == "pass-through" || option == "squeeze") && 
                    !registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = CREVICE_ID, option = option) {
                            teleportPlayer()
                        }
                        registeredOptions.add(option)
                    } catch (e: IllegalStateException) {
                        // Option already bound, skip
                    } catch (e: Exception) {
                        // Failed to register option
                    }
                }
            }
        } catch (e: Exception) {
            // Could not get object definition
        }

        // Handle using RSCM name for crevice (only if not already registered)
        try {
            val commonOptions = listOf("enter", "use", "operate", "squeeze-through", "climb-through", "pass-through", "squeeze")
            
            for (option in commonOptions) {
                if (objHasOption(CREVICE_OBJECT, option) && !registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = CREVICE_OBJECT, option = option) {
                            teleportPlayer()
                        }
                        registeredOptions.add(option)
                    } catch (e: IllegalStateException) {
                        // Option already bound
                    } catch (e: Exception) {
                        // Failed to register option
                    }
                }
            }
        } catch (e: Exception) {
            // Could not register via RSCM name
        }
        
        // Only try numeric option 1 as a fallback if no string options were registered
        if (registeredOptions.isEmpty()) {
            try {
                onObjOption(obj = CREVICE_OBJECT, option = 1) {
                    teleportPlayer()
                }
            } catch (e: IllegalStateException) {
                // Option already bound
            } catch (e: Exception) {
                // Could not register option 1
            }
        }
    }
}

