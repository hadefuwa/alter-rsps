package org.alter.plugins.content.areas.bossisland

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Boss Island Spawn Plugin
 * 
 * Creates a challenging boss island at the "Bounty Hunter" location where all major bosses
 * are concentrated together with tripled drop rates. This creates a high-risk, high-reward
 * area for experienced players.
 * 
 * Location: Tile(x = 3423, z = 4089, height = 0) - "Bounty Hunter" teleport location
 * 
 * Features:
 * - All major bosses spawned in close proximity
 * - Tripled drop rates handled by BossIslandDropPlugin
 * - Bosses remain linked to their original combat plugins (no duplication of logic)
 * - Multi-combat area for intense PvM challenges
 * 
 * Boss List:
 * - Venenatis (Spider Boss)
 * - Callisto (Bear Boss)  
 * - Vet'ion (Skeletal Boss)
 * - Scorpia (Scorpion Boss)
 * - King Black Dragon
 * - Kalphite Queen
 * - Sewer Abomination (Cerberus model)
 * - Crazy Archaeologist
 * - Chaos Fanatic
 * - Chaos Elemental
 */
class BossIslandSpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Boss Island center coordinates - "Bounty Hunter" teleport location
         */
        private const val ISLAND_CENTER_X = 3423
        private const val ISLAND_CENTER_Z = 4089
        private const val ISLAND_HEIGHT = 0
        
        /**
         * Region for multi-combat setting
         * Region calculation: (floor(x/64) * 256) + floor(z/64)
         * For coordinates (3423, 4089): (53 * 256) + 63 = 13631
         */
        private const val BOSS_ISLAND_REGION = 13631
    }

    /**
     * Track spawned boss locations to prevent overlapping
     * Must be initialized before init block to avoid NullPointerException
     */
    private val spawnedLocations = mutableSetOf<Pair<Int, Int>>()
    
    /**
     * Minimum distance between boss spawn points (in tiles)
     */
    private val MIN_BOSS_DISTANCE = 6

    init {
        // Set the boss island as multi-combat area
        setMultiCombatRegion(region = BOSS_ISLAND_REGION)
        
        // Spawn all major bosses around the island center
        spawnBossesOnIsland()
    }
    
    /**
     * Spawns all major bosses on the boss island in a strategic layout with collision prevention
     */
    private fun spawnBossesOnIsland() {
        // Clear previous spawn tracking
        spawnedLocations.clear()
        
        // Define boss spawn configurations with preferred locations and fallbacks
        val bossConfigs = listOf(
            BossConfig("npc.venenatis", ISLAND_CENTER_X - 15, ISLAND_CENTER_Z + 15, Direction.SOUTH, "Venenatis"),
            BossConfig("npc.callisto", ISLAND_CENTER_X, ISLAND_CENTER_Z + 20, Direction.SOUTH, "Callisto"),
            BossConfig("npc.vetion", ISLAND_CENTER_X + 15, ISLAND_CENTER_Z + 15, Direction.SOUTH, "Vet'ion"),
            BossConfig("npc.scorpia", ISLAND_CENTER_X + 20, ISLAND_CENTER_Z, Direction.WEST, "Scorpia"),
            BossConfig("npc.chaos_elemental", ISLAND_CENTER_X - 17, ISLAND_CENTER_Z - 6, Direction.NORTH, "Chaos Elemental"),
            BossConfig("npc.chaos_fanatic", ISLAND_CENTER_X, ISLAND_CENTER_Z - 20, Direction.NORTH, "Chaos Fanatic"),
            BossConfig("npc.crazy_archaeologist", ISLAND_CENTER_X + 15, ISLAND_CENTER_Z - 15, Direction.NORTH, "Crazy Archaeologist"),
            BossConfig("npc.king_black_dragon", ISLAND_CENTER_X - 25, ISLAND_CENTER_Z, Direction.EAST, "King Black Dragon"),
            BossConfig("npc.kalphite_queen_963", ISLAND_CENTER_X - 5, ISLAND_CENTER_Z + 5, Direction.SOUTH, "Kalphite Queen"),
            BossConfig("npc.cerberus", ISLAND_CENTER_X + 5, ISLAND_CENTER_Z - 5, Direction.NORTH, "Sewer Abomination")
        )
        
        var spawnedCount = 0
        val spawnedBosses = mutableListOf<String>()
        
        // Spawn each boss with collision detection
        bossConfigs.forEach { config ->
            val spawnLocation = findValidSpawnLocation(config.preferredX, config.preferredZ)
            if (spawnLocation != null) {
                spawnNpc(
                    npc = config.npcId,
                    x = spawnLocation.first,
                    z = spawnLocation.second,
                    height = ISLAND_HEIGHT,
                    walkRadius = 5, // Reduced walk radius to prevent overlap
                    direction = config.direction
                )
                
                spawnedLocations.add(spawnLocation)
                spawnedBosses.add(config.displayName)
                spawnedCount++
            }
        }
    }
    
    /**
     * Finds a valid spawn location that doesn't conflict with existing spawns
     */
    private fun findValidSpawnLocation(preferredX: Int, preferredZ: Int): Pair<Int, Int>? {
        // First try the preferred location
        if (isLocationValid(preferredX, preferredZ)) {
            return Pair(preferredX, preferredZ)
        }
        
        // If preferred location is taken, search in expanding radius
        for (radius in 1..15) {
            for (xOffset in -radius..radius) {
                for (zOffset in -radius..radius) {
                    // Only check positions on the edge of the current radius to avoid redundant checks
                    if (kotlin.math.abs(xOffset) == radius || kotlin.math.abs(zOffset) == radius) {
                        val testX = preferredX + xOffset
                        val testZ = preferredZ + zOffset
                        
                        // Make sure we stay within reasonable bounds of the island
                        if (isWithinIslandBounds(testX, testZ) && isLocationValid(testX, testZ)) {
                            return Pair(testX, testZ)
                        }
                    }
                }
            }
        }
        
        return null // No valid location found
    }
    
    /**
     * Checks if a location is valid for spawning (not too close to existing spawns)
     */
    private fun isLocationValid(x: Int, z: Int): Boolean {
        return spawnedLocations.none { (existingX, existingZ) ->
            val distance = kotlin.math.max(
                kotlin.math.abs(x - existingX),
                kotlin.math.abs(z - existingZ)
            )
            distance < MIN_BOSS_DISTANCE
        }
    }
    
    /**
     * Checks if coordinates are within reasonable bounds of Boss Island
     */
    private fun isWithinIslandBounds(x: Int, z: Int): Boolean {
        val distanceX = kotlin.math.abs(x - ISLAND_CENTER_X)
        val distanceZ = kotlin.math.abs(z - ISLAND_CENTER_Z)
        val maxDistance = kotlin.math.max(distanceX, distanceZ)
        return maxDistance <= 30 // Keep within 30 tiles of center
    }
    
    /**
     * Data class to hold boss configuration
     */
    private data class BossConfig(
        val npcId: String,
        val preferredX: Int,
        val preferredZ: Int,
        val direction: Direction,
        val displayName: String
    )
}