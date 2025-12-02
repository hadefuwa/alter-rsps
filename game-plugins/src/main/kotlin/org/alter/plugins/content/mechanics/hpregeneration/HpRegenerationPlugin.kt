package org.alter.plugins.content.mechanics.hpregeneration

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

/**
 * HP Regeneration Plugin
 * 
 * Automatically regenerates 1 HP every 30 seconds for all players.
 * The regeneration only occurs if the player is not at full health.
 */
class HpRegenerationPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * HP regeneration interval in cycles.
         * 30 seconds = 50 cycles (since 1 cycle = 0.6 seconds, 30 / 0.6 = 50)
         */
        private const val HP_REGENERATION_INTERVAL = 50
    }
    
    init {
        /**
         * Initialize HP regeneration timer when player logs in.
         */
        onLogin {
            player.timers[HP_REGENERATION_TIMER] = HP_REGENERATION_INTERVAL
        }

        /**
         * Handle HP regeneration timer expiration.
         * Regenerates 1 HP if player is not at full health, then resets the timer.
         */
        onTimer(HP_REGENERATION_TIMER) {
            val currentHp = player.getCurrentHp()
            
            // Don't regenerate if player is dead or HP is 0 or negative
            // This prevents regeneration from interfering with death sequence
            if (player.isDead() || currentHp <= 0) {
                // Reset the timer and wait for next cycle
                player.timers[HP_REGENERATION_TIMER] = HP_REGENERATION_INTERVAL
                return@onTimer
            }
            
            val maxHp = player.getMaxHp()
            
            // Only regenerate if player is not at full health
            if (currentHp < maxHp) {
                // Regenerate 1 HP, but don't exceed max HP
                val newHp = minOf(currentHp + 1, maxHp)
                player.setCurrentHp(newHp)
            }
            
            // Reset the timer for the next regeneration cycle
            player.timers[HP_REGENERATION_TIMER] = HP_REGENERATION_INTERVAL
        }
    }
}

