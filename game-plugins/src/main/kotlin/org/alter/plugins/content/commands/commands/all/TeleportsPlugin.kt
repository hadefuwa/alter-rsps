package org.alter.plugins.content.commands.commands.all

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.priv.Privilege
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport
import org.alter.plugins.content.magic.TeleportType

class TeleportsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        /**
         * Home teleport button handler for all spellbooks (interface 218, 219, 220, 221, component 7).
         * Left-click (option 1 - "Cast"): Instantly teleports player to home location.
         * Right-click option 10 ("Animation"): Opens teleport menu.
         * 
         * NOTE: The option names "Cast" and "Animation" are defined in the client cache
         * and cannot be changed dynamically. We're using option 10 ("Animation") to trigger
         * the teleport menu.
         * 
         * Different spellbooks use different interface IDs:
         * - Standard: 218
         * - Ancient: 219
         * - Lunar: 220
         * - Arceuus: 221
         * 
         * TO DISABLE TELEPORT MENU: Comment out the entire "if (option == 10)" block below
         */
        // Register home teleport handler for all spellbook interfaces
        val spellbookInterfaces = listOf(218, 219, 220, 221)
        spellbookInterfaces.forEach { interfaceId ->
            onButton(interfaceId = interfaceId, component = 7) {
                val option = player.getInteractingOption()
            
            // Right-click option 10 ("Animation" in menu) - Teleport Menu
            // TO DISABLE: Comment out this entire if block
            if (option == 10) {
                if (!player.lock.canTeleport()) {
                    return@onButton
                }
                
                // Check wilderness level restriction for home teleport
                val wildernessLevel = player.tile.getWildernessLevel()
                if (wildernessLevel > 30) {
                    player.message("A mysterious force blocks your teleport spell!")
                    player.message("You can't use this teleport after level 30 wilderness.")
                    return@onButton
                }

                // Define all teleport locations
                val allTeleportLocations = listOf(
                    // Page 1: Main Cities
                    "Home" to world.gameContext.home,
                    "Varrock" to Tile(x = 3211, z = 3424, height = 0),
                    "Lumbridge" to Tile(x = 3222, z = 3217, height = 0),
                    "Falador" to Tile(x = 2966, z = 3379, height = 0),
                    "Edgeville" to Tile(x = 3087, z = 3499, height = 0),
                    "Yanille" to Tile(x = 2606, z = 3093, height = 0),
                    "Gnome Stronghold" to Tile(x = 2461, z = 3443, height = 0),
                    "Camelot" to Tile(x = 2756, z = 3476, height = 0),
                    "Ardougne" to Tile(x = 2659, z = 3300, height = 0),
                    // Page 2: Wilderness & PvP Areas
                    "Mage Bank" to Tile(x = 2539, z = 4716, height = 0),
                    "Lava Dragon Isle" to Tile(x = 3200, z = 3856, height = 0),
                    "Wilderness Volcano" to Tile(x = 3369, z = 3930, height = 0),
                    "Graveyard of Shadows" to Tile(x = 2978, z = 3650, height = 0),
                    "Dark Warriors' Fortress" to Tile(x = 3038, z = 3642, height = 0),
                    "Chaos Temple" to Tile(x = 2964, z = 3819, height = 0),
                    "Bandit Camp" to Tile(x = 3038, z = 3689, height = 0),
                    "Resource Area" to Tile(x = 3184, z = 3944, height = 0),
                    "Boss Island" to Tile(x = 3423, z = 4089, height = 0),
                    // Page 3: Dungeons & Caves
                    "Taverley Dungeon" to Tile(x = 2884, z = 9798, height = 0),
                    "Brimhaven Dungeon" to Tile(x = 2708, z = 9564, height = 0),
                    "Ancient Cavern" to Tile(x = 1764, z = 5365, height = 0),
                    "God Wars Dungeon" to Tile(x = 2918, z = 3746, height = 0),
                    "Slayer Tower" to Tile(x = 3428, z = 3537, height = 0),
                    "Stronghold of Security" to Tile(x = 3081, z = 3420, height = 0),
                    "Stronghold of Player Safety" to Tile(x = 3081, z = 3420, height = 0),
                    "TzHaar City" to Tile(x = 2436, z = 5171, height = 0),
                    // Page 4: Skilling Locations
                    "Seers' Village" to Tile(x = 2725, z = 3486, height = 0),
                    "Catherby" to Tile(x = 2804, z = 3433, height = 0),
                    "Fishing Guild" to Tile(x = 2611, z = 3391, height = 0),
                    "Mining Guild" to Tile(x = 3046, z = 9756, height = 0),
                    "Crafting Guild" to Tile(x = 2933, z = 3289, height = 0),
                    "Rimmington" to Tile(x = 2954, z = 3214, height = 0),
                    "Port Sarim" to Tile(x = 3014, z = 3176, height = 0),
                    "Draynor Village" to Tile(x = 3093, z = 3244, height = 0),
                    // Page 5: More Cities & Towns
                    "Al Kharid" to Tile(x = 3293, z = 3174, height = 0),
                    "Duel Arena" to Tile(x = 3366, z = 3266, height = 0),
                    "Shantay Pass" to Tile(x = 3304, z = 3116, height = 0),
                    "Pollnivneach" to Tile(x = 3350, z = 2964, height = 0),
                    "Nardah" to Tile(x = 3426, z = 2914, height = 0),
                    "Sophanem" to Tile(x = 3318, z = 2796, height = 0),
                    "Ape Atoll" to Tile(x = 2754, z = 2784, height = 0),
                    "Karamja" to Tile(x = 2944, z = 3146, height = 0),
                    // Page 6: Special Locations
                    "TzHaar Fight Cave" to Tile(x = 2413, z = 5117, height = 0),
                    "TzHaar Fight Pit" to Tile(x = 2398, z = 5177, height = 0),
                    "Barbarian Outpost" to Tile(x = 2516, z = 3571, height = 0),
                    "Barbarian Village" to Tile(x = 3081, z = 3420, height = 0),
                    "Burthorpe" to Tile(x = 2899, z = 3544, height = 0),
                    "Taverley" to Tile(x = 2894, z = 3456, height = 0),
                    "Rellekka" to Tile(x = 2657, z = 3659, height = 0),
                    "Jatizso" to Tile(x = 2400, z = 3808, height = 0),
                )

                // Pagination settings - 3 locations per page (5 total options: Prev + 3 locations + Next)
                val locationsPerPage = 3
                val totalPages = (allTeleportLocations.size + locationsPerPage - 1) / locationsPerPage

                player.queue(TaskPriority.STRONG) {
                    var currentPage = 0

                    while (true) {
                        val startIndex = currentPage * locationsPerPage
                        val endIndex = minOf(startIndex + locationsPerPage, allTeleportLocations.size)
                        val pageLocations = allTeleportLocations.subList(startIndex, endIndex)

                        // Build options list for current page
                        // Structure: [Previous Page] -> [Locations (3 max)] -> [Next Page]
                        // Total: 5 options maximum
                        val pageOptions = mutableListOf<String>()

                        // Check if we have previous/next pages available
                        val hasPrevious = currentPage > 0
                        val hasNext = currentPage < totalPages - 1

                        // Option 1: Previous Page (always shown as 1st option)
                        pageOptions.add("Previous Page")

                        // Options 2-4: Add location options (3 locations max)
                        pageOptions.addAll(pageLocations.map { it.first })

                        // Option 5: Next Page (always shown as last option)
                        pageOptions.add("Next Page")

                        val title = "Teleport Menu (Page ${currentPage + 1}/$totalPages)"
                        val selected = options(player, *pageOptions.toTypedArray(), title = title)

                        if (selected <= 0) {
                            break // Invalid selection
                        }

                        // selected is 1-based (1 = first option, 2 = second option, etc.)
                        // Convert to 0-based index for easier array access
                        val optionIndex = selected - 1
                        val locationCount = pageLocations.size

                        // Handle the selected option based on its position in the menu
                        // Menu structure: [0: Previous Page] [1-3: Locations] [4: Next Page]

                        // Option 0: Previous Page
                        if (optionIndex == 0) {
                            if (hasPrevious) {
                                // User clicked "Previous Page" - go back one page
                                currentPage--
                            }
                            // If no previous page, just ignore and re-show current page
                            continue
                        }

                        // Check if it's the last option (Next Page)
                        // Last option is at index: 1 (prev button) + locationCount (locations)
                        val nextPageIndex = 1 + locationCount
                        if (optionIndex == nextPageIndex) {
                            if (hasNext) {
                                // User clicked "Next Page" - go forward one page
                                currentPage++
                            }
                            // If no next page, just ignore and re-show current page
                            continue
                        }

                        // Check if it's a location selection
                        // Locations are at indices 1 to locationCount (after the Previous Page button)
                        // Example: If there are 3 locations, they're at indices 1, 2, 3
                        if (optionIndex >= 1 && optionIndex <= locationCount) {
                            // Convert to 0-based index for the pageLocations list
                            // optionIndex 1 becomes locationIndex 0 (first location)
                            val locationIndex = optionIndex - 1
                            val destination = pageLocations[locationIndex].second
                            player.prepareForTeleport()
                            player.moveTo(destination)
                            break // Exit the loop - player is teleporting
                        }
                    }
                }
                return@onButton
            }

            // Left-click option (option 1) - Instant home teleport
            if (!player.lock.canTeleport()) {
                return@onButton
            }
            
            // Check wilderness level restriction for home teleport
            val wildernessLevel = player.tile.getWildernessLevel()
            if (wildernessLevel > 30) {
                player.message("A mysterious force blocks your teleport spell!")
                player.message("You can't use this teleport after level 30 wilderness.")
                return@onButton
            }

            val home = world.gameContext.home
            // Use the teleport function with MODERN type for proper animations and behavior
            player.teleport(home, TeleportType.MODERN)
            }
        }

        onCommand("home", description = "Teleports you home") {
            val home = world.gameContext.home
            player.moveTo(home)
        }
        
        onCommand("edge", Privilege.ADMIN_POWER, description = "Teleports you to Edgeville") {
            player.moveTo(Tile(x = 3087, z = 3499, height = 0))
        }

        onCommand("varrock", Privilege.ADMIN_POWER, description = "Teleports you to Varrock") {
            player.moveTo(Tile(x = 3211, z = 3424, height = 0))
        }
        
        onCommand("falador", Privilege.ADMIN_POWER, description = "Teleports you to Falador") {
            player.moveTo(Tile(x = 2966, z = 3379, height = 0))
        }
        
        onCommand("lumbridge", Privilege.ADMIN_POWER, description = "Teleports you to Lumbridge") {
            player.moveTo(Tile(x = 3222, z = 3217, height = 0))
        }
        
        onCommand("yanille", Privilege.ADMIN_POWER, description = "Teleports you to Yanille") {
            player.moveTo(Tile(x = 2606, z = 3093, height = 0))
        }
        
        onCommand("gnome", Privilege.ADMIN_POWER, description = "Teleports you to Gnome Stronghold") {
            player.moveTo(Tile(x = 2461, z = 3443, height = 0))
        }
        
        onCommand("thieving", description = "Teleports you to the test thieving") {
            player.moveTo(Tile(x = 2591, z = 4731, height = 0))
        }
    }
}