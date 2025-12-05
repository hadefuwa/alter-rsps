package org.alter.plugins.content.bosses.bloodmoon


import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server

class BloodMoonBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn the Blood Moon Boss
        spawnNpc(npc = "npc.blood_moon", x = 1392, z = 9632, height = 0, walkRadius = 5)
    }
}
