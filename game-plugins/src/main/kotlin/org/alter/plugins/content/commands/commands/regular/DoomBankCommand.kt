package org.alter.plugins.content.commands.commands.regular

import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.plugins.content.mechanics.doompoints.DoomPoints
import org.alter.api.ext.*
import org.alter.api.dsl.*
import org.alter.plugins.content.interfaces.bank.openBank

class DoomBankCommand(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onCommand("bank") {
            if (player.attr[DoomPoints.REMOTE_BANK_UNLOCK] == true) {
                player.openBank()
            } else {
                player.message("You need to unlock the Remote Banking perk to use this command.")
            }
        }
    }
}
