package org.alter.plugins.content.areas.edgeville.npcs.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class VannakaPlugin(
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
        // Spawn Vannaka in Edgeville Dungeon using working NPC type
        // NOTE: Using placeholder NPC type "npc.prayer_tutor" which conflicts with PrayerTutorPlugin
        // TODO: Replace with proper Vannaka NPC type when available
        spawnNpc("npc.prayer_tutor", 3145, 9913, 0, 3, Direction.SOUTH)

        // COMMENTED OUT: This binding conflicts with PrayerTutorPlugin which binds the same NPC type
        // TODO: Uncomment when proper Vannaka NPC type is used
        // onNpcOption("npc.prayer_tutor", option = "talk-to") { player.queue { dialog(player) } }
    }

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "'Ello there, ${player.username}. Looking for something to kill?")
        chatNpc(player, "I'm Vannaka, and I can set you on the trail of some dangerous monsters.")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> getAssignmentCheck(player)
            2 -> {
                chatPlayer(player, "Have you any tips for me?")
                chatNpc(player, "Aye, don't go picking fights with things much stronger than yourself.")
                chatNpc(player, "You'll need at least level 40 combat for my assignments.")
            }
            3 -> {
                chatPlayer(player, "Er, nothing actually.")
                chatNpc(player, "Come back when you're ready to get your hands dirty!")
            }
        }
    }

    suspend fun QueueTask.getAssignmentCheck(player: Player) {
        val combatLevel = player.combatLevel
        if (combatLevel < 40) {
            chatNpc(player, "You're not tough enough for my assignments yet.")
            chatNpc(player, "Come back when you have at least level 40 combat.")
            chatNpc(player, "Try Mazchna in Canifis for easier tasks.")
            return
        }
        getAssignment(player)
    }

    suspend fun QueueTask.getAssignment(player: Player) {
        chatNpc(player, "Your task is to kill 35 lesser demons. You can find some right here in this dungeon.")
        chatNpc(player, "Come back when you've finished them off!")
        
        // TODO: Implement proper slayer task system
        player.message("You have been assigned to kill 35 lesser demons.")
    }
}