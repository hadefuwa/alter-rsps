package org.alter.plugins.content.areas.barbarianvillage.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Barbarian Village Spawn Plugin
 * 
 * This plugin spawns NPCs in Barbarian Village including:
 * - Barbarians
 * - Barbarian Women
 * 
 * Coordinates are based on AutoSpawn.cfg.txt
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Barbarians
         * 
         * Barbarian NPCs throughout the village
         */
        spawnNpc(npc = "npc.barbarian", x = 3097, z = 3421, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.barbarian", x = 3089, z = 3422, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.barbarian", x = 3086, z = 3420, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.barbarian", x = 3083, z = 3415, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.barbarian", x = 3078, z = 3419, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.barbarian", x = 3086, z = 3424, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Barbarian Women
         * 
         * Barbarian women NPCs in the village
         */
        spawnNpc(npc = "npc.woman", x = 3075, z = 3419, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.woman", x = 3078, z = 3420, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.woman", x = 3091, z = 3421, walkRadius = 3, direction = Direction.SOUTH)
    }
}

