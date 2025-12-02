package org.alter.plugins.content.objects.caves

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
 * Warden Entrance Plugin
 * 
 * This plugin handles the entrance (object 46089) which teleports players 
 * to the Warden boss area.
 * 
 * Entrance Object: entry_46089 (object 46089)
 * Warden Boss Location: 3237, 2774
 */
class WardenEntrancePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Warden boss area coordinates
         * This is where players teleport to when entering the entrance
         */
        private val WARDEN_BOSS_LOCATION = Tile(x = 3237, z = 2774, height = 0)
    }

    init {
        // Function to teleport player to Warden boss area
        val enterWardenArea: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You enter the passage...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to Warden boss area
                player.prepareForTeleport()
                player.moveTo(WARDEN_BOSS_LOCATION)
                player.message("You find yourself in the Warden's chamber.")
            }
        }

        // Handle entrance 46089 - check what options it has
        // Common entrance options: "enter", "go-through", "use", "operate", "climb-down", "go-down"
        val teleportOptions = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through")
        
        // Try using RSCM name first for all options
        var rscmWorked = false
        teleportOptions.forEach { option ->
            try {
                if (objHasOption("object.entry_46089", option)) {
                    onObjOption(obj = "object.entry_46089", option = option, logic = enterWardenArea)
                    rscmWorked = true
                }
            } catch (e: Exception) {
                // RSCM name might not work, will try direct ID below
            }
        }
        
        // If RSCM name didn't work at all, try direct ID as fallback
        if (!rscmWorked) {
            teleportOptions.forEach { option ->
                try {
                    onObjOption(obj = 46089, option = option, logic = enterWardenArea)
                } catch (e: Exception) {
                    // Option doesn't exist for this object, skip it
                }
            }
        }
    }
}


