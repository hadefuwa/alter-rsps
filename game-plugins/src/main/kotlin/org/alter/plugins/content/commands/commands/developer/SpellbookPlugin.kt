package org.alter.plugins.content.commands.commands.developer

import org.alter.api.Spellbook
import org.alter.api.ext.getCommandArgs
import org.alter.api.ext.message
import org.alter.api.ext.player
import org.alter.api.ext.setSpellbook
import org.alter.api.ext.setVarbit
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.priv.Privilege
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class SpellbookPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        onCommand("spellbook", Privilege.DEV_POWER, description = "Switch between spellbooks") {
            val args = player.getCommandArgs()
            val id = args[0].toInt()
            if (id > 3) {
                player.message("SpellBook does not exist.")
                return@onCommand
            }
            val spellbook = Spellbook.values.firstOrNull { it.id == id } ?: Spellbook.NORMAL
            player.setSpellbook(spellbook)
            player.message("Spellbook changed to: ${spellbook.name}")
        }
        
        // Quick spellbook swap commands
        onCommand("s1", description = "Switch to Standard Spellbook") {
            player.setSpellbook(Spellbook.NORMAL)
            player.message("Spellbook changed to: Standard Spellbook")
        }
        
        onCommand("s2", description = "Switch to Ancient Magicks") {
            player.setSpellbook(Spellbook.ANCIENTS)
            player.message("Spellbook changed to: Ancient Magicks")
        }
        
        onCommand("s3", description = "Switch to Lunar Spellbook") {
            player.setSpellbook(Spellbook.LUNAR)
            player.message("Spellbook changed to: Lunar Spellbook")
        }
        
        onCommand("s4", description = "Switch to Arceuus Spellbook") {
            player.setSpellbook(Spellbook.ARCEUUS)
            player.message("Spellbook changed to: Arceuus Spellbook")
        }
    }
}
