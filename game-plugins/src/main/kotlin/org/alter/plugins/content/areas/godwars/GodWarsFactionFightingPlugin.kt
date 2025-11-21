package org.alter.plugins.content.areas.godwars

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
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
         * God factions
         */
        enum class GodFaction {
            SARADOMIN,
            ZAMORAK,
            ARMADYL,
            BANDOS,
            NONE
        }
    }
    
    /**
     * NPC ID to faction mapping
     */
    private val npcFactions = mutableMapOf<Int, GodFaction>()
    
    init {
        // Register NPCs to their factions
        registerFactions()
        
        // Set up combat cleanup timer for all God Wars NPCs
        // This clears stale combat targets that prevent players from attacking NPCs
        // Only clears targets that are truly invalid (dead, despawned, offline)
        onGlobalNpcSpawn {
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE) {
                // Immediately check and clear any stale combat targets on spawn
                // This ensures NPCs aren't stuck with invalid targets from previous sessions
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
                
                // Start combat cleanup timer (check every 10 ticks to avoid interfering with combat)
                npc.timers[COMBAT_CLEANUP_TIMER] = 10
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
        }
        
        // FACTION FIGHTING DISABLED
        // This was preventing players from attacking NPCs
        // If you want faction fighting back, uncomment the code below and adjust the logic
        
        /*
        // Set up faction checking timer for all God Wars NPCs
        onGlobalNpcSpawn {
            val faction = npcFactions[npc.id]
            if (faction != null && faction != GodFaction.NONE) {
                // Start faction checking timer (check every 10 ticks to reduce interference)
                npc.timers[FACTION_CHECK_TIMER] = 10
            }
        }
        
        // Timer that checks for faction enemies and initiates combat
        onTimer(FACTION_CHECK_TIMER) {
            if (npc.lock.canAttack() && npc.isActive()) {
                checkForFactionEnemies(npc)
            }
            // Reset timer to check again (less frequently)
            npc.timers[FACTION_CHECK_TIMER] = 10
        }
        */
    }
    
    /**
     * Register all God Wars NPCs to their factions
     */
    private fun registerFactions() {
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
    }
    
    /**
     * Register a single NPC to a faction
     */
    private fun registerFaction(rscmName: String, faction: GodFaction) {
        try {
            val npcId = getRSCM(rscmName)
            npcFactions[npcId] = faction
        } catch (e: Exception) {
            // Silent error handling
        }
    }
    
    /**
     * Check for nearby faction enemies and attack them
     * 
     * Priority order:
     * 1. If already in combat (with anyone), don't interfere
     * 2. If players are very close (within 5 tiles), don't do anything (let players initiate)
     * 3. Otherwise, attack enemy NPCs if no players are nearby
     */
    private fun checkForFactionEnemies(npc: Npc) {
        val myFaction = npcFactions[npc.id] ?: return
        
        // If NPC is already attacking anyone (player or NPC), don't interfere
        if (npc.isAttacking()) {
            return
        }
        
        // Look for very close players (within 5 tiles) - if present, do nothing
        val veryCloseToPlayer = hasVeryClosePlayer(npc, 5)
        if (veryCloseToPlayer) {
            // Players are very close - let them initiate combat if they want
            return
        }
        
        // NPC is idle and no players are very close - look for enemy NPCs to fight
        val enemyNpc = findNearestEnemy(npc, myFaction, 8)
        if (enemyNpc != null && !enemyNpc.isAttacking()) {
            // Only attack idle enemy NPCs (don't interrupt their combat)
            npc.attack(enemyNpc)
        }
    }
    
    /**
     * Check if there are any players very close to the NPC
     */
    private fun hasVeryClosePlayer(npc: Npc, radius: Int): Boolean {
        var hasClosePlayer = false
        npc.world.players.forEach { player ->
            if (player.isAlive() && 
                player.tile.isWithinRadius(npc.tile, radius) &&
                player.tile.height == npc.tile.height) {
                hasClosePlayer = true
            }
        }
        return hasClosePlayer
    }
    
    /**
     * Find the nearest enemy NPC from a different faction
     */
    private fun findNearestEnemy(npc: Npc, myFaction: GodFaction, radius: Int): Npc? {
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
    }
    
    /**
     * Check if two factions are enemies
     */
    private fun isEnemy(faction1: GodFaction, faction2: GodFaction?): Boolean {
        if (faction2 == null || faction2 == GodFaction.NONE) {
            return false
        }
        
        // All factions are enemies with each other
        return faction1 != faction2
    }
}

