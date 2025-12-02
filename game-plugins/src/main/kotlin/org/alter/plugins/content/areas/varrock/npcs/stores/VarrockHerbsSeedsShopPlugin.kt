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
 * Varrock Herbs & Seeds Shop
 * 
 * A shop in the center of Varrock that sells herbs and seeds for farming and herblore.
 * Located at Varrock center (3209, 3436).
 */
class VarrockHerbsSeedsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.valaine"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Herbs & Seeds shop items
    private val storeItems = listOf(
        // Herbs
        ShopItem(getRSCM("item.guam_leaf"), 500, 100, 50),
        ShopItem(getRSCM("item.marrentill"), 500, 150, 75),
        ShopItem(getRSCM("item.tarromin"), 500, 200, 100),
        ShopItem(getRSCM("item.harralander"), 500, 300, 150),
        ShopItem(getRSCM("item.ranarr_weed"), 500, 500, 250),
        ShopItem(getRSCM("item.toadflax"), 500, 600, 300),
        ShopItem(getRSCM("item.irit_leaf"), 500, 800, 400),
        ShopItem(getRSCM("item.avantoe"), 500, 1000, 500),
        ShopItem(getRSCM("item.kwuarm"), 500, 1200, 600),
        ShopItem(getRSCM("item.snapdragon"), 500, 2000, 1000),
        ShopItem(getRSCM("item.cadantine"), 500, 1500, 750),
        ShopItem(getRSCM("item.lantadyme"), 500, 1800, 900),
        ShopItem(getRSCM("item.dwarf_weed"), 500, 2000, 1000),
        ShopItem(getRSCM("item.torstol"), 500, 5000, 2500),
        // Seeds
        ShopItem(getRSCM("item.guam_seed"), 500, 50, 25),
        ShopItem(getRSCM("item.marrentill_seed"), 500, 75, 37),
        ShopItem(getRSCM("item.tarromin_seed"), 500, 100, 50),
        ShopItem(getRSCM("item.harralander_seed"), 500, 150, 75),
        ShopItem(getRSCM("item.ranarr_seed"), 500, 250, 125),
        ShopItem(getRSCM("item.toadflax_seed"), 500, 300, 150),
        ShopItem(getRSCM("item.irit_seed"), 500, 400, 200),
        ShopItem(getRSCM("item.avantoe_seed"), 500, 500, 250),
        ShopItem(getRSCM("item.kwuarm_seed"), 500, 600, 300),
        ShopItem(getRSCM("item.snapdragon_seed"), 500, 1000, 500),
        ShopItem(getRSCM("item.cadantine_seed"), 500, 750, 375),
        ShopItem(getRSCM("item.lantadyme_seed"), 500, 900, 450),
        ShopItem(getRSCM("item.dwarf_weed_seed"), 500, 1000, 500),
        ShopItem(getRSCM("item.torstol_seed"), 500, 2500, 1250),
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3209, z = 3436, height = 0)
        spawnNpc(shopkeeper, 3209, 3436, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Herbs & Seeds Shopkeeper")
            }
        }

        // Create the herbs & seeds shop with enough space for items
        createShop(
            name = "Varrock Herbs & Seeds Shop",
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

    fun Player.shop() = this.openShop("Varrock Herbs & Seeds Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Herbs & Seeds Shop! We have everything you need for farming and herblore.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

