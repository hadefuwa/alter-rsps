package org.alter.plugins.content.commands.commands.admin

import org.alter.api.ext.getCommandArgs
import org.alter.api.ext.message
import org.alter.api.ext.player
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.priv.Privilege
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class TelePlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

    init {
        onCommand(
                "tele",
                Privilege.ADMIN_POWER,
                description = "Teleport to coordinates or teleport player to you"
        ) {
            val values = player.getCommandArgs()

            // Check if first argument is a number (coordinates) or a player name
            val firstArg = values[0]
            val isNumeric = firstArg.toIntOrNull() != null

            if (isNumeric) {
                // Original behavior: teleport to coordinates
                val x = firstArg.toInt()
                val y = values[1].toInt()
                val height = if (values.size > 2) values[2].toInt() else 0
                player.moveTo(x, y, height)
            } else {
                // New behavior: teleport player to admin's location
                val targetPlayerName = firstArg.replace("_", " ")
                val targetPlayer = world.getPlayerForName(targetPlayerName)

                if (targetPlayer == null) {
                    player.message("Player '$targetPlayerName' not found or is offline.")
                    return@onCommand
                }

                // Teleport target player to admin's location
                targetPlayer.moveTo(player.tile)
                player.message("Teleported ${targetPlayer.username} to your location.")
                targetPlayer.message("You have been teleported to ${player.username}.")
            }
        }
    }
}
