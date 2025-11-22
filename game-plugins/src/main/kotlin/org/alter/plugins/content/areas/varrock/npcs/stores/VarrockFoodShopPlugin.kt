package org.alter.plugins.content.areas.varrock.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.info.NpcInfo
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
 * Located at Varrock center (3219, 3426).
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
                    // Price all foods between 1k-10k based on heal amount
                    // Heal range is 1-22, so we scale linearly: heal 1 = 1k, heal 22 = 10k
                    // Anglerfish has heal = 0 but can heal up to 22 at max level, so treat it as high-tier
                    val healAmount = when {
                        food.heal > 0 -> food.heal
                        food.overheal -> 20  // Anglerfish: treat as high-tier overheal food
                        else -> 1  // Fallback for any other edge cases
                    }
                    val basePrice = 1000 + ((healAmount - 1) * 9000 / 21).toInt()
                    
                    // Apply multipliers for special food types
                    val finalPrice = when {
                        food.comboFood && food.overheal -> (basePrice * 1.5).toInt().coerceIn(1000, 10000)  // Both special properties
                        food.comboFood -> (basePrice * 1.3).toInt().coerceIn(1000, 10000)  // Combo foods: +30%
                        food.overheal -> (basePrice * 1.4).toInt().coerceIn(1000, 10000)  // Overheal foods: +40%
                        else -> basePrice.coerceIn(1000, 10000)  // Regular foods
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
        val shopkeeperTile = Tile(x = 3219, z = 3426, height = 0)
        spawnNpc(shopkeeper, 3219, 3426, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Food Shopkeeper")
            }
        }

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

        // Set up NPC interactions - check tile location to avoid conflicts with other shops using same NPC type
        onNpcOption(shopkeeper, option = "talk-to") { 
            val npc = player.getInteractingNpc()
            if (npc.tile == shopkeeperTile) {
                player.queue { dialog(player) } 
            }
        }

        // Register option 3 (numeric) directly - this is what gets triggered when clicking trade
        // Try to register option 3, fall back to "trade" string option if it's already bound
        try {
            onNpcOption(shopkeeper, option = 3) {
                val npc = player.getInteractingNpc()
                println("VarrockFoodShopPlugin: Option 3 triggered for NPC at ${npc.tile}, expected tile: $shopkeeperTile")
                if (npc.tile == shopkeeperTile) {
                    println("VarrockFoodShopPlugin: Tile match! Opening shop...")
                    player.shop()
                } else {
                    println("VarrockFoodShopPlugin: Tile mismatch - NPC at ${npc.tile.x},${npc.tile.z}, expected ${shopkeeperTile.x},${shopkeeperTile.z}")
                }
            }
            println("VarrockFoodShopPlugin: Successfully registered option 3 for shopkeeper")
        } catch (e: IllegalStateException) {
            // Option 3 already bound by another plugin, try string option as fallback
            println("VarrockFoodShopPlugin: Option 3 already bound, trying string 'trade' option")
            try {
                onNpcOption(shopkeeper, option = "trade") { 
                    val npc = player.getInteractingNpc()
                    if (npc.tile == shopkeeperTile) {
                        player.shop() 
                    }
                }
                println("VarrockFoodShopPlugin: Successfully registered 'trade' option for shopkeeper")
            } catch (e2: IllegalStateException) {
                // "trade" option also already bound, skip
                println("VarrockFoodShopPlugin: 'trade' option also already bound, skipping")
            } catch (e2: Exception) {
                println("VarrockFoodShopPlugin: Could not register 'trade' option: ${e2.message}")
            }
        } catch (e: Exception) {
            // Other error, try string option as fallback
            println("VarrockFoodShopPlugin: Could not register option 3: ${e.message}, trying 'trade' option")
            try {
                onNpcOption(shopkeeper, option = "trade") { 
                    val npc = player.getInteractingNpc()
                    if (npc.tile == shopkeeperTile) {
                        player.shop() 
                    }
                }
            } catch (e2: IllegalStateException) {
                // "trade" option also already bound, skip
                println("VarrockFoodShopPlugin: 'trade' option also already bound, skipping")
            }
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

