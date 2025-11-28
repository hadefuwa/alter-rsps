package org.alter.plugins.content.items.bond

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.doompoints.DoomPoints
import org.alter.rscm.RSCM.getRSCM

/**
 * Bond Plugin
 * 
 * Handles Old School Bond (item 13190) clicks.
 * When a bond is clicked, it gives the player 50 Doom Points.
 */
class BondPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private const val BOND_ITEM = "item.old_school_bond"
        private const val DOOM_POINTS_REWARD = 50
    }

    init {
        // Handle bond clicks - try "redeem" option first, then fallback to other options
        try {
            val bondItemId = getRSCM(BOND_ITEM)
            val itemDef = getItem(bondItemId)
            
            // Try "redeem" option by name (most reliable)
            val hasRedeemOption = itemDef.interfaceOptions.any { it?.lowercase() == "redeem" }
            if (hasRedeemOption) {
                try {
                    onItemOption(BOND_ITEM, "redeem") {
                        redeemBond(player)
                    }
                } catch (e: Exception) {
                    println("Failed to bind 'redeem' option for bond: ${e.message}")
                }
            }
            
            // Also try option 2 (left-click in inventory) as fallback
            val bondItemIdInt = getRSCM(BOND_ITEM)
            if (!world.plugins.isItemBound(bondItemIdInt, 2)) {
                try {
                    onItemOption(BOND_ITEM, 2) {
                        redeemBond(player)
                    }
                } catch (e: Exception) {
                    // Option 2 might not exist or already bound, continue
                }
            }
            
            // Try "use" option as another fallback
            val hasUseOption = itemDef.interfaceOptions.any { it?.lowercase() == "use" }
            if (hasUseOption) {
                try {
                    onItemOption(BOND_ITEM, "use") {
                        redeemBond(player)
                    }
                } catch (e: Exception) {
                    // "use" option might not exist or already bound, continue
                }
            }
        } catch (e: Exception) {
            println("Error registering bond plugin: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Handles bond redemption - gives player 50 Doom Points
     */
    private fun redeemBond(player: Player) {
        // Check if player has a bond in inventory
        val bondItemId = getRSCM(BOND_ITEM)
        if (!player.hasItem(bondItemId)) {
            player.message("You don't have a bond to redeem.")
            return
        }
        
        // Remove the bond from inventory
        if (player.inventory.remove(bondItemId, 1).hasSucceeded()) {
            // Give doom points
            DoomPoints.addDoomPoints(player, DOOM_POINTS_REWARD)
            player.message("You have redeemed your bond for $DOOM_POINTS_REWARD Doom Points!")
        } else {
            player.message("Failed to redeem bond.")
        }
    }
}

