package org.alter.plugins.content.minigames.wintertodt

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Wintertodt Door Plugin
 * 
 * This plugin handles the Doors of Dinh (object 29322) which teleports players 
 * into the Wintertodt minigame area.
 * 
 * Door Object: doors_of_dinh (object 29322)
 * Wintertodt Location: 1631, 3981
 */
class WintertodtDoorPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Wintertodt minigame area coordinates
         * This is where players teleport to when entering through the door
         */
        private val WINTERTODT_LOCATION = Tile(x = 1631, z = 3981, height = 0)
    }

    init {
        // Function to teleport player to Wintertodt minigame area
        val enterWintertodt: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You enter the Wintertodt arena...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to Wintertodt minigame area
                player.prepareForTeleport()
                player.moveTo(WINTERTODT_LOCATION)
                player.message("You find yourself in the Wintertodt arena.")
            }
        }

        // Handle door 29322 - check what options it has
        // Common door options: "open", "enter", "use", "operate", "pass-through"
        val doorOptions = listOf("open", "enter", "use", "operate", "pass-through", "go-through")
        
        // Try using RSCM name first for all options
        var rscmWorked = false
        doorOptions.forEach { option ->
            try {
                if (objHasOption("object.doors_of_dinh", option)) {
                    onObjOption(obj = "object.doors_of_dinh", option = option, logic = enterWintertodt)
                    rscmWorked = true
                }
            } catch (e: Exception) {
                // RSCM name might not work, will try direct ID below
            }
        }
        
        // If RSCM name didn't work at all, try direct ID as fallback
        if (!rscmWorked) {
            doorOptions.forEach { option ->
                try {
                    onObjOption(obj = 29322, option = option, logic = enterWintertodt)
                } catch (e: Exception) {
                    // Option doesn't exist for this object, skip it
                }
            }
        }
    }
}


