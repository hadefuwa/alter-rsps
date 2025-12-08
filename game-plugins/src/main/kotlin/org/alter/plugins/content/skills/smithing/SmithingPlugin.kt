package org.alter.plugins.content.skills.smithing

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.item.Item
import org.alter.game.fs.ObjectExamineHolder
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class SmithingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    // Add furnace object IDs here - use format "object.furnace" or "object.furnace_<ID>"
    // To find a furnace ID, examine the object in-game or check the object definitions
    private val furnaceObjects = setOf(
        "object.furnace",
        "object.furnace_16469",
        "object.furnace_26300",
        "object.furnace_39241",
        "object.furnace_24009",
    )

    private val anvilObjects = setOf(
        "object.anvil",
        "object.anvil_2097",
    )

    // Data class for chat window selection
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

    // OFFICIAL INTERFACE CODE (commented out - using custom chat window instead)
    // Smelting interface ID (270 didn't work - no bar images showed)
    // private val SMELTING_INTERFACE_ID = 270
    //
    // // Data class for smelting recipes (official interface)
    // private data class SmeltingRecipe(
    //     val barName: String,
    //     val primaryOre: String,
    //     val secondaryOre: String?,
    //     val level: Int,
    //     val experience: Double,
    //     val coalNeeded: Int = 0
    // )
    //
    // // Smelting recipes mapped to button components (official interface)
    // private val smeltingRecipes = mapOf(
    //     14 to SmeltingRecipe("item.bronze_bar", "item.copper_ore", "item.tin_ore", 1, 6.2),
    //     15 to SmeltingRecipe("item.iron_bar", "item.iron_ore", null, 15, 12.5),
    //     16 to SmeltingRecipe("item.silver_bar", "item.silver_ore", null, 20, 13.7),
    //     17 to SmeltingRecipe("item.steel_bar", "item.iron_ore", "item.coal", 30, 17.5, coalNeeded = 2),
    //     18 to SmeltingRecipe("item.gold_bar", "item.gold_ore", null, 40, 22.5),
    //     19 to SmeltingRecipe("item.mithril_bar", "item.mithril_ore", "item.coal", 50, 30.0, coalNeeded = 4),
    //     20 to SmeltingRecipe("item.adamantite_bar", "item.adamantite_ore", "item.coal", 70, 37.5, coalNeeded = 6),
    //     21 to SmeltingRecipe("item.runite_bar", "item.runite_ore", "item.coal", 85, 50.0, coalNeeded = 8)
    // )

    /**
     * Opens the paginated smelting menu for the player
     */
    private suspend fun QueueTask.openSmeltingMenu(player: Player) {
        // Pagination settings - 3 bars per page (5 total options: Prev + 3 bars + Next)
        val barsPerPage = 3
        val totalPages = (smeltingRecipes.size + barsPerPage - 1) / barsPerPage
        
        var currentPage = 0
        
        while (true) {
            val startIndex = currentPage * barsPerPage
            val endIndex = minOf(startIndex + barsPerPage, smeltingRecipes.size)
            val pageRecipes = smeltingRecipes.subList(startIndex, endIndex)
            
            // Build options list for current page
            // Structure: [Previous Page] -> [Bars (3 max)] -> [Next Page]
            val pageOptions = mutableListOf<String>()
            
            // Check if we have previous/next pages available
            val hasPrevious = currentPage > 0
            val hasNext = currentPage < totalPages - 1
            
            // Option 1: Previous Page (always shown as 1st option)
            pageOptions.add("Previous Page")
            
            // Options 2-4: Add bar options (3 bars max)
            pageOptions.addAll(pageRecipes.map { it.displayName })
            
            // Last option: Next Page (always shown as last option)
            pageOptions.add("Next Page")
            
            val title = "What would you like to smelt? (Page ${currentPage + 1}/$totalPages)"
            val selected = options(player, *pageOptions.toTypedArray(), title = title)
            
            if (selected <= 0) {
                break // Invalid selection or cancelled
            }
            
            // selected is 1-based (1 = first option, 2 = second option, etc.)
            val optionIndex = selected - 1
            val barCount = pageRecipes.size
            
            // Handle the selected option based on its position in the menu
            // Menu structure: [0: Previous Page] [1-3: Bars] [4: Next Page]
            
            // Option 0: Previous Page
            if (optionIndex == 0) {
                if (hasPrevious) {
                    currentPage--
                }
                continue
            }
            
            // Check if it's the last option (Next Page)
            val nextPageIndex = 1 + barCount
            if (optionIndex == nextPageIndex) {
                if (hasNext) {
                    currentPage++
                }
                continue
            }
            
            // Check if it's a bar selection
            // Bars are at indices 1 to barCount (after the Previous Page button)
            if (optionIndex >= 1 && optionIndex <= barCount) {
                // Convert to 0-based index for the pageRecipes list
                val recipeIndex = optionIndex - 1
                val recipe = pageRecipes[recipeIndex]
                
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
                break // Exit pagination loop after selection
            }
        }
    }

    init {
        // Helper function to check if an action is a smelting option
        fun isSmeltingOption(action: String?): Boolean {
            if (action == null) return false
            val lowerAction = action.lowercase()
            return lowerAction == "smelt" || 
                   lowerAction == "use" || 
                   lowerAction.contains("smelt") ||
                   lowerAction == "smelt furnace" ||
                   lowerAction == "smelt-furnace"
        }
        
        // Helper function to check if an object is a furnace based on examine text
        fun isFurnaceByExamine(objId: Int): Boolean {
            val examine = ObjectExamineHolder.EXAMINES.get(objId) ?: return false
            val lowerExamine = examine.lowercase()
            return lowerExamine.contains("hot") && lowerExamine.contains("furnace") ||
                   lowerExamine.contains("very hot") ||
                   (lowerExamine.contains("hot") && lowerExamine.contains("furnace"))
        }
        
        // Helper function to register smelting handlers for a furnace object (by RSCM name)
        fun registerFurnaceHandlers(furnace: String) {
            try {
                val objDef = getObject(getRSCM(furnace))
                val smeltOptions = objDef.actions.filterNotNull().filter { isSmeltingOption(it) }
                
                // Bind to smelt/use options only
                smeltOptions.forEach { option ->
                    try {
                        onObjOption(obj = furnace, option = option) {
                            player.queue { 
                                openSmeltingMenu(player)
                            }
                        }
                    } catch (e: Exception) {
                        // Option already bound or doesn't exist, skip
                    }
                }
            } catch (e: Exception) {
                // Object doesn't exist in RSCM, skip
            }
        }
        
        // Helper function to register smelting handlers for a furnace object (by numeric ID)
        // Note: This function is no longer used since we handle dynamic detection in onWorldInit
        // Keeping it for reference but it's not called anymore
        @Suppress("UNUSED_PARAMETER")
        fun registerFurnaceHandlersById(objId: Int) {
            // This function is deprecated - dynamic detection handles all furnaces now
        }
        
        // Handle furnace click to open smelting menu (custom chat window)
        // Register handlers for all known furnace objects
        furnaceObjects.forEach { furnace ->
            registerFurnaceHandlers(furnace)

            // OFFICIAL INTERFACE CODE (commented out - using custom chat window instead)
            // // Bind to all available non-examine options
            // availableOptions.forEach { option ->
            //     try {
            //         onObjOption(obj = furnace, option = option) {
            //             player.queue { 
            //                 // Open the official smelting interface with pictures
            //                 player.openInterface(interfaceId = SMELTING_INTERFACE_ID, dest = InterfaceDestination.MAIN_SCREEN)
            //             }
            //         }
            //     } catch (e: Exception) {
            //         // Option already bound or doesn't exist, skip
            //     }
            // }

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

        // OFFICIAL INTERFACE BUTTON HANDLERS (commented out - using custom chat window instead)
        // Handle button clicks on smelting interface (official interface with pictures)
        // smeltingRecipes.forEach { (component, recipe) ->
        //     onButton(interfaceId = SMELTING_INTERFACE_ID, component = component) {
        //         player.queue { 
        //             smeltOre(
        //                 this, 
        //                 player, 
        //                 recipe.primaryOre, 
        //                 recipe.secondaryOre, 
        //                 recipe.barName, 
        //                 recipe.level, 
        //                 recipe.experience, 
        //                 recipe.coalNeeded
        //             ) 
        //         }
        //     }
        // }

        // Dynamically detect all objects with smelting options OR furnace examine text
        // This runs after world initialization to scan the cache for furnaces
        onWorldInit {
            val registeredCombinations = mutableSetOf<Pair<Int, String>>()
            val allObjects = dev.openrune.cache.CacheManager.getObjects()
            
            allObjects.forEach { (objId, objDef) ->
                // Check if object has smelting options OR is a furnace by examine text
                val smeltOptions = objDef.actions.filterNotNull().filter { isSmeltingOption(it) }
                val isFurnace = isFurnaceByExamine(objId)
                
                // If it's a furnace by examine text but has no smelting options, try "use" option
                val optionsToTry = if (smeltOptions.isNotEmpty()) {
                    smeltOptions
                } else if (isFurnace) {
                    // If examine says it's a furnace, try common interaction options
                    objDef.actions.filterNotNull().filter { 
                        val lower = it.lowercase()
                        lower == "use" || lower == "operate" || lower.contains("use")
                    }
                } else {
                    emptyList()
                }
                
                optionsToTry.forEach { option ->
                    val combination = Pair(objId, option.lowercase())
                    if (!registeredCombinations.contains(combination)) {
                        registeredCombinations.add(combination)
                        try {
                            onObjOption(obj = objId, option = option) {
                                player.queue { 
                                    openSmeltingMenu(player)
                                }
                            }
                        } catch (e: Exception) {
                            // Handler already registered or object doesn't exist, skip
                        }
                    }
                }
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
        val unnotedBarId = getUnnotedItemId(barId)
        val notedBarId = getNotedItemId(unnotedBarId)

        if (smithingLevel < level) {
            player.message("You need a Smithing level of $level to smelt this ore.")
            return
        }

        // Track starting position to detect movement
        val startTile = player.tile
        var smeltedCount = 0

        // Helper function to check if we should stop smelting
        fun shouldStop(): Boolean {
            return !player.isOnline || 
                   player.hasMoveDestination() || 
                   !player.tile.sameAs(startTile)
        }

        // Continuously smelt until materials run out or player stops
        while (true) {
            // Check if player has moved or closed interface - allow breaking out
            if (shouldStop()) {
                break
            }

            // Check level requirement
            if (player.getSkills().getCurrentLevel(Skills.SMITHING) < level) {
                player.message("You need a Smithing level of $level to smelt this ore.")
                break
            }

            // Check for primary ore (noted or unnoted)
            if (!player.hasItemIncludingNoted(primaryOreId)) {
                if (smeltedCount == 0) {
                    player.message("You don't have any ${primaryOre.replace("item.", "").replace("_", " ")}.")
                }
                break
            }

            // Check for secondary ore (noted or unnoted)
            if (secondaryOreId != null && !player.hasItemIncludingNoted(secondaryOreId)) {
                val oreName = if (secondaryOre == "item.coal") "coal" else "secondary ore"
                if (smeltedCount == 0) {
                    player.message("You need $oreName to smelt this.")
                }
                break
            }

            // Check for coal (noted or unnoted)
            if (coalNeeded > 0) {
                val coalCount = player.getItemCountIncludingNoted(coalId)
                if (coalCount < coalNeeded) {
                    if (smeltedCount == 0) {
                        player.message("You need $coalNeeded coal to smelt this ore.")
                    }
                    break
                }
            }

            // Check inventory space - consider both noted and unnoted bars
            val hasBarSpace = player.inventory.contains(unnotedBarId) || player.inventory.contains(notedBarId)
            if (player.inventory.isFull && !hasBarSpace) {
                if (smeltedCount == 0) {
                    player.message("You don't have enough inventory space.")
                }
                break
            }

            // Check again before proceeding
            if (shouldStop()) {
                break
            }

            player.lock()
            try {
                player.animate(Animation.SMITHING_SMELT)
                
                // Wait for smelting animation with interruption checks
                var interrupted = false
                for (i in 0 until 3) {
                    if (shouldStop()) {
                        interrupted = true
                        break
                    }
                    task.wait(1)
                }

                if (interrupted) {
                    break
                }

                // Final check after waiting
                if (shouldStop()) {
                    break
                }

                // Re-check materials after waiting
                if (!player.hasItemIncludingNoted(primaryOreId)) {
                    break
                }

                // Remove ores (handles both noted and unnoted)
                if (!player.removeItemIncludingNoted(primaryOreId, 1)) {
                    break
                }
                
                if (secondaryOreId != null) {
                    if (!player.removeItemIncludingNoted(secondaryOreId, 1)) {
                        break
                    }
                }
                
                if (coalNeeded > 0) {
                    if (!player.removeItemIncludingNoted(coalId, coalNeeded)) {
                        break
                    }
                }

                // Add bar (always add noted bar) - give 10 bars per smelt
                player.inventory.add(notedBarId, 10)
                player.addXp(Skills.SMITHING, experience)
                smeltedCount += 10
            } finally {
                player.unlock()
            }

            // Small delay between smelting cycles
            if (shouldStop()) {
                break
            }
            task.wait(1)
        }

        // Show summary message if multiple bars were smelted
        if (smeltedCount > 1) {
            player.message("You smelt $smeltedCount bars.")
        } else if (smeltedCount == 1) {
            player.message("You smelt the ore into a bar.")
        }
    }
}

