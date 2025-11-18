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
 * Venenatis Cave Plugin
 *
 * Handles the Venenatis cave entrance (object 47077) which teleports
 * players to Venenatis' lair in the wilderness.
 *
 * Cave Entrance Object: cave_entrance_47077 (object 47077)
 * Venenatis Lair Location: 3319, 3754 (from VenenatisConfigsPlugin)
 */
class VenenatisCavePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Venenatis' lair coordinates (spawn location from VenenatisConfigsPlugin)
         */
        private val VENENATIS_LAIR_LOCATION = Tile(x = 3319, z = 3754, height = 0)
    }

    init {
        // Function to teleport player to Venenatis' lair
        val enterVenenatisLair: Plugin.() -> Unit = {
            player.queue {
                if (!player.lock.canTeleport()) {
                    player.message("You cannot enter the cave right now.")
                    return@queue
                }

                player.message("You squeeze through the web-covered cave entrance...")
                wait(1)

                player.prepareForTeleport()
                player.moveTo(VENENATIS_LAIR_LOCATION)
                player.message("You find yourself in Venenatis' lair.")
            }
        }

        // Common cave entrance options
        val teleportOptions = listOf("enter", "go-through", "use", "operate", "climb-down", "go-down", "pass-through")

        // Try using RSCM name first
        var rscmWorked = false
        teleportOptions.forEach { option ->
            try {
                if (objHasOption("object.cave_entrance_47077", option)) {
                    onObjOption(obj = "object.cave_entrance_47077", option = option, logic = enterVenenatisLair)
                    rscmWorked = true
                }
            } catch (e: Exception) {
                // Ignore, fall back to raw id
            }
        }

        // Fallback: bind directly by id if RSCM name isn't available
        if (!rscmWorked) {
            teleportOptions.forEach { option ->
                try {
                    onObjOption(obj = 47077, option = option, logic = enterVenenatisLair)
                } catch (e: Exception) {
                    // Option not present for this object, skip
                }
            }
        }
    }
}
