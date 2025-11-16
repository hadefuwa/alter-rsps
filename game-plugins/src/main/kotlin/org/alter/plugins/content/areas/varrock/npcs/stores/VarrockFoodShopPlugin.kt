package org.alter.plugins.content.areas.varrock.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.items.consumables.food.Food
import org.alter.plugins.content.mechanics.shops.CoinCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Varrock Food Shop
 * 
 * A shop in the center of Varrock that sells all mapped food items.
 * Located at Varrock center (3211, 3424).
 */
class VarrockFoodShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_2825"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items from all foods in the Food enum
    private val storeItems: List<ShopItem> = buildList {
        Food.values.forEach { food ->
            try {
                val itemId = getRSCM(food.item)
                if (itemId != -1) {
                    // Special case: anglerfish is always 100k
                    val finalPrice = if (food == Food.ANGLERFISH) {
                        100_000
                    } else {
                        // Scale price from 10k to 100k based on heal amount
                        // Heal amounts range from 1 (potato) to 22 (dark crab)
                        // Formula: price = 10000 + (heal - 1) * (100000 - 10000) / (22 - 1)
                        val minHeal = 1
                        val maxHeal = 22
                        val minPrice = 10_000
                        val maxPrice = 100_000
                        
                        // Ensure heal is within valid range
                        val heal = food.heal.coerceIn(minHeal, maxHeal)
                        
                        // Linear scaling: 1 HP = 10k, 22 HP = 100k
                        val price = if (heal == minHeal) {
                            minPrice
                        } else if (heal == maxHeal) {
                            maxPrice
                        } else {
                            minPrice + ((heal - minHeal) * (maxPrice - minPrice) / (maxHeal - minHeal))
                        }
                        
                        // Apply multipliers for special food types
                        when {
                            food.comboFood -> (price * 1.2).toInt()  // Combo foods cost 20% more
                            else -> price
                        }
                    }
                    
                    // Stock: 100 of each food, restock to 50
                    add(ShopItem(itemId, 100, finalPrice, 50))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
                println("VarrockFoodShopPlugin: Skipping ${food.item} - not found in RSCM")
            }
        }
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        spawnNpc(shopkeeper, 3212, 3425, 0, 0, Direction.SOUTH)

        // Create the food shop with enough space for all food items
        // Using stockSize of 100 to accommodate all foods (there are ~50+ food items)
        createShop(
            name = "Varrock Food Shop",
            currency = CoinCurrency(),
            stockSize = 100,
            purchasePolicy = PurchasePolicy.BUY_TRADEABLES
        ) {
            storeItems.forEachIndexed { index, item ->
                if (index < items.size) {
                    items[index] = item
                }
            }
        }

        // Set up NPC interactions
        onNpcOption(shopkeeper, option = "talk-to") { 
            player.queue { dialog(player) } 
        }

        onNpcOption(shopkeeper, option = "trade") { 
            player.shop() 
        }
    }

    fun Player.shop() = this.openShop("Varrock Food Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Food Shop! We have all kinds of food to keep you healthy.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

