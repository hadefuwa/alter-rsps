package org.alter.plugins.content.bosses.eclipsemoon


import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server

class EclipseMoonBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn the Eclipse Moon Boss
        spawnNpc(npc = "npc.eclipse_moon", x = 1488, z = 9632, height = 0, walkRadius = 5)
    }
}
