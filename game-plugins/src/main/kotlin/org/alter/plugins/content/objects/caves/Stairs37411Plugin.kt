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
 * Stairs 37411 Plugin
 * 
 * This plugin handles stairs object 37411, which teleports players 
 * back to Jormungands Prison at coordinates (2465, 4010) when interacted with.
 * 
 * Stairs Object: steps_37411 (object 37411)
 * Teleport Destination: (2465, 4010, height = 0) - Jormungands Prison
 */
class Stairs37411Plugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Teleport destination coordinates
         * This is where players teleport to when climbing the stairs
         */
        private val TELEPORT_DESTINATION = Tile(x = 2465, z = 4010, height = 0)
        
        /**
         * Stairs object ID
         */
        private const val STAIRS_ID = 37411
    }

    init {
        // Function to teleport player
        val climbStairs: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You climb the stairs...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to destination
                player.prepareForTeleport()
                player.moveTo(TELEPORT_DESTINATION)
                player.message("You find yourself back at Jormungands Prison.")
            }
        }

        // Handle stairs 37411 - check what options it has
        // Common stairs options: "climb", "climb-up", "climb-down", "use", "operate"
        val stairOptions = listOf("climb", "climb-up", "climb-down", "use", "operate")
        
        // Try using RSCM name first for all options
        var rscmWorked = false
        stairOptions.forEach { option ->
            try {
                if (objHasOption("object.steps_37411", option)) {
                    onObjOption(obj = "object.steps_37411", option = option, logic = climbStairs)
                    rscmWorked = true
                }
            } catch (e: Exception) {
                // RSCM name might not work, will try direct ID below
            }
        }
        
        // If RSCM name didn't work at all, try direct ID as fallback
        if (!rscmWorked) {
            stairOptions.forEach { option ->
                try {
                    onObjOption(obj = STAIRS_ID, option = option, logic = climbStairs)
                } catch (e: Exception) {
                    // Option doesn't exist for this object, skip it
                }
            }
        }
    }
}







