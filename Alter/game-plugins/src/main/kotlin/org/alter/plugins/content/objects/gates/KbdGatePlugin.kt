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
        // Function to teleport player to KBD lair
        val enterKbdLair: Plugin.() -> Unit = {
            player.queue {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You pass through the gate...")
                
                // Wait a moment
                wait(1)
                
                // Teleport to KBD lair
                player.prepareForTeleport()
                player.moveTo(KBD_LAIR_LOCATION)
                player.message("You find yourself in the King Black Dragon's lair.")
            }
        }

        // Handle gate 1727 - check what options it has
        // Common gate options for teleportation: "pass-through", "enter", "use", "operate", "go-through"
        // Note: "open" is handled by GatePlugin, so we handle other options for teleportation
        val teleportOptions = listOf("pass-through", "enter", "use", "operate", "go-through", "pass")
        
        teleportOptions.forEach { option ->
            // Try using RSCM name
            try {
                if (objHasOption("object.gate_1727", option)) {
                    onObjOption(obj = "object.gate_1727", option = option, logic = enterKbdLair)
                }
            } catch (e: Exception) {
                // Option might not exist, continue
            }
        }
    }
}

