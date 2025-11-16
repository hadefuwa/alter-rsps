package org.alter.plugins.content.areas.taverley.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class TuraelPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val dialogOptions: List<String> = listOf(
        "I need another assignment.",
        "Have you any tips for me?",
        "Er, nothing actually.",
    )

    init {
        // Spawn Turael in Taverley
        spawnNpc("npc.turael", 2930, 3536, 0, 3, Direction.SOUTH)

        onNpcOption("npc.turael", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.turael", option = "assignment") { player.queue { getAssignment(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Hello there, ${player.username}. I am Turael, a Slayer master.")
        chatNpc(player, "I can give you tasks to slay certain creatures. Would you like an assignment?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignment(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "I'd suggest you start by killing lower level monsters and work your way up.")
                chatNpc(player, "As you improve I can give you harder monsters to kill.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Come back when you need a new assignment!")
            }
        }
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        // For now, give a simple task - you can expand this with a proper slayer system
        chatNpc(player, "Your task is to kill 15 goblins. You can find them around Lumbridge.")
        chatNpc(player, "Come back to me when you have completed this task!")
        
        // TODO: Implement proper slayer task system
        // This would normally set the player's slayer task, amount, etc.
        player.message("You have been assigned to kill 15 goblins.")
    }
}