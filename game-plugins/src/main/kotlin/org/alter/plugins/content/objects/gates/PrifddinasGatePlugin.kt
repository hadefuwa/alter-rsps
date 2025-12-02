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
import org.alter.rscm.RSCM.getRSCM

/**
 * Prifddinas Gate Plugin
 * 
 * This plugin handles the Prifddinas gate (object 36519), which teleports players
 * into the crystal city of Prifddinas when interacted with.
 * 
 * Gate Object: city_gate_36519 (object 36519)
 * Prifddinas Location: Main entrance area (2200, 3356)
 */
class PrifddinasGatePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Prifddinas main entrance coordinates
         * This is where players teleport to when passing through the gate
         */
        private val PRIFDDINAS_ENTRANCE = Tile(x = 2200, z = 3356, height = 0)
        
        /**
         * Gate object ID
         */
        private const val PRIFDDINAS_GATE = 36519
    }

    init {
        // Function to teleport player to Prifddinas
        val teleportToPrifddinas: Plugin.() -> Unit = {
            player.queue {
                player.message("You pass through the gate and enter Prifddinas.")
                player.teleport(PRIFDDINAS_ENTRANCE, TeleportType.MODERN)
            }
        }

        // Handle gate 36519 - try multiple options
        // Track registered options to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        var numericOptionRegistered = false
        
        // Try string options from object definition first (more specific)
        try {
            val gateDef = getObject(PRIFDDINAS_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }

            gateOptions.forEach { option: String ->
                if ((option == "open" || option == "enter" || option == "pass-through" || option == "use" || option == "operate") && !registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = PRIFDDINAS_GATE, option = option, lineOfSightDistance = 1) {
                            teleportToPrifddinas()
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
            if (objHasOption("object.city_gate_36519", "open") && !registeredOptions.contains("open")) {
                try {
                    onObjOption(obj = "object.city_gate_36519", option = "open", lineOfSightDistance = 1) {
                        teleportToPrifddinas()
                    }
                    registeredOptions.add("open")
                } catch (e: IllegalStateException) {
                    // Option already bound
                }
            }
            
            if (objHasOption("object.city_gate_36519", "enter") && !registeredOptions.contains("enter")) {
                try {
                    onObjOption(obj = "object.city_gate_36519", option = "enter", lineOfSightDistance = 1) {
                        teleportToPrifddinas()
                    }
                    registeredOptions.add("enter")
                } catch (e: IllegalStateException) {
                    // Option already bound
                }
            }
            
            if (objHasOption("object.city_gate_36519", "use") && !registeredOptions.contains("use")) {
                try {
                    onObjOption(obj = "object.city_gate_36519", option = "use", lineOfSightDistance = 1) {
                        teleportToPrifddinas()
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
        // This prevents conflicts where "enter" (string) and 1 (numeric) both map to option 1
        if (registeredOptions.isEmpty()) {
            try {
                onObjOption(obj = "object.city_gate_36519", option = 1, lineOfSightDistance = 1) {
                    teleportToPrifddinas()
                }
                numericOptionRegistered = true
            } catch (e: IllegalStateException) {
                // Option 1 already bound, skip
            } catch (e: Exception) {
                // Could not register option 1
            }
        }
    }
}

