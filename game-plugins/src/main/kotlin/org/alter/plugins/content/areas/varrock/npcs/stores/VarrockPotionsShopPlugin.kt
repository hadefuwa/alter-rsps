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
 * Varrock Potions Shop
 * 
 * A shop in the center of Varrock that sells all potions.
 * Located at Varrock center (3220, 3427).
 */
class VarrockPotionsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_assistant_2885"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items with all potions
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add potion with pricing
        fun addPotion(itemName: String, basePrice: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, 100, basePrice, 50))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Prayer Potions (4-dose, 3-dose, 2-dose, 1-dose) - 1k-5k
        addPotion("prayer_potion4", 5000)
        addPotion("antipoison4", 50000)
        addPotion("anti-venom4", 500000)
        
        // Super Restore Potions - 3.5k-8k
        addPotion("super_restore4", 80000)

        
        // Saradomin Brews - 4k-10k
        addPotion("saradomin_brew4", 100000)

        
        // Ranging Potions - 1.5k-3k
        addPotion("ranging_potion4", 3000)

        
        // Antifire Potions - 1.6k-4k
        addPotion("antifire_potion4", 4000)

        
        // Super Attack Potions - 1k-2.5k
        addPotion("super_attack4", 2500)

        
        // Super Strength Potions - 1k-2.5k
        addPotion("super_strength4", 2500)

        
        // Super Defence Potions - 1k-2.5k
        addPotion("super_defence4", 2500)

        
        // Magic Potions - 1.5k-3k
        addPotion("magic_potion4", 3000)
        
        // Agility Potions - 1k-2k
        addPotion("agility_potion4", 2000)

        // Super Antipoison Potions - 1.2k-2k
        addPotion("superantipoison4", 200000)


        

        
        addPotion("strength_potion4", 1000)

    
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        // Placed next to the food shopkeeper
        val shopkeeperTile = Tile(x = 3220, z = 3427, height = 0)
        spawnNpc(shopkeeper, 3220, 3427, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Potions Shopkeeper")
            }
        }

        // Create the potions shop with enough space for all potions
        createShop(
            name = "Varrock Potions Shop",
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

    fun Player.shop() = this.openShop("Varrock Potions Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Potions Shop! We have all kinds of potions to boost your abilities.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

