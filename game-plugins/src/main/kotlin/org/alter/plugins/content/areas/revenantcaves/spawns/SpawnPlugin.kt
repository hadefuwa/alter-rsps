package org.alter.plugins.content.areas.revenantcaves.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Revenant Caves Spawn Plugin
 * 
 * This plugin spawns revenant NPCs in the Revenant Caves dungeon.
 * The Revenant Caves are a dangerous PvP-enabled dungeon in the wilderness
 * where players can encounter various revenant monsters.
 * 
 * Coordinates in this area typically have z values above 10000, indicating
 * they are in a dungeon/cave system.
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Revenant Ork - Revenant Caves
         * 
         * A powerful revenant ork that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3214, 10097 (4 spawns per location)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_ork", x = 3214, z = 10097, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3215, z = 10097, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3214, z = 10098, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3215, z = 10098, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Imp - Revenant Caves
         * 
         * A revenant imp that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3201, 10073 (4 spawns per location)
         * Walk Radius: 6 tiles (smaller creature, moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_imp", x = 3201, z = 10073, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_imp", x = 3202, z = 10073, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_imp", x = 3201, z = 10074, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_imp", x = 3202, z = 10074, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Goblin - Revenant Caves
         * 
         * A revenant goblin that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3224, 10073 (4 spawns per location)
         * Walk Radius: 6 tiles (smaller creature, moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_goblin", x = 3224, z = 10073, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_goblin", x = 3225, z = 10073, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_goblin", x = 3224, z = 10074, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_goblin", x = 3225, z = 10074, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Hobgoblin - Revenant Caves
         * 
         * A revenant hobgoblin that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3243, 10100 (4 spawns per location)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3243, z = 10100, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3244, z = 10100, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3243, z = 10101, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3244, z = 10101, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Ork - Revenant Caves (Second Spawn Location)
         * 
         * A powerful revenant ork that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3224, 10129 (4 spawns per location)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_ork", x = 3224, z = 10129, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3225, z = 10129, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3224, z = 10130, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3225, z = 10130, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Demon - Revenant Caves
         * 
         * A powerful revenant demon that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3163, 10114 (4 spawns per location)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_demon", x = 3163, z = 10114, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_demon", x = 3164, z = 10114, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_demon", x = 3163, z = 10115, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_demon", x = 3164, z = 10115, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Pyrefiend - Revenant Caves
         * 
         * A revenant pyrefiend that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3178, 10149 (4 spawns per location)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3178, z = 10149, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3179, z = 10149, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3178, z = 10150, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3179, z = 10150, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Dark Beast - Revenant Caves
         * 
         * A powerful revenant dark beast that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3206, 10165 (4 spawns per location)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3206, z = 10165, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3207, z = 10165, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3206, z = 10166, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3207, z = 10166, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Demon (Greater Demon) and Revenant Cyclops (Hill Giant) - Revenant Caves
         * 
         * Powerful revenant monsters that spawn in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3175, 10189 (2 revenant demons, 2 revenant cyclops)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        // Revenant Demon (Greater Demon) - 2 spawns
        spawnNpc(npc = "npc.revenant_demon", x = 3175, z = 10189, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_demon", x = 3176, z = 10189, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        // Revenant Cyclops (Hill Giant) - 2 spawns
        spawnNpc(npc = "npc.revenant_cyclops", x = 3175, z = 10190, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_cyclops", x = 3176, z = 10190, height = 0, walkRadius = 8, direction = Direction.SOUTH)
    }
}

