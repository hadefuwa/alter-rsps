package org.alter.plugins.content.areas.wilderness.combat

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.combat.isAttacking
import org.alter.rscm.RSCM.getRSCM
import org.alter.game.model.timer.TimerKey

/**
 * Wilderness Damage Multiplier Plugin
 * 
 * This plugin makes all monsters in the wilderness deal 3x normal damage to players,
 * excluding the King Black Dragon. The damage multiplier is applied by setting the
 * DAMAGE_DEAL_MULTIPLIER attribute on wilderness monsters when they attack players.
 * 
 * Features:
 * - 3x damage multiplier for all wilderness monsters
 * - Excludes King Black Dragon from the multiplier
 * - Only affects damage dealt to players (PvE)
 * - Uses the built-in combat damage multiplier system
 * 
 * Implementation Details:
 * - Uses a timer-based system that checks NPCs every tick
 * - Checks if NPC is in wilderness using getWildernessLevel()
 * - Sets Combat.DAMAGE_DEAL_MULTIPLIER attribute to 3.0 when NPCs attack players
 * - Multiplier is automatically applied by combat formulas
 * 
 * @param r The plugin repository for registering handlers
 * @param world The game world instance
 * @param server The server instance
 */
class WildernessDamageMultiplierPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    /**
     * The damage multiplier applied to wilderness monsters
     */
    private val WILDERNESS_DAMAGE_MULTIPLIER = 3.0
    
    /**
     * Timer key for checking NPCs in combat
     */
    private val WILDERNESS_MULTIPLIER_CHECK_TIMER = TimerKey()

    /**
     * Initialize the plugin and register the damage multiplier handler.
     * 
     * This method sets up a timer-based system that applies the damage multiplier
     * to all wilderness monsters (except KBD) when they attack players.
     */
    init {
        // Hook into global NPC spawn to set up timer for checking multiplier
        onGlobalNpcSpawn {
            npc.timers[WILDERNESS_MULTIPLIER_CHECK_TIMER] = 1
        }
        
        // Timer that runs every tick to check and apply multiplier
        onTimer(WILDERNESS_MULTIPLIER_CHECK_TIMER) {
            if (npc.isAttacking()) {
                val target = npc.getCombatTarget()
                if (target is Player) {
                    applyWildernessDamageMultiplier(npc, target)
                }
            }
            // Reset timer to check again next tick
            npc.timers[WILDERNESS_MULTIPLIER_CHECK_TIMER] = 1
        }
    }

    /**
     * Apply wilderness damage multiplier to the attacking NPC if conditions are met.
     * 
     * This method checks if the NPC is in wilderness and is not the King Black Dragon,
     * then applies the 3x damage multiplier by setting the DAMAGE_DEAL_MULTIPLIER attribute.
     * 
     * Conditions checked:
     * 1. NPC must be in wilderness (wilderness level > 0) OR in Revenant Caves
     * 2. NPC must not be King Black Dragon
     * 3. Target must be a player
     * 
     * @param npc The attacking NPC
     * @param target The target player
     */
    private fun applyWildernessDamageMultiplier(npc: Npc, target: Player) {
        // Check if NPC is in wilderness
        val wildernessLevel = npc.tile.getWildernessLevel()
        
        // Check if NPC is in Revenant Caves (dungeon coordinates z >= 10000)
        val isInRevenantCaves = npc.tile.z >= 10000 && npc.tile.z <= 10300 && 
                               npc.tile.x >= 3100 && npc.tile.x <= 3300
        
        if (wildernessLevel > 0 || isInRevenantCaves) {
            // Exclude King Black Dragon from damage multiplier
            if (npc.id != getRSCM("npc.king_black_dragon")) {
                // Apply 3x damage multiplier to wilderness monsters
                npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = WILDERNESS_DAMAGE_MULTIPLIER
            }
        }
    }
}