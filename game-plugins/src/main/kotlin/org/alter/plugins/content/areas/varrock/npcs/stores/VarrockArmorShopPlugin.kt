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
 * Varrock Armor Shop
 * 
 * A shop in the center of Varrock that sells armor.
 * Located at Varrock center (3219, 3421).
 */
class VarrockArmorShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_2884"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items with armor (1k-10k range)
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add armor with pricing
        fun addArmor(itemName: String, price: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, 50, price, 25))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Rune Armor - 10m-30m
        addArmor("rune_gloves", 10000000)
        
        // Barrows Gloves - 150m
        addArmor("barrows_gloves", 150000000)

        
        // Dragon Boots - 55m
        addArmor("dragon_boots", 55000000)
        
        // Magic Armor - 15m-30m
        addArmor("mystic_robe_top", 300000)
        addArmor("mystic_robe_bottom", 300000)
        
        // Monk Robes - 100k each
        addArmor("monks_robe_top", 100000)
        addArmor("monks_robe", 100000)

        // Bandos Armor - 200m-300m
        addArmor("bandos_chestplate", 200000000)
        addArmor("bandos_tassets", 200000000)
        addArmor("antidragon_shield", 10000000)
        addArmor("dragonfire_shield", 100000000)
        
        // Torva Armor - 1b each
        addArmor("torva_full_helm", 1000000000)
        addArmor("torva_platebody", 1000000000)
        addArmor("torva_platelegs", 1000000000)
        
        // Inquisitor Armor - 1b each
        addArmor("inquisitors_great_helm", 1000000000)
        addArmor("inquisitors_hauberk", 1000000000)
        addArmor("inquisitors_plateskirt", 1000000000)
        addArmor("inquisitors_mace", 1000000000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3219, z = 3421, height = 0)
        spawnNpc(shopkeeper, 3219, 3421, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Armor Shopkeeper")
            }
        }

        // Create the armor shop
        createShop(
            name = "Varrock Armor Shop",
            currency = CoinCurrency(),
            stockSize = 60,
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

    fun Player.shop() = this.openShop("Varrock Armor Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Armor Shop! We have the best protection money can buy.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}
