package org.alter.plugins.content.areas.brimhaven.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Brimhaven Dungeon Spawn Plugin
 * 
 * This plugin spawns NPCs in the Brimhaven Dungeon, located on Karamja.
 * Brimhaven Dungeon requires an entry fee to access and contains various dragons,
 * demons, and other monsters across different areas.
 * 
 * The dungeon contains:
 * - Red Dragons (Combat Level 152): Found in the red dragon area
 * - Baby Red Dragons (Combat Level 48): Found near red dragons
 * - Iron Dragons (Combat Level 189): Found in the metal dragons area
 * - Steel Dragons (Combat Level 246): Found in the metal dragons area
 * - Bronze Dragons (Combat Level 131): Found in the metal dragons area
 * - Black Demons (Combat Level 172): Found in the demon area
 * - Wild Dogs (Combat Level 63): Found in various areas
 * 
 * All dragons are aggressive and use a mix of melee and fire breath attacks.
 * Players should bring antifire potions and protect from melee prayer for safety.
 * 
 * Coordinates are based on NPCList_OSRS.json extraction.
 * Height level: 0 (underground dungeon level)
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Red Dragons (Combat Level 152)
         * 
         * Red dragons are powerful dragons found in Brimhaven Dungeon.
         * They are aggressive and use a mix of melee and fire breath attacks.
         * They drop dragon bones, red dragonhide, and various runes.
         * 
         * Spawn Location: Brimhaven Dungeon red dragon area
         * Total Spawns: 15 Red Dragons
         * Walk Radius: 5 tiles (dragons are relatively stationary)
         */
        spawnNpc(npc = "npc.red_dragon_250", x = 2704, z = 9539, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.red_dragon_249", x = 2704, z = 9546, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.red_dragon_251", x = 2711, z = 9537, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.red_dragon_248", x = 2711, z = 9550, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.red_dragon", x = 2712, z = 9543, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.red_dragon", x = 2702, z = 9504, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.red_dragon_249", x = 2703, z = 9522, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.red_dragon_248", x = 2703, z = 9532, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.red_dragon_250", x = 2706, z = 9516, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.red_dragon_251", x = 2708, z = 9508, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.red_dragon_248", x = 2711, z = 9500, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.red_dragon_248", x = 2714, z = 9526, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.red_dragon_249", x = 2717, z = 9516, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.red_dragon_248", x = 2721, z = 9522, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.red_dragon", x = 2724, z = 9516, height = 0, walkRadius = 5, direction = Direction.WEST)
        
        /**
         * Baby Red Dragons (Combat Level 48)
         * 
         * Baby red dragons are smaller, weaker versions of red dragons.
         * They are aggressive but less dangerous than their adult counterparts.
         * They drop dragon bones and small amounts of loot.
         * 
         * Spawn Location: Brimhaven Dungeon red dragon area
         * Total Spawns: 10 Baby Red Dragons
         * Walk Radius: 5 tiles
         */
        spawnNpc(npc = "npc.baby_red_dragon_244", x = 2701, z = 9548, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.baby_red_dragon_246", x = 2709, z = 9545, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.baby_red_dragon_246", x = 2720, z = 9536, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.baby_red_dragon_245", x = 2708, z = 9500, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.baby_red_dragon_245", x = 2708, z = 9526, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.baby_red_dragon_244", x = 2711, z = 9516, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.baby_red_dragon_246", x = 2714, z = 9505, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.baby_red_dragon_244", x = 2721, z = 9511, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.baby_red_dragon_244", x = 2721, z = 9530, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.baby_red_dragon_245", x = 2730, z = 9512, height = 0, walkRadius = 5, direction = Direction.EAST)
        
        /**
         * Black Demons (Combat Level 172)
         * 
         * Black demons are powerful demons found in Brimhaven Dungeon.
         * They are aggressive melee fighters with high combat stats.
         * They drop rune items, runes, and other valuable loot.
         * 
         * Spawn Location: Brimhaven Dungeon demon area
         * Total Spawns: 4 Black Demons
         * Walk Radius: 5 tiles
         */
        spawnNpc(npc = "npc.black_demon_2050", x = 2700, z = 9489, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon_2048", x = 2703, z = 9483, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.black_demon_2049", x = 2709, z = 9479, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.black_demon_2048", x = 2715, z = 9482, height = 0, walkRadius = 5, direction = Direction.NORTH)
        
        /**
         * Bronze Dragons (Combat Level 131)
         * 
         * Bronze dragons are metal dragons found in Brimhaven Dungeon.
         * They are aggressive and use a mix of melee and fire breath attacks.
         * They drop dragon bones, bronze bars, and various runes.
         * 
         * Spawn Location: Brimhaven Dungeon metal dragons area
         * Total Spawns: 3 Bronze Dragons
         * Walk Radius: 5 tiles
         */
        spawnNpc(npc = "npc.bronze_dragon", x = 2731, z = 9482, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bronze_dragon", x = 2731, z = 9491, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.bronze_dragon", x = 2740, z = 9492, height = 0, walkRadius = 5, direction = Direction.WEST)
        
        /**
         * Iron Dragons (Combat Level 189)
         * 
         * Iron dragons are powerful metal dragons found in Brimhaven Dungeon.
         * They have 160 hitpoints and are aggressive. They drop dragon bones,
         * iron bars, runes, and have a chance to drop rare items like dragon
         * med helms, shield left halves, and visages.
         * 
         * Spawn Location: Brimhaven Dungeon metal dragons area
         * Total Spawns: 13 Iron Dragons
         * Walk Radius: 5 tiles (dragons are relatively stationary)
         */
        spawnNpc(npc = "npc.iron_dragon", x = 2704, z = 9431, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2704, z = 9457, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.iron_dragon", x = 2705, z = 9424, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.iron_dragon", x = 2714, z = 9420, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2714, z = 9449, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2714, z = 9460, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.iron_dragon", x = 2722, z = 9424, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.iron_dragon", x = 2724, z = 9435, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2730, z = 9437, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2732, z = 9459, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.iron_dragon", x = 2736, z = 9424, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.iron_dragon", x = 2738, z = 9440, height = 0, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.iron_dragon", x = 2739, z = 9450, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Steel Dragons (Combat Level 246)
         * 
         * Steel dragons are the strongest metal dragons in Brimhaven Dungeon.
         * They have 210 hitpoints and are very aggressive. They drop dragon bones,
         * steel bars, runes, and have a chance to drop rare items like dragon
         * med helms, shield left halves, and visages.
         * 
         * Spawn Location: Brimhaven Dungeon metal dragons area
         * Total Spawns: 4 Steel Dragons
         * Walk Radius: 5 tiles (dragons are relatively stationary)
         */
        spawnNpc(npc = "npc.steel_dragon_274", x = 2702, z = 9447, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.steel_dragon_274", x = 2712, z = 9435, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.steel_dragon_274", x = 2723, z = 9458, height = 0, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.steel_dragon_274", x = 2726, z = 9445, height = 0, walkRadius = 5, direction = Direction.NORTH)
        
        /**
         * Wild Dogs (Combat Level 63)
         * 
         * Wild dogs are aggressive canines found in Brimhaven Dungeon.
         * They are fast-moving melee fighters that attack players on sight.
         * They drop bones and small amounts of loot.
         * 
         * Spawn Location: Brimhaven Dungeon various areas
         * Total Spawns: 3 Wild Dogs
         * Walk Radius: 5 tiles
         */
        spawnNpc(npc = "npc.wild_dog", x = 2740, z = 9502, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wild_dog", x = 2740, z = 9508, height = 0, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.wild_dog", x = 2743, z = 9503, height = 0, walkRadius = 5, direction = Direction.WEST)
    }
}

