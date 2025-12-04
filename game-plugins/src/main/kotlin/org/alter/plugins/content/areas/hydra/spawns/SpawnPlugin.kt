package org.alter.plugins.content.areas.hydra.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Hydra Spawn Plugin
 * 
 * This plugin spawns the Alchemical Hydra boss in its dungeon area.
 * The Alchemical Hydra is a high-level boss that requires skill to defeat.
 * 
 * Coordinates: 1366, 10266 (dungeon area)
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Alchemical Hydra
         * 
         * The Alchemical Hydra spawns at coordinates 1366, 10266 on height level 0.
         * This is a boss monster located in the Hydra dungeon.
         */
        spawnNpc(npc = "npc.alchemical_hydra", x = 1366, z = 10266, height = 0, walkRadius = 0, direction = Direction.SOUTH)
    }
}
