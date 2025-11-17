package org.alter.plugins.content.items.teleport

import org.alter.api.cfg.Items
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

/**
 * Royal Seed Pod - Custom Coordinate Teleporter (Pnda Only)
 *
 * This plugin allows the Royal Seed Pod to teleport the player "Pnda" to custom coordinates
 * by entering X and Y values through the chatbox.
 *
 * Usage:
 * 1. Click "Teleport" on the Royal Seed Pod
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
        private const val ROYAL_SEED_POD = 19564
        private const val ALLOWED_USERNAME = "pnda"
    }

    init {
        // Handle "Teleport" option on Royal Seed Pod
        onItemOption(item = Items.ROYAL_SEED_POD, option = "teleport") {
            player.queue(TaskPriority.STRONG) {
                player.handleCustomTeleport(this)
            }
        }

        // Also handle direct item ID as fallback
        onItemOption(item = ROYAL_SEED_POD, option = "teleport") {
            player.queue(TaskPriority.STRONG) {
                player.handleCustomTeleport(this)
            }
        }
    }

    private suspend fun Player.handleCustomTeleport(it: QueueTask) {
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
