package org.alter.plugins.content.areas.canifis.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class MazchnaPlugin(
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
        // Spawn Mazchna in Canifis
        spawnNpc("npc.mazchna", 3510, 3506, 0, 3, Direction.WEST)

        onNpcOption("npc.mazchna", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.mazchna", option = "assignment") { player.queue { getAssignment(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Greetings, ${player.username}. I am Mazchna.")
        chatNpc(player, "I can assign you creatures to slay for experience and profit.")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignmentCheck(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "I'd suggest bringing food and armour. Some creatures are quite dangerous.")
                chatNpc(player, "You'll need at least level 20 combat to take assignments from me.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Return when you're ready for a challenge!")
            }
        }
    }

    suspend fun QueueTask.getAssignmentCheck(player: Player) {
        val combatLevel = player.combatLevel
        if (combatLevel < 20) {
            chatNpc(player, "You're not experienced enough for my assignments yet.")
            chatNpc(player, "Come back when you have at least level 20 combat.")
            chatNpc(player, "Try speaking to Turael in Taverley for easier tasks.")
            return
        }
        getAssignment(player)
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        chatNpc(player, "Your task is to kill 25 hill giants. You can find them in the Edgeville dungeon.")
        chatNpc(player, "Come back to me when you have completed this task!")
        
        // TODO: Implement proper slayer task system
        player.message("You have been assigned to kill 25 hill giants.")
    }
}