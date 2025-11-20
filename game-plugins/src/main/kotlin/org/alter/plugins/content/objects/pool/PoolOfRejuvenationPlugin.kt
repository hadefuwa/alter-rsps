package org.alter.plugins.content.objects.pool

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.POISON_TICKS_LEFT_ATTR
import org.alter.game.model.timer.POISON_TIMER
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.poison.Poison
import org.alter.rscm.RSCM.getRSCM

/**
 * Pool of Rejuvenation Plugin
 * 
 * This plugin spawns a pool of rejuvenation at Varrock (3214, 3425) that restores
 * the player's HP and Prayer to full when interacted with.
 */
class PoolOfRejuvenationPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Pool object ID - using ornate pool of rejuvenation
         */
        private val POOL_OBJECT = "object.ornate_pool_of_rejuvenation"
        
        /**
         * Pool location
         */
        private val POOL_TILE = Tile(x = 3214, z = 3425, height = 0)
    }

    init {
        // Spawn the pool of rejuvenation
        spawnObj(
            obj = POOL_OBJECT,
            tile = POOL_TILE,
            type = 10,
            rot = 0
        )

        // Handle pool interaction - try multiple common options
        val poolOptions = listOf("drink", "use", "operate", "dip", "restore")
        val registeredOptions = mutableSetOf<String>()
        
        // Try with RSCM name first
        poolOptions.forEach { option ->
            try {
                if (objHasOption(POOL_OBJECT, option) && !registeredOptions.contains(option)) {
                    onObjOption(obj = POOL_OBJECT, option = option, lineOfSightDistance = 2) {
                        handlePoolInteraction()
                    }
                    registeredOptions.add(option)
                }
            } catch (e: Exception) {
                // Option might not exist or already bound, continue to next
            }
        }

        // Only try with numeric object ID if RSCM name binding failed
        // This prevents duplicate bindings
        if (registeredOptions.isEmpty()) {
            try {
                val poolObjId = getRSCM(POOL_OBJECT)
                if (poolObjId != -1) {
                    poolOptions.forEach { option ->
                        try {
                            if (!registeredOptions.contains(option)) {
                                onObjOption(obj = poolObjId, option = option, lineOfSightDistance = 2) {
                                    handlePoolInteraction()
                                }
                                registeredOptions.add(option)
                            }
                        } catch (e: Exception) {
                            // Option might not exist or already bound
                        }
                    }
                }
            } catch (e: Exception) {
                // RSCM lookup failed
            }
        }
    }

    /**
     * Handle pool interaction - restore HP, Prayer, reset stats, and cure poison/venom
     */
    private fun Plugin.handlePoolInteraction() {
        player.queue {
            val currentHp = player.getCurrentHp()
            val maxHp = player.getMaxHp()
            val currentPrayer = player.getSkills().getCurrentLevel(Skills.PRAYER)
            val maxPrayer = player.getSkills().getBaseLevel(Skills.PRAYER)
            val ticksLeft = player.attr[POISON_TICKS_LEFT_ATTR] ?: 0
            val isPoisoned = ticksLeft > 0
            val isVenomed = ticksLeft < 0
            
            // Check if player needs any restoration
            val needsHp = currentHp < maxHp
            val needsPrayer = currentPrayer < maxPrayer
            val needsCure = isPoisoned || isVenomed
            
            // Check if any stats need resetting (excluding HP and Prayer)
            val needsStatReset = (0 until player.getSkills().maxSkills).any { skillId ->
                skillId != Skills.HITPOINTS && skillId != Skills.PRAYER &&
                player.getSkills().getCurrentLevel(skillId) != player.getSkills().getBaseLevel(skillId)
            }
            
            if (!needsHp && !needsPrayer && !needsCure && !needsStatReset) {
                player.message("You are already at full health, prayer, and have no debuffs.")
                return@queue
            }
            
            // Play animation (drinking from pool)
            player.animate(829) // Drinking animation
            
            // Wait a moment for animation
            wait(cycles = 2)
            
            // Cure poison/venom first
            if (needsCure) {
                player.attr[POISON_TICKS_LEFT_ATTR] = 0
                player.timers.remove(POISON_TIMER)
                Poison.setHpOrb(player, Poison.OrbState.NONE)
            }
            
            // Reset all stats to base level (restores combat stats)
            if (needsStatReset) {
                player.getSkills().restoreAll()
            }
            
            // Restore HP to full
            if (needsHp) {
                player.setCurrentHp(maxHp)
            }
            
            // Restore Prayer to full
            if (needsPrayer) {
                player.getSkills().restore(Skills.PRAYER)
            }
            
            // Play sound effect
            player.playSound(2393) // Drinking sound
            
            // Build message based on what was restored
            val messages = mutableListOf<String>()
            if (needsCure) {
                messages.add(if (isVenomed) "cured of venom" else "cured of poison")
            }
            if (needsStatReset) {
                messages.add("stats restored")
            }
            if (needsHp) {
                messages.add("health fully restored")
            }
            if (needsPrayer) {
                messages.add("prayer fully restored")
            }
            
            val messageText = when (messages.size) {
                1 -> "You drink from the pool of rejuvenation. You are ${messages[0]}."
                2 -> "You drink from the pool of rejuvenation. You are ${messages[0]} and your ${messages[1]}."
                else -> "You drink from the pool of rejuvenation. You are ${messages.dropLast(1).joinToString(", ")} and your ${messages.last()}."
            }
            
            player.message(messageText)
        }
    }
}

