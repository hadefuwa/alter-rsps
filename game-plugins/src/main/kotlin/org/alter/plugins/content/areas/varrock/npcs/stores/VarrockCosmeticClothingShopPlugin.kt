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
 * Located at Varrock center (3194, 3425).
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
    
    // Cosmetic/Clothing shop items - starting with 2 items, user will populate the rest
    private val storeItems = listOf(
        ShopItem(getRSCM("item.red_cape"), 50, 100, 25),
        ShopItem(getRSCM("item.leather_boots"), 50, 50, 10),
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3194, z = 3425, height = 0)
        spawnNpc(shopkeeper, 3194, 3425, 0, 0, Direction.SOUTH)
        
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
        // Note: silk_trader only has "talk-to" option, not "trade"
        onNpcOption(shopkeeper, option = "talk-to") { 
            player.queue { dialog(player) } 
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

