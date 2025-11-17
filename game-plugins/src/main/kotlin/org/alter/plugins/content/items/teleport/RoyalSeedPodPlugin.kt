package org.alter.plugins.content.items.teleport

import org.alter.api.cfg.Items
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.TeleportType

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
            player.queue {
                handleCustomTeleport()
            }
        }

        // Also handle option 1 (first option) as teleport
        onItemOption(item = ROYAL_SEED_POD, option = "teleport") {
            player.queue {
                handleCustomTeleport()
            }
        }
    }

    private suspend fun handleCustomTeleport() {
        // Check if player is Pnda
        if (!player.username.equals(ALLOWED_USERNAME, ignoreCase = true)) {
            player.message("The Royal Seed Pod glows briefly, but nothing happens...")
            return
        }

        // Prompt for X coordinate
        player.message("Enter the X coordinate:")
        val x = inputInt(player, "Enter X coordinate:")

        if (x == null || x < 0) {
            player.message("Teleport cancelled.")
            return
        }

        // Prompt for Y coordinate (Z in RS coordinates)
        player.message("Enter the Y coordinate:")
        val y = inputInt(player, "Enter Y coordinate:")

        if (y == null || y < 0) {
            player.message("Teleport cancelled.")
            return
        }

        // Optional: Prompt for height/plane (default to 0)
        player.message("Enter the height/plane (0-3, or 0 for ground level):")
        val height = inputInt(player, "Enter height (0-3):") ?: 0

        // Validate coordinates
        if (x > 16383 || y > 16383) {
            player.message("Invalid coordinates. X and Y must be between 0 and 16383.")
            return
        }

        if (height < 0 || height > 3) {
            player.message("Invalid height. Height must be between 0 and 3.")
            return
        }

        // Create destination tile
        val destination = Tile(x, y, height)

        // Perform teleport
        player.message("Teleporting to coordinates: X=$x, Y=$y, Height=$height")
        player.teleport(destination, TeleportType.MODERN)

        wait(4) // Wait for teleport to complete
        player.message("You have arrived at your destination!")
    }
}
