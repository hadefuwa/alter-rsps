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
 * Revenant Caves - North Entrance/Exit Plugin
 *
 * Handles the northern Revenant Caves entrance objects (31555 and 31556) and
 * the exit stairs (43868) that teleport players out of the Revenant Caves.
 *
 * Objects:
 * - cavern_31555 (id 31555) - South entrance, teleports to 3197, 10057
 * - cavern_31556 (id 31556) - North entrance, teleports to 3235, 10198 (near revenant dragons)
 * - stairs_43868 (id 43868) - Exit stairs, teleports to north exit (3123, 3805)
 * - stairs_31558 (id 31558) - Exit stairs, teleports to exit (3102, 3656)
 */
class RevenantCaveNorthPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Entry locations inside the Revenant Caves for each entrance.
         * - Object 31555 (South entrance) teleports to 3197, 10057
         * - Object 31556 (North entrance) teleports to 3235, 10198 (near revenant dragons)
         */
        private val REVENANT_CAVES_ENTRY_31555 = Tile(x = 3197, z = 10057, height = 0)
        private val REVENANT_CAVES_ENTRY_31556 = Tile(x = 3235, z = 10198, height = 0)
        
        /**
         * Exit locations on the surface.
         * - Stairs 43868 teleports to north exit (3123, 3805)
         * - Stairs 31558 teleports to exit (3102, 3656)
         */
        private val REVENANT_CAVES_NORTH_EXIT = Tile(x = 3123, z = 3805, height = 0)
        private val REVENANT_CAVES_EXIT_31558 = Tile(x = 3102, z = 3656, height = 0)
    }

    init {
        // Function for entering through object 31555
        val enterRevenantCaves31555: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot enter the cavern right now.")
                    return@queue
                }

                player.message("You step cautiously into the ominous cavern...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(REVENANT_CAVES_ENTRY_31555)
                player.message("You find yourself in the Revenant Caves.")
            }
        }

        // Function for entering through object 31556
        val enterRevenantCaves31556: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot enter the cavern right now.")
                    return@queue
                }

                player.message("You step cautiously into the ominous cavern...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(REVENANT_CAVES_ENTRY_31556)
                player.message("You find yourself in the Revenant Caves.")
            }
        }

        val options = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through")
        
        // Handle object 31555 (South entrance) - teleports to 3197, 10057
        var rscmWorked31555 = false
        options.forEach { option ->
            try {
                if (objHasOption("object.cavern_31555", option)) {
                    onObjOption(obj = "object.cavern_31555", option = option, logic = enterRevenantCaves31555)
                    rscmWorked31555 = true
                }
            } catch (_: Exception) {
            }
        }

        if (!rscmWorked31555) {
            options.forEach { option ->
                try {
                    onObjOption(obj = 31555, option = option, logic = enterRevenantCaves31555)
                } catch (_: Exception) {
                }
            }
        }

        // Handle object 31556 - teleports to 3241, 10233
        var rscmWorked31556 = false
        options.forEach { option ->
            try {
                if (objHasOption("object.cavern_31556", option)) {
                    onObjOption(obj = "object.cavern_31556", option = option, logic = enterRevenantCaves31556)
                    rscmWorked31556 = true
                }
            } catch (_: Exception) {
            }
        }

        if (!rscmWorked31556) {
            options.forEach { option ->
                try {
                    onObjOption(obj = 31556, option = option, logic = enterRevenantCaves31556)
                } catch (_: Exception) {
                }
            }
        }

        // Function for exiting through stairs object 43868
        val exitRevenantCaves: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot exit the cavern right now.")
                    return@queue
                }

                player.message("You climb up the stairs...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(REVENANT_CAVES_NORTH_EXIT)
                player.message("You emerge from the Revenant Caves.")
            }
        }

        // Handle exit stairs object 43868 - teleports to north exit
        val exitOptions = listOf("climb-up", "go-up", "use", "operate", "climb", "exit", "leave")
        
        var rscmWorked43868 = false
        exitOptions.forEach { option ->
            try {
                if (objHasOption("object.stairs_43868", option)) {
                    onObjOption(obj = "object.stairs_43868", option = option, logic = exitRevenantCaves)
                    rscmWorked43868 = true
                }
            } catch (_: Exception) {
            }
        }

        if (!rscmWorked43868) {
            exitOptions.forEach { option ->
                try {
                    onObjOption(obj = 43868, option = option, logic = exitRevenantCaves)
                } catch (_: Exception) {
                }
            }
        }

        // Function for exiting through stairs object 31558
        val exitRevenantCaves31558: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot exit the cavern right now.")
                    return@queue
                }

                player.message("You climb up the stairs...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(REVENANT_CAVES_EXIT_31558)
                player.message("You emerge from the Revenant Caves.")
            }
        }

        // Handle exit stairs object 31558 - teleports to 3102, 3656
        var rscmWorked31558 = false
        exitOptions.forEach { option ->
            try {
                if (objHasOption("object.stairs_31558", option)) {
                    onObjOption(obj = "object.stairs_31558", option = option, logic = exitRevenantCaves31558)
                    rscmWorked31558 = true
                }
            } catch (_: Exception) {
            }
        }

        if (!rscmWorked31558) {
            exitOptions.forEach { option ->
                try {
                    onObjOption(obj = 31558, option = option, logic = exitRevenantCaves31558)
                } catch (_: Exception) {
                }
            }
        }
    }
}
