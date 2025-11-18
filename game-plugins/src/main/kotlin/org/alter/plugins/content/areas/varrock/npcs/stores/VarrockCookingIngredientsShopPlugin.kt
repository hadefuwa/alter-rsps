package org.alter.plugins.content.areas.varrock.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.NpcInfo
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.shops.CoinCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Varrock Cooking Ingredients Shop
 * 
 * A shop in the center of Varrock that sells raw food items and cooking ingredients.
 * Located at Varrock center (3219, 3434).
 */
class VarrockCookingIngredientsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_2894"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Cooking ingredients shop items - safely build list, skipping items that don't exist
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to safely add items
        fun addItem(itemName: String, quantity: Int, price: Int, sellPrice: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, quantity, price, sellPrice))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
                println("VarrockCookingIngredientsShopPlugin: Skipping $itemName - not found in RSCM")
            }
        }
        
        // Raw fish (using raw_ prefix where needed)
        addItem("raw_shrimps", 500, 5, 2)
        addItem("raw_sardine", 500, 5, 2)
        addItem("raw_herring", 500, 10, 5)
        addItem("raw_mackerel", 500, 10, 5)
        addItem("raw_trout", 500, 15, 7)
        addItem("raw_cod", 500, 15, 7)
        addItem("raw_pike", 500, 20, 10)
        addItem("raw_salmon", 500, 25, 12)
        addItem("raw_tuna", 500, 30, 15)
        addItem("raw_lobster", 500, 50, 25)
        addItem("raw_bass", 500, 60, 30)
        addItem("raw_swordfish", 500, 80, 40)
        addItem("raw_shark", 500, 150, 75)
        addItem("raw_manta_ray", 500, 200, 100)
        // Raw meat
        addItem("raw_chicken", 500, 5, 2)
        addItem("raw_beef", 500, 10, 5)
        addItem("raw_rat_meat", 500, 5, 2)
        addItem("raw_bear_meat", 500, 15, 7)
        // Other ingredients
        addItem("bread", 500, 5, 2)
        addItem("potato", 500, 2, 1)
        addItem("onion", 500, 2, 1)
        addItem("cabbage", 500, 2, 1)
        addItem("tomato", 500, 3, 1)
        addItem("cheese", 500, 5, 2)
        addItem("egg", 500, 3, 1)
        addItem("flour", 500, 5, 2)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3219, z = 3434, height = 0)
        spawnNpc(shopkeeper, 3219, 3434, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Cooking Ingredients Shopkeeper")
            }
        }

        // Create the cooking ingredients shop with enough space for items
        createShop(
            name = "Varrock Cooking Ingredients Shop",
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

    fun Player.shop() = this.openShop("Varrock Cooking Ingredients Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Cooking Ingredients Shop! We have all the raw ingredients you need for cooking.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

