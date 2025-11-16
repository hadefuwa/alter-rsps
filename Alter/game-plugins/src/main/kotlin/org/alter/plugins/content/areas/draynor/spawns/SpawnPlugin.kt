package org.alter.plugins.content.areas.draynor.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Draynor Village Spawn Plugin
 * 
 * This plugin spawns NPCs in Draynor Village including:
 * - Bankers at the bank
 * - Shop keepers and assistants (handled in DraynorShopPlugin)
 * - Various NPCs like Ned, Aggie, Wise Old Man, Diango, etc.
 * - Dark Wizards near the village
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
         * Bankers at the Draynor bank (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.banker", x = 3090, z = 3242, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker", x = 3090, z = 3243, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker", x = 3090, z = 3245, walkRadius = 0, direction = Direction.SOUTH)
        
        /**
         * Diango
         * 
         * Diango at his toy shop (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.diango", x = 3079, z = 3250, walkRadius = 1, direction = Direction.SOUTH)
        
        /**
         * Farmer
         * 
         * Farmer NPC in Draynor (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.farmer_3114", x = 3077, z = 3252, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Ned
         * 
         * Ned the sailor, located near the fishing spots (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.ned", x = 3100, z = 3259, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Aggie
         * 
         * Aggie the witch, maker of dyes (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.aggie", x = 3086, z = 3258, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Lady Kili
         * 
         * Lady Kili NPC (coordinates from AutoSpawn.cfg.txt)
         * Note: NPC name not found in RSCM - commented out until correct name is identified
         */
        // spawnNpc(npc = "npc.lady_kili", x = 3115, z = 3244, walkRadius = 1, direction = Direction.SOUTH)
        
        /**
         * Wise Old Man
         * 
         * The Wise Old Man of Draynor Village (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.wise_old_man", x = 3089, z = 3252, walkRadius = 0, direction = Direction.SOUTH)
        
        /**
         * Morgan
         * 
         * Morgan NPC (coordinates from AutoSpawn.cfg.txt)
         * Note: AutoSpawn.cfg.txt lists "Moargan" but RSCM has "morgan" - using morgan
         */
        spawnNpc(npc = "npc.morgan", x = 3097, z = 3268, walkRadius = 3, direction = Direction.SOUTH)
        
        /**
         * Veronica
         * 
         * Veronica NPC (coordinates from AutoSpawn.cfg.txt)
         */
        spawnNpc(npc = "npc.veronica", x = 3112, z = 3327, walkRadius = 1, direction = Direction.SOUTH)
        
        /**
         * Dark Wizards
         * 
         * Dark Wizards near Draynor Village (coordinates from AutoSpawn.cfg.txt)
         * These are aggressive NPCs that attack players
         */
        spawnNpc(npc = "npc.dark_wizard", x = 3083, z = 3240, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3088, z = 3237, walkRadius = 3, direction = Direction.SOUTH)
    }
}

