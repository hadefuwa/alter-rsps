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

/**
 * Gate 26419 Plugin
 * 
 * This plugin handles gate 26419, which teleports players to coordinates (2881, 5309, level 2)
 * when interacted with.
 * 
 * Gate Object: null_26419 (object 26419)
 * Teleport Destination: (2881, 5309, height = 2)
 */
class Gate26419Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Teleport destination coordinates
         */
        private val TELEPORT_DESTINATION = Tile(x = 2881, z = 5309, height = 2)
        
        /**
         * Gate object ID
         */
        private const val GATE_ID = 26419
    }

    init {
        // Function to teleport player
        val teleportPlayer: Plugin.() -> Unit = {
            player.queue {
                player.message("You pass through the gate.")
                player.teleport(TELEPORT_DESTINATION, TeleportType.MODERN)
            }
        }

        // Handle gate 26419 - try multiple options
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

        // Handle using RSCM name for gate (only if not already registered)
        try {
            if (objHasOption("object.null_26419", "open") && !registeredOptions.contains("open")) {
                try {
                    onObjOption(obj = "object.null_26419", option = "open", lineOfSightDistance = 1) {
                        teleportPlayer()
                    }
                    registeredOptions.add("open")
                } catch (e: IllegalStateException) {
                    // Option already bound
                }
            }
            
            if (objHasOption("object.null_26419", "enter") && !registeredOptions.contains("enter")) {
                try {
                    onObjOption(obj = "object.null_26419", option = "enter", lineOfSightDistance = 1) {
                        teleportPlayer()
                    }
                    registeredOptions.add("enter")
                } catch (e: IllegalStateException) {
                    // Option already bound
                }
            }
            
            if (objHasOption("object.null_26419", "use") && !registeredOptions.contains("use")) {
                try {
                    onObjOption(obj = "object.null_26419", option = "use", lineOfSightDistance = 1) {
                        teleportPlayer()
                    }
                    registeredOptions.add("use")
                } catch (e: IllegalStateException) {
                    // Option already bound
                }
            }
        } catch (e: Exception) {
            // Could not register via RSCM name
        }
        
        // Only try numeric option 1 as a fallback if no string options were registered
        if (registeredOptions.isEmpty()) {
            try {
                onObjOption(obj = "object.null_26419", option = 1, lineOfSightDistance = 1) {
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

