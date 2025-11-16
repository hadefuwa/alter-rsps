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
            handleNpcLootDrop(npc)
        }
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
                // Create a new GroundItem with the correct tile and owner
                // (Can't modify ownerUID directly as it's internal)
                val newGroundItem = GroundItem(
                    item = groundItem.item,
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
                
                println("DEBUG: Dropping item ${groundItem.item} x${groundItem.amount} at ${npc.tile}")
                npc.world.spawn(newGroundItem)
                
                // Optional: Send a message to the player about valuable drops
                if (groundItem.amount > 100) { // If item value is high, could add more sophisticated value checking
                    killer.message("${npc.def.name} drops: ${groundItem.amount}x ${getItem(groundItem.item).name}")
                }
            }
            
            // Drop one additional random item from the entire game item table
            dropRandomItemFromGameTable(npc, killer)
        } catch (e: Exception) {
            // Log any errors that occur during loot drop processing
            // This prevents the NPC death from breaking if loot tables are misconfigured
            println("Error processing loot drop for NPC ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Drops a random item from the entire game item table.
     * This provides an additional bonus drop for NPCs with loot tables.
     */
    private fun dropRandomItemFromGameTable(npc: Npc, killer: Player) {
        try {
            // Use the cached valid item list
            if (validItemIds.isEmpty()) {
                println("DEBUG: No valid items found in game item table")
                return
            }
            
            // Randomly select one item from the valid items
            val randomItemId = validItemIds[Random.nextInt(validItemIds.size)]
            val itemDef = getItem(randomItemId)
            
            // Determine amount (1 for most items, random 1-100 for stackable items)
            val amount = if (itemDef.stackable) {
                Random.nextInt(1, 101) // 1-100 for stackable items
            } else {
                1 // Single item for non-stackable
            }
            
            // Create and spawn the random item
            val randomGroundItem = GroundItem(
                item = randomItemId,
                amount = amount,
                tile = npc.tile,
                owner = killer
            )
            
            // Set timers: killer sees for 1 minute, then everyone for 3 minutes
            // timeUntilPublic: 1 minute (100 cycles) - only killer can see initially
            // timeUntilDespawn: 4 minutes (400 cycles) total - 1 min private + 3 min public
            randomGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
            randomGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
            randomGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
            
            println("DEBUG: Dropping random bonus item ${randomItemId} (${itemDef.name}) x${amount} at ${npc.tile}")
            npc.world.spawn(randomGroundItem)
            
            // Notify the player about the bonus drop
            killer.message("Bonus drop: ${amount}x ${itemDef.name}")
            
        } catch (e: Exception) {
            println("Error dropping random item for NPC ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }
}
