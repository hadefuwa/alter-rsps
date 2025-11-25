package org.alter.plugins.content.objects.caves

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Fight Caves Exit Plugin
 * 
 * This plugin handles the exit from the TzHaar Fight Caves.
 * Object 11834 (cave entrance) teleports players back to TzHaar City.
 */
class FightCavesExitPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    // Exit location - TzHaar City near the fight caves entrance
    private val FIGHT_CAVES_EXIT = Tile(x = 2436, z = 5171, height = 0)

    init {
        // Function for exiting the fight caves
        val exitFightCaves: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot exit the fight caves right now.")
                    return@queue
                }

                player.message("You climb through the cave entrance...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(FIGHT_CAVES_EXIT)
                player.message("You emerge from the Fight Caves.")
            }
        }

        // Handle exit object 11834 - teleports to TzHaar City
        val exitOptions = listOf("enter", "go-through", "use", "operate", "climb", "exit", "leave", "climb-through")
        
        var rscmWorked = false
        exitOptions.forEach { option ->
            try {
                if (objHasOption("object.cave_entrance_11834", option)) {
                    onObjOption(obj = "object.cave_entrance_11834", option = option, logic = exitFightCaves)
                    rscmWorked = true
                }
            } catch (_: Exception) {
            }
        }

        // Fallback to numeric ID if RSCM name doesn't work
        if (!rscmWorked) {
            exitOptions.forEach { option ->
                try {
                    onObjOption(obj = 11834, option = option, logic = exitFightCaves)
                } catch (_: Exception) {
                }
            }
        }
    }
}

