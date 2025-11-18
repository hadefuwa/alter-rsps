package org.alter.plugins.content.items.clue_casket

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Plugin for handling clue casket opening and rewards.
 * Supports all clue casket tiers: beginner, easy, medium, hard, elite, and master.
 */
class ClueCasketPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    // Minimum free inventory slots required to open a clue casket
    private val MIN_FREE_SLOTS = 10

    init {
        // Register handlers for all clue casket types (only once each)
        registerCasketHandler("item.casket_easy", ClueTier.EASY)
        registerCasketHandler("item.casket_medium", ClueTier.MEDIUM)
        registerCasketHandler("item.casket_hard", ClueTier.HARD)
        registerCasketHandler("item.casket_elite", ClueTier.ELITE)
        registerCasketHandler("item.reward_casket_master", ClueTier.MASTER)
        registerCasketHandler("item.reward_casket_beginner", ClueTier.BEGINNER)
        
        // Also register variant caskets by ID (medium casket variants)
        // Register 2804, 2806, 2810 directly in init block where r is accessible
        listOf(2804, 2806, 2810).forEach { itemId ->
            try {
                val itemDef = getItem(itemId)
                val itemName = itemDef.name.lowercase()
                
                // Only register if it's actually a casket
                if (itemName.contains("casket") && !world.plugins.isItemBound(itemId, 2)) {
                    if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                        try {
                            r.bindItem(itemId, 2) {
                                println("DEBUG: Casket handler triggered! itemId=$itemId, optionIndex=2, tier=MEDIUM")
                                openCasket(player, ClueTier.MEDIUM, null, itemId)
                            }
                            println("DEBUG: ✓ Successfully registered option 2 for casket ID $itemId (tier: MEDIUM)")
                        } catch (e: Exception) {
                            println("DEBUG: ✗ Failed to register option 2 for casket ID $itemId: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Item might not exist, that's okay
            }
        }
    }

    private fun registerCasketHandler(itemName: String, tier: ClueTier) {
        try {
            val itemId = getRSCM(itemName)
            val itemDef = getItem(itemId)
            
            // Print available options for debugging
            val availableOptions = itemDef.interfaceOptions.filterNotNull().filter { it.isNotBlank() }
            println("DEBUG: Registering casket handler for $itemName (ID: $itemId) with options: $availableOptions")
            
            // Priority: Register option 2 first since that's what the client sends
            // Check if option 2 is already bound
            if (!world.plugins.isItemBound(itemId, 2)) {
                // Check if option 2 exists for this item
                if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                    try {
                        onItemOption(itemName, 2) {
                            println("DEBUG: Handler triggered! itemName=$itemName, optionIndex=2, itemId=$itemId")
                            openCasket(player, tier, itemName)
                        }
                        println("DEBUG: ✓ Successfully registered option 2 for $itemName (ID: $itemId)")
                        return
                    } catch (e: Exception) {
                        println("DEBUG: ✗ Failed to register option 2 for $itemName: ${e.message}")
                    }
                }
            } else {
                println("DEBUG: Option 2 already bound for $itemName (ID: $itemId)")
            }
            
            // Try to register "open" option if it exists and not already bound
            if (itemHasInventoryOption(itemName, "open")) {
                val openOptionIndex = itemDef.interfaceOptions.indexOfFirst { it?.lowercase() == "open" }
                if (openOptionIndex != -1 && !world.plugins.isItemBound(itemId, openOptionIndex + 1)) {
                    try {
                        onItemOption(itemName, "open") {
                            openCasket(player, tier, itemName)
                        }
                        println("DEBUG: Registered 'open' option for $itemName")
                        return
                    } catch (e: Exception) {
                        println("DEBUG: Failed to register 'open' option: ${e.message}")
                    }
                }
            }
            
            // Try common option names
            val commonOptions = listOf("use", "activate", "inspect", "check")
            for (optionName in commonOptions) {
                if (itemHasInventoryOption(itemName, optionName)) {
                    val optionIndex = itemDef.interfaceOptions.indexOfFirst { it?.lowercase() == optionName.lowercase() }
                    if (optionIndex != -1 && !world.plugins.isItemBound(itemId, optionIndex + 1)) {
                        try {
                            onItemOption(itemName, optionName) {
                                openCasket(player, tier, itemName)
                            }
                            println("DEBUG: Registered '$optionName' option for $itemName")
                            return
                        } catch (e: Exception) {
                            println("DEBUG: Failed to register '$optionName' option: ${e.message}")
                        }
                    }
                }
            }
            
            // Fall back to registering option indices 1, 3, and 4 (skip 2 since we already tried it)
            val fallbackOptions = listOf(1, 3, 4)
            for (optionIndex in fallbackOptions) {
                if (world.plugins.isItemBound(itemId, optionIndex)) {
                    println("DEBUG: Option index $optionIndex already bound for $itemName (ID: $itemId), skipping")
                    continue
                }
                
                // Check if this option index actually exists for this item
                if (optionIndex <= itemDef.interfaceOptions.size && itemDef.interfaceOptions[optionIndex - 1] != null) {
                    try {
                        onItemOption(itemName, optionIndex) {
                            println("DEBUG: Handler triggered! itemName=$itemName, optionIndex=$optionIndex, itemId=$itemId")
                            openCasket(player, tier, itemName)
                        }
                        println("DEBUG: ✓ Successfully registered option index $optionIndex for $itemName (ID: $itemId)")
                        return // Only register one option
                    } catch (e: Exception) {
                        println("DEBUG: ✗ Failed to register option index $optionIndex for $itemName: ${e.message}")
                        // Option index doesn't exist or already bound, continue to next
                    }
                }
            }
            
            // Verify registration worked
            try {
                val registered = world.plugins.isItemBound(itemId, 1) || world.plugins.isItemBound(itemId, 2)
                println("DEBUG: Verification - isItemBound($itemId, 1 or 2) = $registered")
            } catch (e: Exception) {
                println("DEBUG: Could not verify registration: ${e.message}")
            }
        } catch (e: Exception) {
            println("Warning: Could not register handler for $itemName: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun openCasket(player: Player, tier: ClueTier, itemName: String?, itemId: Int? = null) {
        println("DEBUG: openCasket called for tier=$tier, itemName=$itemName, itemId=$itemId")
        player.lock()
        
        try {
            // Check if player has enough free inventory slots
            val freeSlots = player.inventory.freeSlotCount
            if (freeSlots < MIN_FREE_SLOTS) {
                player.message("You need at least $MIN_FREE_SLOTS free inventory slots to open this casket.")
                return
            }

            // Get the slot of the item being interacted with
            val slot = player.attr[INTERACTING_ITEM_SLOT] ?: -1
            println("DEBUG: Interacting slot = $slot")
            
            val targetItemId = itemId ?: (itemName?.let { getRSCM(it) } ?: run {
                if (slot == -1 || slot < 0 || slot >= player.inventory.capacity) {
                    player.message("You don't have that casket.")
                    return
                }
                val item = player.inventory[slot]
                if (item == null) {
                    println("DEBUG: No item found at slot $slot")
                    player.message("You don't have that casket.")
                    return
                }
                item.id
            })
            
            // Find the casket in inventory
            val casketSlot = if (slot != -1 && slot >= 0 && slot < player.inventory.capacity) {
                val item = player.inventory[slot]
                if (item != null && item.id == targetItemId) slot else player.inventory.getItemIndex(targetItemId, false)
            } else {
                player.inventory.getItemIndex(targetItemId, false)
            }
            
            if (casketSlot == -1) {
                player.message("You don't have that casket.")
                return
            }
            
            // Remove the casket
            val removeResult = player.inventory.remove(item = targetItemId, amount = 1, beginSlot = casketSlot)
            if (!removeResult.hasSucceeded()) {
                println("DEBUG: Failed to remove casket from inventory")
                player.message("Failed to remove casket.")
                return
            }
            println("DEBUG: Successfully removed casket ID $targetItemId")

            // Play sound effect
            player.playSound(Sound.CASKET_OPEN)

            // Generate and give rewards
            val rewards = generateRewards(tier)

            player.queue {
                for (reward in rewards) {
                    // Try to add to inventory first
                    val transaction = player.inventory.add(item = reward.itemId, amount = reward.amount)
                    if (!transaction.hasSucceeded()) {
                        // Inventory full, drop on ground
                        val groundItem = GroundItem(
                            item = reward.itemId,
                            amount = reward.amount,
                            tile = player.tile,
                            owner = player
                        )
                        groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
                        groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
                        groundItem.ownerShipType = 1
                        world.spawn(groundItem)
                    }
                }

                player.message("You open the ${tier.name.lowercase()} clue casket and find some treasure!")
            }
        } finally {
            player.unlock()
        }
    }

    private fun generateRewards(tier: ClueTier): List<ClueReward> {
        val rewards = mutableListOf<ClueReward>()
        
        // Generate 3-6 random rewards based on tier
        val rewardCount = when (tier) {
            ClueTier.BEGINNER -> Random.nextInt(2, 4) // 2-3 rewards
            ClueTier.EASY -> Random.nextInt(3, 5) // 3-4 rewards
            ClueTier.MEDIUM -> Random.nextInt(4, 6) // 4-5 rewards
            ClueTier.HARD -> Random.nextInt(5, 7) // 5-6 rewards
            ClueTier.ELITE -> Random.nextInt(6, 8) // 6-7 rewards
            ClueTier.MASTER -> Random.nextInt(7, 10) // 7-9 rewards
        }

        for (i in 0 until rewardCount) {
            val reward = rollReward(tier)
            if (reward != null) {
                rewards.add(reward)
            }
        }

        return rewards
    }

    private fun rollReward(tier: ClueTier): ClueReward? {
        // Get reward table for this tier
        val rewardTable = getRewardTable(tier)
        
        // Roll for a reward
        val totalWeight = rewardTable.sumOf { it.weight }
        if (totalWeight == 0) return null
        
        var roll = Random.nextInt(totalWeight)
        for (entry in rewardTable) {
            roll -= entry.weight
            if (roll < 0) {
                val amount = if (entry.minAmount == entry.maxAmount) {
                    entry.minAmount
                } else {
                    Random.nextInt(entry.minAmount, entry.maxAmount + 1)
                }
                return ClueReward(entry.itemId, amount)
            }
        }
        
        // Fallback: return first reward if roll failed
        return if (rewardTable.isNotEmpty()) {
            val entry = rewardTable[0]
            ClueReward(entry.itemId, entry.minAmount)
        } else {
            null
        }
    }

    private fun getRewardTable(tier: ClueTier): List<RewardEntry> {
        return when (tier) {
            ClueTier.BEGINNER -> getBeginnerRewards()
            ClueTier.EASY -> getEasyRewards()
            ClueTier.MEDIUM -> getMediumRewards()
            ClueTier.HARD -> getHardRewards()
            ClueTier.ELITE -> getEliteRewards()
            ClueTier.MASTER -> getMasterRewards()
        }
    }

    private fun getBeginnerRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 100, 500, 30)
            addSafe("item.iron_full_helm", 1, 1, 20)
            addSafe("item.iron_platebody", 1, 1, 20)
            addSafe("item.iron_platelegs", 1, 1, 20)
            addSafe("item.iron_boots", 1, 1, 20)
            addSafe("item.iron_gloves", 1, 1, 20)
            addSafe("item.leather_boots", 1, 1, 15)
            addSafe("item.leather_gloves", 1, 1, 15)
            addSafe("item.leather_vambraces", 1, 1, 15)
            addSafe("item.steel_longsword", 1, 1, 10)
            addSafe("item.steel_scimitar", 1, 1, 10)
            addSafe("item.steel_dagger", 1, 1, 10)
        }
    }

    private fun getEasyRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 500, 2000, 25)
            addSafe("item.steel_full_helm", 1, 1, 15)
            addSafe("item.steel_platebody", 1, 1, 15)
            addSafe("item.steel_platelegs", 1, 1, 15)
            addSafe("item.steel_boots", 1, 1, 15)
            addSafe("item.steel_gloves", 1, 1, 15)
            addSafe("item.mithril_longsword", 1, 1, 10)
            addSafe("item.mithril_scimitar", 1, 1, 10)
            addSafe("item.mithril_dagger", 1, 1, 10)
            addSafe("item.black_full_helm", 1, 1, 8)
            addSafe("item.black_platebody", 1, 1, 8)
            addSafe("item.black_platelegs", 1, 1, 8)
        }
    }

    private fun getMediumRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 1000, 5000, 20)
            addSafe("item.mithril_full_helm", 1, 1, 12)
            addSafe("item.mithril_platebody", 1, 1, 12)
            addSafe("item.mithril_platelegs", 1, 1, 12)
            addSafe("item.mithril_boots", 1, 1, 12)
            addSafe("item.mithril_gloves", 1, 1, 12)
            addSafe("item.adamant_longsword", 1, 1, 8)
            addSafe("item.adamant_scimitar", 1, 1, 8)
            addSafe("item.adamant_dagger", 1, 1, 8)
            addSafe("item.rune_longsword", 1, 1, 5)
            addSafe("item.rune_scimitar", 1, 1, 5)
            addSafe("item.rune_dagger", 1, 1, 5)
        }
    }

    private fun getHardRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 5000, 15000, 15)
            addSafe("item.adamant_full_helm", 1, 1, 10)
            addSafe("item.adamant_platebody", 1, 1, 10)
            addSafe("item.adamant_platelegs", 1, 1, 10)
            addSafe("item.adamant_boots", 1, 1, 10)
            addSafe("item.adamant_gloves", 1, 1, 10)
            addSafe("item.rune_full_helm", 1, 1, 8)
            addSafe("item.rune_platebody", 1, 1, 8)
            addSafe("item.rune_platelegs", 1, 1, 8)
            addSafe("item.rune_boots", 1, 1, 8)
            addSafe("item.rune_gloves", 1, 1, 8)
            addSafe("item.dragon_longsword", 1, 1, 3)
            addSafe("item.dragon_scimitar", 1, 1, 3)
            addSafe("item.dragon_dagger", 1, 1, 3)
        }
    }

    private fun getEliteRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 15000, 50000, 12)
            addSafe("item.rune_full_helm", 1, 1, 8)
            addSafe("item.rune_platebody", 1, 1, 8)
            addSafe("item.rune_platelegs", 1, 1, 8)
            addSafe("item.rune_boots", 1, 1, 8)
            addSafe("item.rune_gloves", 1, 1, 8)
            addSafe("item.dragon_full_helm", 1, 1, 5)
            addSafe("item.dragon_platebody", 1, 1, 5)
            addSafe("item.dragon_platelegs", 1, 1, 5)
            addSafe("item.dragon_boots", 1, 1, 5)
            addSafe("item.dragon_gloves", 1, 1, 5)
            addSafe("item.dragon_longsword", 1, 1, 4)
            addSafe("item.dragon_scimitar", 1, 1, 4)
            addSafe("item.dragon_dagger", 1, 1, 4)
        }
    }

    private fun getMasterRewards(): List<RewardEntry> {
        return buildRewardList {
            addSafe("item.coins", 50000, 200000, 10)
            addSafe("item.dragon_full_helm", 1, 1, 8)
            addSafe("item.dragon_platebody", 1, 1, 8)
            addSafe("item.dragon_platelegs", 1, 1, 8)
            addSafe("item.dragon_boots", 1, 1, 8)
            addSafe("item.dragon_gloves", 1, 1, 8)
            addSafe("item.dragon_longsword", 1, 1, 6)
            addSafe("item.dragon_scimitar", 1, 1, 6)
            addSafe("item.dragon_dagger", 1, 1, 6)
            addSafe("item.abyssal_whip", 1, 1, 3)
            addSafe("item.dragon_2h_sword", 1, 1, 2)
            addSafe("item.dragon_battleaxe", 1, 1, 2)
        }
    }
    
    // Helper function to safely build reward lists, skipping items that don't exist
    private fun buildRewardList(builder: MutableList<RewardEntry>.() -> Unit): List<RewardEntry> {
        val list = mutableListOf<RewardEntry>()
        list.builder()
        return list
    }
    
    // Helper function to safely add reward entries, falling back to coins if item doesn't exist
    private fun MutableList<RewardEntry>.addSafe(itemName: String, minAmount: Int, maxAmount: Int, weight: Int) {
        try {
            val itemId = getRSCM(itemName)
            // Verify the item exists by getting its definition
            getItem(itemId)
            add(RewardEntry(itemId, minAmount, maxAmount, weight))
        } catch (e: Exception) {
            // If item doesn't exist, add coins instead (coins should always exist)
            try {
                val coinsId = getRSCM("item.coins")
                // Increase coin amount slightly to compensate for missing item
                val coinMin = minAmount * 2
                val coinMax = maxAmount * 2
                add(RewardEntry(coinsId, coinMin, coinMax, weight))
            } catch (e2: Exception) {
                // If even coins don't exist, skip this reward (shouldn't happen)
                println("Warning: Could not add reward $itemName and coins fallback also failed")
            }
        }
    }

    private enum class ClueTier {
        BEGINNER,
        EASY,
        MEDIUM,
        HARD,
        ELITE,
        MASTER
    }

    private data class RewardEntry(
        val itemId: Int,
        val minAmount: Int,
        val maxAmount: Int,
        val weight: Int
    )

    private data class ClueReward(
        val itemId: Int,
        val amount: Int
    )
}

