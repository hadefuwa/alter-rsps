package org.alter.plugins.content.areas.varrock.npcs

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

/**
 * Makeover Mage Plugin
 * 
 * Allows players to change their appearance by talking to the Makeover Mage.
 */
class MakeoverMagePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val makeoverMage = "npc.makeover_mage"
    private val APPEARANCE_INTERFACE_ID = 679
    
    private val dialogOptions: List<String> = listOf(
        "Yes, change my appearance please.",
        "No thanks."
    )
    
    init {
        // Handle talk-to option
        onNpcOption(makeoverMage, option = "talk-to") {
            player.queue { dialog(player) }
        }
    }
    
    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Greetings, adventurer! I am the Makeover Mage!")
        chatNpc(player, "I can change your appearance for free! Would you like me to do that?")
        
        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> {
                chatPlayer(player, "Yes, change my appearance please.")
                chatNpc(player, "Excellent! Let me open the appearance customization interface for you.")
                player.openInterface(interfaceId = APPEARANCE_INTERFACE_ID, dest = InterfaceDestination.MAIN_SCREEN, isModal = true)
            }
            2 -> {
                chatPlayer(player, "No thanks.")
                chatNpc(player, "Very well. Come back if you change your mind!")
            }
        }
    }
}


