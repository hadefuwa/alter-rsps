package org.alter.plugins.content.items.consumables.food

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.*
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.asRSCM
import org.alter.rscm.RSCM.getRSCM

class EatingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Bind all foods from the Food enum
        // Note: Using option 2 because the client sends option=2 for "Eat"
        // even though it's at index 0 in interfaceOptions
        Food.values.forEach { food ->
            try {
                onItemOption(food.item, 2) {
                    handleEat(player, food)
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM or items without eat option
            }
        }
    }

    /**
     * Handler for eating any food item
     */
    private fun handleEat(player: Player, food: Food) {
        val inventorySlot = player.getInteractingItemSlot()
        val item = player.inventory[inventorySlot]

        if (item == null) {
            return
        }

        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }

        // Check if player can eat (timer check)
        if (!Foods.canEat(player, food)) {
            return
        }

        // Remove the food from inventory
        val itemId = getRSCM(food.item)
        val removeResult = player.inventory.remove(item = itemId, beginSlot = inventorySlot, assureFullRemoval = false)
        if (removeResult.hasSucceeded() && removeResult.completed > 0) {
            Foods.eat(player, food)

            // Add replacement item if needed
            if (food.replacement != -1) {
                player.inventory.add(item = food.replacement.asRSCM("item"), beginSlot = inventorySlot)
            }
        }
    }
}
