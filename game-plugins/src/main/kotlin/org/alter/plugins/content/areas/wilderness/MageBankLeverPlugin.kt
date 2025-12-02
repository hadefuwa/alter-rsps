package org.alter.plugins.content.areas.wilderness

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Mage Bank Lever Plugin
 * 
 * This plugin handles the Mage Bank lever (object 5960) interaction,
 * allowing players to teleport between the Wilderness lever location
 * and the Mage Bank area.
 * 
 * The lever works bidirectionally:
 * - From Wilderness (level 50): Teleports to Mage Bank
 * - From Mage Bank: Teleports back to Wilderness lever location
 * 
 * Wilderness Lever Location: Around 3090, 3956 (Level 50 Wilderness)
 * Mage Bank Location: 2539, 4716
 */
class MageBankLeverPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Mage Bank destination coordinates
         * This is where players teleport to when using the wilderness lever
         */
        private val MAGE_BANK_DESTINATION = Tile(x = 2539, z = 4716, height = 0)
        
        /**
         * Wilderness lever location (for return teleport from mage bank)
         */
        private val WILDERNESS_LEVER_LOCATION = Tile(x = 3090, z = 3956, height = 0)
        
        /**
         * Approximate coordinates to determine if player is at wilderness lever
         * Using a range check since exact coordinates may vary slightly
         */
        private const val WILDERNESS_LEVER_X_MIN = 3085
        private const val WILDERNESS_LEVER_X_MAX = 3095
        private const val WILDERNESS_LEVER_Z_MIN = 3951
        private const val WILDERNESS_LEVER_Z_MAX = 3961
        
        /**
         * Approximate coordinates to determine if player is at mage bank lever
         */
        private const val MAGE_BANK_LEVER_X_MIN = 2534
        private const val MAGE_BANK_LEVER_X_MAX = 2544
        private const val MAGE_BANK_LEVER_Z_MIN = 4711
        private const val MAGE_BANK_LEVER_Z_MAX = 4721
    }

    init {
        // Function to teleport player to Mage Bank
        val pullLeverToMageBank: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                // Pull lever animation (mage bank specific)
                player.animate(Animation.PULL_MAGE_BANK_LEVER)
                player.message("You pull the lever...")
                
                // Wait for animation
                wait(2)
                
                // Teleport to Mage Bank
                player.prepareForTeleport()
                player.moveTo(MAGE_BANK_DESTINATION)
                player.message("You teleport to the Mage Bank.")
            }
        }

        // Function to teleport player back to Wilderness lever
        val pullLeverToWilderness: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                // Pull lever animation (mage bank specific)
                player.animate(Animation.PULL_MAGE_BANK_LEVER)
                player.message("You pull the lever...")
                
                // Wait for animation
                wait(2)
                
                // Teleport back to wilderness lever location
                player.prepareForTeleport()
                player.moveTo(WILDERNESS_LEVER_LOCATION)
                player.message("You teleport to the wilderness lever.")
            }
        }

        // Helper function to check if player is at wilderness lever location
        fun isAtWildernessLever(tile: Tile): Boolean {
            return tile.x in WILDERNESS_LEVER_X_MIN..WILDERNESS_LEVER_X_MAX &&
                   tile.z in WILDERNESS_LEVER_Z_MIN..WILDERNESS_LEVER_Z_MAX
        }

        // Helper function to check if player is at mage bank lever location
        fun isAtMageBankLever(tile: Tile): Boolean {
            return tile.x in MAGE_BANK_LEVER_X_MIN..MAGE_BANK_LEVER_X_MAX &&
                   tile.z in MAGE_BANK_LEVER_Z_MIN..MAGE_BANK_LEVER_Z_MAX
        }

        // Handle Mage Bank lever (object 5960) - check what options it has
        // Track registered options to avoid duplicates
        val registeredLeverOptions = mutableSetOf<String>()
        val leverLogic: Plugin.() -> Unit = {
            val playerTile = player.tile
            
            // Determine direction based on player location
            if (isAtWildernessLever(playerTile)) {
                // Player is at wilderness lever, teleport to Mage Bank
                pullLeverToMageBank()
            } else if (isAtMageBankLever(playerTile)) {
                // Player is at mage bank lever, teleport back to wilderness
                pullLeverToWilderness()
            } else {
                // Default: if closer to wilderness coordinates, go to mage bank
                // Otherwise, go to wilderness
                val distToWilderness = kotlin.math.abs(playerTile.x - WILDERNESS_LEVER_LOCATION.x) + 
                                      kotlin.math.abs(playerTile.z - WILDERNESS_LEVER_LOCATION.z)
                val distToMageBank = kotlin.math.abs(playerTile.x - MAGE_BANK_DESTINATION.x) + 
                                    kotlin.math.abs(playerTile.z - MAGE_BANK_DESTINATION.z)
                
                if (distToWilderness < distToMageBank) {
                    pullLeverToMageBank()
                } else {
                    pullLeverToWilderness()
                }
            }
        }
        
        try {
            val leverDef = getObject(5960)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                if (!registeredLeverOptions.contains(option)) {
                    onObjOption(obj = 5960, option = option, logic = leverLogic)
                    registeredLeverOptions.add(option)
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, that's okay - plugin will still load
            // We'll use the RSCM name instead
        }

        // Also handle using RSCM name (only if not already registered)
        try {
            if (objHasOption("object.lever_5960", "pull") && !registeredLeverOptions.contains("pull")) {
                onObjOption(obj = "object.lever_5960", option = "pull", logic = leverLogic)
                registeredLeverOptions.add("pull")
            }
            
            if (objHasOption("object.lever_5960", "operate") && !registeredLeverOptions.contains("operate")) {
                onObjOption(obj = "object.lever_5960", option = "operate", logic = leverLogic)
                registeredLeverOptions.add("operate")
            }
        } catch (e: Exception) {
            // Options might not exist, that's okay
        }
    }
}

