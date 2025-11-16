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
 * Varrock Weapons Shop
 * 
 * A shop in the center of Varrock that sells weapons.
 * Located at Varrock center (3216, 3425).
 */
class VarrockWeaponsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_assistant_2824"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items with weapons (1k-10k range)
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add weapon with pricing
        fun addWeapon(itemName: String, price: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, 50, price, 25))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Rune Weapons - 10m-30m
        addWeapon("rune_dagger", 10000000)
        addWeapon("rune_longsword", 30000000)
        addWeapon("rune_scimitar", 30000000)
        addWeapon("rune_battleaxe", 30000000)
        addWeapon("rune_mace", 20000000)
        addWeapon("rune_sword", 25000000)
        addWeapon("rune_2h_sword", 35000000)
        addWeapon("rune_warhammer", 25000000)
        addWeapon("rune_halberd", 30000000)
        
        // Dragon Weapons - 40m-70m
        addWeapon("dragon_longsword", 70000000)
        addWeapon("dragon_scimitar", 70000000)
        addWeapon("dragon_dagger", 60000000)
        addWeapon("dragon_battleaxe", 65000000)
        addWeapon("dragon_mace", 55000000)
        addWeapon("dragon_sword", 65000000)
        addWeapon("dragon_2h_sword", 70000000)
        addWeapon("dragon_warhammer", 65000000)
        addWeapon("dragon_halberd", 68000000)
        
        // Special Weapons - 80m-100m
        addWeapon("abyssal_whip", 100000000)
        addWeapon("abyssal_dagger", 90000000)
        
        // Ranged Weapons - 10m-30m
        addWeapon("rune_crossbow", 30000000)
        addWeapon("rune_knife", 15000000)
        addWeapon("rune_dart", 12000000)
        addWeapon("rune_arrow", 10000000)
        addWeapon("rune_thrownaxe", 20000000)
        addWeapon("rune_javelin", 18000000)
        
        // Magic Weapons - 15m-40m
        addWeapon("staff_of_air", 15000000)
        addWeapon("staff_of_water", 15000000)
        addWeapon("staff_of_earth", 15000000)
        addWeapon("staff_of_fire", 15000000)
        addWeapon("mystic_air_staff", 30000000)
        addWeapon("mystic_water_staff", 30000000)
        addWeapon("mystic_earth_staff", 30000000)
        addWeapon("mystic_fire_staff", 30000000)
        addWeapon("battlestaff", 20000000)
        addWeapon("lava_battlestaff", 40000000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        spawnNpc(shopkeeper, 3216, 3425, 0, 0, Direction.SOUTH)

        // Create the weapons shop
        createShop(
            name = "Varrock Weapons Shop",
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

        onNpcOption(shopkeeper, option = "trade") { 
            player.shop() 
        }
    }

    fun Player.shop() = this.openShop("Varrock Weapons Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Weapons Shop! We have the finest weapons in all of Gielinor.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

