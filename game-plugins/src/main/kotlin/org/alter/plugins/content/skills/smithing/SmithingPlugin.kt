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

    // Data class for smithing recipes
    private data class SmithingRecipe(
        val itemName: String,
        val displayName: String,
        val barName: String,
        val barsRequired: Int,
        val level: Int,
        val experience: Double,
        val quantity: Int = 1  // Number of items produced per smithing action (default 1, 10 for bolts)
    )

    // Smithing recipes for anvil
    private val smithingRecipes = listOf(
        // Bronze items
        SmithingRecipe("item.bronze_dagger", "Bronze dagger", "item.bronze_bar", 1, 1, 12.5),
        SmithingRecipe("item.bronze_sword", "Bronze sword", "item.bronze_bar", 1, 4, 12.5),
        SmithingRecipe("item.bronze_scimitar", "Bronze scimitar", "item.bronze_bar", 2, 5, 25.0),
        SmithingRecipe("item.bronze_longsword", "Bronze longsword", "item.bronze_bar", 2, 6, 25.0),
        SmithingRecipe("item.bronze_2h_sword", "Bronze 2h sword", "item.bronze_bar", 3, 14, 37.5),
        SmithingRecipe("item.bronze_axe", "Bronze axe", "item.bronze_bar", 1, 1, 12.5),
        SmithingRecipe("item.bronze_mace", "Bronze mace", "item.bronze_bar", 1, 2, 12.5),
        SmithingRecipe("item.bronze_warhammer", "Bronze warhammer", "item.bronze_bar", 3, 9, 37.5),
        SmithingRecipe("item.bronze_battleaxe", "Bronze battleaxe", "item.bronze_bar", 3, 10, 37.5),
        SmithingRecipe("item.bronze_chainbody", "Bronze chainbody", "item.bronze_bar", 3, 11, 37.5),
        SmithingRecipe("item.bronze_platebody", "Bronze platebody", "item.bronze_bar", 5, 18, 62.5),
        SmithingRecipe("item.bronze_platelegs", "Bronze platelegs", "item.bronze_bar", 3, 16, 37.5),
        SmithingRecipe("item.bronze_plateskirt", "Bronze plateskirt", "item.bronze_bar", 3, 16, 37.5),
        SmithingRecipe("item.bronze_full_helm", "Bronze full helm", "item.bronze_bar", 2, 7, 25.0),
        SmithingRecipe("item.bronze_med_helm", "Bronze med helm", "item.bronze_bar", 1, 3, 12.5),
        SmithingRecipe("item.bronze_sq_shield", "Bronze sq shield", "item.bronze_bar", 2, 8, 25.0),
        SmithingRecipe("item.bronze_kiteshield", "Bronze kiteshield", "item.bronze_bar", 3, 12, 37.5),
        
        // Iron items
        SmithingRecipe("item.iron_dagger", "Iron dagger", "item.iron_bar", 1, 15, 25.0),
        SmithingRecipe("item.iron_sword", "Iron sword", "item.iron_bar", 1, 19, 25.0),
        SmithingRecipe("item.iron_scimitar", "Iron scimitar", "item.iron_bar", 2, 20, 50.0),
        SmithingRecipe("item.iron_longsword", "Iron longsword", "item.iron_bar", 2, 21, 50.0),
        SmithingRecipe("item.iron_2h_sword", "Iron 2h sword", "item.iron_bar", 3, 29, 75.0),
        SmithingRecipe("item.iron_axe", "Iron axe", "item.iron_bar", 1, 16, 25.0),
        SmithingRecipe("item.iron_mace", "Iron mace", "item.iron_bar", 1, 17, 25.0),
        SmithingRecipe("item.iron_warhammer", "Iron warhammer", "item.iron_bar", 3, 24, 75.0),
        SmithingRecipe("item.iron_battleaxe", "Iron battleaxe", "item.iron_bar", 3, 25, 75.0),
        SmithingRecipe("item.iron_chainbody", "Iron chainbody", "item.iron_bar", 3, 26, 75.0),
        SmithingRecipe("item.iron_platebody", "Iron platebody", "item.iron_bar", 5, 33, 125.0),
        SmithingRecipe("item.iron_platelegs", "Iron platelegs", "item.iron_bar", 3, 31, 75.0),
        SmithingRecipe("item.iron_plateskirt", "Iron plateskirt", "item.iron_bar", 3, 31, 75.0),
        SmithingRecipe("item.iron_full_helm", "Iron full helm", "item.iron_bar", 2, 22, 50.0),
        SmithingRecipe("item.iron_med_helm", "Iron med helm", "item.iron_bar", 1, 18, 25.0),
        SmithingRecipe("item.iron_sq_shield", "Iron sq shield", "item.iron_bar", 2, 23, 50.0),
        SmithingRecipe("item.iron_kiteshield", "Iron kiteshield", "item.iron_bar", 3, 27, 75.0),
        
        // Steel items
        SmithingRecipe("item.steel_dagger", "Steel dagger", "item.steel_bar", 1, 30, 37.5),
        SmithingRecipe("item.steel_sword", "Steel sword", "item.steel_bar", 1, 34, 37.5),
        SmithingRecipe("item.steel_scimitar", "Steel scimitar", "item.steel_bar", 2, 35, 75.0),
        SmithingRecipe("item.steel_longsword", "Steel longsword", "item.steel_bar", 2, 36, 75.0),
        SmithingRecipe("item.steel_2h_sword", "Steel 2h sword", "item.steel_bar", 3, 44, 112.5),
        SmithingRecipe("item.steel_axe", "Steel axe", "item.steel_bar", 1, 31, 37.5),
        SmithingRecipe("item.steel_mace", "Steel mace", "item.steel_bar", 1, 32, 37.5),
        SmithingRecipe("item.steel_warhammer", "Steel warhammer", "item.steel_bar", 3, 39, 112.5),
        SmithingRecipe("item.steel_battleaxe", "Steel battleaxe", "item.steel_bar", 3, 40, 112.5),
        SmithingRecipe("item.steel_chainbody", "Steel chainbody", "item.steel_bar", 3, 41, 112.5),
        SmithingRecipe("item.steel_platebody", "Steel platebody", "item.steel_bar", 5, 48, 187.5),
        SmithingRecipe("item.steel_platelegs", "Steel platelegs", "item.steel_bar", 3, 46, 112.5),
        SmithingRecipe("item.steel_plateskirt", "Steel plateskirt", "item.steel_bar", 3, 46, 112.5),
        SmithingRecipe("item.steel_full_helm", "Steel full helm", "item.steel_bar", 2, 37, 75.0),
        SmithingRecipe("item.steel_med_helm", "Steel med helm", "item.steel_bar", 1, 33, 37.5),
        SmithingRecipe("item.steel_sq_shield", "Steel sq shield", "item.steel_bar", 2, 38, 75.0),
        SmithingRecipe("item.steel_kiteshield", "Steel kiteshield", "item.steel_bar", 3, 42, 112.5),
        
        // Mithril items
        SmithingRecipe("item.mithril_dagger", "Mithril dagger", "item.mithril_bar", 1, 50, 50.0),
        SmithingRecipe("item.mithril_sword", "Mithril sword", "item.mithril_bar", 1, 54, 50.0),
        SmithingRecipe("item.mithril_scimitar", "Mithril scimitar", "item.mithril_bar", 2, 55, 100.0),
        SmithingRecipe("item.mithril_longsword", "Mithril longsword", "item.mithril_bar", 2, 56, 100.0),
        SmithingRecipe("item.mithril_2h_sword", "Mithril 2h sword", "item.mithril_bar", 3, 64, 150.0),
        SmithingRecipe("item.mithril_axe", "Mithril axe", "item.mithril_bar", 1, 51, 50.0),
        SmithingRecipe("item.mithril_mace", "Mithril mace", "item.mithril_bar", 1, 52, 50.0),
        SmithingRecipe("item.mithril_warhammer", "Mithril warhammer", "item.mithril_bar", 3, 59, 150.0),
        SmithingRecipe("item.mithril_battleaxe", "Mithril battleaxe", "item.mithril_bar", 3, 60, 150.0),
        SmithingRecipe("item.mithril_chainbody", "Mithril chainbody", "item.mithril_bar", 3, 61, 150.0),
        SmithingRecipe("item.mithril_platebody", "Mithril platebody", "item.mithril_bar", 5, 68, 250.0),
        SmithingRecipe("item.mithril_platelegs", "Mithril platelegs", "item.mithril_bar", 3, 66, 150.0),
        SmithingRecipe("item.mithril_plateskirt", "Mithril plateskirt", "item.mithril_bar", 3, 66, 150.0),
        SmithingRecipe("item.mithril_full_helm", "Mithril full helm", "item.mithril_bar", 2, 57, 100.0),
        SmithingRecipe("item.mithril_med_helm", "Mithril med helm", "item.mithril_bar", 1, 53, 50.0),
        SmithingRecipe("item.mithril_sq_shield", "Mithril sq shield", "item.mithril_bar", 2, 58, 100.0),
        SmithingRecipe("item.mithril_kiteshield", "Mithril kiteshield", "item.mithril_bar", 3, 62, 150.0),
        
        // Adamant items
        SmithingRecipe("item.adamant_dagger", "Adamant dagger", "item.adamantite_bar", 1, 70, 62.5),
        SmithingRecipe("item.adamant_sword", "Adamant sword", "item.adamantite_bar", 1, 74, 62.5),
        SmithingRecipe("item.adamant_scimitar", "Adamant scimitar", "item.adamantite_bar", 2, 75, 125.0),
        SmithingRecipe("item.adamant_longsword", "Adamant longsword", "item.adamantite_bar", 2, 76, 125.0),
        SmithingRecipe("item.adamant_2h_sword", "Adamant 2h sword", "item.adamantite_bar", 3, 84, 187.5),
        SmithingRecipe("item.adamant_axe", "Adamant axe", "item.adamantite_bar", 1, 71, 62.5),
        SmithingRecipe("item.adamant_mace", "Adamant mace", "item.adamantite_bar", 1, 72, 62.5),
        SmithingRecipe("item.adamant_warhammer", "Adamant warhammer", "item.adamantite_bar", 3, 79, 187.5),
        SmithingRecipe("item.adamant_battleaxe", "Adamant battleaxe", "item.adamantite_bar", 3, 80, 187.5),
        SmithingRecipe("item.adamant_chainbody", "Adamant chainbody", "item.adamantite_bar", 3, 81, 187.5),
        SmithingRecipe("item.adamant_platebody", "Adamant platebody", "item.adamantite_bar", 5, 88, 312.5),
        SmithingRecipe("item.adamant_platelegs", "Adamant platelegs", "item.adamantite_bar", 3, 86, 187.5),
        SmithingRecipe("item.adamant_plateskirt", "Adamant plateskirt", "item.adamantite_bar", 3, 86, 187.5),
        SmithingRecipe("item.adamant_full_helm", "Adamant full helm", "item.adamantite_bar", 2, 77, 125.0),
        SmithingRecipe("item.adamant_med_helm", "Adamant med helm", "item.adamantite_bar", 1, 73, 62.5),
        SmithingRecipe("item.adamant_sq_shield", "Adamant sq shield", "item.adamantite_bar", 2, 78, 125.0),
        SmithingRecipe("item.adamant_kiteshield", "Adamant kiteshield", "item.adamantite_bar", 3, 82, 187.5),
        
        // Rune items
        SmithingRecipe("item.rune_dagger", "Rune dagger", "item.runite_bar", 1, 85, 75.0),
        SmithingRecipe("item.rune_sword", "Rune sword", "item.runite_bar", 1, 89, 75.0),
        SmithingRecipe("item.rune_scimitar", "Rune scimitar", "item.runite_bar", 2, 90, 150.0),
        SmithingRecipe("item.rune_longsword", "Rune longsword", "item.runite_bar", 2, 91, 150.0),
        SmithingRecipe("item.rune_2h_sword", "Rune 2h sword", "item.runite_bar", 3, 99, 225.0),
        SmithingRecipe("item.rune_axe", "Rune axe", "item.runite_bar", 1, 86, 75.0),
        SmithingRecipe("item.rune_mace", "Rune mace", "item.runite_bar", 1, 87, 75.0),
        SmithingRecipe("item.rune_warhammer", "Rune warhammer", "item.runite_bar", 3, 94, 225.0),
        SmithingRecipe("item.rune_battleaxe", "Rune battleaxe", "item.runite_bar", 3, 95, 225.0),
        SmithingRecipe("item.rune_chainbody", "Rune chainbody", "item.runite_bar", 3, 96, 225.0),
        SmithingRecipe("item.rune_platebody", "Rune platebody", "item.runite_bar", 5, 99, 375.0),
        SmithingRecipe("item.rune_platelegs", "Rune platelegs", "item.runite_bar", 3, 99, 225.0),
        SmithingRecipe("item.rune_plateskirt", "Rune plateskirt", "item.runite_bar", 3, 99, 225.0),
        SmithingRecipe("item.rune_full_helm", "Rune full helm", "item.runite_bar", 2, 92, 150.0),
        SmithingRecipe("item.rune_med_helm", "Rune med helm", "item.runite_bar", 1, 88, 75.0),
        SmithingRecipe("item.rune_sq_shield", "Rune sq shield", "item.runite_bar", 2, 93, 150.0),
        SmithingRecipe("item.rune_kiteshield", "Rune kiteshield", "item.runite_bar", 3, 97, 225.0),
        
        // Bronze bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.bronze_bolts_unf", "Bronze bolts (unf)", "item.bronze_bar", 1, 9, 25.0, quantity = 10),
        
        // Iron bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.iron_bolts_unf", "Iron bolts (unf)", "item.iron_bar", 1, 39, 50.0, quantity = 10),
        
        // Steel bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.steel_bolts_unf", "Steel bolts (unf)", "item.steel_bar", 1, 46, 75.0, quantity = 10),
        
        // Mithril bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.mithril_bolts_unf", "Mithril bolts (unf)", "item.mithril_bar", 1, 54, 100.0, quantity = 10),
        
        // Adamant bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.adamant_boltsunf", "Adamant bolts (unf)", "item.adamantite_bar", 1, 61, 125.0, quantity = 10),
        
        // Rune bolts (10 unfinished bolts per bar)
        SmithingRecipe("item.runite_bolts_unf", "Rune bolts (unf)", "item.runite_bar", 1, 88, 150.0, quantity = 10),
        
        // Bronze knives (5 knives per bar)
        SmithingRecipe("item.bronze_knife", "Bronze knife", "item.bronze_bar", 1, 5, 12.5, quantity = 5),
        
        // Iron knives (5 knives per bar)
        SmithingRecipe("item.iron_knife", "Iron knife", "item.iron_bar", 1, 20, 25.0, quantity = 5),
        
        // Steel knives (5 knives per bar)
        SmithingRecipe("item.steel_knife", "Steel knife", "item.steel_bar", 1, 35, 37.5, quantity = 5),
        
        // Mithril knives (5 knives per bar)
        SmithingRecipe("item.mithril_knife", "Mithril knife", "item.mithril_bar", 1, 55, 50.0, quantity = 5),
        
        // Adamant knives (5 knives per bar)
        SmithingRecipe("item.adamant_knife", "Adamant knife", "item.adamantite_bar", 1, 75, 62.5, quantity = 5),
        
        // Rune knives (5 knives per bar)
        SmithingRecipe("item.rune_knife", "Rune knife", "item.runite_bar", 1, 90, 75.0, quantity = 5),
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
        // Must be more specific to avoid false positives (bank booths, crevices, etc.)
        fun isFurnaceByExamine(objId: Int): Boolean {
            val examine = ObjectExamineHolder.EXAMINES.get(objId) ?: return false
            val lowerExamine = examine.lowercase()
            // Only match if it explicitly mentions "furnace" AND "hot"
            // This avoids matching other objects that might be "hot" or have similar text
            return (lowerExamine.contains("hot") && lowerExamine.contains("furnace")) ||
                   lowerExamine.contains("very hot") && lowerExamine.contains("furnace")
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
        
        // Track object IDs that are explicitly registered to avoid duplicate registration in dynamic detection
        val explicitlyRegisteredObjectIds = mutableSetOf<Int>()
        
        // Handle furnace click to open smelting menu (custom chat window)
        // Register handlers for all known furnace objects
        furnaceObjects.forEach { furnace ->
            try {
                val objId = getRSCM(furnace)
                explicitlyRegisteredObjectIds.add(objId)
            } catch (e: Exception) {
                // Object doesn't exist in RSCM, skip
            }
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
        // Skip objects that were already explicitly registered to avoid duplicate bindings
        onWorldInit {
            val registeredCombinations = mutableSetOf<Pair<Int, String>>()
            val allObjects = dev.openrune.cache.CacheManager.getObjects()
            
            allObjects.forEach { (objId, objDef) ->
                // Skip objects that were explicitly registered in furnaceObjects
                if (explicitlyRegisteredObjectIds.contains(objId)) {
                    return@forEach
                }
                
                // Check if object has smelting options OR is a furnace by examine text
                val smeltOptions = objDef.actions.filterNotNull().filter { isSmeltingOption(it) }
                val isFurnace = isFurnaceByExamine(objId)
                
                // Only register if we have explicit smelting options OR confirmed furnace by examine
                // Prefer explicit smelting options over generic "use" to avoid conflicts
                val optionsToTry = if (smeltOptions.isNotEmpty()) {
                    smeltOptions
                } else if (isFurnace) {
                    // Only try "use" if we're confident it's a furnace by examine text
                    // Be more conservative - only try "use" if no other plugins likely own it
                    objDef.actions.filterNotNull().filter { 
                        val lower = it.lowercase()
                        // Prefer "smelt" variants, only fall back to "use" if no smelt options exist
                        lower.contains("smelt") || (lower == "use" && smeltOptions.isEmpty())
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
                        } catch (e: IllegalStateException) {
                            // Object/option already bound to another plugin, skip silently
                            // This is expected for objects like bank booths, crevices, etc.
                        } catch (e: Exception) {
                            // Other errors - skip
                        }
                    }
                }
            }
        }

        // Handle anvil smithing
        anvilObjects.forEach { anvil ->
            onObjOption(obj = anvil, option = "Smith") {
                player.queue {
                    openSmithingMenu(player)
                }
            }
        }
    }

    /**
     * Opens the paginated smithing menu for the player
     */
    private suspend fun QueueTask.openSmithingMenu(player: Player) {
        // Check if player has a hammer
        val hammerId = getRSCM("item.hammer")
        if (!player.inventory.contains(hammerId)) {
            player.message("You need a hammer to smith items.")
            return
        }

        // Filter recipes by what the player can make (has bars and level)
        val availableRecipes = smithingRecipes.filter { recipe ->
            val barId = getRSCM(recipe.barName)
            val hasBars = player.hasItemIncludingNoted(barId) && 
                         player.getItemCountIncludingNoted(barId) >= recipe.barsRequired
            val hasLevel = player.getSkills().getCurrentLevel(Skills.SMITHING) >= recipe.level
            hasBars && hasLevel
        }

        if (availableRecipes.isEmpty()) {
            player.message("You don't have the required bars or level to smith anything.")
            return
        }

        // Pagination settings - 3 items per page (5 total options: Prev + 3 items + Next)
        val itemsPerPage = 3
        val totalPages = (availableRecipes.size + itemsPerPage - 1) / itemsPerPage
        
        var currentPage = 0
        
        while (true) {
            val startIndex = currentPage * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, availableRecipes.size)
            val pageRecipes = availableRecipes.subList(startIndex, endIndex)
            
            // Build options list for current page
            // Structure: [Previous Page] -> [Items (3 max)] -> [Next Page]
            val pageOptions = mutableListOf<String>()
            
            // Check if we have previous/next pages available
            val hasPrevious = currentPage > 0
            val hasNext = currentPage < totalPages - 1
            
            // Option 1: Previous Page (always shown as 1st option)
            pageOptions.add("Previous Page")
            
            // Options 2-4: Add item options (3 items max)
            pageOptions.addAll(pageRecipes.map { recipe ->
                val barId = getRSCM(recipe.barName)
                val barCount = player.getItemCountIncludingNoted(barId)
                "${recipe.displayName} (${recipe.barsRequired} bars) - You have: $barCount"
            })
            
            // Last option: Next Page (always shown as last option)
            pageOptions.add("Next Page")
            
            val title = "What would you like to smith? (Page ${currentPage + 1}/$totalPages)"
            val selected = options(player, *pageOptions.toTypedArray(), title = title)
            
            if (selected <= 0) {
                break
            }
            
            // selected is 1-based (1 = first option, 2 = second option, etc.)
            val optionIndex = selected - 1
            val itemCount = pageRecipes.size
            
            // Handle the selected option based on its position in the menu
            // Menu structure: [0: Previous Page] [1-3: Items] [4: Next Page]
            
            // Option 0: Previous Page
            if (optionIndex == 0) {
                if (hasPrevious) {
                    currentPage--
                }
                continue
            }
            
            // Check if it's the last option (Next Page)
            val nextPageIndex = 1 + itemCount
            if (optionIndex == nextPageIndex) {
                if (hasNext) {
                    currentPage++
                }
                continue
            }
            
            // Check if it's an item selection
            // Items are at indices 1 to itemCount (after the Previous Page button)
            if (optionIndex >= 1 && optionIndex <= itemCount) {
                // Convert to 0-based index for the pageRecipes list
                val recipeIndex = optionIndex - 1
                val recipe = pageRecipes[recipeIndex]
                smithItem(this, player, recipe)
                break
            }
        }
    }

    /**
     * Smiths an item at an anvil continuously until materials run out
     */
    private suspend fun smithItem(
        task: QueueTask,
        player: Player,
        recipe: SmithingRecipe
    ) {
        val smithingLevel = player.getSkills().getCurrentLevel(Skills.SMITHING)
        val barId = getRSCM(recipe.barName)
        val itemId = getRSCM(recipe.itemName)
        val notedItemId = getNotedItemId(itemId)
        val hammerId = getRSCM("item.hammer")

        if (smithingLevel < recipe.level) {
            player.message("You need a Smithing level of ${recipe.level} to smith this item.")
            return
        }

        if (!player.inventory.contains(hammerId)) {
            player.message("You need a hammer to smith items.")
            return
        }

        // Track starting position
        val startTile = player.tile
        var smithedCount = 0

        // Continuously smith until materials run out or player stops
        while (true) {
            // Check if player has moved or closed interface
            if (!player.isOnline || player.hasMoveDestination() || !player.tile.sameAs(startTile)) {
                break
            }

            // Check level requirement
            if (player.getSkills().getCurrentLevel(Skills.SMITHING) < recipe.level) {
                player.message("You need a Smithing level of ${recipe.level} to smith this item.")
                break
            }

            // Check for hammer
            if (!player.inventory.contains(hammerId)) {
                if (smithedCount == 0) {
                    player.message("You need a hammer to smith items.")
                }
                break
            }

            // Check for bars
            val barCount = player.getItemCountIncludingNoted(barId)
            if (barCount < recipe.barsRequired) {
                if (smithedCount == 0) {
                    player.message("You need ${recipe.barsRequired} ${recipe.barName.replace("item.", "").replace("_", " ")} to smith this item.")
                }
                break
            }

            // Check inventory space - consider both noted and unnoted items
            // For items with quantity > 1, we need to check if we have enough space
            val hasItemSpace = if (recipe.quantity > 1) {
                // For multiple items, check if we can add them (either stack on existing or have free slots)
                val existingCount = player.inventory.getItemCount(notedItemId) + player.inventory.getItemCount(itemId)
                val freeSlots = player.inventory.freeSlotCount
                existingCount > 0 || freeSlots >= 1
            } else {
                player.inventory.contains(notedItemId) || player.inventory.contains(itemId)
            }
            if (player.inventory.isFull && !hasItemSpace) {
                if (smithedCount == 0) {
                    player.message("You don't have enough inventory space.")
                }
                break
            }

            // Check again before proceeding
            if (!player.isOnline || player.hasMoveDestination() || !player.tile.sameAs(startTile)) {
                break
            }

            player.lock()
            try {
                player.animate(Animation.SMITHING_ANVIL)
                
                // Wait for smithing animation with interruption checks
                var interrupted = false
                for (i in 0 until 4) {
                    if (!player.isOnline || player.hasMoveDestination() || !player.tile.sameAs(startTile)) {
                        interrupted = true
                        break
                    }
                    task.wait(1)
                }

                if (interrupted) {
                    break
                }

                // Final check after waiting
                if (!player.isOnline || player.hasMoveDestination() || !player.tile.sameAs(startTile)) {
                    break
                }

                // Re-check materials after waiting
                if (!player.hasItemIncludingNoted(barId) || 
                    player.getItemCountIncludingNoted(barId) < recipe.barsRequired) {
                    break
                }

                // Remove bars
                if (!player.removeItemIncludingNoted(barId, recipe.barsRequired)) {
                    break
                }

                // Add noted item (quantity from recipe, default 1)
                player.inventory.add(notedItemId, recipe.quantity)
                player.addXp(Skills.SMITHING, recipe.experience)
                smithedCount++
            } finally {
                player.unlock()
            }

            // Small delay between smithing cycles
            if (!player.isOnline || player.hasMoveDestination() || !player.tile.sameAs(startTile)) {
                break
            }
            task.wait(1)
        }

        // Show summary message
        if (smithedCount > 1) {
            val itemName = recipe.displayName
            player.message("You smith $smithedCount $itemName${if (smithedCount > 1) "s" else ""}.")
        } else if (smithedCount == 1) {
            val itemName = recipe.displayName
            player.message("You smith a $itemName.")
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

