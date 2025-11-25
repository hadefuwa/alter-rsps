package org.alter.plugins.content.death

import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.api.ext.*

class PlayerDeathCoinLossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onPlayerDeath {
            val player = this.player
            val coinId = 995
            val lossAmount = 1_000_000

            val inventoryCoins = player.inventory.getItemCount(coinId)
            
            if (inventoryCoins >= lossAmount) {
                val result = player.inventory.remove(coinId, lossAmount)
                if (result.completed == lossAmount) {
                    player.message("You have lost 1,000,000 coins from your inventory upon death.")
                }
            } else {
                // Not enough in inventory (or none), check bank
                val bankCoins = player.bank.getItemCount(coinId)
                if (bankCoins >= lossAmount) {
                    val result = player.bank.remove(coinId, lossAmount)
                    if (result.completed == lossAmount) {
                        player.message("You have lost 1,000,000 coins from your bank upon death.")
                    }
                } else {
                    // Not enough in bank either
                    player.message("You did not have enough coins to pay the death fee.")
                }
            }
        }
    }
}
