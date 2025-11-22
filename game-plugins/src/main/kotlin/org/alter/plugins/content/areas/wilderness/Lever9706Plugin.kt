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
 * Lever 9706 Plugin
 * 
 * This plugin handles lever 9706 interaction,
 * allowing players to teleport to Mage Arena at coordinates 3105, 3951.
 */
class Lever9706Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Mage Arena destination coordinates
         */
        private val MAGE_ARENA_DESTINATION = Tile(x = 3105, z = 3951, height = 0)
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
                
                // Teleport to Mage Arena
                player.prepareForTeleport()
                player.moveTo(MAGE_ARENA_DESTINATION)
                player.message("You teleport to the Mage Arena.")
            }
        }

        // Track which options were registered to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Handle lever 9706 - check what options it has
        try {
            val leverDef = getObject(9706)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                if (!registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = 9706, option = option) {
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
            if (objHasOption("object.lever_9706", "pull") && !registeredOptions.contains("pull")) {
                onObjOption(obj = "object.lever_9706", option = "pull") {
                    pullLever()
                }
                registeredOptions.add("pull")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }

        try {
            if (objHasOption("object.lever_9706", "operate") && !registeredOptions.contains("operate")) {
                onObjOption(obj = "object.lever_9706", option = "operate") {
                    pullLever()
                }
                registeredOptions.add("operate")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }
    }
}

