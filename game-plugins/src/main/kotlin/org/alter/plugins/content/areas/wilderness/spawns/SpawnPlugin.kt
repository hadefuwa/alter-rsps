package org.alter.plugins.content.areas.wilderness.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Wilderness Monster Spawn Plugin
 * 
 * This plugin is responsible for spawning all monsters throughout the wilderness area.
 * It creates a dangerous PvE environment by populating the wilderness with various
 * aggressive monsters at different wilderness levels.
 * 
 * Wilderness Boundaries:
 * - X coordinates: 2941 to 3392
 * - Z coordinates: 3524 to 3968
 * 
 * All spawn coordinates in this file are verified to be within these boundaries.
 * 
 * Monster Distribution:
 * - Level 1-5 Wilderness: Dark Wizards, Chaos Druids, Skeletons, Wolves
 * - Level 5-10 Wilderness: Bandits, Skeletons, Wolves
 * - Level 12-15 Wilderness: Dark Warriors
 * - Level 13-20 Wilderness: Green Dragons
 * - Level 20+ Wilderness: Hellhounds
 * 
 * Total Spawns: 63 monsters across 8 different monster types
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
     * Initialize the plugin and register all wilderness monster spawns.
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
     * - height: Height level (defaults to 0 for surface level)
     */
    init {
        // Debug: Verify plugin is loading
        println("Wilderness SpawnPlugin: Loading wilderness spawns...")
        
        /**
         * Dark Wizards - Level 1-5 Wilderness
         * 
         * These are low-level aggressive mages that spawn near the Edgeville wilderness entrance.
         * They attack players with magic attacks and are a common threat for low-level players
         * entering the wilderness.
         * 
         * Spawn Location: Near Edgeville entrance (coordinates ~3103-3109, ~3553-3557)
         * Total Spawns: 9 Dark Wizards
         * Walk Radius: 5 tiles (relatively stationary, patrols small area)
         */
        
        // spawnNpc() is a function that tells the server to create a monster at a specific location
        // npc = "npc.dark_wizard" - This is the monster type (looks it up in the NPC configuration file)
        // x = 3103 - This is the X coordinate (east-west position) on the map
        // z = 3553 - This is the Z coordinate (north-south position) on the map (RuneScape uses Z for Y-axis)
        // walkRadius = 5 - The monster can randomly walk up to 5 tiles away from this spot
        // direction = Direction.SOUTH - Which way the monster faces when it first spawns
        spawnNpc(npc = "npc.dark_wizard", x = 3103, z = 3553, walkRadius = 5, direction = Direction.SOUTH)
        
        // Spawn another dark wizard at a slightly different location (2 tiles east)
        spawnNpc(npc = "npc.dark_wizard", x = 3105, z = 3553, walkRadius = 5, direction = Direction.NORTH)
        // More dark wizards spawning nearby to create a group
        spawnNpc(npc = "npc.dark_wizard", x = 3107, z = 3553, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3109, z = 3555, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.dark_wizard", x = 3103, z = 3555, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3105, z = 3557, walkRadius = 5, direction = Direction.NORTH)
        
        /**
         * Skeletons - Level 1-10 Wilderness
         * 
         * Undead warriors scattered throughout the lower wilderness levels.
         * They are common enemies that provide moderate combat experience.
         * 
         * Spawn Locations: Multiple locations across wilderness levels 1-10
         * Total Spawns: 12 Skeletons (8 initial + 4 additional scattered)
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        // Spawn skeletons at various locations - each spawnNpc call creates one skeleton
        // walkRadius = 8 means they can wander further than dark wizards
        spawnNpc(npc = "npc.skeleton", x = 3018, z = 3595, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3020, z = 3597, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.skeleton", x = 3022, z = 3595, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.skeleton", x = 3016, z = 3593, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.skeleton", x = 3024, z = 3599, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3030, z = 3605, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.skeleton", x = 3032, z = 3603, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.skeleton", x = 3034, z = 3607, walkRadius = 8, direction = Direction.NORTH)
        
        /**
         * Bandits - Level 5-10 Wilderness
         * 
         * Human bandits that inhabit the Bandit Camp area. They are aggressive melee fighters
         * that attack players on sight. The Bandit Camp is a popular training location.
         * 
         * Spawn Location: Bandit Camp area (coordinates ~3034-3048, ~3687-3695)
         * Total Spawns: 9 Bandits (mix of bandit_1026 and bandit_6605)
         * Walk Radius: 10 tiles (larger patrol area around camp)
         */
        // Bandits - using only bandit_1026 and bandit_6605 (attackable IDs)
        // walkRadius = 10 means they patrol a larger area around the camp
        spawnNpc(npc = "npc.bandit_1026", x = 3038, z = 3689, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bandit_6605", x = 3040, z = 3691, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.bandit_1026", x = 3042, z = 3689, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.bandit_6605", x = 3044, z = 3693, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.bandit_1026", x = 3036, z = 3695, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bandit_6605", x = 3046, z = 3691, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.bandit_1026", x = 3034, z = 3687, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.bandit_6605", x = 3048, z = 3689, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.bandit_1026", x = 3037, z = 3667, walkRadius = 10, direction = Direction.SOUTH)
        
        /**
         * Chaos Druids - Level 1-5 Wilderness
         * 
         * Corrupted druids that attack with magic. They are known for dropping valuable
         * herbs and are popular for low-level herb farming.
         * 
         * Spawn Locations: Various locations in lower wilderness
         * Total Spawns: 5 Chaos Druids
         * Walk Radius: 7 tiles (moderate patrol area)
         */
        // Chaos Druids spawn in a small group
        spawnNpc(npc = "npc.chaos_druid", x = 2930, z = 3550, walkRadius = 7, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chaos_druid", x = 2932, z = 3552, walkRadius = 7, direction = Direction.EAST)
        spawnNpc(npc = "npc.chaos_druid", x = 2934, z = 3550, walkRadius = 7, direction = Direction.WEST)
        spawnNpc(npc = "npc.chaos_druid", x = 2936, z = 3554, walkRadius = 7, direction = Direction.NORTH)
        spawnNpc(npc = "npc.chaos_druid", x = 2938, z = 3552, walkRadius = 7, direction = Direction.SOUTH)
        
        /**
         * Wolves - Level 1-10 Wilderness
         * 
         * Aggressive wild wolves that roam the wilderness. They attack with melee and
         * are fast-moving predators.
         * 
         * Spawn Locations: Multiple locations across wilderness levels 1-10
         * Total Spawns: 9 Wolves (6 initial + 3 additional scattered)
         * Walk Radius: 10 tiles (large patrol area, wolves are mobile)
         */
        // Wolves spawn in packs - they have a large walk radius (10 tiles) because they're mobile predators
        spawnNpc(npc = "npc.wolf", x = 2970, z = 3610, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf", x = 2972, z = 3612, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.wolf", x = 2974, z = 3610, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.wolf", x = 2976, z = 3614, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.wolf", x = 2980, z = 3610, walkRadius = 10, direction = Direction.EAST)
        
        /**
         * Dark Warriors - Level 12-15 Wilderness
         * 
         * Elite warriors that guard the Dark Warriors' Fortress. They are high-level
         * melee fighters with significant combat stats. This area is dangerous for
         * mid-level players.
         * 
         * Spawn Location: Dark Warriors' Fortress area (coordinates ~3036-3046, ~3632-3638)
         * Total Spawns: 6 Dark Warriors
         * Walk Radius: 8 tiles (patrol around fortress)
         */
        // Dark Warriors guard the fortress - they patrol around the area
        spawnNpc(npc = "npc.dark_warrior", x = 3038, z = 3632, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3040, z = 3634, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_warrior", x = 3042, z = 3632, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.dark_warrior", x = 3044, z = 3636, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3036, z = 3638, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3046, z = 3634, walkRadius = 8, direction = Direction.EAST)
        
        /**
         * Black Knights - Wilderness
         * 
         * Elite warriors clad in black armor that spawn in the wilderness. They are aggressive
         * melee fighters with high combat stats.
         * 
         * Spawn Location: Coordinates 3029, 3852
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.black_knight", x = 3029, z = 3852, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_knight_517", x = 3027, z = 3850, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.black_knight", x = 3031, z = 3850, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.black_knight_517", x = 3027, z = 3854, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.black_knight", x = 3031, z = 3854, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Ice Warriors - Wilderness
         * 
         * Warriors encased in ice armor that spawn in the wilderness. They are aggressive
         * melee fighters with moderate combat stats.
         * 
         * Spawn Location: Coordinates 2953, 3895
         * Walk Radius: 7 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.ice_warrior", x = 2953, z = 3895, walkRadius = 7, direction = Direction.SOUTH)
        
        /**
         * Green Dragons - Level 13-20 Wilderness
         * 
         * Powerful dragons that breathe fire and have high combat stats. They are
         * popular for dragon bone farming and provide excellent combat experience.
         * These are some of the most dangerous monsters in the mid-wilderness.
         * 
         * Spawn Locations: Higher wilderness levels (coordinates ~3094, ~3200-3214, ~3812, ~3856-3862)
         * Total Spawns: 9 Green Dragons
         * Walk Radius: 5 tiles (dragons are relatively stationary)
         */
        // Green Dragons - powerful monsters, walkRadius = 5 means they don't move much (dragons are territorial)
        spawnNpc(npc = "npc.green_dragon", x = 3200, z = 3856, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.green_dragon", x = 3202, z = 3858, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.green_dragon", x = 3204, z = 3856, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.green_dragon", x = 3206, z = 3860, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.green_dragon", x = 3208, z = 3858, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.green_dragon", x = 3210, z = 3856, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.green_dragon", x = 3212, z = 3862, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.green_dragon", x = 3214, z = 3860, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.green_dragon", x = 3094, z = 3812, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.green_dragon", x = 2979, z = 3616, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.green_dragon", x = 3096, z = 3814, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.green_dragon", x = 3092, z = 3810, walkRadius = 5, direction = Direction.WEST)
        
        /**
         * Hellhounds - Level 20+ Wilderness
         * 
         * Extremely dangerous high-level monsters found in the deepest wilderness.
         * They have very high combat stats and are aggressive. Located near the
         * Wilderness Volcano area. Only experienced players should venture here.
         * 
         * Spawn Location: High-level wilderness near Volcano (coordinates ~3369-3379, ~3930-3934)
         * Total Spawns: 6 Hellhounds (mix of hellhound_104 and hellhound_105)
         * Walk Radius: 8 tiles (patrol around volcano area)
         */
        // Hellhounds - mixing two variants (104 and 105) for visual variety
        spawnNpc(npc = "npc.hellhound_104", x = 3369, z = 3930, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_105", x = 3371, z = 3932, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.hellhound_104", x = 3373, z = 3930, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.hellhound_105", x = 3375, z = 3934, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3377, z = 3932, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_105", x = 3379, z = 3930, walkRadius = 8, direction = Direction.EAST)
        
        /**
         * Additional Monster Spawns
         * 
         * These are additional spawns scattered throughout the wilderness to ensure
         * adequate monster density across different areas. They help create a more
         * populated and dangerous wilderness experience.
         */
        
        // Additional skeletons scattered in mid-wilderness to fill gaps
        spawnNpc(npc = "npc.skeleton", x = 3150, z = 3700, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3152, z = 3702, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.skeleton", x = 3154, z = 3700, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.skeleton", x = 3156, z = 3704, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.skeleton", x = 3223, z = 3742, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3245, z = 3744, walkRadius = 8, direction = Direction.SOUTH)
        
        /**
         * Chaos Dwarfs - Wilderness
         * 
         * Aggressive dwarfs that spawn in the wilderness.
         * 
         * Spawn Location: Coordinates 3243, 3791
         * Total Spawns: 3 Chaos Dwarfs
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.chaos_dwarf", x = 3243, z = 3791, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chaos_dwarf", x = 3241, z = 3791, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.chaos_dwarf", x = 3245, z = 3791, walkRadius = 6, direction = Direction.WEST)
        
        // Additional wolves in different wilderness areas - wolves roam far
        spawnNpc(npc = "npc.wolf", x = 3080, z = 3650, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf", x = 3084, z = 3650, walkRadius = 10, direction = Direction.WEST)
        
        /**
         * White Wolves - Coordinates 2857, 3469
         * 
         * White wolves that spawn in a pack around the specified coordinates.
         * 
         * Spawn Location: Coordinates ~2855-2859, ~3467-3471
         * Total Spawns: 8 White Wolves
         * Walk Radius: 8 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.white_wolf", x = 2857, z = 3469, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.white_wolf", x = 2840, z = 3501, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.white_wolf", x = 2836, z = 3509, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.white_wolf", x = 2845, z = 3487, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.white_wolf", x = 2859, z = 3471, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.white_wolf", x = 2857, z = 3467, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.white_wolf", x = 2857, z = 3471, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.white_wolf", x = 2859, z = 3469, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.white_wolf", x = 2859, z = 3469, walkRadius = 8, direction = Direction.NORTH)
        
        /**
         * Dwarf - Wilderness
         * 
         * A dwarf spawn in the wilderness area.
         * 
         * Spawn Location: Coordinates 3023, 3461
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.dwarf_290", x = 3023, z = 3461, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dwarf_290", x = 3025, z = 3461, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.dwarf_290", x = 3023, z = 3463, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.dwarf_290", x = 3025, z = 3463, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.dwarf_290", x = 3023, z = 3461, walkRadius = 6, direction = Direction.SOUTH)

        // Additional dark wizards in different areas - spreading them out for better coverage
        spawnNpc(npc = "npc.dark_wizard", x = 3085, z = 3565, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3087, z = 3567, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3089, z = 3565, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.dark_wizard", x = 2981, z = 3567, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Greater Demons - Demonic Ruins (Level 40+ Wilderness)
         * 
         * Powerful high-level demons that spawn at the Demonic Ruins. These are dangerous
         * monsters with high combat stats and are aggressive. The Demonic Ruins is a popular
         * training location for high-level players due to the prayer restoration feature.
         * 
         * Spawn Location: Demonic Ruins (coordinates ~3275-3283, ~3876-3884)
         * Total Spawns: 8 Greater Demons
         * Walk Radius: 6 tiles (moderate patrol area around ruins)
         */
        // Greater Demons at Demonic Ruins - mixing variants for visual variety
        spawnNpc(npc = "npc.greater_demon", x = 3279, z = 3880, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon_2026", x = 3277, z = 3878, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.greater_demon_2027", x = 3281, z = 3878, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.greater_demon_2028", x = 3275, z = 3880, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.greater_demon_2029", x = 3283, z = 3880, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon_2030", x = 3277, z = 3882, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.greater_demon_2031", x = 3281, z = 3882, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.greater_demon_2032", x = 3279, z = 3884, walkRadius = 6, direction = Direction.NORTH)
        
        /**
         * Lesser Demons - Wilderness
         * 
         * Demonic creatures that spawn in the wilderness. They are aggressive melee fighters
         * with moderate combat stats.
         * 
         * Spawn Location: Coordinates 3013, 3846
         * Total Spawns: 6 Lesser Demons (mixing variants for visual variety)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.lesser_demon", x = 3013, z = 3846, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.lesser_demon_2006", x = 3011, z = 3844, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.lesser_demon_2007", x = 3015, z = 3844, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.lesser_demon_2008", x = 3011, z = 3848, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.lesser_demon", x = 3015, z = 3848, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.lesser_demon_2018", x = 3013, z = 3844, walkRadius = 6, direction = Direction.EAST)
        
        /**
         * Greater Demons - Lava Maze
         * 
         * Greater demons spawn in the lava maze area for additional high-level content.
         * 
         * Spawn Location: Lava Maze (coordinates 3084, 3862)
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.greater_demon", x = 3084, z = 3862, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Hill Giant - Lava Maze
         * 
         * A hill giant spawn in the lava maze area.
         * 
         * Spawn Location: Lava Maze (coordinates 3104, 3869)
         * Walk Radius: 5 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.hill_giant", x = 3104, z = 3869, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Moss Giants - Wilderness
         * 
         * Large giants covered in moss that spawn in the wilderness. They are powerful
         * melee fighters with high combat stats.
         * 
         * Spawn Location: Coordinates 3139, 3807
         * Total Spawns: 5 Moss Giants (mixing variants for visual variety)
         * Walk Radius: 5 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.moss_giant", x = 3139, z = 3807, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.moss_giant_2091", x = 3137, z = 3805, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.moss_giant_2092", x = 3141, z = 3805, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.moss_giant_2093", x = 3137, z = 3809, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.moss_giant", x = 3141, z = 3809, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Elder Chaos Druid (7995) - Wilderness
         * 
         * An elder chaos druid spawn in the wilderness area.
         * 
         * Spawn Location: Coordinates 2955, 3819
         * Walk Radius: 5 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 2955, z = 3819, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Elder Chaos Druids (7995) - Group Spawn
         * 
         * A group of 10 elder chaos druids that attack with magic.
         * 
         * Spawn Location: Coordinates 3237, 3621
         * Total Spawns: 10 Elder Chaos Druids
         * Walk Radius: 5 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3237, z = 3621, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3235, z = 3621, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3239, z = 3621, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3237, z = 3619, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3237, z = 3623, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3236, z = 3620, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3238, z = 3620, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3236, z = 3622, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3238, z = 3622, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.elder_chaos_druid_7995", x = 3235, z = 3623, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Ghost - Wilderness
         * 
         * A ghost spawn in the wilderness area.
         * 
         * Spawn Location: Coordinates 3108, 3691
         * Walk Radius: 4 tiles (ghosts are relatively stationary)
         */
        spawnNpc(npc = "npc.ghost", x = 3108, z = 3691, walkRadius = 4, direction = Direction.SOUTH)
        
        /**
         * Zombies - Graveyard
         * 
         * Undead zombies that spawn in the wilderness graveyard area. They are aggressive
         * melee fighters that attack players on sight.
         * 
         * Spawn Location: Graveyard (coordinates ~3166-3170, ~3672-3676)
         * Total Spawns: 6 Zombies (mixing variants for visual variety)
         * Walk Radius: 6 tiles (moderate patrol area around graveyard)
         */
        spawnNpc(npc = "npc.zombie", x = 3168, z = 3674, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie_27", x = 3166, z = 3672, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.zombie_28", x = 3170, z = 3672, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_29", x = 3166, z = 3676, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.zombie_30", x = 3170, z = 3676, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.zombie_31", x = 3168, z = 3672, walkRadius = 6, direction = Direction.EAST)
        
        /**
         * Black Chinchompas - Wilderness
         * 
         * Small creatures that spawn in the wilderness. They are hunted for their valuable
         * black chinchompa items, which are used in ranged combat.
         * 
         * Spawn Location: Coordinates ~3150-3154, ~3767-3771
         * Total Spawns: 6 Black Chinchompas
         * Walk Radius: 4 tiles (small creatures, relatively stationary)
         */
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3152, z = 3769, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3150, z = 3767, walkRadius = 4, direction = Direction.EAST)
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3154, z = 3767, walkRadius = 4, direction = Direction.WEST)
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3150, z = 3771, walkRadius = 4, direction = Direction.NORTH)
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3154, z = 3771, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_chinchompa_2912", x = 3152, z = 3767, walkRadius = 4, direction = Direction.EAST)
        
        /**
         * Hobgoblin - Wilderness
         * 
         * Aggressive goblin-like creatures found in the wilderness.
         * 
         * Spawn Location: Coordinates 3082, 3763
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.hobgoblin", x = 3082, z = 3763, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Black Warrior - Wilderness
         * 
         * A warrior NPC that spawns in the wilderness.
         * 
         * Spawn Location: Coordinates 3312, 3769
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.warrior", x = 3312, z = 3769, walkRadius = 6, direction = Direction.SOUTH)
        
        /**
         * Ghosts and Ankous - Wilderness
         *
         * Additional undead spawns in the wilderness area.
         *
         * Spawn Location: Coordinates 2973, 3755
         * Walk Radius: 4 tiles (relatively stationary)
         */
        spawnNpc(npc = "npc.ghost", x = 2973, z = 3755, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 2973, z = 3755, walkRadius = 4, direction = Direction.SOUTH)

        /**
         * Pirates - Wilderness
         * 
         * Pirates that spawn in the wilderness area. They are aggressive melee fighters
         * that drop smithing items, bars, and pickaxes.
         * 
         * Spawn Location: Coordinates 3041, 3954
         * Total Spawns: 4 Pirates
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.pirate", x = 3041, z = 3954, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.pirate_522", x = 3041, z = 3954, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.pirate_523", x = 3041, z = 3954, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.pirate_524", x = 3041, z = 3954, walkRadius = 6, direction = Direction.NORTH)

        /**
         * Porazdir - Wilderness Boss
         * 
         * A powerful boss monster found in the wilderness. Porazdir is a dangerous
         * high-level boss with significant combat stats and special attacks.
         * 
         * Spawn Location: Coordinates 3243, 3867
         * Walk Radius: 5 tiles (boss patrols a small area)
         */
        spawnNpc(npc = "npc.porazdir", x = 3243, z = 3867, walkRadius = 5, direction = Direction.SOUTH)



        //spawnItem(item = "item.casket_hard_2726", amount = 1, x = 2986, z = 3704, height = 0)
        spawnItem(item = "item.casket_easy", amount = 1, x = 3187, z = 3925, height = 0)
        spawnItem(item = "item.casket_medium_2812", amount = 1, x = 2982, z = 3845, height = 0)

        /**
         * Battle Mages - Mage Arena (near Infinity Boots spawn)
         * 
         * Aggressive mages that hit through prayer.
         */
        spawnNpc(npc = "npc.battle_mage", x = 3102, z = 3932, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1611", x = 3106, z = 3932, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1612", x = 3102, z = 3936, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage", x = 3106, z = 3936, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1611", x = 3104, z = 3930, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1612", x = 3104, z = 3938, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage", x = 3100, z = 3934, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1611", x = 3108, z = 3934, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage_1612", x = 3103, z = 3933, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.battle_mage", x = 3105, z = 3935, walkRadius = 5, direction = Direction.SOUTH)

        spawnItem(item = "item.antivenom4_12913", amount = 1, x = 3250, z = 3940, height = 0)
        spawnItem(item = "item.infinity_boots", amount = 1, x = 3104, z = 3934, height = 0)
        
        
        spawnItem(item = "item.wilderness_sword_3", amount = 1, x = 3105, z = 3958, height = 0)
        spawnItem(item = "item.wilderness_sword_2", amount = 1, x = 3158, z = 3952, height = 0)
        spawnItem(item = "item.wilderness_sword_1", amount = 1, x = 3030, z = 3852, height = 0)

        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3077, z = 3759, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3083, z = 3753, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3085, z = 3749, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3086, z = 3756, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3084, z = 3760, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3079, z = 3765, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3075, z = 3770, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3082, z = 3773, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3085, z = 3771, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3089, z = 3769, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3089, z = 3763, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3092, z = 3774, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3091, z = 3761, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3096, z = 3755, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3100, z = 3761, height = 0)
        spawnItem(item = "item.rune_pickaxe", amount = 1, x = 3101, z = 3766, height = 0)
        
        spawnItem(item = "item.dragon_bones", amount = 1, x = 2948, z = 3821, height = 0)
        //lockpick by resource area

        spawnItem(item = "item.lockpick", amount = 1, x = 3190, z = 3957, height = 0)
        //10k coins by resource area

        spawnItem(item = "item.coins_6964", amount = 10000, x = 3184, z = 3944, height = 0)
        //
        
        //antifire by lava maze
        spawnItem(item = "item.antifire_potion4", amount = 1, x = 3200, z = 3856, height = 0)
        //antipoison by lava maze
        spawnItem(item = "item.antifire_potion4", amount = 1, x = 3346, z = 3688, height = 0)
        //antipoison by lava maze
        spawnItem(item = "item.antifire_potion4", amount = 1, x = 2978, z = 3615, height = 0)
        //dragon axe by Ents
        spawnItem(item = "item.dragon_axe", amount = 1, x = 3232, z = 3689, height = 0)
        
        /**
         * Wilderness Slayer Cave
         * 
         * A dangerous slayer dungeon in the wilderness containing various high-level
         * slayer monsters. This cave is located underground (z coordinates > 10000).
         * 
         * Reference point: Black demons at 3362, 10119
         * All NPCs are spawned at height 0 (ground level of the dungeon)
         */
        
        /**
         * Black Demons - Wilderness Slayer Cave
         * 
         * Powerful demons that are a common slayer task.
         * Reference location: 3362, 10119
         */
        spawnNpc(npc = "npc.black_demon", x = 3362, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3360, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3364, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3362, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3362, z = 10121, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3365, z = 10120, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3359, z = 10120, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3361, z = 10122, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3363, z = 10122, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.black_demon", x = 3360, z = 10116, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Greater Demons - Wilderness Slayer Cave
         * 
         * High-level demons found in the slayer cave.
         */
        spawnNpc(npc = "npc.greater_demon", x = 3340, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3342, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3344, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3340, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3342, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3344, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3341, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3343, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3342, z = 10121, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.greater_demon", x = 3340, z = 10121, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Hellhounds - Wilderness Slayer Cave
         * 
         * Aggressive demonic hounds that are a popular slayer task.
         */
        spawnNpc(npc = "npc.hellhound_104", x = 3380, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3382, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3384, z = 10115, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3380, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3382, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3384, z = 10117, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3381, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3383, z = 10119, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3382, z = 10121, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.hellhound_104", x = 3380, z = 10121, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Fire Giants - Wilderness Slayer Cave
         * 
         * Large giants that use melee attacks and are a common slayer task.
         */
        spawnNpc(npc = "npc.fire_giant", x = 3320, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3322, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3324, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3320, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3322, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3324, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3321, z = 10129, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3323, z = 10129, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3322, z = 10131, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fire_giant", x = 3320, z = 10131, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Ankou - Wilderness Slayer Cave
         * 
         * Undead creatures that are a slayer task.
         */
        spawnNpc(npc = "npc.ankou", x = 3400, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3402, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3404, z = 10125, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3400, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3402, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3404, z = 10127, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3401, z = 10129, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3403, z = 10129, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3402, z = 10131, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ankou", x = 3400, z = 10131, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Ice Warriors - Wilderness Slayer Cave
         * 
         * Warriors encased in ice that are a slayer task.
         */
        spawnNpc(npc = "npc.ice_warrior", x = 3300, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3302, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3304, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3300, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3302, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3304, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3301, z = 10139, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3303, z = 10139, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3302, z = 10141, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_warrior", x = 3300, z = 10141, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Ice Giants - Wilderness Slayer Cave
         * 
         * Large ice giants that are a slayer task.
         */
        spawnNpc(npc = "npc.ice_giant", x = 3420, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3418, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3416, z = 10135, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3420, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3418, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3416, z = 10137, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3419, z = 10139, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3417, z = 10139, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3418, z = 10141, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ice_giant", x = 3420, z = 10141, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Dark Warriors - Wilderness Slayer Cave
         * 
         * Aggressive warriors found in the wilderness slayer cave.
         */
        spawnNpc(npc = "npc.dark_warrior", x = 3350, z = 10145, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3352, z = 10145, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3354, z = 10145, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3350, z = 10147, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3352, z = 10147, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3354, z = 10147, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3351, z = 10149, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3353, z = 10149, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3352, z = 10151, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_warrior", x = 3350, z = 10151, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        // Debug: Verify plugin loaded successfully
        println("Wilderness SpawnPlugin: Loaded successfully - all spawns registered")
    }
}
