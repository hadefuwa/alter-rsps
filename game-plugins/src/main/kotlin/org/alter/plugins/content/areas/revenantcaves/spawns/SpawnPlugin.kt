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
         * Spawn Location: Coordinates 3214, 10097 (2 spawns per location - reduced from 4)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_ork", x = 3212, z = 10095, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3214, z = 10095, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Imp - Revenant Caves
         * 
         * A revenant imp that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3201, 10073 (2 spawns per location - reduced from 4)
         * Walk Radius: 6 tiles (smaller creature, moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_imp", x = 3199, z = 10071, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_imp", x = 3204, z = 10071, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Imp - Revenant Caves (Second Spawn Location)
         * 
         * A revenant imp that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3216, 10194 (1 spawn per location - reduced from 2)
         * Walk Radius: 6 tiles (smaller creature, moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_imp", x = 3214, z = 10192, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Goblin - Revenant Caves
         * 
         * A revenant goblin that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3224, 10073 (2 spawns per location - reduced from 4)
         * Walk Radius: 6 tiles (smaller creature, moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_goblin", x = 3222, z = 10071, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_goblin", x = 3227, z = 10071, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Hobgoblin - Revenant Caves
         * 
         * A revenant hobgoblin that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3243, 10100 (2 spawns per location - reduced from 4)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3241, z = 10098, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3246, z = 10098, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Ork - Revenant Caves (Second Spawn Location)
         * 
         * A powerful revenant ork that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3224, 10129 (2 spawns per location - reduced from 4)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_ork", x = 3222, z = 10127, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_ork", x = 3218, z = 10123, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Demon - Revenant Caves
         * 
         * A powerful revenant demon that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3163, 10114 (2 spawns per location - reduced from 4)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_demon", x = 3161, z = 10112, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_demon", x = 3166, z = 10112, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Pyrefiend - Revenant Caves
         * 
         * A revenant pyrefiend that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3178, 10149 (2 spawns per location - reduced from 4)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3176, z = 10147, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3181, z = 10147, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Dark Beast - Revenant Caves
         * 
         * A powerful revenant dark beast that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3206, 10165 (2 spawns per location - reduced from 4)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3204, z = 10163, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_dark_beast", x = 3209, z = 10163, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Demon (Greater Demon) and Revenant Cyclops (Hill Giant) - Revenant Caves
         * 
         * Powerful revenant monsters that spawn in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3175, 10189 (1 revenant demon, 1 revenant cyclops - reduced from 2 each)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        // Revenant Demon (Greater Demon) - 1 spawn
        spawnNpc(npc = "npc.revenant_demon", x = 3173, z = 10187, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        // Revenant Cyclops (Hill Giant) - 1 spawn
        spawnNpc(npc = "npc.revenant_cyclops", x = 3173, z = 10192, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Knight - Revenant Caves
         * 
         * A powerful revenant knight that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3210, 10218 (1 spawn per location - reduced from 3)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_knight", x = 3208, z = 10216, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Dragon - Revenant Caves
         * 
         * A powerful revenant dragon that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3237, 10200 (2 spawns per location - reduced from 4)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_dragon", x = 3235, z = 10198, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.revenant_dragon", x = 3240, z = 10198, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Revenant Hellhound and Revenant Pyrefiend - Revenant Caves
         * 
         * Powerful revenant monsters that spawn in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3245, 10172 (1 revenant hellhound, 1 revenant pyrefiend - reduced from 2 each)
         * Walk Radius: 8 tiles for hellhounds, 6 tiles for pyrefiends (moderate patrol area)
         */
        // Revenant Hellhound - 1 spawn
        spawnNpc(npc = "npc.revenant_hellhound", x = 3243, z = 10170, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        
        // Revenant Pyrefiend - 1 spawn
        spawnNpc(npc = "npc.revenant_pyrefiend", x = 3243, z = 10175, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Revenant Hobgoblin - Revenant Caves (Second Spawn Location)
         * 
         * A revenant hobgoblin that spawns in the Revenant Caves.
         * Revenants are dangerous monsters that can use multiple combat styles
         * and are aggressive to players.
         * 
         * Spawn Location: Coordinates 3253, 10144 (1 spawn per location - reduced from 3)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.revenant_hobgoblin", x = 3251, z = 10142, height = 0, walkRadius = 6, direction = Direction.SOUTH)
    }
}

