package org.alter.plugins.content.mechanics.sigils

import org.alter.api.ext.hasEquipped
import org.alter.api.ext.message
import org.alter.api.ext.player
import org.alter.game.*
import org.alter.game.model.World
import org.alter.game.plugin.*

/**
 * Handles sigil item interactions
 */
class SigilInteractionPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Sigil of Adroit (29664) - Option 2: Check/Activate
        onItemOption(item = "item.sigil_of_adroit", option = 2) {
            player.message("The Sigil of Adroit increases your accuracy by 1% for each missing hitpoint.")
            player.message("This sigil is currently ${if (player.hasEquipped(intArrayOf(29664))) "active" else "inactive"}.")
        }

        // Sigil of Exaggeration (26056) - Option 4: Destroy
        onItemOption(item = "item.sigil_of_exaggeration", option = 4) {
            player.message("You cannot destroy this sigil while it's in your inventory.")
            player.message("Drop it on the ground if you wish to discard it.")
        }

        // Sigil of the Dwarves (26083) - Option 4: Destroy
        onItemOption(item = "item.sigil_of_the_dwarves", option = 4) {
            player.message("You cannot destroy this sigil while it's in your inventory.")
            player.message("Drop it on the ground if you wish to discard it.")
        }

        // Sigil of Pious Protection (26128) - Option 4: Destroy
        onItemOption(item = "item.sigil_of_pious_protection", option = 4) {
            player.message("You cannot destroy this sigil while it's in your inventory.")
            player.message("Drop it on the ground if you wish to discard it.")
        }

        // Sigil of Versatility (26113) - Option 2: Check/Activate
        onItemOption(item = "item.sigil_of_versatility", option = 2) {
            player.message("The Sigil of Versatility allows you to use any combat style effectively.")
            player.message("This sigil is currently ${if (player.hasEquipped(intArrayOf(26113))) "active" else "inactive"}.")
        }

        // Sigil of Rampart (29655) - Option 2: Check
        onItemOption(item = "item.sigil_of_rampart", option = 2) {
            player.message("The Sigil of Rampart increases your defences by +100.")
            player.message("This sigil is currently ${if (player.hasEquipped(intArrayOf(29655))) "active" else "inactive"}.")
        }

        // Sigil of Titanium (28523) - Option 2: Check
        onItemOption(item = "item.sigil_of_titanium", option = 2) {
            player.message("The Sigil of Titanium reduces damage taken from monsters by 60%.")
            player.message("This sigil is currently ${if (player.hasEquipped(intArrayOf(28523))) "active" else "inactive"}.")
        }
    }
}
