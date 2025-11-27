package org.alter.plugins.content.areas.varrock.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class KrystilliaPlugin(
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
        onNpcOption("npc.krystilia", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.krystilia", option = "assignment") { player.queue { getAssignment(player) } }
        
        // Trade option - opens a general slayer equipment shop
        onNpcOption("npc.krystilia", option = "trade") { 
            player.openShop("Slayer Equipment Shop")
        }
        
        // Rewards option - opens slayer rewards shop
        onNpcOption("npc.krystilia", option = "rewards") { 
            player.openShop("Slayer Rewards Shop")
        }
    }

    suspend fun QueueTask.dialog(player: Player) {
        val slayerPoints = org.alter.plugins.content.skills.slayer.Slayer.getSlayerPoints(player)
        
        chatNpc(player, "Greetings, ${player.username}. I am Krystillia, a Slayer master.")
        
        // Show current task if player has one
        val currentTaskInfo = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskInfo(player)
        if (currentTaskInfo != null) {
            chatNpc(player, currentTaskInfo)
        }
        
        chatNpc(player, "You currently have $slayerPoints Slayer Point${if (slayerPoints == 1) "" else "s"}.")
        chatNpc(player, "I assign tasks that must be completed in the Wilderness. Are you brave enough?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignment(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "The Wilderness is dangerous, but the rewards can be great.")
                chatNpc(player, "Be prepared for player killers - you can be attacked by other players.")
                chatNpc(player, "I assign tasks to players of any combat level, but beware the risks.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Return when you're ready to face the dangers of the Wilderness!")
            }
        }
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        if (player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)) {
             val taskMessage = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskName(player)
             chatNpc(player, taskMessage ?: "You already have a task. You need to finish it first.")
             return
        }
        org.alter.plugins.content.skills.slayer.Slayer.assign(player, org.alter.plugins.content.skills.slayer.SlayerMaster.KRYSTILLIA)
    }
}

