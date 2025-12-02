package org.alter.plugins.content.objects.caves

import dev.openrune.cache.CacheManager.getObject
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
 * Entrance 37433 Plugin
 * 
 * This plugin handles entrance object 37433, which teleports players 
 * to coordinates (2460, 10415) when interacted with.
 * 
 * Entrance Object: null_37433 (object 37433)
 * Teleport Destination: (2460, 10415, height = 0)
 */
class Entrance37433Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Teleport destination coordinates
         * This is where players teleport to when entering
         */
        private val TELEPORT_DESTINATION = Tile(x = 2460, z = 10415, height = 0)
        
        /**
         * Entrance object ID
         */
        private const val ENTRANCE_ID = 37433
    }

    init {
        // Function to teleport player
        val enterEntrance: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You enter the entrance...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to destination
                player.prepareForTeleport()
                player.moveTo(TELEPORT_DESTINATION)
                player.message("You find yourself at your destination.")
            }
        }

        // Handle entrance 37433 - get available options from object definition
        try {
            // First, try to get available options for this object
            val objDef = getObject(ENTRANCE_ID)
            val availableOptions = objDef.actions.filterNotNull().filter { action: String? -> action != null && action.isNotBlank() }
            
            // Only proceed if object has options
            if (availableOptions.isNotEmpty()) {
                // Try common entrance interaction options
                val possibleOptions = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through", "open", "activate", "touch")
                var optionBound = false
                
                // Try string-based options first
                for (option in possibleOptions) {
                    try {
                        // Check if the object has this option by checking the actions array
                        val optionIndex = objDef.actions.indexOfFirst { action: String? -> 
                            action != null && action.lowercase() == option.lowercase() 
                        }
                        if (optionIndex != -1) {
                            onObjOption(obj = ENTRANCE_ID, option = option) {
                                enterEntrance()
                            }
                            optionBound = true
                            break // Found a working option, no need to try others
                        }
                    } catch (e: Exception) {
                        // Option not available or binding failed, try next
                        continue
                    }
                }
                
                // If no string options worked, try binding to all available non-examine options
                if (!optionBound && availableOptions.isNotEmpty()) {
                    // Try option indices 1, 2, 3, 4 (skip 0 which is usually examine)
                    // bindObject uses 1-based indexing, so optionIndex 1 = actions[0], optionIndex 2 = actions[1], etc.
                    for (optionIndex in 1..4) {
                        try {
                            // Check if this option index exists (not null and not blank)
                            val arrayIndex = optionIndex - 1
                            if (arrayIndex < objDef.actions.size && 
                                objDef.actions[arrayIndex] != null && 
                                objDef.actions[arrayIndex]!!.isNotBlank() &&
                                objDef.actions[arrayIndex]!!.lowercase() != "examine") {
                                r.bindObject(ENTRANCE_ID, optionIndex, -1) {
                                    enterEntrance()
                                }
                                optionBound = true
                                break // Found a working option
                            }
                        } catch (e: Exception) {
                            // Failed to bind this option index, try next
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Could not get object definition or bind options
        }
    }
}

