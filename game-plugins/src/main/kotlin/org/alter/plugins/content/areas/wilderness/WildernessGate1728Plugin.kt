package org.alter.plugins.content.areas.wilderness

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Wilderness Gate 1728 Plugin
 * 
 * This plugin handles the wilderness wrought iron gate (object 1728),
 * allowing players to open and close it.
 * 
 * Gate Object: gate_1728 (object 1728)
 * Opened State: gate_1729 (object 1729) - even though marked as null in RSCM, it's the opened state
 */
class WildernessGate1728Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Closed gate object ID
         */
        private const val CLOSED_GATE = 1728
        
        /**
         * Opened gate object ID
         */
        private const val OPENED_GATE = 1729
    }

    init {
        // Function to open the gate
        val openGate: Plugin.() -> Unit = {
            val obj = player.getInteractingGameObj()
            val oldRot = obj.rot
            
            player.queue {
                // Play sound
                player.playSound(Sound.OPEN_DOOR_SFX)
                
                // Remove closed gate
                world.remove(obj)
                
                // Spawn opened gate with appropriate rotation
                // When gate opens, it typically rotates and moves slightly
                val newGate = DynamicObject(
                    id = OPENED_GATE,
                    type = obj.type,
                    rot = (oldRot + 1) % 4, // Rotate 90 degrees
                    tile = obj.tile
                )
                
                world.spawn(newGate)
                player.message("You open the gate.")
            }
        }

        // Function to close the gate
        val closeGate: Plugin.() -> Unit = {
            val obj = player.getInteractingGameObj()
            val oldRot = obj.rot
            
            player.queue {
                // Play sound
                player.playSound(Sound.CLOSE_DOOR_SFX)
                
                // Remove opened gate
                world.remove(obj)
                
                // Spawn closed gate with appropriate rotation
                val newGate = DynamicObject(
                    id = CLOSED_GATE,
                    type = obj.type,
                    rot = (oldRot + 3) % 4, // Rotate back 90 degrees
                    tile = obj.tile
                )
                
                world.spawn(newGate)
                player.message("You close the gate.")
            }
        }

        // Handle gate 1728 (closed state) - open action
        // Track registered options to avoid duplicates
        val registeredOpenOptions = mutableSetOf<String>()
        
        // First, get available options from object definition
        val gateOptionsFromDef = try {
            val gateDef = getObject(CLOSED_GATE)
            gateDef.actions.filterNotNull().map { it.toLowerCase() }
        } catch (e: Exception) {
            emptyList<String>()
        }
        
        // Check if option 1 maps to "open" or "operate" to avoid duplicate registration
        val option1MapsTo = try {
            val gateDef = getObject(CLOSED_GATE)
            if (gateDef.actions.size > 0 && gateDef.actions[0] != null) {
                gateDef.actions[0]!!.lowercase()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        // Try numeric option 1 first (most common for gates/doors)
        // Only if it doesn't map to "open" or "operate" (which we'll register as strings)
        if (option1MapsTo != "open" && option1MapsTo != "operate") {
            try {
                onObjOption(obj = "object.gate_1728", option = 1, lineOfSightDistance = 1) {
                    openGate()
                }
                println("WildernessGate1728Plugin: Successfully registered option 1 for gate $CLOSED_GATE")
            } catch (e: IllegalStateException) {
                // Option already bound, skip
                println("WildernessGate1728Plugin: Option 1 already bound for gate $CLOSED_GATE, skipping")
            } catch (e: Exception) {
                println("WildernessGate1728Plugin: Could not register option 1 for gate $CLOSED_GATE: ${e.message}")
            }
        } else {
            // Option 1 maps to "open" or "operate", we'll register it as a string instead to avoid duplicate
            if (option1MapsTo != null) {
                registeredOpenOptions.add(option1MapsTo)
            }
        }
        
        // Register string options from object definition
        gateOptionsFromDef.forEach { option: String ->
            if ((option == "open" || option == "operate") && !registeredOpenOptions.contains(option)) {
                try {
                    onObjOption(obj = CLOSED_GATE, option = option, lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add(option)
                    println("WildernessGate1728Plugin: Successfully registered option '$option' for gate $CLOSED_GATE")
                } catch (e: IllegalStateException) {
                    // Option already bound, skip
                    println("WildernessGate1728Plugin: Option '$option' for gate $CLOSED_GATE already bound, skipping")
                } catch (e: Exception) {
                    println("WildernessGate1728Plugin: Failed to register option '$option' for gate $CLOSED_GATE: ${e.message}")
                }
            }
        }

        // Handle using RSCM name for closed gate (only if not already registered)
        try {
            if (objHasOption("object.gate_1728", "open") && !registeredOpenOptions.contains("open")) {
                try {
                    onObjOption(obj = "object.gate_1728", option = "open", lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add("open")
                    println("WildernessGate1728Plugin: Successfully registered 'open' option via RSCM for gate 1728")
                } catch (e: IllegalStateException) {
                    println("WildernessGate1728Plugin: 'open' option for gate 1728 already bound via RSCM, skipping")
                }
            }
            
            if (objHasOption("object.gate_1728", "operate") && !registeredOpenOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.gate_1728", option = "operate", lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add("operate")
                    println("WildernessGate1728Plugin: Successfully registered 'operate' option via RSCM for gate 1728")
                } catch (e: IllegalStateException) {
                    println("WildernessGate1728Plugin: 'operate' option for gate 1728 already bound via RSCM, skipping")
                }
            }
        } catch (e: Exception) {
            println("WildernessGate1728Plugin: Could not register via RSCM name: ${e.message}")
        }

        // Handle gate 1729 (opened state) - close action
        // Track registered options to avoid duplicates
        val registeredCloseOptions = mutableSetOf<String>()
        
        // Try numeric option 1 first (most common for gates/doors)
        try {
            onObjOption(obj = "object.null_1729", option = 1, lineOfSightDistance = 1) {
                closeGate()
            }
            println("WildernessGate1728Plugin: Successfully registered option 1 for gate $OPENED_GATE")
        } catch (e: IllegalStateException) {
            // Option already bound, skip
            println("WildernessGate1728Plugin: Option 1 already bound for gate $OPENED_GATE, skipping")
        } catch (e: Exception) {
            println("WildernessGate1728Plugin: Could not register option 1 for gate $OPENED_GATE: ${e.message}")
        }
        
        try {
            val gateDef = getObject(OPENED_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }

            gateOptions.forEach { option: String ->
                if ((option == "close" || option == "operate") && !registeredCloseOptions.contains(option)) {
                    try {
                        onObjOption(obj = OPENED_GATE, option = option, lineOfSightDistance = 1) {
                            closeGate()
                        }
                        registeredCloseOptions.add(option)
                        println("WildernessGate1728Plugin: Successfully registered option '$option' for gate $OPENED_GATE")
                    } catch (e: IllegalStateException) {
                        // Option already bound, skip
                        println("WildernessGate1728Plugin: Option '$option' for gate $OPENED_GATE already bound, skipping")
                    } catch (e: Exception) {
                        println("WildernessGate1728Plugin: Failed to register option '$option' for gate $OPENED_GATE: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("WildernessGate1728Plugin: Could not get object definition for gate $OPENED_GATE: ${e.message}")
        }

        // Handle using RSCM name for opened gate (only if not already registered)
        try {
            if (objHasOption("object.null_1729", "close") && !registeredCloseOptions.contains("close")) {
                try {
                    onObjOption(obj = "object.null_1729", option = "close", lineOfSightDistance = 1) {
                        closeGate()
                    }
                    registeredCloseOptions.add("close")
                    println("WildernessGate1728Plugin: Successfully registered 'close' option via RSCM for gate 1729")
                } catch (e: IllegalStateException) {
                    println("WildernessGate1728Plugin: 'close' option for gate 1729 already bound via RSCM, skipping")
                }
            }
            
            if (objHasOption("object.null_1729", "operate") && !registeredCloseOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.null_1729", option = "operate", lineOfSightDistance = 1) {
                        closeGate()
                    }
                    registeredCloseOptions.add("operate")
                    println("WildernessGate1728Plugin: Successfully registered 'operate' option via RSCM for gate 1729")
                } catch (e: IllegalStateException) {
                    println("WildernessGate1728Plugin: 'operate' option for gate 1729 already bound via RSCM, skipping")
                }
            }
        } catch (e: Exception) {
            println("WildernessGate1728Plugin: Could not register via RSCM name for opened gate: ${e.message}")
        }
    }
}

