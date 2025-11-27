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
        // Spawn Turael in Taverley using working NPC type
        // NOTE: Using placeholder NPC type "npc.hans" which conflicts with HansPlugin
        // TODO: Replace with proper Turael NPC type when available
        spawnNpc("npc.hans", 2930, 3536, 0, 3, Direction.SOUTH)

        // COMMENTED OUT: This binding conflicts with HansPlugin which binds the same NPC type
        // TODO: Uncomment when proper Turael NPC type is used
        // onNpcOption("npc.hans", option = "talk-to") { player.queue { dialog(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Hello there, ${player.username}. I am Turael, a Slayer master.")
        
        // Show current task if player has one
        val currentTaskInfo = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskInfo(player)
        if (currentTaskInfo != null) {
            chatNpc(player, currentTaskInfo)
        }
        
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
        if (player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)) {
             val taskMessage = org.alter.plugins.content.skills.slayer.Slayer.getCurrentTaskName(player)
             chatNpc(player, taskMessage ?: "You already have a task. You need to finish it first.")
             return
        }
        // TURAEL has been removed - functionality disabled
        // org.alter.plugins.content.skills.slayer.Slayer.assign(player, org.alter.plugins.content.skills.slayer.SlayerMaster.TURAEL)
        chatNpc(player, "I'm sorry, but I'm no longer available as a slayer master. Please visit Krystillia in Varrock centre.")
        
        // Check if assigned
        if (player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)) {
             // Message is sent in assign
        }
    }
}