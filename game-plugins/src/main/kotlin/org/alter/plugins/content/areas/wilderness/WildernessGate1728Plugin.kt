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
        try {
            val gateDef = getObject(CLOSED_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }

            gateOptions.forEach { option: String ->
                if (option == "open" || option == "operate") {
                    onObjOption(obj = CLOSED_GATE, option = option, lineOfSightDistance = 1) {
                        openGate(this)
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, use RSCM name instead
        }

        // Handle using RSCM name for closed gate
        try {
            if (objHasOption("object.gate_1728", "open")) {
                onObjOption(obj = "object.gate_1728", option = "open", lineOfSightDistance = 1) {
                    openGate()
                }
            }
            
            if (objHasOption("object.gate_1728", "operate")) {
                onObjOption(obj = "object.gate_1728", option = "operate", lineOfSightDistance = 1) {
                    openGate()
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }

        // Handle gate 1729 (opened state) - close action
        try {
            val gateDef = getObject(OPENED_GATE)
            val gateOptions: List<String> = gateDef.actions.filterNotNull().map { it.toLowerCase() }

            gateOptions.forEach { option: String ->
                if (option == "close" || option == "operate") {
                    onObjOption(obj = OPENED_GATE, option = option, lineOfSightDistance = 1) {
                        closeGate(this)
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, use RSCM name instead
        }

        // Handle using RSCM name for opened gate (if it exists)
        try {
            if (objHasOption("object.null_1729", "close")) {
                onObjOption(obj = "object.null_1729", option = "close", lineOfSightDistance = 1) {
                    closeGate()
                }
            }
            
            if (objHasOption("object.null_1729", "operate")) {
                onObjOption(obj = "object.null_1729", option = "operate", lineOfSightDistance = 1) {
                    closeGate()
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
    }
}

