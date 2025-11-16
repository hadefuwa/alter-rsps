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
        // Helper function to handle food eating
        fun handleEatFood(food: Food, foodItemId: Int, optionNum: Int) {
            try {
                onItemOption(item = food.item, option = optionNum) {
                    val inventorySlot = player.getInteractingItemSlot()
                    val item = player.inventory[inventorySlot] ?: return@onItemOption
                    
                    // Only process if this is actually the food item
                    if (item.id != foodItemId) {
                        return@onItemOption
                    }
                    
                    // Check if player can interact with items
                    if (!player.lock.canItemInteract()) {
                        return@onItemOption
                    }
                    
                    // Check if player can eat (cooldown check)
                    if (!Foods.canEat(player, food)) {
                        return@onItemOption
                    }

                    // Remove the food from inventory
                    if (player.inventory.remove(item = food.item, beginSlot = inventorySlot).hasSucceeded()) {
                        Foods.eat(player, food)
                        if (food.replacement != -1) {
                            player.inventory.add(item = food.replacement.asRSCM("item"), beginSlot = inventorySlot)
                        }
                    }
                }
            } catch (e: Exception) {
                // Option binding failed, skip this option
                println("EatingPlugin: Failed to bind ${food.item} to option $optionNum: ${e.message}")
            }
        }
        
        Food.values.forEach { food ->
            val foodItemId = getRSCM(food.item)
            val itemDef = getItem(foodItemId)
            
            // Try to find "Eat" option in the item's interface options
            val eatOptionIndex = itemDef.interfaceOptions.indexOfFirst { 
                it?.lowercase()?.contains("eat") == true 
            }
            
            if (eatOptionIndex != -1) {
                // Found "Eat" option, use it (option numbers are 1-indexed)
                val optionToUse = eatOptionIndex + 1
                handleEatFood(food, foodItemId, optionToUse)
            } else {
                // Try option 1 first (most common for "Eat" in OSRS)
                handleEatFood(food, foodItemId, 1)
            }
        }
    }
}
