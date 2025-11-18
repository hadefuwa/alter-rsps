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
 * Varrock Skilling Supplies Shop
 * 
 * A shop in the center of Varrock that sells tools and supplies for various skills.
 * Located at Varrock center (3216, 3435).
 */
class VarrockSkillingSuppliesShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_7913"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Skilling supplies shop items - starting with 2 items, user will populate the rest
    private val storeItems = listOf(
        ShopItem(getRSCM("item.iron_pickaxe"), 50, 5000, 25),
        ShopItem(getRSCM("item.iron_axe"), 50, 5000, 25),
        ShopItem(getRSCM("item.hammer"), 50, 5000, 25),
        ShopItem(getRSCM("item.rune_axe"), 50, 5000, 25),
        ShopItem(getRSCM("item.rune_pickaxe"), 50, 5000, 25),
        ShopItem(getRSCM("item.rope"), 50, 5000, 25),
        ShopItem(getRSCM("item.feather"), 50, 5000, 25),
        ShopItem(getRSCM("item.fishing_rod"), 50, 5000, 25),
        ShopItem(getRSCM("item.fishing_bait"), 50, 5000, 25),
        ShopItem(getRSCM("item.small_fishing_net"), 50, 5000, 25),
        ShopItem(getRSCM("item.big_fishing_net"), 50, 5000, 25),
        ShopItem(getRSCM("item.sandworms"), 50, 5000, 25),
        ShopItem(getRSCM("item.fly_fishing_rod"), 50, 5000, 25),
        ShopItem(getRSCM("item.harpoon"), 50, 5000, 25),
        ShopItem(getRSCM("item.lobster_pot"), 50, 5000, 25),
        ShopItem(getRSCM("item.dragon_harpoon"), 50, 5000000, 25),
        ShopItem(getRSCM("item.impling_jar"), 50, 5000, 25),
        ShopItem(getRSCM("item.rake"), 50, 5000, 25),
        ShopItem(getRSCM("item.spade"), 50, 5000, 25),
        ShopItem(getRSCM("item.tinderbox"), 50, 5000, 25),
        ShopItem(getRSCM("item.bucket"), 50, 5000, 25),
        ShopItem(getRSCM("item.logs"), 50, 5000, 25),
        ShopItem(getRSCM("item.oak_logs"), 50, 5000, 25),
        ShopItem(getRSCM("item.maple_logs"), 50, 5000, 25),
        ShopItem(getRSCM("item.magic_logs"), 50, 5000, 25),
        ShopItem(getRSCM("item.rune_essence_noted"), 5000, 5000, 25),
        ShopItem(getRSCM("item.butterfly_net"), 5000, 5000, 25),
        ShopItem(getRSCM("item.box_trap"), 5000, 5000, 25),
        
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3216, z = 3435, height = 0)
        spawnNpc(shopkeeper, 3216, 3435, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Skilling Supplies Shopkeeper")
            }
        }

        // Create the skilling supplies shop with enough space for items
        createShop(
            name = "Varrock Skilling Supplies Shop",
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

    fun Player.shop() = this.openShop("Varrock Skilling Supplies Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Skilling Supplies Shop! We have all the tools you need for your skills.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}