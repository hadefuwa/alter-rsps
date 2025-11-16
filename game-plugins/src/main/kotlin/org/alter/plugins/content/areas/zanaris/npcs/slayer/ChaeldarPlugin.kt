package org.alter.plugins.content.areas.zanaris.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class ChaeldarPlugin(
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
        // Spawn Chaeldar in Zanaris (Lost City) using working NPC type
        // NOTE: Using placeholder NPC type "npc.goblin_cook_4851" which conflicts with GoblinCookPlugin
        // TODO: Replace with proper Chaeldar NPC type when available
        spawnNpc("npc.goblin_cook_4851", 2445, 4431, 0, 3, Direction.NORTH)

        // COMMENTED OUT: This binding conflicts with GoblinCookPlugin which binds the same NPC type
        // TODO: Uncomment when proper Chaeldar NPC type is used
        // onNpcOption("npc.goblin_cook_4851", option = "talk-to") { player.queue { dialog(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Greetings, mortal. I am Chaeldar.")
        chatNpc(player, "I have knowledge of the most dangerous creatures in this realm.")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignmentCheck(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "The creatures I assign are not to be taken lightly.")
                chatNpc(player, "You'll need at least level 70 combat and completion of the Lost City quest.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Return when you seek a true challenge, mortal.")
            }
        }
    }

    suspend fun QueueTask.getAssignmentCheck(player: Player) {
        val combatLevel = player.combatLevel
        if (combatLevel < 70) {
            chatNpc(player, "You are not yet ready for the creatures I would have you face.")
            chatNpc(player, "Come back when you have at least level 70 combat.")
            chatNpc(player, "Try Vannaka in the Edgeville dungeon for less challenging tasks.")
            return
        }
        getAssignment(player)
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        chatNpc(player, "Your task is to kill 50 blue dragons. You may find them in Taverley Dungeon.")
        chatNpc(player, "Return to me when your task is complete.")
        
        // TODO: Implement proper slayer task system
        player.message("You have been assigned to kill 50 blue dragons.")
    }
}