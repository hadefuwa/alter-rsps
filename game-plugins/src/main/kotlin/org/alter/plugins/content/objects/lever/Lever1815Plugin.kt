package org.alter.plugins.content.objects.lever

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Lever 1815 Plugin
 * 
 * This plugin handles lever 1815 interaction,
 * allowing players to teleport home.
 */
class Lever1815Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Function to teleport player home when lever is pulled
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
                
                // Teleport to home
                val home = world.gameContext.home
                player.prepareForTeleport()
                player.moveTo(home)
                player.message("You teleport home.")
            }
        }

        // Track which options were registered to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Handle lever 1815 - check what options it has
        try {
            val leverDef = getObject(1815)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                if (!registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = 1815, option = option) {
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
            if (objHasOption("object.lever_1815", "pull") && !registeredOptions.contains("pull")) {
                onObjOption(obj = "object.lever_1815", option = "pull") {
                    pullLever()
                }
                registeredOptions.add("pull")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }

        try {
            if (objHasOption("object.lever_1815", "operate") && !registeredOptions.contains("operate")) {
                onObjOption(obj = "object.lever_1815", option = "operate") {
                    pullLever()
                }
                registeredOptions.add("operate")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }
    }
}

