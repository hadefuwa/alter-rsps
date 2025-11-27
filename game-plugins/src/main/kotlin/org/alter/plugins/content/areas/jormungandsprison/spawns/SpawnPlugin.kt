package org.alter.plugins.content.areas.jormungandsprison.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Jormungand's Prison Spawn Plugin
 * 
 * This plugin spawns NPCs in Jormungand's Prison, an underground area beneath Rellekka.
 * Contains Basilisks and Dagannoths for Fremennik Slayer tasks.
 * Coordinates are based on NPCList_OSRS.json
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Height 0 (Underground Level) - Basilisks and Dagannoths
         * 
         * Basilisks are slayer creatures that require level 40 Slayer to kill.
         * Dagannoths are aggressive creatures found in Fremennik areas.
         */
        
        // Basilisk spawns - 9 total
        spawnNpc(npc = "npc.basilisk_417", x = 2475, z = 10403, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2478, z = 10408, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2480, z = 10401, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2459, z = 10398, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2459, z = 10403, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2461, z = 10407, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2463, z = 10399, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2464, z = 10403, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_417", x = 2467, z = 10398, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        // Basilisk Knight spawns - 3 total (higher level variants)
        spawnNpc(npc = "npc.basilisk_knight_9293", x = 2470, z = 10410, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_knight_9293", x = 2455, z = 10405, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.basilisk_knight_9293", x = 2475, z = 10395, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        // Dagannoth spawns - 4 total
        spawnNpc(npc = "npc.dagannoth_7260", x = 2446, z = 10430, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dagannoth_7260", x = 2479, z = 10430, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dagannoth_7259", x = 2452, z = 10432, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dagannoth_7260", x = 2475, z = 10433, height = 0, walkRadius = 5, direction = Direction.SOUTH)
    }
}