package org.alter.plugins.content.areas.corporealbeast.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Corporeal Beast Spawn Plugin
 * 
 * This plugin spawns the Corporeal Beast in its dungeon area.
 * The Corporeal Beast is a high-level boss that requires a team to defeat.
 * 
 * Coordinates are based on the Corporeal Beast quest dungeon area.
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Corporeal Beast
         * 
         * The Corporeal Beast spawns at coordinates 2986, 4386 on height level 2.
         * This is a boss monster that requires multiple players to defeat.
         */
        spawnNpc(npc = "npc.corporeal_beast", x = 2986, z = 4386, height = 2, walkRadius = 0, direction = Direction.SOUTH)
    }
}

