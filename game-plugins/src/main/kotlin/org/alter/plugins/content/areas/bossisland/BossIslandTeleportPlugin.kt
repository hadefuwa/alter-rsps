package org.alter.plugins.content.areas.bossisland

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Boss Island Teleport Plugin
 * 
 * Provides easy access to the Boss Island through the existing teleport system.
 * The Boss Island is accessible via the "Boss Island" teleport option.
 * 
 * This plugin ensures the teleport location matches exactly with the boss spawns
 * and provides appropriate warnings about the dangers of the island.
 */
class BossIslandTeleportPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Boss Island teleport location - should match BossIslandSpawnPlugin
         */
        private val BOSS_ISLAND_LOCATION = Tile(x = 3423, z = 4089, height = 0)
    }

    init {
        // Note: The actual teleport is handled by TeleportsPlugin.kt where "Boss Island" maps to this location
        // This plugin provides additional functionality and warnings
        
        // Add region enter warnings for Boss Island
        onEnterRegion(13631) { // Boss Island region
            val player = ctx as Player
            if (player.tile.height == 0) { // Make sure they're on the main level
                showBossIslandWarning(player)
            }
        }
        
        println("Boss Island Teleport: Plugin initialized")
        println("Boss Island Teleport: Location (${BOSS_ISLAND_LOCATION.x}, ${BOSS_ISLAND_LOCATION.z})")
        println("Boss Island Teleport: Access via 'Bounty Hunter' teleport option")
    }
    
    /**
     * Shows a warning message to players entering Boss Island
     */
    private fun showBossIslandWarning(player: Player) {
        player.queue {
            player.message("<col=ff0000>Welcome to Boss Island!</col>")
            wait(1)
            player.message("<col=ff6600>⚠ Warning: All major bosses are concentrated here!</col>")
            wait(1)
            player.message("<col=00ff00>💰 Reward: All bosses drop 3x the normal loot!</col>")
            wait(1)
            player.message("<col=ffff00>🔥 This is a multi-combat area - be prepared!</col>")
            wait(1)
            player.message("<col=ffffff>Bosses available: Venenatis, Callisto, Vet'ion, Scorpia, KBD, KQ, and more!</col>")
        }
    }
}