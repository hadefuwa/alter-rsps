package org.alter.plugins.content.items.bond

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
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
        // Handle bond clicks - try option 2 first (inventory click)
        try {
            val bondItemId = getRSCM(BOND_ITEM)
            val itemDef = getItem(bondItemId)
            
            // Try option 2 (first click in inventory)
            if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                if (!world.plugins.isItemBound(bondItemId, 2)) {
                    onItemOption(BOND_ITEM, 2) {
                        redeemBond(player)
                    }
                }
            }
            
            // Try option 1 as fallback
            if (itemDef.interfaceOptions.size >= 1 && itemDef.interfaceOptions[0] != null) {
                if (!world.plugins.isItemBound(bondItemId, 1)) {
                    onItemOption(BOND_ITEM, 1) {
                        redeemBond(player)
                    }
                }
            }
            
            // Try "redeem" option if it exists
            if (itemDef.interfaceOptions.any { it?.lowercase() == "redeem" }) {
                try {
                    onItemOption(BOND_ITEM, "redeem") {
                        redeemBond(player)
                    }
                } catch (e: Exception) {
                    // Option not available, continue
                }
            }
            
            // Try "use" option if it exists
            if (itemDef.interfaceOptions.any { it?.lowercase() == "use" }) {
                try {
                    onItemOption(BOND_ITEM, "use") {
                        redeemBond(player)
                    }
                } catch (e: Exception) {
                    // Option not available, continue
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
        if (!player.inventory.hasItem(bondItemId)) {
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

