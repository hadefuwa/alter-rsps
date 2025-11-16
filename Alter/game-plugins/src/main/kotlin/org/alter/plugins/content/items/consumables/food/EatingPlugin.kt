package org.alter.plugins.content.items.consumables.food

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
        Food.values.forEach { food ->
            val foodItemId = getRSCM(food.item)
            
            // Bind to option 1 (standard "Eat" option in OSRS)
            // This is the first option that appears when right-clicking food
            try {
                onItemOption(item = food.item, option = 1) {
                    val inventorySlot = player.getInteractingItemSlot()
                    val item = player.inventory[inventorySlot]
                    
                    // Verify this is the correct food item and player can interact
                    if (item != null && item.id == foodItemId && player.lock.canItemInteract()) {
                        // Check if player can eat (cooldown check)
                        if (Foods.canEat(player, food)) {
                            // Remove the food from inventory and eat it
                            if (player.inventory.remove(item = food.item, beginSlot = inventorySlot).hasSucceeded()) {
                                Foods.eat(player, food)
                                if (food.replacement != -1) {
                                    player.inventory.add(item = food.replacement.asRSCM("item"), beginSlot = inventorySlot)
                                }
                            }
                        }
                    }
                }
                println("EatingPlugin: Successfully bound ${food.item} (ID: $foodItemId) to option 1")
            } catch (e: Exception) {
                println("EatingPlugin: ERROR - Failed to bind ${food.item} (ID: $foodItemId) to option 1: ${e.message}")
                e.printStackTrace()
            }
        }
        println("EatingPlugin: Initialized - bound ${Food.values.size} food types")
    }
}
