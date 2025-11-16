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
 * Varrock Potions Shop
 * 
 * A shop in the center of Varrock that sells all potions.
 * Located at Varrock center (3214, 3425).
 */
class VarrockPotionsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_assistant_2826"
    
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
        addPotion("prayer_potion3", 4000)
        addPotion("prayer_potion2", 3000)
        addPotion("prayer_potion1", 2000)
        
        // Super Restore Potions - 3.5k-8k
        addPotion("super_restore4", 8000)
        addPotion("super_restore3", 6500)
        addPotion("super_restore2", 5000)
        addPotion("super_restore1", 3500)
        
        // Saradomin Brews - 4k-10k
        addPotion("saradomin_brew4", 10000)
        addPotion("saradomin_brew3", 8000)
        addPotion("saradomin_brew2", 6000)
        addPotion("saradomin_brew1", 4000)
        
        // Ranging Potions - 1.5k-3k
        addPotion("ranging_potion4", 3000)
        addPotion("ranging_potion3", 2500)
        addPotion("ranging_potion2", 2000)
        addPotion("ranging_potion1", 1500)
        
        // Antifire Potions - 1.6k-4k
        addPotion("antifire_potion4", 4000)
        addPotion("antifire_potion3", 3200)
        addPotion("antifire_potion2", 2400)
        addPotion("antifire_potion1", 1600)
        
        // Super Attack Potions - 1k-2.5k
        addPotion("super_attack4", 2500)
        addPotion("super_attack3", 2000)
        addPotion("super_attack2", 1500)
        addPotion("super_attack1", 1000)
        
        // Super Strength Potions - 1k-2.5k
        addPotion("super_strength4", 2500)
        addPotion("super_strength3", 2000)
        addPotion("super_strength2", 1500)
        addPotion("super_strength1", 1000)
        
        // Super Defence Potions - 1k-2.5k
        addPotion("super_defence4", 2500)
        addPotion("super_defence3", 2000)
        addPotion("super_defence2", 1500)
        addPotion("super_defence1", 1000)
        
        // Magic Potions - 1.5k-3k
        addPotion("magic_potion4", 3000)
        addPotion("magic_potion3", 2500)
        addPotion("magic_potion2", 2000)
        addPotion("magic_potion1", 1500)
        
        // Energy Potions - 1k-2k
        addPotion("energy_potion4", 2000)
        addPotion("energy_potion3", 1600)
        addPotion("energy_potion2", 1200)
        addPotion("energy_potion1", 1000)
        
        // Agility Potions - 1k-2k
        addPotion("agility_potion4", 2000)
        addPotion("agility_potion3", 1600)
        addPotion("agility_potion2", 1200)
        addPotion("agility_potion1", 1000)
        
        // Antipoison Potions - 1k-1.5k
        addPotion("antipoison3", 1500)
        addPotion("antipoison2", 1200)
        addPotion("antipoison1", 1000)
        
        // Super Antipoison Potions - 1.2k-2k
        addPotion("superantipoison3", 2000)
        addPotion("superantipoison2", 1600)
        addPotion("superantipoison1", 1200)
        
        // Zamorak Brews - 3k-6k
        addPotion("zamorak_brew4", 6000)
        addPotion("zamorak_brew3", 5000)
        addPotion("zamorak_brew2", 4000)
        addPotion("zamorak_brew1", 3000)
        
        // Basic Combat Potions - 1k minimum
        addPotion("attack_potion3", 1000)
        addPotion("attack_potion2", 1000)
        addPotion("attack_potion1", 1000)
        
        addPotion("strength_potion4", 1000)
        addPotion("strength_potion3", 1000)
        addPotion("strength_potion2", 1000)
        addPotion("strength_potion1", 1000)
        
        addPotion("defence_potion3", 1000)
        addPotion("defence_potion2", 1000)
        addPotion("defence_potion1", 1000)
        
        addPotion("restore_potion3", 1000)
        addPotion("restore_potion2", 1000)
        addPotion("restore_potion1", 1000)
        
        // Fishing Potions - 1k-1.5k
        addPotion("fishing_potion3", 1500)
        addPotion("fishing_potion2", 1200)
        addPotion("fishing_potion1", 1000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        // Placed next to the food shopkeeper
        spawnNpc(shopkeeper, 3214, 3425, 0, 0, Direction.SOUTH)

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

