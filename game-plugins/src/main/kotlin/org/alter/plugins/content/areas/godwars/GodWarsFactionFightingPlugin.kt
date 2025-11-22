package org.alter.plugins.content.areas.godwars

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.combat.isAttacking
import org.alter.plugins.content.combat.removeCombatTarget
import org.alter.rscm.RSCM.getRSCM

/**
 * God Wars Faction Fighting Plugin
 * 
 * Makes NPCs from different god factions attack each other:
 * - Saradomin vs Zamorak vs Armadyl vs Bandos
 * 
 * NPCs will:
 * 1. Prioritize attacking players
 * 2. Attack NPCs from enemy factions when no players are nearby
 * 3. Fight in multi-combat style (multiple NPCs can gang up)
 */
class GodWarsFactionFightingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Timer key for checking faction enemies
         */
        val FACTION_CHECK_TIMER = TimerKey()
        
        /**
         * Timer key for cleaning up stale combat targets
         */
        val COMBAT_CLEANUP_TIMER = TimerKey()
        
        /**
         * Timer key for resetting NPCs after inactivity
         */
        val INACTIVITY_RESET_TIMER = TimerKey()
        
        /**
         * God factions
         */
        enum class GodFaction {
            SARADOMIN,
            ZAMORAK,
            ARMADYL,
            BANDOS,
            NONE
        }
        
        /**
         * God Wars Dungeon region IDs (height 2)
         */
        private val GODWARS_REGIONS = setOf(
            11602, // Saradomin area
            11603, // Zamorak area
            11346, // Armadyl area
            11347, // Bandos area
            11345, // Adjacent region
            11601, // Adjacent region
            11604, // Adjacent region
            11858, // Adjacent region
            11859  // Adjacent region
        )
        
        /**
         * Time in ticks before resetting NPCs when no players present
         * 120 seconds = 200 ticks (1 tick = 0.6 seconds)
         */
        private const val INACTIVITY_RESET_TICKS = 200
    }
    
    /**
     * NPC ID to faction mapping
     */
    private val npcFactions = mutableMapOf<Int, GodFaction>()
    
    /**
     * Track the number of players currently in God Wars regions
     * This is updated via region enter/exit events (event-based, not polling)
     */
    private var playersInGodWars: Int = 0
    
    /**
     * Check if any players are currently in God Wars Dungeon
     */
    private fun hasPlayersInGodWars(): Boolean {
        return playersInGodWars > 0
    }
    
    /**
     * Enable faction fighting for all God Wars NPCs
     */
    /*private fun enableFactionFighting() {
        world.npcs.forEach { npc ->
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE && npc.isSpawned() && npc.isAlive()) {
                // Start faction timer if not already running
                if (!npc.timers.has(FACTION_CHECK_TIMER)) {
                    npc.timers[FACTION_CHECK_TIMER] = 20
                }
            }
        }
    }*/
    
    /**
     * Disable faction fighting for all God Wars NPCs
     */
    /*private fun disableFactionFighting() {
        world.npcs.forEach { npc ->
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE) {
                // Stop faction fighting timer
                npc.timers.remove(FACTION_CHECK_TIMER)
            }
        }
    }*/
    
    /**
     * Reset all God Wars NPCs (clear combat state only)
     * NPCs will naturally return to spawn via their walk radius
     */
    /*private fun resetAllGodWarsNpcs() {
        world.npcs.forEach { npc ->
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE && npc.isSpawned()) {
                // Clear combat targets and reset state
                npc.removeCombatTarget()
                npc.resetFacePawn()
                npc.interruptQueues()
                
                // Stop faction fighting timer
                npc.timers.remove(FACTION_CHECK_TIMER)
            }
        }
    }*/
    
    init {
        // Register NPCs to their factions
        //registerFactions()
        
        // EVENT-BASED PLAYER TRACKING (much more efficient than polling)
        // Track players entering God Wars regions
        /*GODWARS_REGIONS.forEach { regionId ->
            onEnterRegion(regionId) {
                val player = ctx as? Player ?: return@onEnterRegion
                
                // Increment player count
                playersInGodWars++
                
                // If this is the first player, enable faction fighting
                if (playersInGodWars == 1) {
                    //enableFactionFighting()
                    // Cancel any pending inactivity reset
                    world.timers.remove(INACTIVITY_RESET_TIMER)
                }
            }
            
            // Track players exiting God Wars regions
            onExitRegion(regionId) {
                val player = ctx as? Player ?: return@onExitRegion
                
                // Decrement player count
                playersInGodWars = maxOf(0, playersInGodWars - 1)
                
                // If no players remain, disable faction fighting and start inactivity timer
                if (playersInGodWars == 0) {
                    //disableFactionFighting()
                    // Start inactivity reset timer (only runs when needed)
                    world.timers[INACTIVITY_RESET_TIMER] = INACTIVITY_RESET_TICKS
                }
            }
        }
        
        // Timer that resets NPCs after inactivity (only runs when no players are present)
        onTimer(INACTIVITY_RESET_TIMER) {
            // Double-check no players are present (in case someone entered during the timer)
            if (playersInGodWars == 0) {
                //resetAllGodWarsNpcs()
            }
            // Timer will not reset itself - it only runs once when triggered
        }
        
        // Set up combat cleanup timer and faction fighting for all God Wars NPCs
        onGlobalNpcSpawn {
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE) {
                // Immediately check and clear any stale combat targets on spawn
                val combatTarget = npc.getCombatTarget()
                if (combatTarget != null) {
                    val isTrulyInvalid = combatTarget.isDead() || 
                                         (combatTarget is Npc && !combatTarget.isSpawned()) ||
                                         (combatTarget is Player && !combatTarget.isOnline)
                    if (isTrulyInvalid) {
                        npc.removeCombatTarget()
                        npc.resetFacePawn()
                        npc.interruptQueues()
                    }
                }
                
                // Start combat cleanup timer (always runs)
                npc.timers[COMBAT_CLEANUP_TIMER] = 10
                
                // Only start faction timer if players are present
                /*if (hasPlayersInGodWars()) {
                    npc.timers[FACTION_CHECK_TIMER] = 20
                }*/
            }
        }
        
        // Timer that cleans up stale combat targets
        // Only clears targets that are truly invalid (dead, despawned, offline)
        // Does NOT clear targets that are temporarily far away during active combat
        onTimer(COMBAT_CLEANUP_TIMER) {
            val npc = ctx as Npc
            val faction = npcFactions[npc.id]
            if (faction == null || faction == GodFaction.NONE) {
                return@onTimer
            }
            
            // Only process if NPC is alive and spawned
            if (!npc.isAlive() || !npc.isSpawned()) {
                return@onTimer
            }
            
            // Check if NPC has a combat target
            val combatTarget = npc.getCombatTarget()
            if (combatTarget != null) {
                // Only clear targets that are TRULY invalid:
                // 1. Target is dead
                // 2. Target is not spawned (for NPCs) or not online (for players)
                // 
                // NOTE: We do NOT check distance or height here because:
                // - NPCs might be temporarily far away while moving in combat
                // - The combat system itself handles distance/height checks
                // - We only want to clear targets that are permanently invalid
                val isTrulyInvalid = combatTarget.isDead() || 
                                     (combatTarget is Npc && !combatTarget.isSpawned()) ||
                                     (combatTarget is Player && !combatTarget.isOnline)
                
                if (isTrulyInvalid) {
                    // Clear the stale combat target
                    npc.removeCombatTarget()
                    npc.resetFacePawn()
                    npc.interruptQueues()
                }
            }
            
            // Reset timer to check again (less frequently to avoid interference)
            npc.timers[COMBAT_CLEANUP_TIMER] = 10
        }*/
        
        // FACTION FIGHTING ENABLED
        // NPCs will attack each other, but can be interrupted by players
        // Timer only runs when players are present (managed by event handlers)
        /*onTimer(FACTION_CHECK_TIMER) {
            // ... (already commented out)
        }*/
    }    

    
    /**
     * Register all God Wars NPCs to their factions
     */
    /*private fun registerFactions() {
        try {
            // Saradomin faction
            registerFaction("npc.spiritual_warrior", GodFaction.SARADOMIN)
            registerFaction("npc.spiritual_ranger", GodFaction.SARADOMIN)
            registerFaction("npc.spiritual_mage", GodFaction.SARADOMIN)
            registerFaction("npc.knight_of_saradomin", GodFaction.SARADOMIN)
            registerFaction("npc.knight_of_saradomin_2214", GodFaction.SARADOMIN)
            
            // Zamorak faction
            registerFaction("npc.spiritual_warrior_3159", GodFaction.ZAMORAK)
            registerFaction("npc.spiritual_ranger_3160", GodFaction.ZAMORAK)
            registerFaction("npc.spiritual_mage_3161", GodFaction.ZAMORAK)
            registerFaction("npc.imp_3134", GodFaction.ZAMORAK)
            
            // Armadyl faction
            registerFaction("npc.spiritual_warrior_3166", GodFaction.ARMADYL)
            registerFaction("npc.spiritual_ranger_3167", GodFaction.ARMADYL)
            registerFaction("npc.spiritual_mage_3168", GodFaction.ARMADYL)
            registerFaction("npc.aviansie", GodFaction.ARMADYL)
            
            // Bandos faction
            registerFaction("npc.spiritual_warrior_2243", GodFaction.BANDOS)
            registerFaction("npc.spiritual_ranger_2242", GodFaction.BANDOS)
            registerFaction("npc.spiritual_mage_2244", GodFaction.BANDOS)
            registerFaction("npc.goblin_2245", GodFaction.BANDOS)
            registerFaction("npc.goblin_2246", GodFaction.BANDOS)
        } catch (e: Exception) {
            // Silent error handling
        }
    }*/
    
    /**
     * Register a single NPC to a faction
     */
    /*private fun registerFaction(rscmName: String, faction: GodFaction) {
        try {
            val npcId = getRSCM(rscmName)
            npcFactions[npcId] = faction
        } catch (e: Exception) {
            // Silent error handling
        }
    }*/
    
    /**
     * Check for nearby faction enemies and attack them
     * 
     * Simple logic:
     * - If not already in combat, look for enemy NPCs and attack them
     * - Players can interrupt at any time by attacking the NPC
     */
    /*private fun checkForFactionEnemies(npc: Npc) {
        val myFaction = npcFactions[npc.id] ?: return
        
        // If NPC is already attacking, don't change targets
        if (npc.isAttacking()) {
            return
        }
        
        // Look for the nearest enemy NPC to attack
        val enemyNpc = findNearestEnemy(npc, myFaction, 10)
        if (enemyNpc != null) {
            npc.attack(enemyNpc)
        }
    }*/
    
    /**
     * Find the nearest enemy NPC from a different faction
     */
    /*private fun findNearestEnemy(npc: Npc, myFaction: GodFaction, radius: Int): Npc? {
        val enemies = mutableListOf<Npc>()
        
        npc.world.npcs.forEach { otherNpc ->
            // Must be alive and in range
            if (otherNpc.isAlive() &&
                otherNpc != npc &&
                otherNpc.tile.isWithinRadius(npc.tile, radius) &&
                otherNpc.tile.height == npc.tile.height &&
                // Must be from a different faction
                isEnemy(myFaction, npcFactions[otherNpc.id])) {
                enemies.add(otherNpc)
            }
        }
        
        // Return the closest enemy
        var closestEnemy: Npc? = null
        var closestDistance = Int.MAX_VALUE
        
        for (enemy in enemies) {
            val distance = npc.tile.getDistance(enemy.tile)
            if (distance < closestDistance) {
                closestDistance = distance
                closestEnemy = enemy
            }
        }
        
        return closestEnemy
    }*/
    
    /**
     * Check if two factions are enemies
     */
    /*private fun isEnemy(faction1: GodFaction, faction2: GodFaction?): Boolean {
        if (faction2 == null || faction2 == GodFaction.NONE) {
            return false
        }
        
        // All factions are enemies with each other
        return faction1 != faction2
    }*/
}

