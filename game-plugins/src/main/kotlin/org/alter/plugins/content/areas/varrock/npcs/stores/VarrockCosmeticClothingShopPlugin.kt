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
 * Varrock Cosmetic/Clothing Shop
 * 
 * A shop in the center of Varrock that sells cosmetic items and clothing for fashion.
 * Located at Varrock center (3218, 3422).
 */
class VarrockCosmeticClothingShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.silk_trader"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Cosmetic/Clothing shop items
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add item with pricing
        fun addItem(itemName: String, price: Int, stock: Int = 50, restock: Int = 10) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, stock, price, restock))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Bunny items
        addItem("bunny_ears", 1000000)
        addItem("bunny_feet", 1000000)
        addItem("bunnyman_mask", 1000000)
        addItem("bunny_top", 1000000)
        addItem("bunny_legs", 1000000)
        addItem("bunny_paws", 1000000)
        
        // Flower crown
        addItem("flower_crown", 1000000)
        
        // 3rd age items
        addItem("_3rd_age_robe", 10000000)
        addItem("_3rd_age_robe_top", 10000000)
        addItem("_3rd_age_mage_hat", 10000000)
        addItem("_3rd_age_robe_20577", 10000000)
        
        // Party hats
        addItem("blue_partyhat", 100000)
        addItem("rainbow_partyhat", 100000)
        
        // Unicorn items
        addItem("unicorn_hat", 100000)
        addItem("unicorn_shirt", 100000)
        addItem("unicorn_pants", 100000)
        addItem("unicorn_boots", 100000)
        addItem("unicorn_gloves", 100000)
        addItem("unicorn_mask", 100000)
        addItem("unicorn_top", 100000)
        addItem("unicorn_bottom", 100000)
        
        // Other cosmetic items
        addItem("mole_slippers", 100000)
        addItem("soul_cape", 100000, 50, 25)
        
        // Pirate items
        addItem("pirate_hat", 100000)
        addItem("pirate_shirt", 100000)
        addItem("pirate_pants", 100000)
        addItem("pirate_boots", 100000)
        addItem("pirate_gloves", 100000)
        addItem("pirate_mask", 100000)
        addItem("pirate_top", 100000)
        addItem("pirate_bottom", 100000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3218, z = 3422, height = 0)
        spawnNpc(shopkeeper, 3220, 3424, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Cosmetic Shopkeeper")
            }
        }

        // Create the cosmetic/clothing shop with enough space for items
        createShop(
            name = "Varrock Cosmetic/Clothing Shop",
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

        // Try to register option 3 (numeric) for trade - this is what gets triggered when clicking trade
        // Since silk_trader only has "Talk-to", we use option 3 as fallback
        try {
            onNpcOption(shopkeeper, option = 3) {
                val npc = player.getInteractingNpc()
                if (npc.tile == shopkeeperTile) {
                    player.shop()
                }
            }
        } catch (e: IllegalStateException) {
            // Option 3 already bound by another plugin, skip
        } catch (e: Exception) {
            // Other error, skip
        }
    }

    fun Player.shop() = this.openShop("Varrock Cosmetic/Clothing Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Cosmetic/Clothing Shop! We have fashionable items to make you look great.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}