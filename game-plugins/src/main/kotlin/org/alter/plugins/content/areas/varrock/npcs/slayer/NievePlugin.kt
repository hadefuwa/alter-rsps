package org.alter.plugins.content.areas.varrock.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class NievePlugin(
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
        onNpcOption("npc.nieve_6797", option = "talk-to") { player.queue { dialog(player) } }
        onNpcOption("npc.nieve_6797", option = "assignment") { player.queue { getAssignmentCheck(player) } }
        
        // Trade option - opens a general slayer equipment shop
        try {
            onNpcOption("npc.nieve_6797", option = 3) { 
                player.openShop("Slayer Equipment Shop")
            }
        } catch (e: IllegalStateException) {
            // Option 3 already bound, try string option
            try {
                onNpcOption("npc.nieve_6797", option = "trade") { 
                    player.openShop("Slayer Equipment Shop")
                }
            } catch (e2: Exception) {
                // Both options already bound, skip
            }
        }
        
        // Rewards option - opens slayer rewards shop
        try {
            onNpcOption("npc.nieve_6797", option = 4) { 
                player.openShop("Slayer Rewards Shop")
            }
        } catch (e: IllegalStateException) {
            // Option 4 already bound, try string option
            try {
                onNpcOption("npc.nieve_6797", option = "rewards") { 
                    player.openShop("Slayer Rewards Shop")
                }
            } catch (e2: Exception) {
                // Both options already bound, skip
            }
        }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Hello, ${player.username}. I am Nieve, a Slayer master.")
        chatNpc(player, "I can assign you challenging slayer tasks. Would you like an assignment?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignmentCheck(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "I assign tasks to experienced slayers.")
                chatNpc(player, "You'll need at least level 85 combat to receive tasks from me.")
                chatNpc(player, "My tasks are more challenging but offer better rewards.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Come back when you're ready for a challenge!")
            }
        }
    }

    suspend fun QueueTask.getAssignmentCheck(player: Player) {
        val combatLevel = player.combatLevel
        if (combatLevel < 85) {
            chatNpc(player, "You're not experienced enough for my assignments yet.")
            chatNpc(player, "Come back when you have at least level 85 combat.")
            return
        }
        getAssignment(player)
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        if (player.attr.has(org.alter.plugins.content.skills.slayer.Slayer.SLAYER_TASK_ATTR)) {
             chatNpc(player, "You already have a task. You need to finish it first.")
             return
        }
        org.alter.plugins.content.skills.slayer.Slayer.assign(player, org.alter.plugins.content.skills.slayer.SlayerMaster.NIEVE)
    }
}

