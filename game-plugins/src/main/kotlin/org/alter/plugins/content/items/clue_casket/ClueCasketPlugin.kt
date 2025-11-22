package org.alter.plugins.content.items.clue_casket

import dev.openrune.cache.CacheManager.getItem
import dev.openrune.cache.CacheManager.getItems
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
import org.alter.rscm.RSCM.asRSCM
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
        // CRITICAL: Register these items FIRST by direct item ID before any other registrations
        // This ensures we bind option 2 before any other plugins might bind it
        // We use forceRegisterCasket to bypass item definition checks since we know these work with option 2
        println("DEBUG: Registering reward items by ID FIRST: 19836, 20544, 28088, 30619")
        forceRegisterCasket(19836, ClueTier.MASTER) // reward_casket_master
        forceRegisterCasket(20544, ClueTier.HARD)   // reward_casket_hard
        forceRegisterCasket(28088, ClueTier.MEDIUM) // bounty_crate_tier_4
        forceRegisterCasket(30619, ClueTier.MEDIUM) // item 30619
        
        // Register handlers for all clue casket types (only once each)
        registerCasketHandler("item.casket_easy", ClueTier.EASY)
        registerCasketHandler("item.casket_medium", ClueTier.MEDIUM)
        registerCasketHandler("item.casket_hard", ClueTier.HARD)
        registerCasketHandler("item.casket_elite", ClueTier.ELITE)
        registerCasketHandler("item.reward_casket_master", ClueTier.MASTER)
        registerCasketHandler("item.reward_casket_beginner", ClueTier.BEGINNER)
        
        // Register additional reward items that spawn random items when clicked
        // Note: item.reward_casket_hard (20544) and item.bounty_crate_tier_4 (28088) are already 
        // registered by forceRegisterCasket above, skip to avoid duplicate registrations
        
        // Register loot keys by item ID only (avoid duplicate registration)
        // Note: These may already be bound by other plugins, which is fine
        if (!world.plugins.isItemBound(26651, 2)) {
            registerRewardItemByItemId(26651, ClueTier.MEDIUM) // loot_key
        }
        if (!world.plugins.isItemBound(26655, 2)) {
            registerRewardItemByItemId(26655, ClueTier.MEDIUM) // loot_key_26655
        }
        
        // Register all known casket variants by item ID
        registerCasketVariants()
        
        // Auto-detect and register any remaining casket variants by scanning item names
        autoRegisterCasketVariants()
    }

    /**
     * Force registers a casket handler for option 2 (and 1 as backup).
     * This bypasses item definition checks to ensure the handler is bound.
     */
    private fun forceRegisterCasket(itemId: Int, tier: ClueTier) {
        try {
            // Force bind option 2 (standard "Open" for these items)
            world.plugins.bindItem(itemId, 2) {
                openCasket(player, tier, null, itemId)
            }
            // Also bind option 1 just in case
            world.plugins.bindItem(itemId, 1) {
                openCasket(player, tier, null, itemId)
            }
            println("DEBUG: Forced registration for casket ID $itemId (tier=$tier)")
        } catch (e: Exception) {
            println("DEBUG: Failed to force register casket ID $itemId: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Registers known casket variants by item ID.
     * This ensures all commonly used casket variants are properly registered.
     */
    private fun registerCasketVariants() {
        // Medium casket variants
        val mediumCaskets = listOf(2802, 2804, 2806, 2810, 2808, 2812, 2814, 2816, 2818, 2820, 2822, 2824, 2826, 2828, 2830)
        mediumCaskets.forEach { itemId ->
            registerCasketByItemId(itemId, ClueTier.MEDIUM)
        }
        
        // Hard casket variants (including 2738 which was missing)
        val hardCaskets = listOf(
            2724, 2726, 2728, 2730, 2732, 2734, 2736, 2738, 2740, 2742, 2744, 2746, 2748,
            2775, 2777, 2779, 2781, 2784, 2787, 2789, 2791
        )
        hardCaskets.forEach { itemId ->
            registerCasketByItemId(itemId, ClueTier.HARD)
        }
        
        // Easy casket variants
        val easyCaskets = listOf(2714, 2715, 2717, 2718, 2720, 2721)
        easyCaskets.forEach { itemId ->
            registerCasketByItemId(itemId, ClueTier.EASY)
        }
        
        // Elite casket variants
        val eliteCaskets = listOf(12084, 12112, 12129, 12131, 12160)
        eliteCaskets.forEach { itemId ->
            registerCasketByItemId(itemId, ClueTier.ELITE)
        }
    }
    
    /**
     * Auto-detects and registers casket variants by scanning item definitions.
     * This catches any caskets that weren't explicitly listed above.
     */
    private fun autoRegisterCasketVariants() {
        try {
            val registeredIds = mutableSetOf<Int>()
            
            // Get all items from cache
            val allItems = getItems()
            
            for ((itemId, _) in allItems) {
                try {
                    // Skip if already bound (already registered)
                    if (world.plugins.isItemBound(itemId, 1) || world.plugins.isItemBound(itemId, 2)) {
                        continue
                    }
                    
                    val itemDef = getItem(itemId)
                    val itemName = itemDef.name.lowercase()
                    
                    // Skip if already registered in this pass or not a casket
                    if (registeredIds.contains(itemId) || !itemName.contains("casket")) {
                        continue
                    }
                    
                    // Determine tier from item name
                    val tier = when {
                        itemName.contains("beginner") -> ClueTier.BEGINNER
                        itemName.contains("easy") -> ClueTier.EASY
                        itemName.contains("medium") -> ClueTier.MEDIUM
                        itemName.contains("hard") -> ClueTier.HARD
                        itemName.contains("elite") -> ClueTier.ELITE
                        itemName.contains("master") -> ClueTier.MASTER
                        else -> null
                    }
                    
                    // Register if we determined a tier
                    if (tier != null) {
                        if (registerCasketByItemId(itemId, tier, silent = true)) {
                            registeredIds.add(itemId)
                        }
                    }
                } catch (e: Exception) {
                    // Skip items that cause errors
                    continue
                }
            }
        } catch (e: Exception) {
            println("Warning: Could not auto-register casket variants: ${e.message}")
        }
    }
    
    /**
     * Registers a casket handler by item ID.
     * @param itemId The item ID to register
     * @param tier The clue tier for this casket
     * @param silent If true, suppresses debug output
     * @return true if registration was successful, false otherwise
     */
    private fun registerCasketByItemId(itemId: Int, tier: ClueTier, silent: Boolean = false): Boolean {
        return try {
            val itemDef = getItem(itemId)
            val itemName = itemDef.name.lowercase()
            
            // Verify it's actually a casket
            if (!itemName.contains("casket")) {
                return false
            }
            
            var registered = false
            
            // Try option 2 first (most common)
            if (!world.plugins.isItemBound(itemId, 2)) {
                if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                    try {
                        world.plugins.bindItem(itemId, 2) {
                            openCasket(player, tier, null, itemId)
                        }
                        if (!silent) {
                            println("DEBUG: ✓ Registered casket ID $itemId (tier=$tier) with option 2")
                        }
                        registered = true
                    } catch (e: Exception) {
                        if (!silent) {
                            println("DEBUG: ✗ Failed to register option 2 for casket ID $itemId: ${e.message}")
                        }
                    }
                }
            }
            
            // Try option 1 as fallback
            if (!registered && !world.plugins.isItemBound(itemId, 1)) {
                if (itemDef.interfaceOptions.size >= 1 && itemDef.interfaceOptions[0] != null) {
                    try {
                        world.plugins.bindItem(itemId, 1) {
                            openCasket(player, tier, null, itemId)
                        }
                        if (!silent) {
                            println("DEBUG: ✓ Registered casket ID $itemId (tier=$tier) with option 1")
                        }
                        registered = true
                    } catch (e: Exception) {
                        if (!silent) {
                            println("DEBUG: ✗ Failed to register option 1 for casket ID $itemId: ${e.message}")
                        }
                    }
                }
            }
            
            // Try other options (3, 4) if still not registered
            if (!registered) {
                for (optionIndex in listOf(3, 4)) {
                    if (world.plugins.isItemBound(itemId, optionIndex)) {
                        continue
                    }
                    if (optionIndex <= itemDef.interfaceOptions.size && itemDef.interfaceOptions[optionIndex - 1] != null) {
                        try {
                            world.plugins.bindItem(itemId, optionIndex) {
                                openCasket(player, tier, null, itemId)
                            }
                            if (!silent) {
                                println("DEBUG: ✓ Registered casket ID $itemId (tier=$tier) with option $optionIndex")
                            }
                            registered = true
                            break
                        } catch (e: Exception) {
                            // Continue to next option
                        }
                    }
                }
            }
            
            registered
        } catch (e: Exception) {
            if (!silent) {
                println("DEBUG: Failed to register casket ID $itemId: ${e.message}")
            }
            false
        }
    }

    /**
     * Registers a reward item handler that spawns random items when clicked.
     * This is used for items like loot keys, bounty crates, etc.
     */
    private fun registerRewardItem(itemName: String, tier: ClueTier) {
        try {
            val itemId = getRSCM(itemName)
            registerRewardItemByItemId(itemId, tier)
        } catch (e: Exception) {
            println("Warning: Could not get RSCM for $itemName: ${e.message}")
        }
    }
    
    /**
     * Registers a reward item handler by direct item ID.
     * Uses bindItem directly to ensure option 2 is registered for inventory clicks.
     */
    private fun registerRewardItemByItemId(itemId: Int, tier: ClueTier) {
        try {
            val itemDef = getItem(itemId)
            
            // Check if any available option is already bound before doing anything else
            // If all options are bound, another plugin is handling it, so skip silently
            var hasAvailableOption = false
            for (optionIndex in 1..4) {
                if (optionIndex <= itemDef.interfaceOptions.size && 
                    itemDef.interfaceOptions[optionIndex - 1] != null &&
                    !world.plugins.isItemBound(itemId, optionIndex)) {
                    hasAvailableOption = true
                    break
                }
            }
            
            if (!hasAvailableOption) {
                // All available options are already bound, skip silently
                return
            }
            
            println("DEBUG: registerRewardItemByItemId - Item ID $itemId, name='${itemDef.name}', interfaceOptions.size=${itemDef.interfaceOptions.size}")
            if (itemDef.interfaceOptions.size > 0) {
                println("DEBUG:   Option 1: ${itemDef.interfaceOptions[0]}")
                if (itemDef.interfaceOptions.size > 1) {
                    println("DEBUG:   Option 2: ${itemDef.interfaceOptions[1]}")
                }
                if (itemDef.interfaceOptions.size > 2) {
                    println("DEBUG:   Option 3: ${itemDef.interfaceOptions[2]}")
                }
            }
            
            var registered = false
            
            // CRITICAL: Always try option 2 first (this is what inventory clicks send)
            if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                try {
                    // Try using bindItem directly - this is what executeItem uses
                    world.plugins.bindItem(itemId, 2) {
                        println("DEBUG: Handler triggered for item ID $itemId, option 2")
                        openCasket(player, tier, null, itemId)
                    }
                    println("DEBUG: ✓ Registered reward item ID $itemId (tier=$tier) with option 2 using bindItem")
                    registered = true
                } catch (e: IllegalStateException) {
                    // Item is already bound - skip silently
                    println("DEBUG: ⚠ Option 2 already bound for item ID $itemId by another plugin, skipping")
                } catch (e: Exception) {
                    println("DEBUG: ✗ Failed to register option 2 for item ID $itemId: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                println("DEBUG: ⚠ Item ID $itemId does not have option 2 (interfaceOptions.size=${itemDef.interfaceOptions.size})")
            }
            
            // Try option 3 (secondary click) if option 2 failed
            if (!registered && itemDef.interfaceOptions.size >= 3 && itemDef.interfaceOptions[2] != null) {
                if (!world.plugins.isItemBound(itemId, 3)) {
                    try {
                        world.plugins.bindItem(itemId, 3) {
                            openCasket(player, tier, null, itemId)
                        }
                        println("DEBUG: ✓ Registered reward item ID $itemId (tier=$tier) with option 3 using bindItem")
                        registered = true
                    } catch (e: IllegalStateException) {
                        // Skip silently if already bound
                    } catch (e: Exception) {
                        println("DEBUG: ✗ Failed to register option 3 for item ID $itemId: ${e.message}")
                    }
                }
            }
            
            // Try option 1 as fallback (but check if it's already bound first)
            if (!registered && itemDef.interfaceOptions.size >= 1 && itemDef.interfaceOptions[0] != null) {
                if (!world.plugins.isItemBound(itemId, 1)) {
                    try {
                        world.plugins.bindItem(itemId, 1) {
                            openCasket(player, tier, null, itemId)
                        }
                        println("DEBUG: ✓ Registered reward item ID $itemId (tier=$tier) with option 1 using bindItem")
                        registered = true
                    } catch (e: IllegalStateException) {
                        // Skip silently if already bound
                    } catch (e: Exception) {
                        println("DEBUG: ✗ Failed to register option 1 for item ID $itemId: ${e.message}")
                    }
                }
            }
            
            // Try option 4 if still not registered
            if (!registered && itemDef.interfaceOptions.size >= 4 && itemDef.interfaceOptions[3] != null) {
                if (!world.plugins.isItemBound(itemId, 4)) {
                    try {
                        world.plugins.bindItem(itemId, 4) {
                            openCasket(player, tier, null, itemId)
                        }
                        println("DEBUG: ✓ Registered reward item ID $itemId (tier=$tier) with option 4 using bindItem")
                        registered = true
                    } catch (e: IllegalStateException) {
                        // Skip silently if already bound
                    } catch (e: Exception) {
                        println("DEBUG: ✗ Failed to register option 4 for item ID $itemId: ${e.message}")
                    }
                }
            }
            
            // Only show warning if we actually tried to register but failed
            // If all options were already bound, we return early and don't show this message
            if (!registered) {
                // This should rarely happen now since we check early, but keep for debugging
                println("DEBUG: ⚠ Could not register reward item ID $itemId - all available options are bound or unavailable")
            }
        } catch (e: Exception) {
            println("Warning: Could not register reward item handler for ID $itemId: ${e.message}")
            e.printStackTrace()
        }
    }
    
    
    private fun registerCasketHandler(itemName: String, tier: ClueTier) {
        try {
            val itemId = getRSCM(itemName)
            val itemDef = getItem(itemId)
            
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
                    player.message("You don't have that item.")
                    return
                }
                val item = player.inventory[slot]
                if (item == null) {
                    println("DEBUG: No item found at slot $slot")
                    player.message("You don't have that item.")
                    return
                }
                item.id
            })
            
            // Find the item in inventory
            val itemSlot = if (slot != -1 && slot >= 0 && slot < player.inventory.capacity) {
                val item = player.inventory[slot]
                if (item != null && item.id == targetItemId) slot else player.inventory.getItemIndex(targetItemId, false)
            } else {
                player.inventory.getItemIndex(targetItemId, false)
            }
            
            if (itemSlot == -1) {
                player.message("You don't have that item.")
                return
            }
            
            // Remove the item
            val removeResult = player.inventory.remove(item = targetItemId, amount = 1, beginSlot = itemSlot)
            if (!removeResult.hasSucceeded()) {
                println("DEBUG: Failed to remove item from inventory")
                player.message("Failed to remove item.")
                return
            }
            println("DEBUG: Successfully removed item ID $targetItemId")

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

                // Get item name for message
                val itemDef = getItem(targetItemId)
                val itemName = itemDef.name.lowercase()
                val message = when {
                    itemName.contains("casket") -> "You open the ${tier.name.lowercase()} clue casket and find some treasure!"
                    itemName.contains("crate") -> "You open the crate and find some treasure!"
                    itemName.contains("loot key") -> "You use the loot key and receive some treasure!"
                    else -> "You find some treasure!"
                }
                player.message(message)
            }
        } finally {
            player.unlock()
        }
    }

    private fun generateRewards(tier: ClueTier): List<ClueReward> {
        val rewards = mutableListOf<ClueReward>()
        
        // Generate truly random rewards based on tier: 3, 5, 7, or 9 items
        val rewardCount = when (tier) {
            ClueTier.BEGINNER -> 3
            ClueTier.EASY -> 5
            ClueTier.MEDIUM -> 7
            ClueTier.HARD -> 9
            ClueTier.ELITE -> 9
            ClueTier.MASTER -> 9
        }

        // Get all items from cache and filter valid ones
        val allItems = getItems()
        val excludedNames = setOf("null", "empty", "placeholder", "unobtainable", "test", "spawn", "debug", "admin", "moderator", "developer")
        
        val validItemIds = allItems.keys.filter { itemId ->
            try {
                val itemDef = getItem(itemId)
                
                // Skip placeholder items and notes
                val isPlaceholder = itemDef.placeholderTemplate > 0 && itemDef.placeholderLink > 0
                val isNote = itemDef.noteTemplateId > 0
                if (isPlaceholder || isNote) {
                    return@filter false
                }
                
                // Check item name
                val itemName = itemDef.name.trim()
                
                // Skip if name is null, empty, or too short (likely invalid)
                if (itemName.isEmpty() || itemName.length < 2) {
                    return@filter false
                }
                
                // Skip items with excluded names (case-insensitive)
                val itemNameLower = itemName.lowercase()
                if (excludedNames.any { itemNameLower.contains(it) }) {
                    return@filter false
                }
                
                // Skip if name is literally "null" (string)
                if (itemNameLower == "null") {
                    return@filter false
                }
                
                true
            } catch (e: Exception) {
                false
            }
        }

        println("DEBUG: generateRewards - Found ${validItemIds.size} valid items out of ${allItems.size} total items")

        if (validItemIds.isEmpty()) {
            println("DEBUG: generateRewards - No valid items found, using coins fallback")
            // Fallback to coins if no valid items
            val coinsId = getRSCM("item.coins_995")
            return List(rewardCount) { ClueReward(coinsId, Random.nextInt(100, 1000)) }
        }

        // Generate random items - try to avoid duplicates but allow if needed
        val usedItemIds = mutableSetOf<Int>()
        var attempts = 0
        val maxAttempts = rewardCount * 50 // Increased max attempts
        
        while (rewards.size < rewardCount && attempts < maxAttempts) {
            val itemId = validItemIds[Random.nextInt(validItemIds.size)]
            
            // Skip if we already used this item and we have enough unique items available
            if (usedItemIds.contains(itemId) && usedItemIds.size < validItemIds.size && validItemIds.size >= rewardCount) {
                attempts++
                continue
            }
            
            usedItemIds.add(itemId)
            
            try {
                val itemDef = getItem(itemId)
                val itemName = itemDef.name.trim()
                
                // Double-check the item is still valid (name might be null/empty)
                if (itemName.isEmpty() || itemName.lowercase() == "null" || itemName.length < 2) {
                    attempts++
                    usedItemIds.remove(itemId)
                    continue
                }
                
                val itemNameLower = itemName.lowercase()
                
                // Generate random amount based on item type
                val amount = when {
                    // Coins can have a lot
                    itemId == getRSCM("item.coins_995") -> Random.nextInt(100, 100000)
                    // Stackable items can have more
                    itemDef.stackable -> Random.nextInt(1, 1000)
                    // Runes can stack
                    itemNameLower.contains("rune") -> Random.nextInt(10, 500)
                    // Herbs can stack
                    itemNameLower.contains("herb") || itemNameLower.contains("leaf") || itemNameLower.contains("weed") -> Random.nextInt(1, 50)
                    // Potions can stack
                    itemNameLower.contains("potion") -> Random.nextInt(1, 10)
                    // Gems can stack
                    itemNameLower.contains("gem") || itemNameLower.contains("uncut") || itemNameLower.contains("sapphire") || itemNameLower.contains("emerald") || itemNameLower.contains("ruby") || itemNameLower.contains("diamond") -> Random.nextInt(1, 20)
                    // Everything else is 1
                    else -> 1
                }
                
                println("DEBUG: generateRewards - Adding item ID $itemId (${itemDef.name}) x$amount")
                rewards.add(ClueReward(itemId, amount))
                attempts = 0 // Reset attempts on success
            } catch (e: Exception) {
                println("DEBUG: generateRewards - Error processing item $itemId: ${e.message}")
                // Skip items that cause errors
                attempts++
                usedItemIds.remove(itemId) // Remove from used set so we can try again
                continue
            }
        }

        println("DEBUG: generateRewards - Generated ${rewards.size} rewards, need $rewardCount")

        // If we didn't get enough rewards, fill with random items (not just coins)
        while (rewards.size < rewardCount) {
            val itemId = validItemIds[Random.nextInt(validItemIds.size)]
            try {
                val itemDef = getItem(itemId)
                val itemName = itemDef.name.trim()
                
                // Verify item is still valid before adding
                if (itemName.isEmpty() || itemName.lowercase() == "null" || itemName.length < 2) {
                    continue
                }
                
                val amount = if (itemDef.stackable) Random.nextInt(1, 100) else 1
                println("DEBUG: generateRewards - Filling with item ID $itemId (${itemDef.name}) x$amount")
                rewards.add(ClueReward(itemId, amount))
            } catch (e: Exception) {
                // If we can't get the item, use coins as last resort
                val coinsId = getRSCM("item.coins_995")
                rewards.add(ClueReward(coinsId, Random.nextInt(100, 1000)))
            }
        }

        println("DEBUG: generateRewards - Final reward count: ${rewards.size}")
        return rewards
    }


    private enum class ClueTier {
        BEGINNER,
        EASY,
        MEDIUM,
        HARD,
        ELITE,
        MASTER
    }

    private data class ClueReward(
        val itemId: Int,
        val amount: Int
    )
}

