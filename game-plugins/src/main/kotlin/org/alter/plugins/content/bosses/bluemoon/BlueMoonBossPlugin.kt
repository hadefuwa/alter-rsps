package org.alter.plugins.content.bosses.bluemoon

import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server

class BlueMoonBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn the Blue Moon Boss
        spawnNpc(npc = "npc.blue_moon", x = 1440, z = 9680, height = 0, walkRadius = 5)
    }
}
