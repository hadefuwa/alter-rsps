package org.alter.plugins.content.areas.varrock.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Plugin for Ethereal Slayer Masters
 * NPCs: 778 (ethereal_being_778), 779 (ethereal_numerator), 780 (ethereal_expert)
 * These NPCs use option 3 (Assignment) in the right-click menu
 */
class EtherealSlayerMastersPlugin(
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
        // Register option 3 (Assignment) for ethereal NPCs
        onNpcOption("npc.ethereal_being_778", option = 3) { player.queue { getAssignment(player) } }
        onNpcOption("npc.ethereal_numerator", option = 3) { player.queue { getAssignment(player) } }
        onNpcOption("npc.ethereal_expert", option = 3) { player.queue { getAssignment(player) } }
        
        // Also register talk-to option for dialogue
        onNpcOption("npc.ethereal_being_778", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.ethereal_numerator", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.ethereal_expert", option = "talk-to") { player.queue { dialog(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        val slayerPoints = org.alter.plugins.content.skills.slayer.Slayer.getSlayerPoints(player)
        
        chatNpc(player, "Greetings, ${player.username}. I am an Ethereal Slayer master.")
        
        // Show current task if player has one
        val currentTaskInfo = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskInfo(player)
        if (currentTaskInfo != null) {
            chatNpc(player, currentTaskInfo)
        }
        
        chatNpc(player, "You currently have $slayerPoints Slayer Point${if (slayerPoints == 1) "" else "s"}.")
        chatNpc(player, "I can assign you slayer tasks. Would you like an assignment?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignment(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "Slayer tasks help you train your Slayer skill while earning combat experience.")
                chatNpc(player, "Complete tasks to earn slayer points and unlock new abilities.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Return when you're ready for a task!")
            }
        }
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        if (player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)) {
             val taskMessage = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskName(player)
             chatNpc(player, taskMessage ?: "You already have a task. You need to finish it first.")
             return
        }
        
        // Use Krystillia as the default master for ethereal NPCs (no combat requirement)
        val hadTaskBefore = player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)
        org.alter.plugins.content.skills.slayer.Slayer.assign(player, org.alter.plugins.content.skills.slayer.SlayerMaster.KRYSTILLIA)
        
        // Check if assignment was successful
        if (!player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR) && !hadTaskBefore) {
            // Assignment failed - likely no valid tasks for player's slayer level
            chatNpc(player, "I'm sorry, but you don't meet the requirements for any tasks I can assign.")
            chatNpc(player, "You may need to train your Slayer skill first.")
        }
    }
}

