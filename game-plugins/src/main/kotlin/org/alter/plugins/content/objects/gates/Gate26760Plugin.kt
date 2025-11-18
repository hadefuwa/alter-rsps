package org.alter.plugins.content.objects.gates

import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.move.MovementQueue
import org.alter.game.model.move.walkTo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Resource Area Gate Plugin (Gate 26760)
 * 
 * This plugin handles the Resource Area gate in the Wilderness, which requires payment to pass through.
 * 
 * Gate Object: gate_26760 (object 26760)
 * Resource Area Location: (3184, 3944, 0)
 * Entry Fee: 7,500 coins (default), reduced with Wilderness Diary completion
 */
class Gate26760Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Gate object ID
         */
        private const val GATE_ID = 26760
        
        /**
         * Resource Area coordinates (inside the gate)
         */
        private val RESOURCE_AREA_INSIDE = Tile(x = 3184, z = 3944, height = 0)
        
        /**
         * Resource Area coordinates (outside the gate)
         * Approximate location - may need adjustment based on actual gate position
         */
        private val RESOURCE_AREA_OUTSIDE = Tile(x = 3184, z = 3943, height = 0)
        
        /**
         * Default entry fee (7,500 coins)
         * TODO: Implement Wilderness Diary discount system
         */
        private const val ENTRY_FEE = 7_500
        
        /**
         * Coins item ID
         */
        private val COINS = getRSCM("item.coins_995")
    }

    init {
        // Handle gate interaction - "open" or "operate" option
        val registeredOptions = mutableSetOf<String>()
        
        // Try with numeric ID first
        try {
            onObjOption(obj = GATE_ID, option = "open", lineOfSightDistance = 2) {
                handleGateInteraction()
            }
            registeredOptions.add("open")
        } catch (e: Exception) {
            // Option might not exist
        }
        
        try {
            onObjOption(obj = GATE_ID, option = "operate", lineOfSightDistance = 2) {
                handleGateInteraction()
            }
            registeredOptions.add("operate")
        } catch (e: Exception) {
            // Option might not exist
        }
        
        // Try with RSCM name
        try {
            if (!registeredOptions.contains("open")) {
                onObjOption(obj = "obj.gate_26760", option = "open", lineOfSightDistance = 2) {
                    handleGateInteraction()
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
        
        try {
            if (!registeredOptions.contains("operate")) {
                onObjOption(obj = "obj.gate_26760", option = "operate", lineOfSightDistance = 2) {
                    handleGateInteraction()
                }
            }
        } catch (e: Exception) {
            // Options might not exist
        }
    }
    
    /**
     * Handle gate interaction - check payment and allow passage
     */
    private fun Plugin.handleGateInteraction() {
        val coinsCount = player.inventory.getItemCount(COINS)
        
        if (coinsCount < ENTRY_FEE) {
            player.queue {
                player.message("You need ${ENTRY_FEE} coins to enter the Resource Area.")
                player.message("You only have ${coinsCount} coins.")
            }
            return
        }
        
        // Determine if player is going in or out
        val isGoingIn = player.tile.z < RESOURCE_AREA_INSIDE.z || 
                       (player.tile.z == RESOURCE_AREA_INSIDE.z && player.tile.x < RESOURCE_AREA_INSIDE.x)
        
        val targetTile = if (isGoingIn) {
            RESOURCE_AREA_INSIDE
        } else {
            RESOURCE_AREA_OUTSIDE
        }
        
        player.queue {
            // Remove payment
            player.inventory.remove(COINS, ENTRY_FEE)
            player.message("You pay ${ENTRY_FEE} coins to enter the Resource Area.")
            
            // Play sound
            player.playSound(Sound.OPEN_DOOR_SFX)
            
            // Open the gate temporarily
            val obj = player.getInteractingGameObj()
            val oldRot = obj.rot
            
            // Remove closed gate
            world.remove(obj)
            
            // Spawn opened gate (rotate 90 degrees)
            val openedGate = DynamicObject(
                id = GATE_ID, // Same ID, different rotation
                type = obj.type,
                rot = (oldRot + 1) % 4,
                tile = obj.tile
            )
            world.spawn(openedGate)
            
            // Move player through
            player.walkTo(targetTile.x, targetTile.z, MovementQueue.StepType.FORCED_WALK)
            wait(3)
            
            // Close the gate
            world.remove(openedGate)
            val closedGate = DynamicObject(
                id = GATE_ID,
                type = obj.type,
                rot = oldRot,
                tile = obj.tile
            )
            world.spawn(closedGate)
            
            player.playSound(Sound.CLOSE_DOOR_SFX)
            
            if (isGoingIn) {
                player.message("You pass through the gate and enter the Resource Area.")
            } else {
                player.message("You pass through the gate and leave the Resource Area.")
            }
        }
    }
}

