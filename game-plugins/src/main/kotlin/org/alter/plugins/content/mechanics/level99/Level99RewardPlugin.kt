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
import java.text.DecimalFormat

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
        private val NUMBER_FORMAT = DecimalFormat("#,###")
        private val REWARDED_SKILLS_KEY = AttributeKey<MutableSet<Int>>("level99_rewarded_skills")
    }
    
    init {
        /**
         * Hook into skill level ups to detect when a player reaches level 99
         * Also shows the level up dialog for all level ups
         */
        try {
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
                        // Check if we've already rewarded this skill
                        var rewardedSkills = player.attr[REWARDED_SKILLS_KEY]
                        if (rewardedSkills == null) {
                            rewardedSkills = mutableSetOf<Int>()
                            player.attr[REWARDED_SKILLS_KEY] = rewardedSkills
                        }
                        if (skill !in rewardedSkills) {
                            rewardedSkills.add(skill)
                            // Award reward after a small delay to ensure level up dialog is shown first
                            player.queue(TaskPriority.STRONG) {
                                wait(2)
                                awardLevel99Reward(player, skill)
                            }
                        }
                    }
                }
            }
        } catch (e: IllegalStateException) {
            // Another plugin already set level up logic, we need to hook into it differently
            // This shouldn't happen, but if it does, we'll need to use a different approach
            // Just silently fail - the plugin won't work but won't crash the server
        }
        
        /**
         * Command to check and award rewards for skills that are already 99
         * Usage: ::check99rewards
         */
        onCommand("check99rewards") {
            checkAndAwardExisting99s(player)
        }
    }
    
    /**
     * Checks all skills and awards rewards for any that are 99 but haven't been rewarded yet
     */
    private fun checkAndAwardExisting99s(player: Player) {
        var rewardedSkills = player.attr[REWARDED_SKILLS_KEY]
        if (rewardedSkills == null) {
            rewardedSkills = mutableSetOf<Int>()
            player.attr[REWARDED_SKILLS_KEY] = rewardedSkills
        }
        var awardedCount = 0
        
        for (skill in 0 until player.getSkills().maxSkills) {
            val level = player.getSkills().getBaseLevel(skill)
            if (level >= LEVEL_99 && skill !in rewardedSkills) {
                rewardedSkills.add(skill)
                awardLevel99Reward(player, skill)
                awardedCount++
            }
        }
        
        if (awardedCount == 0) {
            player.message("<col=ffff00>You have already received rewards for all your level 99 skills.")
        } else {
            player.message("<col=00ff00>Awarded rewards for $awardedCount skill(s) that were already level 99.")
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
            player.message("<col=00ff00>You have been awarded ${NUMBER_FORMAT.format(REWARD_AMOUNT)} coins!")
        } else {
            // Inventory is full, drop coins on the floor
            val coinsToAdd = addResult.completed
            val coinsToDrop = REWARD_AMOUNT - coinsToAdd
            
            // Add what we can to inventory
            if (coinsToAdd > 0) {
                player.message("<col=00ff00>Congratulations! You've achieved level 99 in $skillName!")
                player.message("<col=00ff00>You have been awarded ${NUMBER_FORMAT.format(coinsToAdd)} coins in your inventory!")
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
                    player.message("<col=ffff00>The remaining ${NUMBER_FORMAT.format(coinsToDrop)} coins were dropped on the floor!")
                } else {
                    player.message("<col=00ff00>Congratulations! You've achieved level 99 in $skillName!")
                    player.message("<col=ffff00>You have been awarded ${NUMBER_FORMAT.format(REWARD_AMOUNT)} coins, but your inventory is full!")
                    player.message("<col=ffff00>The coins have been dropped on the floor!")
                }
            }
        }
    }
}

