package org.alter.plugins.content.areas.varrock.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
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
 * Located at Varrock center (3210, 3425).
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

        
        // Dragon Armor - 40m-70m
        addArmor("dragon_full_helm", 70000000)
        addArmor("dragon_med_helm", 60000000)
        addArmor("dragon_platebody", 70000000)
        addArmor("dragon_chainbody", 65000000)
        addArmor("dragon_platelegs", 65000000)
        addArmor("dragon_plateskirt", 65000000)
        addArmor("dragon_boots", 55000000)
        addArmor("dragon_gloves", 40000000)
        addArmor("dragon_sq_shield", 60000000)
        addArmor("dragon_kiteshield", 65000000)
        
        // Barrows Armor - 80m-100m
        addArmor("dharoks_helm", 85000000)
        addArmor("dharoks_platebody", 100000000)
        addArmor("dharoks_platelegs", 90000000)
        addArmor("dharoks_greataxe", 100000000)
        
        addArmor("guthans_helm", 85000000)
        addArmor("guthans_platebody", 100000000)
        addArmor("guthans_platelegs", 90000000)
        addArmor("guthans_warspear", 100000000)
    
        
        addArmor("veracs_helm", 85000000)
        addArmor("veracs_brassard", 100000000)
        addArmor("veracs_plateskirt", 90000000)
        addArmor("veracs_flail", 100000000)
        
        addArmor("ahrims_hood", 85000000)
        addArmor("ahrims_robetop", 100000000)
        addArmor("ahrims_robeskirt", 90000000)
        addArmor("ahrims_staff", 100000000)
        
        // Karil's Equipment - 80m-100m
        addArmor("karils_coif", 85000000)
        addArmor("karils_leathertop", 100000000)
        addArmor("karils_leatherskirt", 90000000)
        addArmor("karils_crossbow", 100000000)
        addArmor("karils_armour_set", 95000000)
        
        // Ranged Armor - 10m-20m
        addArmor("rune_arrow", 1000)
        addArmor("rune_bolts", 1200)
        addArmor("rune_dart", 1200)
        addArmor("rune_knife", 1500)
        
        // Magic Armor - 15m-30m
        addArmor("mystic_robe_top", 300000)
        addArmor("mystic_robe_bottom", 300000)

        // Bandos Armor - 200m-300m
        addArmor("bandos_chestplate", 200000000)
        addArmor("bandos_tassets", 200000000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        spawnNpc(shopkeeper, 3210, 3425, 0, 0, Direction.SOUTH)

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
