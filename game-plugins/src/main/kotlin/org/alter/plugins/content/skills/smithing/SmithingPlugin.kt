package org.alter.plugins.content.skills.smithing

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.item.Item
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class SmithingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val furnaceObjects = setOf(
        "object.furnace",
        "object.furnace_16469",
    )

    private val anvilObjects = setOf(
        "object.anvil",
        "object.anvil_2097",
    )

    // Data class for smelting recipes
    private data class SmeltingRecipe(
        val barName: String,
        val displayName: String,
        val primaryOre: String,
        val secondaryOre: String?,
        val level: Int,
        val experience: Double,
        val coalNeeded: Int = 0
    )

    // Smelting recipes for chat window selection
    private val smeltingRecipes = listOf(
        SmeltingRecipe("item.bronze_bar", "Bronze bar", "item.copper_ore", "item.tin_ore", 1, 6.2),
        SmeltingRecipe("item.iron_bar", "Iron bar", "item.iron_ore", null, 15, 12.5),
        SmeltingRecipe("item.silver_bar", "Silver bar", "item.silver_ore", null, 20, 13.7),
        SmeltingRecipe("item.steel_bar", "Steel bar", "item.iron_ore", "item.coal", 30, 17.5, coalNeeded = 2),
        SmeltingRecipe("item.gold_bar", "Gold bar", "item.gold_ore", null, 40, 22.5),
        SmeltingRecipe("item.mithril_bar", "Mithril bar", "item.mithril_ore", "item.coal", 50, 30.0, coalNeeded = 4),
        SmeltingRecipe("item.adamantite_bar", "Adamantite bar", "item.adamantite_ore", "item.coal", 70, 37.5, coalNeeded = 6),
        SmeltingRecipe("item.runite_bar", "Runite bar", "item.runite_ore", "item.coal", 85, 50.0, coalNeeded = 8)
    )

    init {
        // Handle furnace click to open smelting menu
        furnaceObjects.forEach { furnace ->
            // Get available options for this furnace
            val objDef = getObject(getRSCM(furnace))
            val availableOptions = objDef.actions.filterNotNull().filter { action -> 
                action.isNotEmpty() && action.lowercase() != "examine" 
            }
            
            // Bind to all available non-examine options
            availableOptions.forEach { option ->
                try {
                    onObjOption(obj = furnace, option = option) {
                        // Close interface 311 if it's visible (in case client opened it by default)
                        if (player.isInterfaceVisible(311)) {
                            player.closeInterface(interfaceId = 311)
                        }
                        
                        player.queue { 
                            val barOptions = smeltingRecipes.map { it.displayName }.toTypedArray()
                            val selected = options(player, *barOptions, title = "What would you like to smelt?")
                            
                            if (selected > 0 && selected <= smeltingRecipes.size) {
                                val recipe = smeltingRecipes[selected - 1]
                                smeltOre(
                                    this,
                                    player,
                                    recipe.primaryOre,
                                    recipe.secondaryOre,
                                    recipe.barName,
                                    recipe.level,
                                    recipe.experience,
                                    recipe.coalNeeded
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Option already bound or doesn't exist, skip
                }
            }

            // Keep existing item-on-object handlers for backward compatibility
            onItemOnObj(obj = furnace, item = "item.copper_ore") {
                player.queue { smeltOre(this, player, "item.copper_ore", "item.tin_ore", "item.bronze_bar", 1, 6.2) }
            }
            onItemOnObj(obj = furnace, item = "item.tin_ore") {
                player.queue { smeltOre(this, player, "item.tin_ore", "item.copper_ore", "item.bronze_bar", 1, 6.2) }
            }
            onItemOnObj(obj = furnace, item = "item.iron_ore") {
                player.queue { smeltOre(this, player, "item.iron_ore", null, "item.iron_bar", 15, 12.5) }
            }
            onItemOnObj(obj = furnace, item = "item.silver_ore") {
                player.queue { smeltOre(this, player, "item.silver_ore", null, "item.silver_bar", 20, 13.7) }
            }
            onItemOnObj(obj = furnace, item = "item.gold_ore") {
                player.queue { smeltOre(this, player, "item.gold_ore", null, "item.gold_bar", 40, 22.5) }
            }
            onItemOnObj(obj = furnace, item = "item.coal") {
                // Coal is used with other ores, not smelted alone
                player.message("You need to combine coal with other ores to smelt them.")
            }
            onItemOnObj(obj = furnace, item = "item.mithril_ore") {
                player.queue { smeltOre(this, player, "item.mithril_ore", "item.coal", "item.mithril_bar", 50, 30.0, coalNeeded = 4) }
            }
            onItemOnObj(obj = furnace, item = "item.adamantite_ore") {
                player.queue { smeltOre(this, player, "item.adamantite_ore", "item.coal", "item.adamantite_bar", 70, 37.5, coalNeeded = 6) }
            }
            onItemOnObj(obj = furnace, item = "item.runite_ore") {
                player.queue { smeltOre(this, player, "item.runite_ore", "item.coal", "item.runite_bar", 85, 50.0, coalNeeded = 8) }
            }
        }

    }

    /**
     * Gets the noted version of an item ID, or returns the original if it doesn't have a noted version
     */
    private fun getNotedItemId(itemId: Int): Int {
        return org.alter.game.model.item.Item(itemId).toNoted().id
    }

    /**
     * Gets the unnoted version of an item ID, or returns the original if it's already unnoted
     */
    private fun getUnnotedItemId(itemId: Int): Int {
        return org.alter.game.model.item.Item(itemId).toUnnoted().id
    }

    /**
     * Gets the total count of an item (both noted and unnoted versions)
     */
    private fun Player.getItemCountIncludingNoted(itemId: Int): Int {
        val unnotedId = getUnnotedItemId(itemId)
        val notedId = getNotedItemId(unnotedId)
        return inventory.getItemCount(unnotedId) + inventory.getItemCount(notedId)
    }

    /**
     * Checks if inventory contains an item (either noted or unnoted version)
     */
    private fun Player.hasItemIncludingNoted(itemId: Int): Boolean {
        val unnotedId = getUnnotedItemId(itemId)
        val notedId = getNotedItemId(unnotedId)
        return inventory.contains(unnotedId) || inventory.contains(notedId)
    }

    /**
     * Removes an item from inventory, handling both noted and unnoted versions
     * Returns true if the item was successfully removed
     */
    private fun Player.removeItemIncludingNoted(itemId: Int, amount: Int): Boolean {
        val unnotedId = getUnnotedItemId(itemId)
        val notedId = getNotedItemId(unnotedId)
        
        var remaining = amount
        
        // First try to remove from unnoted items
        val unnotedCount = inventory.getItemCount(unnotedId)
        if (unnotedCount > 0) {
            val toRemove = minOf(remaining, unnotedCount)
            inventory.remove(unnotedId, toRemove)
            remaining -= toRemove
        }
        
        // Then remove from noted items (unnoting them)
        if (remaining > 0) {
            val notedCount = inventory.getItemCount(notedId)
            if (notedCount > 0) {
                val toRemove = minOf(remaining, notedCount)
                // Remove noted items - they will be unnoted automatically when removed
                inventory.remove(notedId, toRemove)
                remaining -= toRemove
            }
        }
        
        return remaining == 0
    }

    private suspend fun smeltOre(
        task: QueueTask,
        player: Player,
        primaryOre: String,
        secondaryOre: String?,
        barName: String,
        level: Int,
        experience: Double,
        coalNeeded: Int = 0
    ) {
        val smithingLevel = player.getSkills().getCurrentLevel(Skills.SMITHING)
        val primaryOreId = getRSCM(primaryOre)
        val secondaryOreId = secondaryOre?.let { getRSCM(it) }
        val barId = getRSCM(barName)
        val coalId = getRSCM("item.coal")

        if (smithingLevel < level) {
            player.message("You need a Smithing level of $level to smelt this ore.")
            return
        }

        // Check for primary ore (noted or unnoted)
        if (!player.hasItemIncludingNoted(primaryOreId)) {
            return
        }

        // Check for secondary ore (noted or unnoted)
        if (secondaryOreId != null && !player.hasItemIncludingNoted(secondaryOreId)) {
            val oreName = if (secondaryOre == "item.coal") "coal" else "secondary ore"
            player.message("You need $oreName to smelt this.")
            return
        }

        // Check for coal (noted or unnoted)
        if (coalNeeded > 0) {
            val coalCount = player.getItemCountIncludingNoted(coalId)
            if (coalCount < coalNeeded) {
                player.message("You need $coalNeeded coal to smelt this ore.")
                return
            }
        }

        // Check inventory space - consider both noted and unnoted bars
        val unnotedBarId = getUnnotedItemId(barId)
        val notedBarId = getNotedItemId(unnotedBarId)
        val hasBarSpace = player.inventory.contains(unnotedBarId) || player.inventory.contains(notedBarId)
        if (player.inventory.isFull && !hasBarSpace) {
            player.message("You don't have enough inventory space.")
            return
        }

        player.lock()
        try {
            player.animate(Animation.SMITHING_SMELT)
            task.wait(3)

            // Re-check after waiting
            if (!player.hasItemIncludingNoted(primaryOreId)) {
                return
            }

            // Remove ores (handles both noted and unnoted)
            if (!player.removeItemIncludingNoted(primaryOreId, 1)) {
                return
            }
            
            if (secondaryOreId != null) {
                if (!player.removeItemIncludingNoted(secondaryOreId, 1)) {
                    return
                }
            }
            
            if (coalNeeded > 0) {
                if (!player.removeItemIncludingNoted(coalId, coalNeeded)) {
                    return
                }
            }

            // Add bar (always add noted bar)
            player.inventory.add(notedBarId, 1)
            player.addXp(Skills.SMITHING, experience)
            player.message("You smelt the ore into a bar.")
        } finally {
            player.unlock()
        }
    }
}

