package org.alter.plugins.content.areas.edgeville

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Edgeville Lever Plugin
 * 
 * This plugin handles the Edgeville lever (object 26761) interaction,
 * allowing players to teleport to the Wilderness lever location.
 * 
 * The lever is located in Edgeville and provides quick access to
 * the deep wilderness for PvP activities.
 * 
 * Edgeville Lever Location: Around 3090, 3470
 * Wilderness Lever Location: 3153, 3923 (Level 50 Wilderness)
 */
class EdgevilleLeverPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Wilderness lever destination coordinates
         * This is where players teleport to when using the Edgeville lever
         */
        private val WILDERNESS_LEVER_DESTINATION = Tile(x = 3153, z = 3923, height = 0)
        
        /**
         * Edgeville lever location (for return teleport)
         */
        private val EDGEVILLE_LEVER_LOCATION = Tile(x = 3090, z = 3470, height = 0)
    }

    init {
        // Function to teleport player to wilderness lever
        val pullLeverToWilderness: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                // Pull lever animation
                player.animate(Animation.PULL_LEVER)
                player.message("You pull the lever...")
                
                // Wait for animation
                wait(2)
                
                // Teleport to wilderness lever location
                player.prepareForTeleport()
                player.moveTo(WILDERNESS_LEVER_DESTINATION)
                player.message("You teleport to the wilderness lever.")
            }
        }

        // Function to teleport player back to Edgeville (from wilderness lever)
        val pullLeverToEdgeville: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                // Pull lever animation
                player.animate(Animation.PULL_LEVER)
                player.message("You pull the lever...")
                
                // Wait for animation
                wait(2)
                
                // Teleport back to Edgeville lever location
                player.prepareForTeleport()
                player.moveTo(EDGEVILLE_LEVER_LOCATION)
                player.message("You teleport back to Edgeville.")
            }
        }

        // Handle Edgeville lever (object 26761) - check what options it has
        try {
            val leverDef = getObject(26761)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                // Check if we're at Edgeville lever location (teleport to wilderness)
                onObjOption(obj = 26761, option = option) {
                    val obj = player.getInteractingGameObj()
                    val playerTile = player.tile
                    
                    // Check if player is near Edgeville lever (around 3090, 3470)
                    // If player is in wilderness (Z > 3520), teleport to Edgeville
                    // Otherwise, teleport to wilderness
                    if (playerTile.z > 3520) {
                        // Player is in wilderness, teleport back to Edgeville
                        pullLeverToEdgeville()
                    } else {
                        // Player is in Edgeville, teleport to wilderness
                        pullLeverToWilderness()
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, that's okay - plugin will still load
            // We'll use the RSCM name instead
        }

        // Also handle using RSCM name
        try {
            if (objHasOption("object.lever_26761", "pull")) {
                onObjOption(obj = "object.lever_26761", option = "pull") {
                    val playerTile = player.tile
                    if (playerTile.z > 3520) {
                        pullLeverToEdgeville()
                    } else {
                        pullLeverToWilderness()
                    }
                }
            }
            
            if (objHasOption("object.lever_26761", "operate")) {
                onObjOption(obj = "object.lever_26761", option = "operate") {
                    val playerTile = player.tile
                    if (playerTile.z > 3520) {
                        pullLeverToEdgeville()
                    } else {
                        pullLeverToWilderness()
                    }
                }
            }
        } catch (e: Exception) {
            // Options might not exist, that's okay
        }
    }
}

