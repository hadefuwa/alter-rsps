package org.alter.plugins.content.objects.gates

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
 * KBD Gate Plugin
 * 
 * This plugin handles gate 1727 which teleports players to the King Black Dragon (KBD) lair.
 * 
 * Gate Object: gate_1727 (object 1727)
 * KBD Location: 2274, 4698 (from KbdConfigsPlugin)
 */
class KbdGatePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * KBD lair entrance coordinates
         * This is where players teleport to when passing through the gate
         */
        private val KBD_LAIR_LOCATION = Tile(x = 2274, z = 4698, height = 0)
    }

    init {
        // KBD gate teleport behaviour has been disabled so that
        // object 1727 behaves purely as a normal open/close gate.
        // If KBD teleporting via a gate is desired in future,
        // bind it to a different, KBD-specific gate object ID.
    }
}

