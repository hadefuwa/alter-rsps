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
 * Varrock Jewelry Shop
 * 
 * A shop in the center of Varrock that sells jewelry.
 * Located at Varrock center (3215, 3421).
 */
class VarrockJewelryShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.gnome_shop_keeper"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items with jewelry
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add jewelry with pricing
        fun addJewelry(itemName: String, price: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, 50, price, 25))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Amulet of Glory - 10m
        addJewelry("amulet_of_glory", 10000000)
        
        // Amulet of Fury - 100m
        addJewelry("amulet_of_fury", 100000000)
        
        // Blood Fury - 200m
        addJewelry("amulet_of_blood_fury", 200000000)
        
        // Amulet of Rancour - 120m
        addJewelry("amulet_of_rancour", 300000000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3215, z = 3421, height = 0)
        spawnNpc(shopkeeper, 3215, 3421, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Jewelry Shopkeeper")
            }
        }

        // Create the jewelry shop
        createShop(
            name = "Varrock Jewelry Shop",
            currency = CoinCurrency(),
            stockSize = 50,
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
        // Since gnome_shop_keeper only has "Talk-to", we use option 3 as fallback
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

    fun Player.shop() = this.openShop("Varrock Jewelry Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Jewelry Shop! We have the finest jewelry in all of Gielinor.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

