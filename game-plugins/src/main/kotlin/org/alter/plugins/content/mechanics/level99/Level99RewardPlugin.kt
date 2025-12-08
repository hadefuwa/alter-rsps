package org.alter.plugins.content.mechanics.level99

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.*
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.TaskPriority
import org.alter.game.model.timer.TimeConstants
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Level 99 Reward Plugin
 * 
 * Awards 200 million coins when a player reaches level 99 in any skill.
 * Coins are added to inventory if there's space, otherwise dropped on the floor.
 */
class Level99RewardPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        private const val COINS_ITEM_ID = 995
        private const val REWARD_AMOUNT = 200_000_000 // 200 million coins
        private const val LEVEL_99 = 99
    }
    
    init {
        /**
         * Hook into skill level ups to detect when a player reaches level 99
         * Also shows the level up dialog for all level ups
         */
        setLevelUpLogic {
            val skill = player.attr[LEVEL_UP_SKILL_ID] ?: return@setLevelUpLogic
            val increment = player.attr[LEVEL_UP_INCREMENT] ?: return@setLevelUpLogic
            val newLevel = player.getSkills().getBaseLevel(skill)
            
            // Show the level up dialog
            player.queue {
                levelUpMessageBox(player, skill, increment)
            }
            
            // Check if the player just reached level 99
            if (newLevel == LEVEL_99 && increment > 0) {
                // Only reward once per skill (check if they were below 99 before)
                val oldLevel = newLevel - increment
                if (oldLevel < LEVEL_99) {
                    // Award reward after a small delay to ensure level up dialog is shown first
                    player.queue(TaskPriority.STRONG) {
                        wait(2)
                        awardLevel99Reward(player, skill)
                    }
                }
            }
        }
    }
    
    /**
     * Awards 200 million coins to the player for reaching level 99
     * Tries to add to inventory first, drops on floor if inventory is full
     */
    private fun awardLevel99Reward(player: Player, skill: Int) {
        val skillName = Skills.getSkillName(player.world, skill)
        
        // Try to add coins to inventory
        val addResult = player.inventory.add(item = COINS_ITEM_ID, amount = REWARD_AMOUNT)
        
        if (addResult.hasSucceeded()) {
            // All coins fit in inventory
            player.message("<col=00ff00>Congratulations! You've achieved level 99 in $skillName!")
            player.message("<col=00ff00>You have been awarded ${REWARD_AMOUNT.formatNumber()} coins!")
        } else {
            // Inventory is full, drop coins on the floor
            val coinsToAdd = addResult.completed
            val coinsToDrop = REWARD_AMOUNT - coinsToAdd
            
            // Add what we can to inventory
            if (coinsToAdd > 0) {
                player.message("<col=00ff00>Congratulations! You've achieved level 99 in $skillName!")
                player.message("<col=00ff00>You have been awarded ${coinsToAdd.formatNumber()} coins in your inventory!")
            }
            
            // Drop the rest on the floor
            if (coinsToDrop > 0) {
                val groundItem = GroundItem(
                    item = COINS_ITEM_ID,
                    amount = coinsToDrop,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                player.world.spawn(groundItem)
                
                if (coinsToAdd > 0) {
                    player.message("<col=ffff00>The remaining ${coinsToDrop.formatNumber()} coins were dropped on the floor!")
                } else {
                    player.message("<col=00ff00>Congratulations! You've achieved level 99 in $skillName!")
                    player.message("<col=ffff00>You have been awarded ${REWARD_AMOUNT.formatNumber()} coins, but your inventory is full!")
                    player.message("<col=ffff00>The coins have been dropped on the floor!")
                }
            }
        }
    }
}

