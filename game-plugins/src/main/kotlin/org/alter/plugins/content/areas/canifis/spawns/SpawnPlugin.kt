package org.alter.plugins.content.areas.canifis.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Canifis/Morytania Spawn Plugin
 * 
 * This plugin spawns NPCs in the Canifis and Morytania area.
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Vampyre Juvinates - Coordinates 3563, 3234
         * 
         * Vampyre juvinates that spawn in Morytania.
         * 
         * Spawn Location: Coordinates ~3561-3565, ~3232-3236
         * Total Spawns: 5 Vampyre Juvinates
         * Walk Radius: 6 tiles (moderate patrol area)
         */
        spawnNpc(npc = "npc.vampyre_juvinate", x = 3563, z = 3234, walkRadius = 6, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.vampyre_juvinate", x = 3561, z = 3232, walkRadius = 6, direction = Direction.EAST)
        spawnNpc(npc = "npc.vampyre_juvinate", x = 3565, z = 3232, walkRadius = 6, direction = Direction.WEST)
        spawnNpc(npc = "npc.vampyre_juvinate", x = 3561, z = 3236, walkRadius = 6, direction = Direction.NORTH)
        spawnNpc(npc = "npc.vampyre_juvinate", x = 3565, z = 3236, walkRadius = 6, direction = Direction.SOUTH)
    }
}

