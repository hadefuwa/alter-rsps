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
                    // Price based on heal amount (roughly 10-20 coins per HP)
                    val price = when {
                        food.heal <= 5 -> food.heal * 2  // Cheap foods: 2 coins per HP
                        food.heal <= 10 -> food.heal * 3  // Mid-tier: 3 coins per HP
                        food.heal <= 15 -> food.heal * 4  // High-tier: 4 coins per HP
                        else -> food.heal * 5  // Premium: 5 coins per HP
                    }
                    // Special pricing for combo foods and overheal foods
                    val finalPrice = when {
                        food.comboFood -> price * 2  // Combo foods cost more
                        food.overheal -> price * 3   // Overheal foods cost even more
                        else -> price
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
        // Register this BEFORE the string "trade" option to avoid conflicts
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
            // Option 3 already bound by another plugin, use string option as fallback
            println("VarrockFoodShopPlugin: Option 3 already bound, using string 'trade' option")
            onNpcOption(shopkeeper, option = "trade") { 
                val npc = player.getInteractingNpc()
                if (npc.tile == shopkeeperTile) {
                    player.shop() 
                }
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

