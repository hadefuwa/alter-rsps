package org.alter.plugins.content.mechanics.doompoints

import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.api.ext.*

/**
 * Passive XP Timer Plugin
 * 
 * Grants passive XP to players who have unlocked the Passive XP perk.
 * XP is granted to the player's lowest skill every 100 game ticks (1 minute).
 */
class PassiveXpTimerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        private val PASSIVE_XP_TIMER = TimerKey("passive_xp_timer", tickOffline = false)
        private const val TIMER_DELAY = 100 // 100 ticks = 1 minute
        private const val XP_AMOUNT = 100.0 // XP granted per interval
    }
    
    init {
        // Set up a global timer that runs for all online players
        onWorldInit {
            world.queue {
                while (true) {
                    wait(TIMER_DELAY)
                    
                    // Grant passive XP to all eligible players
                    world.players.forEach { player ->
                        if (player.initiated && !player.isDead()) {
                            DoomPoints.applyPassiveXp(player)
                        }
                    }
                }
            }
        }
    }
}
