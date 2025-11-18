package org.alter.plugins.content.death

import dev.openrune.cache.CacheManager.getItem
import dev.openrune.cache.CacheManager.itemSize
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.KILLER_ATTR
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimeConstants
import org.alter.game.model.weightedTableBuilder.roll
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference
import kotlin.random.Random

/**
 * Plugin to handle NPC loot drops when they die.
 * This plugin fixes the issue where NPCs don't drop anything upon death.
 * 
 * @author GitHub Copilot
 */
class NpcLootDropPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    /**
     * Cached list of valid item IDs from the entire game item table.
     * This is built once when the plugin initializes to avoid rebuilding it on every NPC death.
     */
    private val validItemIds: List<Int> by lazy {
        buildValidItemList()
    }

    init {
        println("NpcLootDropPlugin: Plugin initialized")
        
        // Pre-build the valid item list (lazy initialization will trigger on first use)
        println("NpcLootDropPlugin: Building valid item list...")
        val itemCount = validItemIds.size
        println("NpcLootDropPlugin: Found $itemCount valid items in game item table")
        
        // Register a handler for ANY NPC death to handle loot drops
        onAnyNpcDeath {
            println("NpcLootDropPlugin: onAnyNpcDeath triggered")
            val npc = ctx as Npc
            
            // Skip NPCs that use shared loot system (handled by SharedLootDropPlugin)
            // Check if multiple players dealt damage - if so, this NPC likely uses shared loot
            val playersWhoDamaged = mutableListOf<Player>()
            npc.world.players.forEach { player ->
                if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                    playersWhoDamaged.add(player)
                }
            }
            
            // If multiple players dealt damage, this is likely a shared loot NPC - skip default handler
            // SharedLootDropPlugin will handle it instead
            if (playersWhoDamaged.size > 1) {
                println("NpcLootDropPlugin: Skipping NPC ${npc.id} (${npc.def.name}) - multiple players dealt damage, likely uses shared loot")
                return@onAnyNpcDeath
            }
            
            // Also check if this is Crazy Archaeologist specifically (known shared loot NPC)
            try {
                val crazyArchId = getRSCM("npc.crazy_archaeologist")
                if (npc.id == crazyArchId) {
                    println("NpcLootDropPlugin: Skipping Crazy Archaeologist - handled by SharedLootDropPlugin")
                    return@onAnyNpcDeath
                }
            } catch (e: Exception) {
                // If we can't find the ID, continue with normal handling
            }
            
            handleNpcLootDrop(npc)
        }
    }
    
    /**
     * Converts a clue scroll item ID to its corresponding clue casket item ID.
     * Returns the original item ID if it's not a clue scroll.
     * 
     * This function specifically targets clue scrolls (easy, medium, hard, elite, master, beginner)
     * and converts them to their corresponding clue caskets.
     * 
     * @param itemId The item ID to check and potentially convert
     * @return The clue casket item ID if the input was a clue scroll, otherwise the original item ID
     */
    private fun convertClueScrollToCasket(itemId: Int): Int {
        try {
            val itemDef = getItem(itemId)
            val itemName = itemDef.name.lowercase()
            
            // Check if this is a clue scroll item
            // We check for specific patterns to identify clue scrolls
            if (itemName.contains("clue") && itemName.contains("scroll") && (
                itemName.contains("easy") || 
                itemName.contains("medium") || 
                itemName.contains("hard") || 
                itemName.contains("elite") || 
                itemName.contains("master") ||
                itemName.contains("beginner")
            )) {
                // Try to find the corresponding clue casket
                val casketName = when {
                    itemName.contains("beginner") -> "item.casket_easy"
                    itemName.contains("easy") -> "item.casket_easy"
                    itemName.contains("medium") -> "item.casket_medium"
                    itemName.contains("hard") -> "item.casket_hard"
                    itemName.contains("elite") -> "item.casket_elite"
                    itemName.contains("master") -> "item.casket_master"
                    else -> null
                }
                
                if (casketName != null) {
                    try {
                        val casketId = getRSCM(casketName)
                        println("DEBUG: Converted clue scroll ${itemDef.name} (ID: $itemId) to clue casket (ID: $casketId)")
                        return casketId
                    } catch (e: Exception) {
                        println("DEBUG: Failed to convert clue scroll ${itemDef.name} (ID: $itemId) to clue casket: ${e.message}")
                        // Fall through to return original item ID
                    }
                } else {
                    println("DEBUG: Clue scroll ${itemDef.name} (ID: $itemId) matched clue scroll pattern but no casket mapping found")
                }
            }
        } catch (e: Exception) {
            // If we can't get item definition, just return original ID
            println("DEBUG: Error checking if item $itemId is a clue scroll: ${e.message}")
        }
        
        // Not a clue scroll or conversion failed, return original item ID
        return itemId
    }
    
    /**
     * Builds a list of valid item IDs from the entire game item table.
     * Filters out placeholders, null names, and empty names.
     */
    private fun buildValidItemList(): List<Int> {
        val validItems = mutableListOf<Int>()
        
        for (itemId in 0 until itemSize()) {
            try {
                val def = getItem(itemId)
                // Filter out invalid items: placeholders, null names, and empty names
                if (!def.isPlaceholder && def.name.isNotBlank() && def.name.lowercase() != "null") {
                    validItems.add(itemId)
                }
            } catch (e: Exception) {
                // Skip items that can't be loaded
                continue
            }
        }
        
        return validItems.toList()
    }

    /**
     * Handles the loot drop logic for an NPC that has died.
     */
    private fun handleNpcLootDrop(npc: Npc) {
        println("DEBUG: NPC ${npc.id} (${npc.def.name}) died")
        
        // Get the killer (player who dealt the most damage)
        val killer = npc.attr[KILLER_ATTR]?.get() as? Player
        if (killer == null) {
            println("DEBUG: No killer found for NPC ${npc.id}")
            return
        }
        println("DEBUG: Killer is ${killer.username}")
        
        // Get the loot tables from the NPC's combat definition
        val lootTables = npc.combatDef.LootTables
        println("DEBUG: Loot tables for NPC ${npc.id}: ${lootTables?.size ?: 0} tables")
        
        // If there are no loot tables defined, no drops
        if (lootTables.isNullOrEmpty()) {
            println("DEBUG: No loot tables configured for NPC ${npc.id}")
            return
        }
        
        try {
            // Use the existing loot table rolling system
            val droppedItems = roll(killer, lootTables)
            println("DEBUG: Generated ${droppedItems.size} dropped items")
            
            // Spawn each dropped item on the ground at the NPC's location
            droppedItems.forEach { groundItem ->
                // Convert clue scrolls to clue caskets before dropping
                val itemIdToDrop = convertClueScrollToCasket(groundItem.item)
                
                // Create a new GroundItem with the correct tile and owner
                // (Can't modify ownerUID directly as it's internal)
                val newGroundItem = GroundItem(
                    item = itemIdToDrop,
                    amount = groundItem.amount,
                    tile = npc.tile,
                    owner = killer
                )
                
                // Set timers: killer sees for 1 minute, then everyone for 3 minutes
                // timeUntilPublic: 1 minute (100 cycles) - only killer can see initially
                // timeUntilDespawn: 4 minutes (400 cycles) total - 1 min private + 3 min public
                newGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
                newGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
                newGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
                
                println("DEBUG: Dropping item ${itemIdToDrop} x${groundItem.amount} at ${npc.tile}")
                npc.world.spawn(newGroundItem)
                
                // Optional: Send a message to the player about valuable drops
                if (groundItem.amount > 100) { // If item value is high, could add more sophisticated value checking
                    killer.message("${npc.def.name} drops: ${groundItem.amount}x ${getItem(itemIdToDrop).name}")
                }
            }
            
            // Drop one additional random item from the entire game item table (chance scales with NPC level)
            dropRandomItemFromGameTable(npc, killer)
            
            // Drop coins for wilderness NPCs (10k-300k)
            dropWildernessCoins(npc, killer)
        } catch (e: Exception) {
            // Log any errors that occur during loot drop processing
            // This prevents the NPC death from breaking if loot tables are misconfigured
            println("Error processing loot drop for NPC ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Drops coins for all NPCs in the wilderness, scaled by combat level.
     * This makes wilderness combat more rewarding, with higher level monsters dropping more coins.
     */
    private fun dropWildernessCoins(npc: Npc, killer: Player) {
        try {
            // Check if NPC is in wilderness
            val wildernessLevel = npc.tile.getWildernessLevel()
            if (wildernessLevel <= 0) {
                // Not in wilderness, no coin drop
                return
            }
            
            // Get NPC combat level
            val combatLevel = npc.def.combatLevel
            if (combatLevel < 0) {
                // Invalid combat level, use minimum
                return
            }
            
            // Scale coin amount based on combat level
            val (minCoins, maxCoins) = when {
                combatLevel <= 20 -> 10_000 to 50_000      // Low level: 10k-50k
                combatLevel <= 50 -> 50_000 to 150_000    // Mid-low: 50k-150k
                combatLevel <= 100 -> 150_000 to 400_000  // Mid: 150k-400k
                combatLevel <= 150 -> 400_000 to 700_000  // High: 400k-700k
                else -> 700_000 to 1_000_000              // Very high: 700k-1m
            }
            
            // Generate random coin amount within the scaled range
            val coinAmount = Random.nextInt(minCoins, maxCoins + 1) // Inclusive range
            
            // Coin item ID is 995
            val coinItemId = 995
            
            // Create and spawn the coin drop
            val coinGroundItem = GroundItem(
                item = coinItemId,
                amount = coinAmount,
                tile = npc.tile,
                owner = killer
            )
            
            // Set timers: killer sees for 1 minute, then everyone for 3 minutes
            coinGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
            coinGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
            coinGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
            
            println("DEBUG: Dropping ${coinAmount} coins for wilderness NPC ${npc.id} (${npc.def.name}, level $combatLevel) at ${npc.tile} (range: ${minCoins}-${maxCoins})")
            npc.world.spawn(coinGroundItem)
            
            // Notify the player about the coin drop
            killer.message("${npc.def.name} drops: ${coinAmount} coins")
            
        } catch (e: Exception) {
            println("Error dropping wilderness coins for NPC ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Gets the drop chance for a random item based on NPC combat level.
     * Returns the denominator of the chance (e.g., 30 means 1/30 chance).
     * 
     * Scaling (tripled rarity):
     * - Level 1-20: 1/30 chance (~3.33%)
     * - Level 21-50: 1/15 chance (~6.67%)
     * - Level 51-100: 1/9 chance (~11.11%)
     * - Level 101-150: 1/6 chance (~16.67%)
     * - Level 151-199: 1/3 chance (~33.33%)
     * - Level 200-299: 1/2 chance (50%)
     * - Level 300-399: 2/3 chance (~66.67%)
     * - Level 400+: Guaranteed (100%)
     */
    private fun getRandomDropChanceDenominator(combatLevel: Int): Int {
        return when {
            combatLevel <= 20 -> 30  // 1/30 chance (~3.33%)
            combatLevel <= 50 -> 15  // 1/15 chance (~6.67%)
            combatLevel <= 100 -> 9  // 1/9 chance (~11.11%)
            combatLevel <= 150 -> 6  // 1/6 chance (~16.67%)
            combatLevel < 200 -> 3   // 1/3 chance (~33.33%)
            combatLevel < 300 -> 2   // 1/2 chance (50%)
            combatLevel < 400 -> 3   // 2/3 chance (~66.67%) - using 3 as denominator, roll 0-2
            else -> 1                // Guaranteed (100%)
        }
    }
    
    /**
     * Drops a random item from the entire game item table.
     * Gnome minions (6097, 6098, 6099) get a guaranteed random drop.
     * Other NPCs get a chance based on combat level.
     */
    private fun dropRandomItemFromGameTable(npc: Npc, killer: Player) {
        try {
            // Gnome minions get guaranteed random drop
            val isGnomeMinion = npc.id == 6097 || npc.id == 6098 || npc.id == 6099
            
            if (!isGnomeMinion) {
                // For other NPCs, use the level-based chance system
                val combatLevel = npc.def.combatLevel
                if (combatLevel < 0) {
                    return
                }
                
                val chanceDenominator = getRandomDropChanceDenominator(combatLevel)
                val roll = Random.nextInt(chanceDenominator)
                val shouldDrop = if (combatLevel >= 300 && combatLevel < 400) {
                    roll <= 1
                } else {
                    roll == 0
                }
                
                if (!shouldDrop) {
                    return
                }
            }
            
            // Use the cached valid item list
            if (validItemIds.isEmpty()) {
                return
            }
            
            // Randomly select one item from the valid items
            val randomItemId = validItemIds[Random.nextInt(validItemIds.size)]
            
            // Convert clue scrolls to clue caskets before dropping
            val itemIdToDrop = convertClueScrollToCasket(randomItemId)
            val finalItemDef = getItem(itemIdToDrop)
            
            // Determine amount (1 for most items, random 1-100 for stackable items)
            val amount = if (finalItemDef.stackable) {
                Random.nextInt(1, 101) // 1-100 for stackable items
            } else {
                1 // Single item for non-stackable
            }
            
            // Create and spawn the random item
            val randomGroundItem = GroundItem(
                item = itemIdToDrop,
                amount = amount,
                tile = npc.tile,
                owner = killer
            )
            
            // Set timers: killer sees for 1 minute, then everyone for 3 minutes
            randomGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
            randomGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
            randomGroundItem.ownerShipType = 1
            
            npc.world.spawn(randomGroundItem)
            
            // Notify the player about the bonus drop
            killer.message("Bonus drop: ${amount}x ${finalItemDef.name}")
            
        } catch (e: Exception) {
            println("Error dropping random item for NPC ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }
}
