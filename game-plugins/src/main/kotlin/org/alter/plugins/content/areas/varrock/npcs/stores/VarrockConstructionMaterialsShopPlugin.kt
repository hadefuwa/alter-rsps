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
 * Varrock Construction Materials Shop
 * 
 * A shop in the center of Varrock that sells construction materials like planks and nails.
 * Located at Varrock center (3220, 3431).
 */
class VarrockConstructionMaterialsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_assistant_2826"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Construction materials shop items - safely build list, skipping items that don't exist
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
                println("VarrockConstructionMaterialsShopPlugin: Skipping $itemName - not found in RSCM")
            }
        }
        
        // Planks
        addItem("plank", 1000, 100, 50)
        addItem("oak_plank", 1000, 250, 125)
        addItem("teak_plank", 1000, 500, 250)
        addItem("mahogany_plank", 1000, 1500, 750)
        // Nails
        addItem("bronze_nails", 1000, 10, 5)
        addItem("iron_nails", 1000, 20, 10)
        addItem("steel_nails", 1000, 40, 20)
        addItem("mithril_nails", 1000, 80, 40)
        addItem("adamantite_nails", 1000, 160, 80) // Note: spelled "adamantite" not "adamant"
        addItem("rune_nails", 1000, 320, 160)
        // Other materials
        addItem("soft_clay", 1000, 50, 25)
        addItem("limestone_brick", 1000, 100, 50)
        addItem("steel_bar", 1000, 200, 100)
        addItem("gold_leaf", 500, 5000, 2500)
        addItem("marble_block", 100, 10000, 5000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3220, z = 3431, height = 0)
        spawnNpc(shopkeeper, 3220, 3431, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Construction Materials Shopkeeper")
            }
        }

        // Create the construction materials shop with enough space for items
        createShop(
            name = "Varrock Construction Materials Shop",
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

    fun Player.shop() = this.openShop("Varrock Construction Materials Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Construction Materials Shop! We have all the materials you need for building.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

