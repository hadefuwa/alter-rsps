package org.alter.plugins.content.objects.lever

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
 * Lever 5959 Plugin
 * 
 * This plugin handles lever 5959 interaction,
 * allowing players to teleport to Mage Bank at coordinates 2539, 4715.
 */
class Lever5959Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Mage Bank destination coordinates
         */
        private val MAGE_BANK_DESTINATION = Tile(x = 2539, z = 4715, height = 0)
    }

    init {
        // Function to teleport player to Mage Bank when lever is pulled
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
                
                // Teleport to Mage Bank
                player.prepareForTeleport()
                player.moveTo(MAGE_BANK_DESTINATION)
                player.message("You teleport to the Mage Bank.")
            }
        }

        // Track which options were registered to avoid duplicates
        val registeredOptions = mutableSetOf<String>()
        
        // Handle lever 5959 - check what options it has
        try {
            val leverDef = getObject(5959)
            val leverOptions = leverDef.actions.filterNotNull().map { it.lowercase() }
            
            // Common lever options: "pull", "operate", "use", "pull-lever"
            leverOptions.forEach { option ->
                if (!registeredOptions.contains(option)) {
                    try {
                        onObjOption(obj = 5959, option = option) {
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
            if (objHasOption("object.lever_5959", "pull") && !registeredOptions.contains("pull")) {
                onObjOption(obj = "object.lever_5959", option = "pull") {
                    pullLever()
                }
                registeredOptions.add("pull")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }

        try {
            if (objHasOption("object.lever_5959", "operate") && !registeredOptions.contains("operate")) {
                onObjOption(obj = "object.lever_5959", option = "operate") {
                    pullLever()
                }
                registeredOptions.add("operate")
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }
    }
}

