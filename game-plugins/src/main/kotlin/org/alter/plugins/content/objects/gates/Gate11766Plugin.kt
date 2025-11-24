package org.alter.plugins.content.objects.gates

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Gate 11766 Plugin
 * 
 * This plugin handles gate 11766, allowing players to open and close it.
 * 
 * Gate Object: gate_11766 (object 11766) - closed state
 * Opened State: gate_11767 (object 11767) - opened state
 */
class Gate11766Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Closed gate object ID
         */
        private const val CLOSED_GATE = 11766
        
        /**
         * Opened gate object ID
         */
        private const val OPENED_GATE = 11767
    }

    init {
        // Function to open the gate (replace closed with opened state)
        val openGate: Plugin.() -> Unit = {
            val obj = player.getInteractingGameObj()
            if (obj.id == CLOSED_GATE) {
                val oldRot = obj.rot
                
                player.queue {
                    player.playSound(Sound.OPEN_DOOR_SFX)
                    // Remove closed gate
                    world.remove(obj)
                    
                    // Spawn opened gate with appropriate rotation
                    val openedGate = DynamicObject(
                        id = OPENED_GATE,
                        type = obj.type,
                        rot = (oldRot + 1) % 4, // Rotate 90 degrees
                        tile = obj.tile
                    )
                    
                    world.spawn(openedGate)
                    player.message("You open the gate.")
                }
            }
        }
        
        // Function to close the gate (replace opened with closed state)
        val closeGate: Plugin.() -> Unit = {
            val obj = player.getInteractingGameObj()
            if (obj.id == OPENED_GATE) {
                val oldRot = obj.rot
                
                player.queue {
                    player.playSound(Sound.CLOSE_DOOR_SFX)
                    // Remove opened gate
                    world.remove(obj)
                    
                    // Spawn closed gate with appropriate rotation
                    val closedGate = DynamicObject(
                        id = CLOSED_GATE,
                        type = obj.type,
                        rot = (oldRot + 3) % 4, // Rotate back 90 degrees
                        tile = obj.tile
                    )
                    
                    world.spawn(closedGate)
                    player.message("You close the gate.")
                }
            }
        }
        
        // Handle closed gate (11766) - open it when interacted with
        val registeredOpenOptions = mutableSetOf<String>()
        
        // Try string options
        try {
            val gateDef = getObject(CLOSED_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }
            
            gateOptions.forEach { option: String ->
                if ((option == "open" || option == "operate") && !registeredOpenOptions.contains(option)) {
                    try {
                        onObjOption(obj = CLOSED_GATE, option = option, lineOfSightDistance = 1) {
                            openGate()
                        }
                        registeredOpenOptions.add(option)
                    } catch (e: IllegalStateException) {
                        // Option already bound by another plugin, skip
                    } catch (e: Exception) {
                        // Option might not exist
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache
        }
        
        // Try with RSCM name for closed gate (with string options)
        try {
            if (objHasOption("object.gate_11766", "open") && !registeredOpenOptions.contains("open")) {
                try {
                    onObjOption(obj = "object.gate_11766", option = "open", lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add("open")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
            
            if (objHasOption("object.gate_11766", "operate") && !registeredOpenOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.gate_11766", option = "operate", lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add("operate")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
        
        // Handle opened gate (11767) - close it when interacted with
        val registeredCloseOptions = mutableSetOf<String>()
        
        // Try string options
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
                    } catch (e: IllegalStateException) {
                        // Option already bound by another plugin, skip
                    } catch (e: Exception) {
                        // Option might not exist
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache
        }
        
        // Try with RSCM name for opened gate (with string options)
        try {
            if (objHasOption("object.gate_11767", "close") && !registeredCloseOptions.contains("close")) {
                try {
                    onObjOption(obj = "object.gate_11767", option = "close", lineOfSightDistance = 1) {
                        closeGate()
                    }
                    registeredCloseOptions.add("close")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
            
            if (objHasOption("object.gate_11767", "operate") && !registeredCloseOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.gate_11767", option = "operate", lineOfSightDistance = 1) {
                        closeGate()
                    }
                    registeredCloseOptions.add("operate")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
        
        // Also register numeric options as fallback (using RSCM names)
        try {
            onObjOption(obj = "object.gate_11766", option = 1, lineOfSightDistance = 1) {
                openGate()
            }
        } catch (e: IllegalStateException) {
            // Option already bound by another plugin, skip
        } catch (e: Exception) {
            // Option might not exist
        }
        
        try {
            onObjOption(obj = "object.gate_11767", option = 1, lineOfSightDistance = 1) {
                closeGate()
            }
        } catch (e: IllegalStateException) {
            // Option already bound by another plugin, skip
        } catch (e: Exception) {
            // Option might not exist
        }
    }
}

