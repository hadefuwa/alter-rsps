package org.alter.plugins.content.death

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.Tile
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimeConstants
import org.alter.game.model.weightedTableBuilder.roll
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Global Shared Loot Drop Plugin
 * 
 * This plugin handles shared loot drops for NPCs where all players who dealt damage
 * receive their own independent loot roll, rather than only the player who dealt
 * the most damage getting the drops.
 * 
 * To add an NPC to the shared loot system, simply add their RSCM name to the
 * SHARED_LOOT_NPCS set below.
 * 
 * Example:
 * ```
 * private val SHARED_LOOT_NPCS = setOf(
 *     "npc.crazy_archaeologist",
 *     "npc.callisto",
 *     "npc.venenatis"
 * )
 * ```
 * 
 * @author Auto-generated
 */
class SharedLootDropPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Set of NPC RSCM names that should use shared loot drops.
         * All players who dealt damage to these NPCs will receive their own loot roll.
         * 
         * To add an NPC, simply add their RSCM name (e.g., "npc.crazy_archaeologist")
         * to this set.
         */
        private val SHARED_LOOT_NPCS = setOf(
            "npc.crazy_archaeologist",
            // Add more NPCs here as needed:
            // "npc.callisto",
            // "npc.venenatis",
            // "npc.vetion",
            // "npc.chaos_elemental",
            // "npc.scorpia",
            // "npc.chaos_fanatic",
        )
    }
    
    /**
     * Cached set of NPC IDs that use shared loot.
     * This is populated at plugin initialization to avoid repeated RSCM lookups.
     */
    private val sharedLootNpcIds = mutableSetOf<Int>()
    
    /**
     * Set of NPC IDs that should always drop a random item in addition to their normal loot.
     */
    private val alwaysRandomDropNpcIds = mutableSetOf<Int>()
    
    /**
     * Map of NPC IDs to guaranteed coin drop amounts.
     * Each player who dealt damage will receive this amount of coins.
     */
    private val guaranteedCoinDrops = mutableMapOf<Int, Int>()
    
    /**
     * Set of NPC IDs that should drop loot at the player's location instead of the NPC's location.
     * This is useful for NPCs where players cannot stand on the same tile.
     */
    private val dropAtPlayerLocationNpcIds = mutableSetOf<Int>()
    
    /**
     * Cached list of valid item IDs from the entire game item table.
     * This is built once when the plugin initializes to avoid rebuilding it on every NPC death.
     */
    private val validItemIds: List<Int> by lazy {
        buildValidItemList()
    }

    init {
        // Convert RSCM names to NPC IDs at initialization
        SHARED_LOOT_NPCS.forEach { rscmName ->
            try {
                val npcId = getRSCM(rscmName)
                sharedLootNpcIds.add(npcId)
                println("SharedLootDropPlugin: Added ${rscmName} (ID: $npcId) to shared loot system")
            } catch (e: Exception) {
                println("SharedLootDropPlugin: Warning - Could not find NPC ${rscmName}: ${e.message}")
            }
        }
        
        // Add NPCs that should always drop a random item
        // Currently: Crazy Archaeologist
        try {
            val crazyArchId = getRSCM("npc.crazy_archaeologist")
            alwaysRandomDropNpcIds.add(crazyArchId)
            println("SharedLootDropPlugin: Added Crazy Archaeologist (ID: $crazyArchId) to always-random-drop list")
        } catch (e: Exception) {
            println("SharedLootDropPlugin: Warning - Could not find NPC npc.crazy_archaeologist: ${e.message}")
        }
        
        // Add NPCs that should always drop guaranteed coins
        // Currently: Crazy Archaeologist (250k per player)
        try {
            val crazyArchId = getRSCM("npc.crazy_archaeologist")
            guaranteedCoinDrops[crazyArchId] = 250000 // 250k coins
            println("SharedLootDropPlugin: Added Crazy Archaeologist (ID: $crazyArchId) to guaranteed-coin-drop list (250k per player)")
        } catch (e: Exception) {
            println("SharedLootDropPlugin: Warning - Could not find NPC npc.crazy_archaeologist for coin drops: ${e.message}")
        }
        
        // Add NPCs that should drop loot at player locations instead of NPC location
        // Currently: Crazy Archaeologist (players can't stand on same tile)
        try {
            val crazyArchId = getRSCM("npc.crazy_archaeologist")
            dropAtPlayerLocationNpcIds.add(crazyArchId)
            println("SharedLootDropPlugin: Added Crazy Archaeologist (ID: $crazyArchId) to drop-at-player-location list")
        } catch (e: Exception) {
            println("SharedLootDropPlugin: Warning - Could not find NPC npc.crazy_archaeologist for drop location: ${e.message}")
        }
        
        println("SharedLootDropPlugin: Initialized with ${sharedLootNpcIds.size} NPCs using shared loot")
        
        // Register handler for any NPC death
        onAnyNpcDeath {
            val npc = ctx as Npc
            
            // Check if this NPC should use shared loot by comparing NPC ID
            if (sharedLootNpcIds.contains(npc.id)) {
                handleSharedLootDrop(npc)
            }
        }
    }

    /**
     * Handles shared loot drops for an NPC.
     * All players who dealt damage to the NPC will receive their own loot roll.
     * 
     * This creates a fair system where everyone who participates gets rewarded,
     * rather than only the player who dealt the most damage.
     * 
     * @param npc The NPC that died
     */
    private fun handleSharedLootDrop(npc: Npc) {
        // Get the loot tables from the NPC's combat definition
        val lootTables = npc.combatDef.LootTables
        if (lootTables.isNullOrEmpty()) {
            return // No loot tables configured
        }
        
        // Get all players who dealt damage to the NPC
        // We check all players in the world and see if they have damage > 0 in the damage map
        val playersWhoDamaged = mutableListOf<Player>()
        
        npc.world.players.forEach { player ->
            if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                playersWhoDamaged.add(player)
            }
        }
        
        if (playersWhoDamaged.isEmpty()) {
            return // No players dealt damage
        }
        
        // Determine if loot should drop at player locations or NPC location
        val dropAtPlayerLocation = dropAtPlayerLocationNpcIds.contains(npc.id)
        println("SharedLootDropPlugin: NPC ${npc.id} (${npc.def.name}) - dropAtPlayerLocation: $dropAtPlayerLocation")
        
        // Give each player their own loot roll
        playersWhoDamaged.forEach { player ->
            try {
                // Roll loot tables for this player
                val droppedItems = roll(player, lootTables)
                
                // Determine drop location: player's tile if configured, otherwise NPC's tile
                val dropTile = if (dropAtPlayerLocation) {
                    player.tile
                } else {
                    npc.tile
                }
                
                println("SharedLootDropPlugin: Dropping ${droppedItems.size} items for player ${player.username} at tile $dropTile (player tile: ${player.tile}, npc tile: ${npc.tile})")
                
                // Spawn each dropped item on the ground at the determined location
                droppedItems.forEach { groundItem ->
                    val newGroundItem = GroundItem(
                        item = groundItem.item,
                        amount = groundItem.amount,
                        tile = dropTile,
                        owner = player
                    )
                    
                    // Set timers: player sees for 1 minute, then everyone for 3 minutes
                    newGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
                    newGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
                    newGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
                    
                    npc.world.spawn(newGroundItem)
                }
                
                // Notify the player
                if (droppedItems.isNotEmpty()) {
                    player.message("You receive loot from ${npc.def.name}!")
                }
                
                // If this NPC should always drop guaranteed coins, add them now
                if (guaranteedCoinDrops.containsKey(npc.id)) {
                    val coinAmount = guaranteedCoinDrops[npc.id]!!
                    val coinDropTile = if (dropAtPlayerLocation) player.tile else npc.tile
                    dropCoinsForPlayer(npc, player, coinAmount, coinDropTile)
                }
                
                // If this NPC should always drop a random item, add it now
                if (alwaysRandomDropNpcIds.contains(npc.id)) {
                    val randomDropTile = if (dropAtPlayerLocation) player.tile else npc.tile
                    dropRandomItemForPlayer(npc, player, randomDropTile)
                }
            } catch (e: Exception) {
                println("Error processing shared loot drop for player ${player.username} from NPC ${npc.id} (${npc.def.name}): ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Drops guaranteed coins for a specific player.
     * This is used to give guaranteed coin drops to players who dealt damage.
     * 
     * @param npc The NPC that died
     * @param player The player who should receive the coins
     * @param amount The amount of coins to drop
     * @param dropTile The tile where the coins should be dropped
     */
    private fun dropCoinsForPlayer(npc: Npc, player: Player, amount: Int, dropTile: Tile) {
        try {
            val coinsItemId = getRSCM("item.coins_995")
            
            // Create and spawn the coins
            val coinsGroundItem = GroundItem(
                item = coinsItemId,
                amount = amount,
                tile = dropTile,
                owner = player
            )
            
            // Set timers: player sees for 1 minute, then everyone for 3 minutes
            coinsGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
            coinsGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
            coinsGroundItem.ownerShipType = 1
            
            npc.world.spawn(coinsGroundItem)
            
            // Notify the player about the coin drop
            player.message("You receive ${amount} coins from ${npc.def.name}!")
            
        } catch (e: Exception) {
            println("Error dropping coins for player ${player.username} from NPC ${npc.id} (${npc.def.name}): ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Drops a random item from the game's item table for a specific player.
     * This is used to give guaranteed random drops to players who dealt damage.
     * 
     * @param npc The NPC that died
     * @param player The player who should receive the random item
     * @param dropTile The tile where the item should be dropped
     */
    private fun dropRandomItemForPlayer(npc: Npc, player: Player, dropTile: Tile) {
        try {
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
                tile = dropTile,
                owner = player
            )
            
            // Set timers: player sees for 1 minute, then everyone for 3 minutes
            randomGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
            randomGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
            randomGroundItem.ownerShipType = 1
            
            npc.world.spawn(randomGroundItem)
            
            // Notify the player about the bonus drop
            player.message("Bonus random drop: ${amount}x ${finalItemDef.name}")
            
        } catch (e: Exception) {
            println("Error dropping random item for player ${player.username} from NPC ${npc.id} (${npc.def.name}): ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Converts a clue scroll item ID to its corresponding clue casket item ID.
     * Returns the original item ID if it's not a clue scroll.
     * 
     * @param itemId The item ID to check and potentially convert
     * @return The clue casket item ID if the input was a clue scroll, otherwise the original item ID
     */
    private fun convertClueScrollToCasket(itemId: Int): Int {
        try {
            val itemDef = getItem(itemId)
            val itemName = itemDef.name.lowercase()
            
            // Check if this is a clue scroll item
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
                        return getRSCM(casketName)
                    } catch (e: Exception) {
                        // Fall through to return original item ID
                    }
                }
            }
        } catch (e: Exception) {
            // Fall through to return original item ID
        }
        
        return itemId
    }
    
    /**
     * Builds a list of valid item IDs from the game's item table.
     * This excludes certain item types that shouldn't be dropped randomly.
     * 
     * @return A list of valid item IDs that can be dropped randomly
     */
    private fun buildValidItemList(): List<Int> {
        val validItems = mutableListOf<Int>()
        val excludedNames = setOf(
            "null", "empty", "placeholder", "unobtainable", "test",
            "spawn", "debug", "admin", "moderator", "developer"
        )
        
        // Iterate through a reasonable range of item IDs
        // Most RuneScape servers use item IDs in the range 0-30000
        for (itemId in 0..30000) {
            try {
                val itemDef = getItem(itemId)
                val itemName = itemDef.name.lowercase()
                
                // Skip items with excluded names
                if (excludedNames.any { itemName.contains(it) }) {
                    continue
                }
                
                // Skip items with no name or very short names (likely placeholders)
                if (itemName.isEmpty() || itemName.length < 2) {
                    continue
                }
                
                // Add valid items
                validItems.add(itemId)
            } catch (e: Exception) {
                // Item doesn't exist or can't be loaded, skip it
                continue
            }
        }
        
        return validItems
    }
}

