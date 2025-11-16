package org.alter.plugins.content.items.consumables.food

import dev.openrune.cache.CacheManager.getItem
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
import org.alter.plugins.content.items.consumables.food.Foods
import org.alter.plugins.content.items.consumables.food.Food
import org.alter.rscm.RSCM.asRSCM
import org.alter.rscm.RSCM.getRSCM

class EatingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        println("EatingPlugin: Starting initialization...")
        
        Food.values.forEach { food ->
            try {
                val foodItemId = getRSCM(food.item)
                println("EatingPlugin: Binding ${food.item} (ID: $foodItemId) to option 1")
                
                // Bind to option 1 (standard "Eat" option in OSRS)
                onItemOption(item = food.item, option = 1) {
                    eatFood(player, food, foodItemId)
                }
                println("EatingPlugin: Successfully bound ${food.item}")
            } catch (e: Exception) {
                println("EatingPlugin: ERROR binding ${food.item}: ${e.message}")
                e.printStackTrace()
            }
        }
        println("EatingPlugin: Initialization complete - bound ${Food.values.size} food types")
    }
    
    /**
     * Helper function to handle eating food
     */
    private fun eatFood(player: Player, food: Food, foodItemId: Int) {
        val inventorySlot = player.getInteractingItemSlot()
        val item = player.inventory[inventorySlot] ?: return
        
        // Verify the item at the slot matches what we expect
        if (item.id != foodItemId) {
            return
        }
        
        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }
        
        // Check if player can eat (cooldown check)
        if (!Foods.canEat(player, food)) {
            return
        }
        
        // Use queue to handle the eating process with animation
        player.queue {
            // Remove the food from inventory
            val removeResult = player.inventory.remove(item = foodItemId, amount = 1, beginSlot = inventorySlot, assureFullRemoval = false)
            if (removeResult.hasSucceeded() && removeResult.completed > 0) {
                // Eat the food (handles animation, sound, healing, messages)
                Foods.eat(player, food)
                
                // Add replacement item if needed (e.g., empty vial)
                if (food.replacement != -1) {
                    player.inventory.add(item = food.replacement.asRSCM("item"), beginSlot = inventorySlot)
                }
            }
        }
    }
}
