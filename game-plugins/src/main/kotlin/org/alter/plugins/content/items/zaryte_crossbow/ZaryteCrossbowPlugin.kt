package org.alter.plugins.content.items.zaryte_crossbow

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

/**
 * Zaryte Crossbow Plugin
 * 
 * Allows players to combine an Armadyl crossbow with a Nihil horn
 * to create a Zaryte crossbow, requiring 250 nihil shards.
 */
class ZaryteCrossbowPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val ARMADYL_CROSSBOW = getRSCM("item.armadyl_crossbow")
    private val NIHIL_HORN = getRSCM("item.nihil_horn")
    private val NIHIL_SHARD = getRSCM("item.nihil_shard")
    private val ZARYTE_CROSSBOW = getRSCM("item.zaryte_crossbow")
    private val REQUIRED_SHARDS = 250

    init {
        // Combine armadyl crossbow with nihil horn to create zaryte crossbow
        onItemOnItem(item1 = "item.armadyl_crossbow", item2 = "item.nihil_horn") {
            val sourceSlot = player.getInteractingItemSlot()
            val targetSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: return@onItemOnItem
            
            val source = player.inventory[sourceSlot] ?: return@onItemOnItem
            val target = player.inventory[targetSlot] ?: return@onItemOnItem
            
            // Verify we have both items (one must be crossbow, one must be horn)
            val hasCrossbow = source.id == ARMADYL_CROSSBOW || target.id == ARMADYL_CROSSBOW
            val hasHorn = source.id == NIHIL_HORN || target.id == NIHIL_HORN
            
            if (!hasCrossbow || !hasHorn) {
                return@onItemOnItem
            }
            
            // Check if player has 250 nihil shards
            val shardCount = player.inventory.getItemCount(NIHIL_SHARD)
            if (shardCount < REQUIRED_SHARDS) {
                player.message("You need at least $REQUIRED_SHARDS nihil shards to create a Zaryte crossbow.")
                player.message("You currently have $shardCount nihil shard${if (shardCount != 1) "s" else ""}.")
                return@onItemOnItem
            }
            
            // Remove armadyl crossbow, nihil horn, and 250 nihil shards
            player.inventory.remove(ARMADYL_CROSSBOW, 1)
            player.inventory.remove(NIHIL_HORN, 1)
            player.inventory.remove(NIHIL_SHARD, REQUIRED_SHARDS)
            
            // Add zaryte crossbow
            val transaction = player.inventory.add(ZARYTE_CROSSBOW, 1)
            if (transaction.hasSucceeded()) {
                player.message("You combine the Armadyl crossbow with the Nihil horn.")
                player.message("The nihil shards are consumed in the process.")
                player.message("You now have a Zaryte crossbow.")
                player.animate(Animation.SMITHING_ANVIL) // Smithing animation
                player.graphic(1160) // Crafting/smithing graphic
            } else {
                // If inventory is full, drop the crossbow on the ground
                val groundItem = GroundItem(
                    item = ZARYTE_CROSSBOW,
                    amount = 1,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full! The Zaryte crossbow appears on the ground.")
            }
        }
    }
}
