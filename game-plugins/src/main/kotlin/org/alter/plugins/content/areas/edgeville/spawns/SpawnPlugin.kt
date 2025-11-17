package org.alter.plugins.content.areas.edgeville.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Edgeville Spawn Plugin
 * 
 * This plugin spawns NPCs in Edgeville including:
 * - Bankers at the bank
 * - Guards protecting the town
 * - Men and women NPCs
 * - Shop keepers and assistants
 * - Dungeon NPCs: Hill Giants, Thugs, Chaos Druids
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
         * Bankers
         * 
         * Bankers at the Edgeville bank
         */
        spawnNpc(npc = "npc.banker", x = 3096, z = 3489, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker", x = 3096, z = 3491, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker", x = 3096, z = 3492, walkRadius = 2, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker", x = 3098, z = 3492, walkRadius = 2, direction = Direction.SOUTH)
        
        /**
         * Men
         * 
         * Men NPCs in Edgeville
         */
        spawnNpc(npc = "npc.man_3106", x = 3098, z = 3509, walkRadius = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3095, z = 3511, walkRadius = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3095, z = 3508, walkRadius = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3164, z = 3678, walkRadius = 1, direction = Direction.SOUTH)
        
        /**
         * Guards
         * 
         * Guards protecting Edgeville
         */
        spawnNpc(npc = "npc.guard_397", x = 3110, z = 3514, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_397", x = 3108, z = 3514, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_397", x = 3113, z = 3514, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_397", x = 3113, z = 3516, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Shop Keeper
         * 
         * Shop keeper in Edgeville
         */
        spawnNpc(npc = "npc.shop_keeper", x = 3082, z = 3513, walkRadius = 0, direction = Direction.SOUTH)
        
        /**
         * General Assistant
         * 
         * General store assistant
         */
        spawnNpc(npc = "npc.shop_assistant", x = 3079, z = 3509, walkRadius = 0, direction = Direction.SOUTH)
        
        /**
         * Hill Giants - Dungeon Area
         * 
         * Hill giants in the Edgeville dungeon (height 0, underground)
         */
        spawnNpc(npc = "npc.hill_giant", x = 3119, z = 9834, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3114, z = 9833, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3116, z = 9836, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3121, z = 9844, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3116, z = 9843, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3111, z = 9845, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3109, z = 9841, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3108, z = 9835, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3107, z = 9828, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3100, z = 9832, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3101, z = 9835, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hill_giant", x = 3118, z = 9849, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Thugs - Dungeon Area
         * 
         * Thugs in the Edgeville dungeon
         */
        spawnNpc(npc = "npc.thug", x = 3129, z = 9930, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.thug", x = 3132, z = 9932, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.thug", x = 3125, z = 9929, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Chaos Druids - Dungeon Area
         * 
         * Chaos druids in the Edgeville dungeon
         */
        spawnNpc(npc = "npc.chaos_druid", x = 3117, z = 9931, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chaos_druid", x = 3114, z = 9930, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chaos_druid", x = 3112, z = 9928, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chaos_druid", x = 3113, z = 9926, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Ground Items
         * 
         * Items spawned on the ground in Edgeville
         */
        spawnItem(item = "item.adamant_gloves", amount = 1, x = 3100, z = 3485, height = 0)
    }
}

