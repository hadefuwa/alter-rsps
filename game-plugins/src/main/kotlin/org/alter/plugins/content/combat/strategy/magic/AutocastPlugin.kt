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
        // Handle autocast spell selection on interface 201
        // Handle autocast spell selection on interface 201
        for (componentId in 0..200) {
            onButton(interfaceId = 201, component = componentId) {
                // Find spell with this autoCastId
                // We assume the component ID corresponds to the autoCastId
                val spell = CombatSpell.values.firstOrNull { it.autoCastId == componentId }

                if (spell != null) {
                    player.attr[Combat.CASTING_SPELL] = spell
                    player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, spell.autoCastId)

                    // Return to the attack tab
                    player.openInterface(interfaceId = 593, dest = InterfaceDestination.ATTACK)
                }
            }
        }
    }
}
