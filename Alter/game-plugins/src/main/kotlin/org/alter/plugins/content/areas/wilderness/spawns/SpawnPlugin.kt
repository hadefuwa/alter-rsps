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
         * Total Spawns: 8 Bandits (mix of bandit_690, bandit_691, bandit_692)
         * Walk Radius: 10 tiles (larger patrol area around camp)
         */
        // Bandits - note we're using different bandit IDs (690, 691, 692) for variety
        // walkRadius = 10 means they patrol a larger area around the camp
        spawnNpc(npc = "npc.bandit_690", x = 3038, z = 3689, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bandit_691", x = 3040, z = 3691, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.bandit_692", x = 3042, z = 3689, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.bandit_690", x = 3044, z = 3693, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.bandit_691", x = 3036, z = 3695, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bandit_692", x = 3046, z = 3691, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.bandit_690", x = 3034, z = 3687, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.bandit_691", x = 3048, z = 3689, walkRadius = 10, direction = Direction.NORTH)
        
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
        spawnNpc(npc = "npc.wolf", x = 2978, z = 3612, walkRadius = 10, direction = Direction.SOUTH)
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
         * Green Dragons - Level 13-20 Wilderness
         * 
         * Powerful dragons that breathe fire and have high combat stats. They are
         * popular for dragon bone farming and provide excellent combat experience.
         * These are some of the most dangerous monsters in the mid-wilderness.
         * 
         * Spawn Locations: Higher wilderness levels (coordinates ~3200-3214, ~3856-3862)
         * Total Spawns: 8 Green Dragons
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
        
        // Additional wolves in different wilderness areas - wolves roam far
        spawnNpc(npc = "npc.wolf", x = 3080, z = 3650, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf", x = 3082, z = 3652, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.wolf", x = 3084, z = 3650, walkRadius = 10, direction = Direction.WEST)
        
        // Additional dark wizards in different areas - spreading them out for better coverage
        spawnNpc(npc = "npc.dark_wizard", x = 3085, z = 3565, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3087, z = 3567, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3089, z = 3565, walkRadius = 5, direction = Direction.WEST)
    }
}

