package org.alter.plugins.content.areas.varrock.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Varrock Castle Guard Spawn Plugin
 * 
 * This plugin spawns guards at Varrock Castle to protect the royal palace.
 * Guards are stationed at the castle entrance and throughout the castle grounds.
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Varrock Square Guards
         * 
         * Guards in the main Varrock square area for testing.
         */
        spawnNpc(npc = "npc.guard_397", x = 3211, z = 3424, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3213, z = 3424, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_399", x = 3209, z = 3424, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Castle Entrance Guards
         * 
         * Guards stationed at the main entrance to Varrock Castle.
         * Castle is north of Varrock square, around coordinates 3200-3230, 3370-3400
         */
        spawnNpc(npc = "npc.guard_397", x = 3210, z = 3376, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3212, z = 3376, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_399", x = 3214, z = 3376, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_400", x = 3216, z = 3376, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Castle Courtyard Guards
         * 
         * Guards patrolling inside the castle courtyard.
         */
        spawnNpc(npc = "npc.guard_397", x = 3208, z = 3380, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_398", x = 3218, z = 3380, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_399", x = 3205, z = 3385, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard_400", x = 3221, z = 3385, walkRadius = 8, direction = Direction.WEST)
        
        /**
         * Castle Interior Guards
         * 
         * Guards stationed inside the castle building.
         */
        spawnNpc(npc = "npc.guard_397", x = 3208, z = 3395, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3218, z = 3395, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Upper Floor Guards
         * 
         * Guards on the first floor of the castle.
         */
        spawnNpc(npc = "npc.guard_399", x = 3208, z = 3395, height = 1, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_400", x = 3218, z = 3395, height = 1, walkRadius = 5, direction = Direction.NORTH)
    }
}

