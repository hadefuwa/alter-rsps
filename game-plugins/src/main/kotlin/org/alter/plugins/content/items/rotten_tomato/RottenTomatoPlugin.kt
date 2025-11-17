package org.alter.plugins.content.items.rotten_tomato

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

/**
 * Special Rotten Tomato Plugin for Pnda
 *
 * This item allows the player named "Pnda" to spawn any item by entering its ID.
 * When the rotten tomato is used (right-clicked), it prompts for an item ID
 * and spawns that item in the player's inventory.
 *
 * @author Claude Code
 */
class RottenTomatoPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        /**
         * Handle using the rotten tomato on itself
         * This will be the spawn item functionality for Pnda
         */
        onItemOnItem(item1 = "item.rotten_tomato", item2 = "item.rotten_tomato") {
            // Check if the player is Pnda
            if (player.username.equals("Pnda", ignoreCase = true)) {
                player.queue {
                    // Prompt the player to enter an item ID
                    val itemId = inputInt(player, "Enter item ID to spawn:")

                    if (itemId != null && itemId > 0) {
                        // Try to add the item to the player's inventory
                        val result = player.inventory.add(itemId, 1)

                        if (result.completed > 0) {
                            player.message("Spawned item ID: $itemId")
                        } else {
                            player.message("Failed to spawn item ID: $itemId (inventory full or invalid ID)")
                        }
                    } else {
                        player.message("Invalid item ID entered.")
                    }
                }
            } else {
                // For non-Pnda players, show a funny message
                player.message("The rotten tomatoes squish together disgustingly.")
            }
        }

        /**
         * Give Pnda the rotten tomato when they log in (if they don't have one)
         */
        onLogin {
            if (player.username.equals("Pnda", ignoreCase = true)) {
                // Check if player doesn't already have a rotten tomato
                if (!player.inventory.contains(getRSCM("item.rotten_tomato")) &&
                    !player.bank.contains(getRSCM("item.rotten_tomato"))) {
                    // Add the rotten tomato to their inventory
                    val added = player.inventory.add(getRSCM("item.rotten_tomato"), 1)
                    if (added.completed > 0) {
                        player.message("A magical rotten tomato appears in your inventory!")
                    }
                }
            }
        }
    }
}
