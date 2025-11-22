package org.alter.plugins.content.objects.gates

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.TeleportType
import org.alter.plugins.content.magic.teleport
import org.alter.rscm.RSCM.asRSCM

/**
 * Gate 39653 Plugin
 * 
 * This plugin handles gate 39653, which teleports players to coordinates (3141, 3629)
 * when interacted with.
 * 
 * Gate Object: 39653
 * Teleport Destination: (3141, 3629, height = 0)
 */
class Gate39653Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Teleport destination coordinates
         */
        private val TELEPORT_DESTINATION = Tile(x = 3141, z = 3629, height = 0)
        
        /**
         * Gate object ID
         */
        private const val GATE_ID = 39653
    }

    init {
        // Function to teleport player
        val teleportPlayer: Plugin.() -> Unit = {
            player.queue {
                player.message("You pass through the gate.")
                player.teleport(TELEPORT_DESTINATION, TeleportType.MODERN)
            }
        }

        // Handle gate 39653 - try multiple options
        // Track registered options to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Try string options from object definition first (more specific)
        try {
            val gateDef = getObject(GATE_ID)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }

            gateOptions.forEach { option: String ->
                if ((option == "open" || option == "enter" || option == "pass-through" || option == "use" || option == "operate") && !registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = GATE_ID, option = option, lineOfSightDistance = 1) {
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

        // Handle using RSCM name with numeric options as fallback (only if not already registered)
        if (registeredOptions.isEmpty()) {
            try {
                // Try to get RSCM name for the object
                val rscmName = GATE_ID.asRSCM("object")
                try {
                    onObjOption(obj = rscmName, option = 1, lineOfSightDistance = 1) {
                        teleportPlayer()
                    }
                } catch (e: IllegalStateException) {
                    // Option already bound
                } catch (e: Exception) {
                    // Could not register option 1
                }
            } catch (e: Exception) {
                // Could not get RSCM name, try with Int ID and string options as last resort
                try {
                    onObjOption(obj = GATE_ID, option = "1", lineOfSightDistance = 1) {
                        teleportPlayer()
                    }
                } catch (e2: Exception) {
                    // Could not register
                }
            }
        }
    }
}

