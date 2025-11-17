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
 * Callisto Cave Plugin
 * 
 * This plugin handles the Callisto cave entrance (object 47140) which teleports players 
 * to Callisto's Den in the wilderness.
 * 
 * Cave Entrance Object: cave_entrance_47140 (object 47140)
 * Callisto's Den Location: 3307, 3737 (from CallistoConfigsPlugin)
 */
class CallistoCavePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Callisto's Den entrance coordinates
         * This is where players teleport to when entering the cave
         */
        private val CALLISTO_DEN_LOCATION = Tile(x = 3307, z = 3737, height = 0)
    }

    init {
        // Function to teleport player to Callisto's Den
        val enterCallistoDen: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You enter the cave...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to Callisto's Den
                player.prepareForTeleport()
                player.moveTo(CALLISTO_DEN_LOCATION)
                player.message("You find yourself in Callisto's Den.")
            }
        }

        // Handle cave entrance 47140 - check what options it has
        // Common cave entrance options: "enter", "go-through", "use", "operate", "climb-down", "go-down"
        val teleportOptions = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through")
        
        // Try using RSCM name first for all options
        var rscmWorked = false
        teleportOptions.forEach { option ->
            try {
                if (objHasOption("object.cave_entrance_47140", option)) {
                    onObjOption(obj = "object.cave_entrance_47140", option = option, logic = enterCallistoDen)
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
                    onObjOption(obj = 47140, option = option, logic = enterCallistoDen)
                } catch (e: Exception) {
                    // Option doesn't exist for this object, skip it
                }
            }
        }
    }
}

