package org.alter.plugins.content.areas.strongholdofsecurity.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Stronghold of Security Spawn Plugin
 * 
 * This plugin is responsible for spawning all monsters throughout the Stronghold of Security.
 * The Stronghold consists of four levels, each with different monsters and increasing difficulty.
 * 
 * Stronghold of Security Location:
 * - Entrance: X: 3081, Z: 3420 (Barbarian Village)
 * - Floor 1 (The Vault of War): X: ~1860-1907, Z: ~5221-5244
 * - Floor 2 (The Catacomb of Famine): X: ~2020-2042, Z: ~5215-5245
 * - Floor 3 (The Pit of Pestilence): X: ~2122-2144, Z: ~5251-5281
 * - Floor 4 (The Sepulchre of Death): X: ~2344-2358, Z: ~5210-5220
 * 
 * Monster Distribution by Floor:
 * - Floor 1: Goblins (Levels 5-7), Rats (Level 1), Minotaurs (Levels 14, 52), Wolves (Level 23)
 * - Floor 2: Zombies (Levels 11-12), Flesh Crawlers (Levels 39-40), Rats (Level 1), Giant Rats (Level 9)
 * - Floor 3: Spiders (Level 15), Giant Spiders (Level 39), Scorpions (Levels 25-26), Catablepon (Levels 53-54)
 * - Floor 4: Shades (Level 61), Skeletons (Levels 43-49), Ghosts (Levels 42-48), Ankou (Levels 60-63)
 * 
 * @param r The plugin repository for registering spawns
 * @param world The game world instance
 * @param server The server instance
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    /**
     * Initialize the plugin and register all Stronghold of Security monster spawns.
     * 
     * This method is called automatically when the plugin is loaded by the server.
     * Each spawnNpc call registers a monster that will spawn when the server starts.
     * 
     * Spawn Parameters:
     * - npc: The NPC identifier from the RSCM configuration
     * - x: X coordinate in the game world
     * - z: Z coordinate in the game world (Y-axis in RuneScape)
     * - walkRadius: Maximum distance the NPC can randomly walk from spawn point
     * - direction: Initial facing direction when spawned
     * - height: Height level (defaults to 0 for underground levels)
     */
    init {
        /**
         * FLOOR 1: The Vault of War
         * 
         * This is the first level of the Stronghold, featuring low to mid-level monsters.
         * Monsters: Goblins, Rats, Minotaurs, Wolves
         * Coordinates: X: ~1860-1907, Z: ~5221-5244
         */
        
        // Goblins - Level 1 (Combat levels 5-7)
        spawnNpc(npc = "npc.goblin", x = 1865, z = 5225, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin", x = 1867, z = 5227, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.goblin", x = 1869, z = 5225, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin", x = 1871, z = 5229, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.goblin", x = 1873, z = 5227, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin", x = 1875, z = 5225, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.goblin", x = 1877, z = 5231, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin", x = 1879, z = 5229, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.goblin", x = 1881, z = 5227, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin", x = 1883, z = 5225, walkRadius = 6, direction = Direction.EAST)
        
        // Rats - Level 1 (Combat level 1)
        spawnNpc(npc = "npc.rat", x = 1885, z = 5233, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat", x = 1887, z = 5231, walkRadius = 4, direction = Direction.EAST)
        spawnNpc(npc = "npc.rat", x = 1889, z = 5235, walkRadius = 4, direction = Direction.WEST)
        spawnNpc(npc = "npc.rat", x = 1891, z = 5233, walkRadius = 4, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat", x = 1893, z = 5231, walkRadius = 4, direction = Direction.SOUTH)
        
        // Minotaurs - Level 1 (Combat levels 14, 52)
        spawnNpc(npc = "npc.minotaur", x = 1895, z = 5237, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.minotaur", x = 1897, z = 5235, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.minotaur", x = 1899, z = 5239, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.minotaur", x = 1901, z = 5237, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.minotaur", x = 1903, z = 5235, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.minotaur", x = 1905, z = 5241, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.minotaur", x = 1907, z = 5239, walkRadius = 5, direction = Direction.WEST)
        
        // Wolves - Level 1 (Combat level 23)
        spawnNpc(npc = "npc.wolf", x = 1860, z = 5230, walkRadius = 7, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf", x = 1862, z = 5232, walkRadius = 7, direction = Direction.EAST)
        spawnNpc(npc = "npc.wolf", x = 1864, z = 5230, walkRadius = 7, direction = Direction.WEST)
        spawnNpc(npc = "npc.wolf", x = 1866, z = 5234, walkRadius = 7, direction = Direction.NORTH)
        spawnNpc(npc = "npc.wolf", x = 1868, z = 5232, walkRadius = 7, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf", x = 1870, z = 5230, walkRadius = 7, direction = Direction.EAST)
        
        /**
         * FLOOR 2: The Catacomb of Famine
         * 
         * The second level features undead and crawling creatures.
         * Monsters: Zombies, Flesh Crawlers, Rats, Giant Rats
         * Coordinates: X: ~2020-2042, Z: ~5215-5245
         */
        
        // Zombies - Level 2 (Combat levels 11-12)
        spawnNpc(npc = "npc.zombie", x = 2022, z = 5217, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 2024, z = 5219, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.zombie", x = 2026, z = 5217, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie", x = 2028, z = 5221, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.zombie", x = 2030, z = 5219, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 2032, z = 5217, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.zombie", x = 2034, z = 5223, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie", x = 2036, z = 5221, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.zombie", x = 2038, z = 5219, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie", x = 2040, z = 5225, walkRadius = 6, direction = Direction.EAST)
        
        // Flesh Crawlers - Level 2 (Combat levels 39-40)
        spawnNpc(npc = "npc.flesh_crawler", x = 2020, z = 5227, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.flesh_crawler", x = 2022, z = 5229, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.flesh_crawler", x = 2024, z = 5227, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.flesh_crawler", x = 2026, z = 5231, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.flesh_crawler", x = 2028, z = 5229, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.flesh_crawler", x = 2030, z = 5233, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.flesh_crawler", x = 2032, z = 5231, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.flesh_crawler", x = 2034, z = 5229, walkRadius = 5, direction = Direction.NORTH)
        
        // Rats - Level 2 (Combat level 1)
        spawnNpc(npc = "npc.rat", x = 2036, z = 5235, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.rat", x = 2038, z = 5233, walkRadius = 4, direction = Direction.EAST)
        spawnNpc(npc = "npc.rat", x = 2040, z = 5237, walkRadius = 4, direction = Direction.WEST)
        spawnNpc(npc = "npc.rat", x = 2042, z = 5235, walkRadius = 4, direction = Direction.NORTH)
        
        // Giant Rats - Level 2 (Combat level 9)
        spawnNpc(npc = "npc.giant_rat", x = 2020, z = 5237, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_rat", x = 2022, z = 5239, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_rat", x = 2024, z = 5237, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.giant_rat", x = 2026, z = 5241, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.giant_rat", x = 2028, z = 5239, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_rat", x = 2030, z = 5243, walkRadius = 5, direction = Direction.EAST)
        
        /**
         * FLOOR 3: The Pit of Pestilence
         * 
         * The third level features poisonous and crawling creatures.
         * Monsters: Spiders, Giant Spiders, Scorpions, Catablepon
         * Coordinates: X: ~2122-2144, Z: ~5251-5281
         */
        
        // Spiders - Level 3 (Combat level 15)
        spawnNpc(npc = "npc.spider", x = 2124, z = 5253, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.spider", x = 2126, z = 5255, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.spider", x = 2128, z = 5253, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.spider", x = 2130, z = 5257, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.spider", x = 2132, z = 5255, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.spider", x = 2134, z = 5259, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.spider", x = 2136, z = 5257, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.spider", x = 2138, z = 5255, walkRadius = 5, direction = Direction.NORTH)
        
        // Giant Spiders - Level 3 (Combat level 39)
        spawnNpc(npc = "npc.giant_spider", x = 2122, z = 5261, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_spider", x = 2124, z = 5263, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_spider", x = 2126, z = 5261, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.giant_spider", x = 2128, z = 5265, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.giant_spider", x = 2130, z = 5263, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_spider", x = 2132, z = 5267, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_spider", x = 2134, z = 5265, walkRadius = 6, direction = Direction.WEST)
        
        // Scorpions - Level 3 (Combat levels 25-26)
        spawnNpc(npc = "npc.scorpion", x = 2136, z = 5269, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.scorpion", x = 2138, z = 5267, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.scorpion", x = 2140, z = 5271, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.scorpion", x = 2142, z = 5269, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.scorpion", x = 2144, z = 5267, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.scorpion", x = 2122, z = 5273, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.scorpion", x = 2124, z = 5271, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.scorpion", x = 2126, z = 5275, walkRadius = 5, direction = Direction.NORTH)
        
        // Catablepon - Level 3 (Combat levels 53-54)
        spawnNpc(npc = "npc.catablepon", x = 2128, z = 5277, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.catablepon", x = 2130, z = 5275, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.catablepon", x = 2132, z = 5279, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.catablepon", x = 2134, z = 5277, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.catablepon", x = 2136, z = 5275, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.catablepon", x = 2138, z = 5281, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.catablepon", x = 2140, z = 5279, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.catablepon", x = 2142, z = 5277, walkRadius = 6, direction = Direction.NORTH)
        
        /**
         * FLOOR 4: The Sepulchre of Death
         * 
         * The fourth and final level features high-level undead creatures.
         * Monsters: Shades, Skeletons, Ghosts, Ankou
         * Coordinates: X: ~2344-2358, Z: ~5210-5220
         */
        
        // Shades - Level 4 (Combat level 61)
        spawnNpc(npc = "npc.shade", x = 2346, z = 5213, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.shade", x = 2348, z = 5215, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.shade", x = 2350, z = 5213, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.shade", x = 2352, z = 5215, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.shade", x = 2354, z = 5213, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.shade", x = 2356, z = 5215, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.shade", x = 2358, z = 5213, walkRadius = 5, direction = Direction.WEST)
        
        // Skeletons - Level 4 (Combat levels 43-49)
        spawnNpc(npc = "npc.skeleton", x = 2344, z = 5214, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 2346, z = 5216, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.skeleton", x = 2348, z = 5214, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.skeleton", x = 2350, z = 5216, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.skeleton", x = 2352, z = 5214, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 2354, z = 5216, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.skeleton", x = 2356, z = 5214, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.skeleton", x = 2358, z = 5216, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.skeleton", x = 2344, z = 5212, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 2346, z = 5210, walkRadius = 6, direction = Direction.EAST)
        
        // Ghosts - Level 4 (Combat levels 42-48)
        spawnNpc(npc = "npc.ghost", x = 2348, z = 5212, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ghost", x = 2350, z = 5210, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.ghost", x = 2352, z = 5212, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.ghost", x = 2354, z = 5210, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.ghost", x = 2356, z = 5212, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ghost", x = 2358, z = 5210, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.ghost", x = 2344, z = 5218, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.ghost", x = 2346, z = 5220, walkRadius = 5, direction = Direction.NORTH)
        
        // Ankou - Level 4 (Combat levels 60-63)
        spawnNpc(npc = "npc.ankou", x = 2348, z = 5218, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 2350, z = 5216, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.ankou", x = 2352, z = 5220, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.ankou", x = 2354, z = 5218, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.ankou", x = 2356, z = 5216, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 2358, z = 5220, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.ankou", x = 2344, z = 5220, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.ankou", x = 2346, z = 5218, walkRadius = 6, direction = Direction.NORTH)
    }
}

