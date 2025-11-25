package org.alter.plugins.content.combat.strategy.magic

import org.alter.api.InterfaceDestination
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.interfaces.attack.AttackTab

class AutocastPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Handle Cancel button on autocast interface (201)
        // Component 0 is typically the Cancel button
        onButton(interfaceId = 201, component = 0) {
            player.closeInterface(interfaceId = 201)
        }
        
        // Handle autocast spell selection on interface 201
        for (componentId in 1..200) {
            onButton(interfaceId = 201, component = componentId) {
                // Find spell with this autoCastId
                // We assume the component ID corresponds to the autoCastId
                val spell = CombatSpell.values.firstOrNull { it.autoCastId == componentId }

                if (spell != null) {
                    player.attr[Combat.CASTING_SPELL] = spell
                    player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, spell.autoCastId)

                    // Refresh weapon component information to update attack tab with autocast selection
                    player.sendWeaponComponentInformation()

                    // Close the autocast interface - the attack tab (593) is already open
                    player.closeInterface(interfaceId = 201)
                } else {
                    // Debug: log if spell not found
                    player.message("Spell not found for component $componentId")
                }
            }
        }
    }
}
