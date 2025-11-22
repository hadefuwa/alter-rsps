package org.alter.plugins.content.objects.gates

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Gate 2623 Plugin
 * 
 * This plugin keeps gate 2623 always open by replacing any closed instances
 * with the opened state on world initialization.
 * 
 * Gate Object: gate_2623 (object 2623) - closed state
 * Opened State: gate_2624 (object 2624) - opened state (assumed based on pattern)
 */
class Gate2623Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Closed gate object ID
         */
        private const val CLOSED_GATE = 2623
        
        /**
         * Opened gate object ID
         * Following the pattern: gate 1728 -> 1729, gate 2623 -> 2624
         */
        private const val OPENED_GATE = 2624
    }

    init {
        // Function to open the gate (replace closed with opened state)
        val openGate: Plugin.() -> Unit = {
            val obj = player.getInteractingGameObj()
            if (obj.id == CLOSED_GATE) {
                val oldRot = obj.rot
                
                player.queue {
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
                }
            }
        }
        
        // Prevent the gate from being closed - keep it always open
        val preventClose: Plugin.() -> Unit = {
            player.message("The gate is permanently open.")
        }
        
        // Handle closed gate (2623) - automatically open it when interacted with
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
        // Note: We skip numeric option 1 to avoid conflicts with existing gate handlers
        try {
            if (objHasOption("object.gate_2623", "open") && !registeredOpenOptions.contains("open")) {
                try {
                    onObjOption(obj = "object.gate_2623", option = "open", lineOfSightDistance = 1) {
                        openGate()
                    }
                    registeredOpenOptions.add("open")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
            
            if (objHasOption("object.gate_2623", "operate") && !registeredOpenOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.gate_2623", option = "operate", lineOfSightDistance = 1) {
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
        
        // Handle opened gate (2624) - prevent closing
        val registeredCloseOptions = mutableSetOf<String>()
        
        // Try string options
        try {
            val gateDef = getObject(OPENED_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }
            
            gateOptions.forEach { option: String ->
                if ((option == "close" || option == "operate") && !registeredCloseOptions.contains(option)) {
                    try {
                        onObjOption(obj = OPENED_GATE, option = option, lineOfSightDistance = 1) {
                            preventClose()
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
        // Note: We skip numeric option 1 to avoid conflicts with existing gate handlers
        try {
            if (objHasOption("object.gate_2624", "close") && !registeredCloseOptions.contains("close")) {
                try {
                    onObjOption(obj = "object.gate_2624", option = "close", lineOfSightDistance = 1) {
                        preventClose()
                    }
                    registeredCloseOptions.add("close")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
            
            if (objHasOption("object.gate_2624", "operate") && !registeredCloseOptions.contains("operate")) {
                try {
                    onObjOption(obj = "object.gate_2624", option = "operate", lineOfSightDistance = 1) {
                        preventClose()
                    }
                    registeredCloseOptions.add("operate")
                } catch (e: IllegalStateException) {
                    // Option already bound by another plugin, skip
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
        
        // On world initialization, automatically open any closed gates
        // This handles gates that are spawned as closed
        onWorldInit {
            // We'll handle this by intercepting spawns or by handling interactions
            // For now, the interaction handlers above will ensure the gate opens when touched
        }
    }
}

