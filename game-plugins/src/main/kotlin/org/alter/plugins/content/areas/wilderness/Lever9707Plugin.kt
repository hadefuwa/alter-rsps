package org.alter.plugins.content.areas.wilderness

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Lever 9707 Plugin
 * 
 * This plugin handles lever 9707 interaction,
 * allowing players to teleport to coordinates 3105, 3956.
 */
class Lever9707Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Lever 9707 destination coordinates
         */
        private val LEVER_DESTINATION = Tile(x = 3105, z = 3956, height = 0)
    }

    init {
        // Function to teleport player when lever is pulled
        val pullLever: Plugin.() -> Unit = {
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
                
                // Teleport to destination
                player.prepareForTeleport()
                player.moveTo(LEVER_DESTINATION)
                player.message("You teleport to the destination.")
            }
        }

        // Track which options were registered to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Handle lever 9707 - check what options it has
        try {
            val leverDef = getObject(9707)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                if (!registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = 9707, option = option) {
                            pullLever()
                        }
                        registeredOptions.add(option)
                    } catch (e: IllegalStateException) {
                        // Option already bound, skip
                    } catch (e: Exception) {
                        // Other error, continue
                    }
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, that's okay - plugin will still load
            // We'll use the RSCM name instead
        }

        // Also handle using RSCM name (only if not already registered)
        try {
            if (objHasOption("object.lever_9707", "pull") && !registeredOptions.contains("pull")) {
                onObjOption(obj = "object.lever_9707", option = "pull") {
                    pullLever()
                }
                registeredOptions.add("pull")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }

        try {
            if (objHasOption("object.lever_9707", "operate") && !registeredOptions.contains("operate")) {
                onObjOption(obj = "object.lever_9707", option = "operate") {
                    pullLever()
                }
                registeredOptions.add("operate")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }
    }
}




