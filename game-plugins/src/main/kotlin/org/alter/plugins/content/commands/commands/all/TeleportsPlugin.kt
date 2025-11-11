package org.alter.plugins.content.commands.commands.all

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.priv.Privilege
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

class TeleportsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        /**
         * Home teleport button handler (interface 218, component 7).
         * Left-click (option 1 - "Cast"): Instantly teleports player to home location.
         * Right-click option 2 ("Animation"): Opens teleport menu.
         * 
         * NOTE: The option names "Cast" and "Animation" are defined in the client cache
         * and cannot be changed dynamically. We're using option 2 ("Animation") to trigger
         * the teleport menu.
         * 
         * TO DISABLE TELEPORT MENU: Comment out the entire "if (option == 2)" block below
         */
        onButton(interfaceId = 218, component = 7) {
            val option = player.getInteractingOption()
            
            // Right-click option 2 ("Animation" in menu) - Teleport Menu
            // TO DISABLE: Comment out this entire if block
            if (option == 2) {
                if (!player.lock.canTeleport()) {
                    return@onButton
                }

                // Define teleport locations for the menu
                val teleportLocations = listOf(
                    "Home" to world.gameContext.home,
                    "Varrock" to Tile(x = 3211, z = 3424, height = 0),
                    "Lumbridge" to Tile(x = 3222, z = 3217, height = 0),
                    "Falador" to Tile(x = 2966, z = 3379, height = 0),
                    "Edgeville" to Tile(x = 3087, z = 3499, height = 0),
                    "Yanille" to Tile(x = 2606, z = 3093, height = 0),
                    "Gnome Stronghold" to Tile(x = 2461, z = 3443, height = 0),
                    "Camelot" to Tile(x = 2756, z = 3476, height = 0),
                    "Ardougne" to Tile(x = 2659, z = 3300, height = 0),
                )

                player.queue(TaskPriority.STRONG) {
                    val locationNames = teleportLocations.map { it.first }.toTypedArray()
                    val selected = options(player, *locationNames, title = "Select Teleport Destination")
                    
                    if (selected > 0 && selected <= teleportLocations.size) {
                        val destination = teleportLocations[selected - 1].second
                        player.prepareForTeleport()
                        player.moveTo(destination)
                    }
                }
                return@onButton
            }

            // Left-click option (option 1) - Instant home teleport
            if (!player.lock.canTeleport()) {
                return@onButton
            }

            val home = world.gameContext.home
            player.prepareForTeleport()
            player.moveTo(home)
        }

        onCommand("home", description = "Teleports you home") {
            val home = world.gameContext.home
            player.moveTo(home)
        }
        onCommand("edge", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Edgeville") {
            player.moveTo(Tile(x = 3087, z = 3499, height = 0))
        }

        onCommand("varrock", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Varrock") {
            player.moveTo(Tile(x = 3211, z = 3424, height = 0))
        }
        onCommand("falador", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Falador") {
            player.moveTo(Tile(x = 2966, z = 3379, height = 0))
        }
        onCommand("lumbridge", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Lumbridge") {
            player.moveTo(Tile(x = 3222, z = 3217, height = 0))
        }
        onCommand("yanille", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Yanille") {
            player.moveTo(Tile(x = 2606, z = 3093, height = 0))
        }
        onCommand("gnome", Privilege.Companion.ADMIN_POWER, description = "Teleports you to Gnome Stronghold") {
            player.moveTo(Tile(x = 2461, z = 3443, height = 0))
        }
        onCommand("thieving", description = "Teleports you to the test thieving") {
            player.moveTo(Tile(x = 2591, z = 4731, height = 0))
        }
    }
}