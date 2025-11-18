package org.alter.plugins.content.items.teleport

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.TeleportType
import org.alter.plugins.content.magic.teleport
import org.alter.rscm.RSCM.getRSCM

/**
 * Royal Seed Pod - Custom Coordinate Teleporter (Pnda Only)
 *
 * This plugin allows the Royal Seed Pod to teleport the player "Pnda" to custom coordinates
 * by entering X and Y values through the chatbox.
 *
 * Usage:
 * 1. Right-click the Royal Seed Pod and select "Commune"
 * 2. Enter X coordinate when prompted
 * 3. Enter Y coordinate when prompted
 * 4. Optionally enter height/plane (0-3)
 * 5. Player will be teleported to the specified coordinates
 */
class RoyalSeedPodPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private const val ROYAL_SEED_POD_ITEM = "item.royal_seed_pod"
        private const val ALLOWED_USERNAME = "pnda"
    }

    init {
        // Get the item ID to check if options are already bound
        val itemId = getRSCM(ROYAL_SEED_POD_ITEM)
        
        // Handle "Commune" option on Royal Seed Pod using RSCM name
        // The log shows option=2 is being clicked, so "Commune" is the second option
        // Try multiple option names in case the exact name doesn't match
        val optionNames = listOf("commune", "Commune", "use", "Use", "teleport", "Teleport")
        var registered = false
        
        for (optionName in optionNames) {
            if (itemHasInventoryOption(ROYAL_SEED_POD_ITEM, optionName)) {
                try {
                    onItemOption(item = ROYAL_SEED_POD_ITEM, option = optionName) {
                        player.queue(TaskPriority.STRONG) {
                            player.handleCustomTeleport(this)
                        }
                    }
                    registered = true
                    println("RoyalSeedPodPlugin: Successfully registered string option '$optionName'")
                    break
                } catch (e: Throwable) {
                    println("RoyalSeedPodPlugin: Failed to register option '$optionName': ${e.message}")
                    // Continue to next option
                }
            }
        }
        
        // The log shows option=2 is being clicked, so register option 2
        // Check if option 2 is already bound before registering
        if (!world.plugins.isItemBound(itemId, 2)) {
            try {
                onItemOption(item = ROYAL_SEED_POD_ITEM, option = 2) {
                    player.queue(TaskPriority.STRONG) {
                        player.handleCustomTeleport(this)
                    }
                }
                println("RoyalSeedPodPlugin: Successfully registered option 2 (Commune)")
                registered = true
            } catch (e: Throwable) {
                println("RoyalSeedPodPlugin: Failed to register option 2: ${e.message}")
            }
        } else {
            println("RoyalSeedPodPlugin: Option 2 already bound, skipping registration")
        }
        
        // Also register option 1 as a backup, but only if it's not already bound
        if (!world.plugins.isItemBound(itemId, 1)) {
            try {
                onItemOption(item = ROYAL_SEED_POD_ITEM, option = 1) {
                    player.queue(TaskPriority.STRONG) {
                        player.handleCustomTeleport(this)
                    }
                }
                println("RoyalSeedPodPlugin: Successfully registered option 1 (backup)")
            } catch (e: Throwable) {
                println("RoyalSeedPodPlugin: Failed to register option 1: ${e.message}")
            }
        } else {
            println("RoyalSeedPodPlugin: Option 1 already bound, skipping registration")
        }
    }

    private suspend fun Player.handleCustomTeleport(it: QueueTask) {
        println("RoyalSeedPodPlugin: handleCustomTeleport called for player ${username}")
        // Check if player is Pnda
        if (!username.equals(ALLOWED_USERNAME, ignoreCase = true)) {
            message("The Royal Seed Pod glows briefly, but nothing happens...")
            return
        }

        // Prompt for X coordinate
        message("Enter the X coordinate:")
        val x = it.inputInt(this, "Enter X coordinate:")

        if (x == null || x < 0) {
            message("Teleport cancelled.")
            return
        }

        // Prompt for Y coordinate (Z in RS coordinates)
        message("Enter the Y coordinate:")
        val y = it.inputInt(this, "Enter Y coordinate:")

        if (y == null || y < 0) {
            message("Teleport cancelled.")
            return
        }

        // Optional: Prompt for height/plane (default to 0)
        message("Enter the height/plane (0-3, or 0 for ground level):")
        val height = it.inputInt(this, "Enter height (0-3):") ?: 0

        // Validate coordinates
        if (x > 16383 || y > 16383) {
            message("Invalid coordinates. X and Y must be between 0 and 16383.")
            return
        }

        if (height < 0 || height > 3) {
            message("Invalid height. Height must be between 0 and 3.")
            return
        }

        // Create destination tile
        val destination = Tile(x, y, height)

        // Perform teleport
        message("Teleporting to coordinates: X=$x, Y=$y, Height=$height")
        teleport(destination, TeleportType.MODERN)

        it.wait(4) // Wait for teleport to complete
        message("You have arrived at your destination!")
    }
}
