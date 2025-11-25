package org.alter.plugins.content.areas.bossisland

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.combat.Combat

/**
 * Boss Island Combat Enhancement Plugin
 * 
 * Makes all bosses on Boss Island deal double damage to increase the challenge
 * and maintain the high-risk, high-reward nature of the island.
 * 
 * Features:
 * - Doubles all damage dealt by bosses on Boss Island
 * - Works with all combat types (melee, ranged, magic)
 * - Applies to special attacks and regular attacks
 * - Preserves all existing combat mechanics
 * 
 * Location: Boss Island at coordinates (3423, 4089, height = 0)
 */
class BossIslandCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Boss Island center coordinates
         */
        private const val ISLAND_CENTER_X = 3423
        private const val ISLAND_CENTER_Z = 4089
        private const val ISLAND_HEIGHT = 0
        
        /**
         * Radius around the island center to consider as "Boss Island"
         */
        private const val ISLAND_RADIUS = 35
        
        /**
         * Damage multiplier for Boss Island monsters
         */
        private const val DAMAGE_MULTIPLIER = 2
        
        /**
         * Set of boss NPC RSCM names that should deal enhanced damage on the island
         */
        private val BOSS_ISLAND_NPCS = setOf(
            "npc.venenatis",
            "npc.callisto", 
            "npc.vetion",
            "npc.scorpia",
            "npc.king_black_dragon",
            "npc.kalphite_queen_963",
            "npc.kalphite_queen_964", // Form 2 (if it transforms)
            "npc.cerberus", // Sewer Abomination
            "npc.crazy_archaeologist",
            "npc.chaos_fanatic",
            "npc.chaos_elemental"
        )
    }
    
    /**
     * Cached set of NPC IDs for fast lookup
     */
    private val bossIslandNpcIds = mutableSetOf<Int>()
    
    init {
        // Cache NPC IDs at initialization
        BOSS_ISLAND_NPCS.forEach { rscmName ->
            try {
                val npcId = getRSCM(rscmName)
                bossIslandNpcIds.add(npcId)
                println("Boss Island Combat: Registered $rscmName (ID: $npcId) for enhanced damage")
            } catch (e: Exception) {
                println("Boss Island Combat: Warning - Could not find NPC $rscmName: ${e.message}")
            }
        }
        
        // Set up damage enhancement system
        setupDamageEnhancement()
        
        println("Boss Island Combat: Initialized double damage system for ${bossIslandNpcIds.size} boss types")
        println("Boss Island Combat: Island location (${ISLAND_CENTER_X}, ${ISLAND_CENTER_Z}) with radius ${ISLAND_RADIUS}")
    }
    
    /**
     * Sets up the damage enhancement system using NPC spawn hooks to apply damage multipliers
     */
    private fun setupDamageEnhancement() {
        // Apply damage multipliers to each boss type when they spawn on Boss Island
        BOSS_ISLAND_NPCS.forEach { rscmName ->
            try {
                onNpcSpawn(npc = rscmName) {
                    // Check if this NPC spawned on Boss Island
                    if (isBossOnIsland(npc)) {
                        // Set the damage multiplier attribute
                        npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = DAMAGE_MULTIPLIER.toDouble()
                        println("Boss Island Combat: Applied ${DAMAGE_MULTIPLIER}x damage multiplier to ${npc.def.name} (ID: ${npc.id})")
                        
                        // Message nearby players about the enhanced damage
                        npc.world.players.forEach { player ->
                            val distance = player.tile.getDistance(npc.tile)
                            if (distance <= 10) {
                                player.message("<col=ff0000>Boss Island: ${npc.def.name} deals ${DAMAGE_MULTIPLIER}x damage here!</col>")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Boss Island Combat: Warning - Could not register spawn hook for $rscmName: ${e.message}")
            }
        }
    }
    
    /**
     * Checks if an NPC is a boss located on Boss Island
     */
    private fun isBossOnIsland(npc: Npc): Boolean {
        // Check if it's a registered boss type
        if (!bossIslandNpcIds.contains(npc.id)) {
            return false
        }
        
        // Check if it's within the Boss Island area
        val npcTile = npc.tile
        if (npcTile.height != ISLAND_HEIGHT) {
            return false
        }
        
        val distanceX = kotlin.math.abs(npcTile.x - ISLAND_CENTER_X)
        val distanceZ = kotlin.math.abs(npcTile.z - ISLAND_CENTER_Z)
        val distance = kotlin.math.max(distanceX, distanceZ)
        
        return distance <= ISLAND_RADIUS
    }
}