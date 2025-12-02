package org.alter.plugins.content.areas.tzhaar.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * TzHaar City Spawn Plugin
 * 
 * This plugin spawns NPCs in TzHaar City including:
 * - Banker at the TzHaar bank
 * - Various TzHaar NPCs (Mej, Hur, Ket, Xil) throughout the city
 * - Named TzHaar NPCs (Mej-Jal, Mej-Kah, Hur-Tel, Hur-Lek, Mej-Roh)
 * 
 * TzHaar City Coordinates:
 * - X coordinates: 2436 to 2483
 * - Z coordinates: 5153 to 5178
 * - Height: 0 (surface level)
 * 
 * Coordinates are based on AutoSpawn.cfg.txt and teleport locations
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Banker
         * 
         * Banker at the TzHaar bank for banking services
         */
        spawnNpc(npc = "npc.banker", x = 2445, z = 5178, height = 0, walkRadius = 2, direction = Direction.SOUTH)
        
        /**
         * TzHaar-Mej NPCs
         * 
         * TzHaar-Mej are mage-type TzHaar NPCs found throughout the city
         */
        spawnNpc(npc = "npc.tzhaarmej", x = 2439, z = 5171, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.tzhaarmej_2155", x = 2449, z = 5169, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.tzhaarmej_2156", x = 2456, z = 5160, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.tzhaarmej_2157", x = 2455, z = 5156, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.tzhaarmej_2158", x = 2465, z = 5163, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.tzhaarmej_2159", x = 2463, z = 5170, height = 0, walkRadius = 5, direction = Direction.EAST)
        
        /**
         * TzHaar-Hur NPCs
         * 
         * TzHaar-Hur are warrior-type TzHaar NPCs that guard and patrol the city
         */
        spawnNpc(npc = "npc.tzhaarhur_2161", x = 2471, z = 5168, height = 0, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.tzhaarhur_2162", x = 2477, z = 5169, height = 0, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.tzhaarhur_2163", x = 2475, z = 5153, height = 0, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.tzhaarhur_2164", x = 2483, z = 5154, height = 0, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.tzhaarhur_2165", x = 2447, z = 5165, height = 0, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.tzhaarhur_2166", x = 2450, z = 5175, height = 0, walkRadius = 6, direction = Direction.NORTH)
        
        /**
         * TzHaar-Ket NPCs
         * 
         * TzHaar-Ket are fighter-type TzHaar NPCs found in the city
         */
        spawnNpc(npc = "npc.tzhaarket", x = 2440, z = 5165, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.tzhaarket_2174", x = 2442, z = 5167, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.tzhaarket_2175", x = 2452, z = 5158, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.tzhaarket_2176", x = 2460, z = 5165, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.tzhaarket_2177", x = 2468, z = 5157, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * TzHaar-Xil NPCs
         * 
         * TzHaar-Xil are ranger-type TzHaar NPCs found in the city
         */
        spawnNpc(npc = "npc.tzhaarxil", x = 2443, z = 5162, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.tzhaarxil_2168", x = 2451, z = 5164, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.tzhaarxil_2169", x = 2458, z = 5172, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.tzhaarxil_2170", x = 2470, z = 5160, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Named TzHaar NPCs
         * 
         * These are specific named NPCs with unique roles in TzHaar City
         */
        // TzHaar-Mej-Jal - Library assistant or quest NPC
        spawnNpc(npc = "npc.tzhaarmejjal", x = 2448, z = 5168, height = 0, walkRadius = 2, direction = Direction.SOUTH)
        
        // TzHaar-Mej-Kah - Quest or dialogue NPC
        spawnNpc(npc = "npc.tzhaarmejkah", x = 2453, z = 5166, height = 0, walkRadius = 2, direction = Direction.EAST)
        
        // TzHaar-Hur-Tel - Equipment store owner
        spawnNpc(npc = "npc.tzhaarhurtel", x = 2455, z = 5172, height = 0, walkRadius = 2, direction = Direction.WEST)
        
        // TzHaar-Hur-Lek - Ore and Gem Store owner
        spawnNpc(npc = "npc.tzhaarhurlek", x = 2460, z = 5170, height = 0, walkRadius = 2, direction = Direction.NORTH)
        
        // TzHaar-Mej-Roh - Quest or dialogue NPC
        spawnNpc(npc = "npc.tzhaarmejroh", x = 2442, z = 5170, height = 0, walkRadius = 2, direction = Direction.SOUTH)
        
        /**
         * TzTok-Jad
         * 
         * TzTok-Jad, the final boss of the Fight Cave minigame
         * Spawned at coordinates 2394, 5087 in the Fight Cave area
         */
        spawnNpc(npc = "npc.tztokjad", x = 2394, z = 5087, height = 0, walkRadius = 0, direction = Direction.SOUTH)
    }
}




