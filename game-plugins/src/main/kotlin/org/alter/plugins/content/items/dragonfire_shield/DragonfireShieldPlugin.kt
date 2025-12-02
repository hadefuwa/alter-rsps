package org.alter.plugins.content.items.dragonfire_shield

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

class DragonfireShieldPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val DRACONIC_VISAGE = getRSCM("item.draconic_visage")
    private val ANTI_DRAGON_SHIELD = getRSCM("item.antidragon_shield")
    private val DRAGONFIRE_SHIELD = getRSCM("item.dragonfire_shield")

    init {
        // Combine draconic visage with anti-dragon shield to create dragonfire shield
        onItemOnItem(item1 = "item.draconic_visage", item2 = "item.antidragon_shield") {
            val sourceSlot = player.getInteractingItemSlot()
            val targetSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: return@onItemOnItem
            
            val source = player.inventory[sourceSlot] ?: return@onItemOnItem
            val target = player.inventory[targetSlot] ?: return@onItemOnItem
            
            // Verify we have both items (one must be visage, one must be shield)
            val hasVisage = source.id == DRACONIC_VISAGE || target.id == DRACONIC_VISAGE
            val hasShield = source.id == ANTI_DRAGON_SHIELD || target.id == ANTI_DRAGON_SHIELD
            
            if (!hasVisage || !hasShield) {
                return@onItemOnItem
            }
            
            // Remove both items
            player.inventory.remove(DRACONIC_VISAGE, 1)
            player.inventory.remove(ANTI_DRAGON_SHIELD, 1)
            
            // Add dragonfire shield
            val transaction = player.inventory.add(DRAGONFIRE_SHIELD, 1)
            if (transaction.hasSucceeded()) {
                player.message("You attach the draconic visage to your anti-dragon shield.")
                player.message("You now have a dragonfire shield.")
                player.animate(Animation.SMITHING_ANVIL) // Smithing animation
                player.graphic(1160) // Dragonfire shield graphic
            } else {
                // If inventory is full, drop the shield on the ground
                val groundItem = GroundItem(
                    item = DRAGONFIRE_SHIELD,
                    amount = 1,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full! The dragonfire shield appears on the ground.")
            }
        }
        
        // Equip animation for dragonfire shield
        onItemEquip("item.dragonfire_shield") {
            player.queue {
                player.animate(-1)
                player.graphic(-1)
                player.animate(3996, 3)
                player.graphic(1160, 90, 3) // Dragonfire shield equip graphic
            }
        }
    }
}
