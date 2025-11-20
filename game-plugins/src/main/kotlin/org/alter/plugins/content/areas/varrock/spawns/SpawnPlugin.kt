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
         * Using guard_3010 and guard_3011 which have the pickpocket option.
         */
        spawnNpc(npc = "npc.guard_3010", x = 3211, z = 3424, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3011", x = 3213, z = 3424, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3010", x = 3209, z = 3424, walkRadius = 5, direction = Direction.SOUTH)

        /**
         * Makeover Mage
         *
         * Makeover Mage in Varrock square for character customization.
         */
        spawnNpc(npc = "npc.makeover_mage", x = 3201, z = 3424, walkRadius = 0, direction = Direction.SOUTH)

        /**
         * Castle Entrance Guards
         *
         * Guards stationed at the main entrance to Varrock Castle.
         * Castle is north of Varrock square, around coordinates 3200-3230, 3370-3400
         */
        spawnNpc(npc = "npc.guard_3010", x = 3210, z = 3469, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3011", x = 3212, z = 3469, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3010", x = 3214, z = 3469, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3011", x = 3216, z = 3469, walkRadius = 5, direction = Direction.SOUTH)

        /**
         * Castle Courtyard Guards
         *
         * Guards patrolling inside the castle courtyard.
         */
        spawnNpc(npc = "npc.guard_3010", x = 3208, z = 3380, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_3011", x = 3218, z = 3380, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_3010", x = 3205, z = 3385, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard_3011", x = 3221, z = 3385, walkRadius = 8, direction = Direction.WEST)

        /**
         * Castle Interior Guards
         *
         * Guards stationed inside the castle building.
         */
        spawnNpc(npc = "npc.guard_3010", x = 3208, z = 3395, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3011", x = 3218, z = 3395, walkRadius = 5, direction = Direction.SOUTH)

        /**
         * Upper Floor Guards
         *
         * Guards on the first floor of the castle.
         */
        spawnNpc(npc = "npc.guard_3010", x = 3208, z = 3395, height = 1, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_3011", x = 3218, z = 3395, height = 1, walkRadius = 5, direction = Direction.NORTH)
        /**
         * Entrance Dark Wizards
         * 
         * Dark Wizards at the entrance of the Varrock City, South.
         */
        spawnNpc(npc = "npc.dark_wizard", x = 3224, z = 3369, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3226, z = 3367, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3233, z = 3370, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3234, z = 3372, walkRadius = 4, direction = Direction.SOUTH)

        //Scorpion in Mine
        spawnNpc(npc = "npc.scorpion", x = 3285, z = 3366, walkRadius = 4, direction = Direction.SOUTH)

        //unicorn south of mine
        spawnNpc(npc = "npc.unicorn", x = 3281, z = 3349, walkRadius = 12, direction = Direction.SOUTH)

        //bear south of mine
        spawnNpc(npc = "npc.grizzly_bear", x = 3279, z = 3349, walkRadius = 8, direction = Direction.SOUTH)
    }
}

