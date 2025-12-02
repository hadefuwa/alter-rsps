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
 * Varrock Smithing Shop
 * 
 * A shop in the center of Varrock that sells smithing items.
 * Located at Varrock center (3208, 3434).
 */
class VarrockSmithingShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.scavvo"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Smithing shop items - starting with 2 items, user will populate the rest
    private val storeItems = listOf(
        ShopItem(getRSCM("item.feather"), 10000, 5, 500),
        ShopItem(getRSCM("item.hammer"), 1000, 10, 500),
        ShopItem(getRSCM("item.bronze_bar"), 1000, 1000, 500),  
        ShopItem(getRSCM("item.iron_bar"), 1000, 2500, 500),
        ShopItem(getRSCM("item.steel_bar"), 1000, 5000, 500),
        ShopItem(getRSCM("item.silver_bar"), 1000, 2500, 500),
        ShopItem(getRSCM("item.gold_bar"), 1000, 5000, 500),        
        ShopItem(getRSCM("item.mithril_bar"), 1000, 10000, 500),
        ShopItem(getRSCM("item.adamantite_bar"), 1000, 25000, 500),
        ShopItem(getRSCM("item.runite_bar"), 1000, 50000, 500),
        ShopItem(getRSCM("item.bronze_bolts_unf"), 1000, 100, 500),
        ShopItem(getRSCM("item.iron_bolts_unf"), 1000, 250, 500),
        ShopItem(getRSCM("item.steel_bolts_unf"), 1000, 500, 500),
        ShopItem(getRSCM("item.mithril_bolts_unf"), 1000, 1000, 500),
        ShopItem(getRSCM("item.adamant_boltsunf"), 1000, 5000, 500),
        ShopItem(getRSCM("item.amulet_mould"), 1000, 10000, 500),
        ShopItem(getRSCM("item.unholy_mould"), 1000, 10000, 500),        
        ShopItem(getRSCM("item.holy_mould"), 1000, 10000, 500),
        ShopItem(getRSCM("item.ring_mould"), 1000, 10000, 500),
        ShopItem(getRSCM("item.sickle_mould"), 1000, 10000, 500),
        ShopItem(getRSCM("item.necklace_mould"), 1000, 10000, 500),

    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3208, z = 3434, height = 0)
        spawnNpc(shopkeeper, 3208, 3434, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Smithing Shopkeeper")
            }
        }

        // Create the runes shop with enough space for items
        createShop(
            name = "Varrock Smithing Shop",
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

    fun Player.shop() = this.openShop("Varrock Smithing Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Smithing Shop! We have all kinds of items for your smithing.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}