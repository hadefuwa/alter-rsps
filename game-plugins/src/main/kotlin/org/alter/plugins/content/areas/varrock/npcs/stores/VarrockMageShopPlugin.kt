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
 * Varrock Mage Shop
 * 
 * A shop in the center of Varrock that sells magic equipment and supplies.
 * Located at Varrock center (3214, 3438).
 */
class VarrockMageShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.peksa"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Mage shop items - starting with 2 items, user will populate the rest
    private val storeItems = listOf(
        ShopItem(getRSCM("item.fire_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.water_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.air_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.earth_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.body_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.cosmic_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.chaos_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.law_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.death_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.blood_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.soul_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.mind_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.wrath_rune"), 50000, 500, 25),
        ShopItem(getRSCM("item.ancient_staff"), 500000, 2000000, 25),
        ShopItem(getRSCM("item.mind_shield"), 50000, 1000000, 25)
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3214, z = 3438, height = 0)
        spawnNpc(shopkeeper, 3214, 3438, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Mage Shopkeeper")
            }
        }

        // Create the mage shop with enough space for items
        createShop(
            name = "Varrock Mage Shop",
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

    fun Player.shop() = this.openShop("Varrock Mage Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Mage Shop! We have all kinds of magical equipment and supplies.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

