package org.alter.plugins.content.areas.varrock.sewers

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Varrock Sewers Spawn Plugin
 * 
 * This plugin spawns NPCs in the Varrock sewer system including:
 * - Sewer Rats
 * - Zombies
 * - Scorpions
 * - Skeletons
 * - Ghosts
 * 
 * Coordinates are based on AutoSpawn.cfg.txt and user-provided locations.
 * Height 0 = ground level, Height 2 = second underground level
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Sewer Rats - Height 0 (Main sewer level)
         * 
         * Rats found throughout the Varrock sewer system
         */
        spawnNpc(npc = "npc.rat_2854", x = 3241, z = 9866, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3244, z = 9867, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3238, z = 9870, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3232, z = 9866, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3227, z = 9872, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3237, z = 9867, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3241, z = 9865, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3248, z = 9867, height = 0, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Sewer Rats - Height 2 (Lower sewer level)
         * 
         * Rats on the lower level of the sewers
         */
        spawnNpc(npc = "npc.rat_2854", x = 3278, z = 9895, height = 2, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3282, z = 9896, height = 2, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat_2854", x = 3283, z = 9895, height = 2, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Zombies - Height 0 (Main sewer level)
         * 
         * Undead creatures roaming the sewers
         */
        spawnNpc(npc = "npc.zombie", x = 3243, z = 9893, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 3259, z = 9891, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 3236, z = 9907, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 3231, z = 9905, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Scorpions - Height 0 (Main sewer level)
         * 
         * Venomous scorpions found in the sewers
         */
        spawnNpc(npc = "npc.scorpion", x = 3257, z = 9908, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.scorpion", x = 3251, z = 9906, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Scorpions - Height 2 (Lower sewer level)
         * 
         * Scorpions on the lower level (user-provided location)
         */
        spawnNpc(npc = "npc.scorpion", x = 3251, z = 9905, height = 2, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Skeletons - Height 0 (Main sewer level)
         * 
         * Skeletal remains animated in the sewers
         */
        spawnNpc(npc = "npc.skeleton", x = 3277, z = 9911, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3275, z = 9909, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3271, z = 9914, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3255, z = 9917, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3250, z = 9915, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Ghosts - Height 0 (Main sewer level)
         * 
         * Spectral entities haunting the sewers
         */
        spawnNpc(npc = "npc.ghost", x = 3241, z = 9915, height = 0, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ghost", x = 3241, z = 9907, height = 0, walkRadius = 4, direction = Direction.SOUTH)
    }
}

