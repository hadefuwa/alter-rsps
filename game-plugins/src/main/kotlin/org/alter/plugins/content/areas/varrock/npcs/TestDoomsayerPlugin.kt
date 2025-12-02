package org.alter.plugins.content.areas.varrock.npcs

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.info.NpcInfo
import org.alter.game.model.Tile

/**
 * Simple Test Doomsayer Plugin
 */
class TestDoomsayerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Test 1: Try string binding
        onNpcOption("npc.wise_old_man", option = "talk-to") {
            player.message("SUCCESS: String NPC binding worked!")
            player.queue { 
                chatNpc(player, "Hello from string binding!", animation = 567)
            }
        }
        
        // Test command
        onCommand("testdoom") {
            player.message("Test Doomsayer plugin is loaded!")
        }
    }
}