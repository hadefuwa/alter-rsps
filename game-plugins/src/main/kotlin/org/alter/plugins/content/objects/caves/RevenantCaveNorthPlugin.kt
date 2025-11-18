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
 * Revenant Caves - North Entrance Plugin
 *
 * Handles the northern Revenant Caves entrance object (31556) and
 * teleports players into the Revenant Caves.
 *
 * Object: cavern_31556 (id 31556)
 * Destination: Revenant Caves interior (approximate entry tile).
 */
class RevenantCaveNorthPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Approximate entry location inside the Revenant Caves.
         * Adjust if you want a different tile.
         */
        private val REVENANT_CAVES_ENTRY = Tile(x = 3120, z = 3830, height = 0)
    }

    init {
        val enterRevenantCaves: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot enter the cavern right now.")
                    return@queue
                }

                player.message("You step cautiously into the ominous cavern...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(REVENANT_CAVES_ENTRY)
                player.message("You find yourself in the Revenant Caves.")
            }
        }

        val options = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through")

        var rscmWorked = false
        options.forEach { option ->
            try {
                if (objHasOption("object.cavern_31556", option)) {
                    onObjOption(obj = "object.cavern_31556", option = option, logic = enterRevenantCaves)
                    rscmWorked = true
                }
            } catch (_: Exception) {
            }
        }

        if (!rscmWorked) {
            options.forEach { option ->
                try {
                    onObjOption(obj = 31556, option = option, logic = enterRevenantCaves)
                } catch (_: Exception) {
                }
            }
        }
    }
}
