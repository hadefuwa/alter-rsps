package org.alter.plugins.content.areas.shilo.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class DuradelPlugin(
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
        // Spawn Duradel in Shilo Village
        spawnNpc("npc.duradel", 2851, 2914, 0, 3, Direction.WEST)

        onNpcOption("npc.duradel", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.duradel", option = "assignment") { player.queue { getAssignment(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome, ${player.username}. I am Duradel, the most experienced Slayer master.")
        chatNpc(player, "I deal only with the most dangerous creatures in existence.")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignmentCheck(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "The creatures I assign require the utmost preparation.")
                chatNpc(player, "You'll need at least level 100 combat and access to Shilo Village.")
                chatNpc(player, "Bring your best equipment - you'll need it.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Return when you're prepared for the ultimate challenge.")
            }
        }
    }

    suspend fun QueueTask.getAssignmentCheck(player: Player) {
        val combatLevel = player.combatLevel
        if (combatLevel < 100) {
            chatNpc(player, "You are nowhere near ready for my assignments.")
            chatNpc(player, "Come back when you have at least level 100 combat.")
            chatNpc(player, "Try Chaeldar in Zanaris for challenging but more manageable tasks.")
            return
        }
        getAssignment(player)
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        chatNpc(player, "Your task is to kill 75 black dragons. You'll find them in the Evil Chicken's Lair.")
        chatNpc(player, "This is not a task for the unprepared. Good luck.")
        
        // TODO: Implement proper slayer task system
        player.message("You have been assigned to kill 75 black dragons.")
    }
}