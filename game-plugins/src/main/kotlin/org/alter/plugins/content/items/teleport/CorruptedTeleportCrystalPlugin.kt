package org.alter.plugins.content.items.teleport

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.TeleportType
import org.alter.plugins.content.magic.teleport
import org.alter.rscm.RSCM.getRSCM

/**
 * Corrupted Teleport Crystal - Gauntlet Teleporter
 *
 * This plugin allows item 23858 (corrupted_teleport_crystal) to teleport the player
 * to the Gauntlet entrance at coordinates 2176, 3328.
 *
 * Usage:
 * Click on the corrupted teleport crystal in your inventory to teleport to the Gauntlet.
 */
class CorruptedTeleportCrystalPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private const val CORRUPTED_TELEPORT_CRYSTAL_ITEM = "item.corrupted_teleport_crystal"
        private const val GAUNTLET_X = 2176
        private const val GAUNTLET_Y = 3328
        private const val GAUNTLET_HEIGHT = 0
    }

    init {
        val itemId = 23858
        
        // Register option 1 (usually "Use" or first option)
        if (!world.plugins.isItemBound(itemId, 1)) {
            try {
                r.bindItem(itemId, 1) {
                    player.queue(TaskPriority.STRONG) {
                        teleportToGauntlet(player)
                    }
                }
            } catch (e: Exception) {
                // Failed to register option 1
            }
        }
        
        // Register option 2 as backup (usually second option)
        if (!world.plugins.isItemBound(itemId, 2)) {
            try {
                r.bindItem(itemId, 2) {
                    player.queue(TaskPriority.STRONG) {
                        teleportToGauntlet(player)
                    }
                }
            } catch (e: Exception) {
                // Failed to register option 2
            }
        }
        
        // Also try using RSCM name if available
        try {
            val rscmItemId = getRSCM(CORRUPTED_TELEPORT_CRYSTAL_ITEM)
            if (rscmItemId == itemId && !world.plugins.isItemBound(rscmItemId, 1)) {
                onItemOption(item = CORRUPTED_TELEPORT_CRYSTAL_ITEM, option = 1) {
                    player.queue(TaskPriority.STRONG) {
                        teleportToGauntlet(player)
                    }
                }
            }
        } catch (e: Exception) {
            // RSCM name might not exist, that's okay
        }
    }

    private suspend fun teleportToGauntlet(player: Player) {
        val destination = Tile(GAUNTLET_X, GAUNTLET_Y, GAUNTLET_HEIGHT)
        player.message("The corrupted crystal shatters as you teleport to the Gauntlet!")
        player.teleport(destination, TeleportType.MODERN)
    }
}

