package org.alter.plugins.content.objects.nexusportal

import dev.openrune.cache.CacheManager.getObject
import net.rsprot.protocol.game.incoming.resumed.ResumePauseButton
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.*
import org.alter.plugins.content.magic.prepareForTeleport
import org.alter.rscm.RSCM.getRSCM

/**
 * Nexus Portal Teleportation Plugin
 *
 * This plugin creates a Portal Nexus-style teleportation system similar to POH portals.
 * Players can interact with a portal object to access a menu of teleport destinations.
 *
 * Features:
 * - Portal object interaction (right-click)
 * - Paginated teleport menu
 * - Multiple teleport categories
 * - Varrock Centre and other major locations
 *
 * Object IDs used:
 * - 409: Portal (generic portal object)
 * - Can be customized to use other portal object IDs
 */
class NexusPortalPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * All available teleport destinations organized by category
         */
        private val TELEPORT_LOCATIONS = listOf(
            // Bosses
            "Cerberus" to Tile(x = 1240, z = 1253, height = 0),
            "Obor's Lair" to Tile(x = 3107, z = 9831, height = 0),
            "Jormungands Prison" to Tile(x = 2437, z = 3936, height = 0),
            // Cities & Towns
            "God Wars Dungeon" to Tile(x = 2881, z = 5309, height = 2),
            "Revenants South" to Tile(x = 3197, z = 10056, height = 0),
            "Revenants Orks" to Tile(x = 3214, z = 10097, height = 0),
            "Revenants North" to Tile(x = 3235, z = 10198, height = 0),
            "Taverley Dungeon" to Tile(x = 2884, z = 9798, height = 0),
            "Slayer Tower" to Tile(x = 3428, z = 3537, height = 0),
            "Kalphite Queen Dungeon" to Tile(x = 3499, z = 9492, height = 0),
            "Stronghold of Security" to Tile(x = 3081, z = 3420, height = 0),
            "TzHaar City" to Tile(x = 2436, z = 5171, height = 0),
            "Brimhaven Dungeon Steel/Iron dragons" to Tile(x = 2722, z = 9443, height = 0),
            "Varrock Sewers" to Tile(x = 3239, z = 9866, height = 0),
            "Bounty Hunter" to Tile(x = 3423, z = 4089, height = 0),
            "Blue Dragons" to Tile(x = 2899, z = 9802, height = 0),
            "Crazy Archaeologist" to Tile(x = 2986, z = 3702, height = 0),
            "White Wolves" to Tile(x = 2856, z = 3482, height = 0),


            "Varrock Centre" to Tile(x = 3213, z = 3428, height = 0),
            "Varrock East Bank" to Tile(x = 3253, z = 3420, height = 0),
            "Varrock West Bank" to Tile(x = 3185, z = 3436, height = 0),
            "Lumbridge" to Tile(x = 3222, z = 3217, height = 0),
            "Falador" to Tile(x = 2966, z = 3379, height = 0),
            "Edgeville" to Tile(x = 3087, z = 3499, height = 0),
            "Ardougne" to Tile(x = 2659, z = 3300, height = 0),
            "Camelot" to Tile(x = 2756, z = 3476, height = 0),
            "Yanille" to Tile(x = 2606, z = 3093, height = 0),
            "Seers' Village" to Tile(x = 2725, z = 3486, height = 0),
            "Draynor Village" to Tile(x = 3093, z = 3244, height = 0),
            "Port Sarim" to Tile(x = 3014, z = 3176, height = 0),
            "Rimmington" to Tile(x = 2954, z = 3214, height = 0),
            "Taverley" to Tile(x = 2894, z = 3456, height = 0),
            "Burthorpe" to Tile(x = 2899, z = 3544, height = 0),
            "Rellekka" to Tile(x = 2657, z = 3659, height = 0),
            "Jatizso" to Tile(x = 2400, z = 3808, height = 0),
            "Gnome Stronghold" to Tile(x = 2461, z = 3443, height = 0),

            // Desert
            "Al Kharid" to Tile(x = 3293, z = 3174, height = 0),
            "Shantay Pass" to Tile(x = 3304, z = 3116, height = 0),
            "Pollnivneach" to Tile(x = 3350, z = 2964, height = 0),
            "Nardah" to Tile(x = 3426, z = 2914, height = 0),
            "Duel Arena" to Tile(x = 3366, z = 3266, height = 0),
            "Sophanem" to Tile(x = 3318, z = 2796, height = 0),

            // Wilderness
            "Wilderness: Edgeville" to Tile(x = 3087, z = 3499, height = 0),
            "Wilderness: Mage Bank" to Tile(x = 2539, z = 4716, height = 0),
            "Wilderness: Lava Dragon Isle" to Tile(x = 3200, z = 3856, height = 0),
            "Wilderness: Resource Area" to Tile(x = 3184, z = 3944, height = 0),
            "Wilderness: Volcano" to Tile(x = 3369, z = 3930, height = 0),
            "Wilderness: Graveyard of Shadows" to Tile(x = 2978, z = 3650, height = 0),
            "Wilderness: Dark Warriors' Fortress" to Tile(x = 3012, z = 3632, height = 0),
            "Wilderness: Chaos Temple" to Tile(x = 2964, z = 3819, height = 0),
            "Wilderness: Bandit Camp" to Tile(x = 3038, z = 3689, height = 0),

            // Skilling Locations
            "Catherby" to Tile(x = 2804, z = 3433, height = 0),
            "Fishing Guild" to Tile(x = 2611, z = 3391, height = 0),
            "Mining Guild" to Tile(x = 3046, z = 9756, height = 0),
            "Crafting Guild" to Tile(x = 2933, z = 3289, height = 0),
            "Barbarian Village" to Tile(x = 3081, z = 3420, height = 0),
            "Barbarian Outpost" to Tile(x = 2516, z = 3571, height = 0),

            // Dungeons & Caves
            "Taverley Dungeon" to Tile(x = 2884, z = 9798, height = 0),
            "Brimhaven Dungeon" to Tile(x = 2708, z = 9564, height = 0),
            "Ancient Cavern" to Tile(x = 1767, z = 5363, height = 0),
            "God Wars Dungeon" to Tile(x = 2918, z = 3746, height = 0),
            "Slayer Tower" to Tile(x = 3428, z = 3537, height = 0),
            "Stronghold of Security" to Tile(x = 3081, z = 3420, height = 0),
            "TzHaar City" to Tile(x = 2436, z = 5171, height = 0),

            // Special & Island Locations
            "Karamja" to Tile(x = 2944, z = 3146, height = 0),
            "Ape Atoll" to Tile(x = 2754, z = 2784, height = 0),
            "TzHaar Fight Cave" to Tile(x = 2413, z = 5117, height = 0),
            "TzHaar Fight Pit" to Tile(x = 2398, z = 5177, height = 0),

            // From TELEPORT_LOCATIONS.md
            "Stronghold of Player Safety" to Tile(x = 3081, z = 3420, height = 0),
            "Thieving Test Area" to Tile(x = 2591, z = 4731, height = 0),
            // Imported from TELEPORT_LOCATIONS.md

            // Main Cities
            "Varrock" to Tile(x = 3211, z = 3424, height = 0),

            // Wilderness & PvP Areas
            "Mage Bank" to Tile(x = 2539, z = 4716, height = 0),
            "Lava Dragon Isle" to Tile(x = 3200, z = 3856, height = 0),
            "Wilderness Volcano" to Tile(x = 3369, z = 3930, height = 0),
            "Graveyard of Shadows" to Tile(x = 2978, z = 3650, height = 0),
            "Dark Warriors' Fortress" to Tile(x = 3038, z = 3642, height = 0),
            "Chaos Temple" to Tile(x = 2964, z = 3819, height = 0),
            "Bandit Camp" to Tile(x = 3038, z = 3689, height = 0),
            "Resource Area" to Tile(x = 3184, z = 3944, height = 0),

            // Coordinate System Notes
            "Jatizso Mine" to Tile(x = 2406, z = 10190, height = 0),
            "Waterbirth First floor" to Tile(x = 2448, z = 10147, height = 0),
            "Penguin HQ" to Tile(x = 2658, z = 10386, height = 0),
            "Miscellenia Dungeon" to Tile(x = 2512, z = 10256, height = 0),
            "Eagle Cave (snow)" to Tile(x = 2718, z = 10221, height = 0),
            "Olafs Quest Dungeon" to Tile(x = 2691, z = 10125, height = 0),
            "Troll Path Snow" to Tile(x = 2788, z = 10198, height = 0),
            "Cave to  Zemouregal castle (directly west of Trollweiss Mountain)" to Tile(x = 2810, z = 10264, height = 0),
            "Tale of Muspah buriel cave w/ jellies" to Tile(x = 2833, z = 10330, height = 0),
            "Temple at Senntisten dungeon 1" to Tile(x = 2903, z = 10415, height = 0),
            "Temple at Senntisten dungeon 2" to Tile(x = 2948, z = 10350, height = 0),
            "Wilderness Agility pitfall" to Tile(x = 3004, z = 10353, height = 0),
            "Wilderness Firegiant dungeon" to Tile(x = 3042, z = 10343, height = 0),
            "KBD lever" to Tile(x = 3168, z = 10257, height = 0),
            "Wilderness Black Dragon Dungeon" to Tile(x = 3021, z = 10250, height = 0),
            "Tzhaar Fightpits" to Tile(x = 2398, z = 5151, height = 0),
            "Tzhaar Fightcaves" to Tile(x = 2412, z = 5107, height = 0),
            "Tzhaar Bank" to Tile(x = 2447, z = 5178, height = 0),
            "Keldagrim Cave" to Tile(x = 2838, z = 10125, height = 0),
            "-North mines" to Tile(x = 2870, z = 10250, height = 0),
            "-South mines" to Tile(x = 3040, z = 10243, height = 0),
            "-bank" to Tile(x = 2837, z = 10208, height = 0),
            "Trollhiem Cages" to Tile(x = 2934, z = 10080, height = 0),
            "trollheim goutweed maze thing" to Tile(x = 2860, z = 10081, height = 0),
            "Mountain daughter dungeon/The Kendal" to Tile(x = 2787, z = 10077, height = 0),
            "Death Plateau Saba's Cave" to Tile(x = 2893, z = 10086, height = 0),
            "*Small cave w/ crates" to Tile(x = 2933, z = 10085, height = 0),
            "Fremink Slayer Cave" to Tile(x = 2795, z = 9988, height = 0),
            "Small Troll Cave" to Tile(x = 2921, z = 10024, height = 0),
            "Ice Queen Cave" to Tile(x = 2865, z = 9951, height = 0),
            "Heros' Guild Dungeon" to Tile(x = 2894, z = 9907, height = 0),
            "Dwarf WhiteWolf shortcut" to Tile(x = 2869, z = 9878, height = 0),
            //"Living Rock Caverns" to Tile(x = 3636, z = 5099, height = 0),
            "Taverly Dungeon" to Tile(x = 2884, z = 9798, height = 0),
            "Recipe For Disaster Goblin Village Kitchen" to Tile(x = 2980, z = 9910, height = 0),
            "Recipe For Disaster Goblin Village exploded" to Tile(x = 2979, z = 9868, height = 0),
            "Camdozaal (Sacred Forge) Under Ice mountain" to Tile(x = 3038, z = 9889, height = 0),
            "Elite Black Knight Cave(goup)" to Tile(x = 3060, z = 9947, height = 0),
            "Evil Daves Dungeon" to Tile(x = 3079, z = 9886, height = 0),
            "Edgeville Dungeon Enterance" to Tile(x = 3096, z = 9873, height = 0),
            "Edgeville Dungeon Hill Giants" to Tile(x = 3111, z = 9836, height = 0),
            "Edgeville Dungeon Wilderness Druids" to Tile(x = 3115, z = 9930, height = 0),
            "Varrock Dungeon Moss Giants" to Tile(x = 3159, z = 9903, height = 0),
            "Varrock Sewers" to Tile(x = 3239, z = 9866, height = 0),
            "Varrock West Bank Basement" to Tile(x = 3191, z = 9826, height = 0),
            "Champions Challenge/Champions Guild Basement" to Tile(x = 3167, z = 9767, height = 0),
            "Draynor Manor Basement (ernest chicken w/ levers)" to Tile(x = 3112, z = 9754, height = 0),
            "Count Draynors Coffin" to Tile(x = 3080, z = 9777, height = 0),
            "Dwarven Mines Dragon Slayer Chest" to Tile(x = 3055, z = 9840, height = 0),
            "Dwarven Mines Mining Guild" to Tile(x = 3042, z = 9743, height = 0),
            "Dwarven Mines Pickaxe Shop" to Tile(x = 2997, z = 9845, height = 0),
            "Stealing Creation???" to Tile(x = 2969, z = 9703, height = 0),
            "Falador Artisans Workshop Dungeon" to Tile(x = 3060, z = 9709, height = 0),
            "Draynor Sewers" to Tile(x = 3110, z = 9686, height = 0),
            "Port Sarim Rat Pits" to Tile(x = 2987, z = 9637, height = 0),
            "Asgarnia Ice Dungeon Ice Warriors/Giants" to Tile(x = 3050, z = 9581, height = 0),
            "Asgarnia Ice Dungeon Skeletal Wyverns" to Tile(x = 3050, z = 9549, height = 0),
            "Melzars Maze" to Tile(x = 2936, z = 9651, height = 0),
            "Crandor Dungeon Elvarg" to Tile(x = 2853, z = 9637, height = 0),
            "Karamja Volcano Lesser Demons" to Tile(x = 2837, z = 9560, height = 0),
            "Brimhaven Agility Dungeon" to Tile(x = 2805, z = 9590, height = 0),
            "Brimhaven Dungeon Steel/Iron dragons" to Tile(x = 2722, z = 9443, height = 0),
            "Brimhaven Dungeon Start" to Tile(x = 2703, z = 9566, height = 0),
            "Brimhaven Dungeon Red Dragons" to Tile(x = 2703, z = 9514, height = 0),
            "Karamja Jogre Cave (Jungle Potion)" to Tile(x = 2838, z = 9520, height = 0),
            "Ogre City Greater Demon Pit" to Tile(x = 2617, z = 9424, height = 0),
            "Ogre City Blue Dragons" to Tile(x = 2574, z = 9448, height = 0),
            "Mages Guild Basement" to Tile(x = 2591, z = 9488, height = 0),
            "Yanille Agility Dungeon - End" to Tile(x = 2614, z = 9521, height = 0),
            "Yanille Agility Dungeon Start" to Tile(x = 2570, z = 9526, height = 0),
            "Yanille Agility Dungoen Chest" to Tile(x = 2564, z = 9507, height = 0),
            "Yanille Agility Dungeon Poison Spider Pit Trap" to Tile(x = 2578, z = 9578, height = 0),
            "Yanille Agility Dungeon stairs & rubble before end" to Tile(x = 2616, z = 9568, height = 0),
            "Tree Gnome Village Dungeon" to Tile(x = 2531, z = 9556, height = 0),
            "Ogre/Zogre  Slash Bash cave" to Tile(x = 2448, z = 9428, height = 0),
            "Castle Wars Zamorak Waiting Area" to Tile(x = 2421, z = 9522, height = 0),
            "Castle Wars Saradomin Waiting Area" to Tile(x = 2379, z = 9490, height = 0),
            "Caslte Wars Dungeon" to Tile(x = 2401, z = 9506, height = 0),
            "Feldip Hills Cave (I think ,no map data part of quest)(no ! on map)" to Tile(x = 2520, z = 9322, height = 0),
            "Feldip Hills Cave (same as above, could be Rantz cave)" to Tile(x = 2647, z = 9392, height = 0),
            "Mobolising Armies Basement" to Tile(x = 2393, z = 9245, height = 0),
            "Observatory Dungeon (no map data but 99% sure)" to Tile(x = 2356, z = 9391, height = 0),
            "Underground Pass Exit to Elven Area" to Tile(x = 2316, z = 9625, height = 0),
            "Underground Pass Pitfall" to Tile(x = 2355, z = 9644, height = 0),
            "Underground Pass Fail" to Tile(x = 2480, z = 9606, height = 0),
            "Underground Pass Start" to Tile(x = 2493, z = 9716, height = 0),
            "Underground Pass Grid" to Tile(x = 2473, z = 9679, height = 0),
            "Underground Pass before killing unicorn" to Tile(x = 2397, z = 9605, height = 0),
            "Underground Pass After killing unicorn" to Tile(x = 2373, z = 9606, height = 0),
            "Underground Pass Giant Spider" to Tile(x = 2358, z = 9911, height = 0),
            "Underground Pass Dwarf Camp" to Tile(x = 2313, z = 9807, height = 0),
            "Underground Pass Tomb" to Tile(x = 2355, z = 9801, height = 0),
            "Gnome Stronghold South-West House Dungeon" to Tile(x = 2388, z = 9821, height = 0),
            "Gnome Stronghold Grand Tree Black Demon fight" to Tile(x = 2483, z = 9865, height = 0),
            "Gnome Stronghold Hanger (Monkey Maddness)" to Tile(x = 2390, z = 9895, height = 0),
            "Lighthouse Dungeon" to Tile(x = 2521, z = 10018, height = 0),
            "Piscatoris Summoning Obelisk" to Tile(x = 2333, z = 10013, height = 0),
            "Relleka Dungeon Thorvald the Warrior" to Tile(x = 2658, z = 10086, height = 0),
            "Relleka Dungeon Swensen the Navigator Maze" to Tile(x = 2641, z = 10027, height = 0),
            "Temple of Ikov Boots of Lightness room" to Tile(x = 2641, z = 9763, height = 0),
            "Temple of Ikov Boots of Lightness dark" to Tile(x = 2642, z = 9740, height = 0),
            "Temple of Ikov Armadyl Room" to Tile(x = 2643, z = 9907, height = 0),
            "Ardougne East to West Sewers (Plague City)" to Tile(x = 2517, z = 9754, height = 0),
            "Ardougne Dungeon" to Tile(x = 2697, z = 9689, height = 0),
            "Ardougne Dungeon Perfect Gold Ore" to Tile(x = 2734, z = 9689, height = 0),
            "Chaos Druid Tower Dungeon" to Tile(x = 2573, z = 9750, height = 0),
            "Roving Elves Moss Giant Dungeon Glarial's tomb" to Tile(x = 2542, z = 9818, height = 0),
            "Waterfall Flooded dungeon" to Tile(x = 2538, z = 9913, height = 0),
            "Waterfall dungeon Fire Giants" to Tile(x = 2578, z = 9894, height = 0),
            "Waterfall dungeon end" to Tile(x = 2604, z = 9911, height = 0),
            "Goblin Dungeon North of Ardogune" to Tile(x = 2587, z = 9833, height = 0),
            "Elemental Workshop" to Tile(x = 2724, z = 9892, height = 0),
            "Legends' Guild Dungeon" to Tile(x = 2701, z = 9775, height = 0),
            "Karamja Carin Isle Dungeon" to Tile(x = 2763, z = 9375, height = 0),
            "Karamja Legends Guild Dungeon" to Tile(x = 2778, z = 9338, height = 0),
            "Karamja Gem Rock Dungeon" to Tile(x = 2840, z = 9388, height = 0),
            "Karamja Shilo Village quest Dungeon" to Tile(x = 2890, z = 9292, height = 0),
            "Karamja Shilo Village quest Dungoen" to Tile(x = 2891, z = 9387, height = 0),
            "Karamja Shilo Village quest noth Bone key dungeon -end fight" to Tile(x = 2893, z = 9487, height = 0),
            "Karamja Jadinko lair dungeon" to Tile(x = 3037, z = 9237, height = 0),
            "Recipe for Disaster underwater area" to Tile(x = 2976, z = 9492, height = 0),
            "Desert Enakhra's Lament Granite Dungeon" to Tile(x = 3103, z = 9312, height = 0),
            "Desert Treasure Pyramid" to Tile(x = 3235, z = 9316, height = 0),
            "Desert Contact Quest Dungeon" to Tile(x = 3232, z = 9250, height = 0),
            "Desert Contact Dungeon" to Tile(x = 3283, z = 9242, height = 0),
            "Desert Sophanem Pyramid" to Tile(x = 3307, z = 9198, height = 0),
            "Desert Obelisk" to Tile(x = 3296, z = 9313, height = 0),
            "Desert Genie Area by Nardah" to Tile(x = 3373, z = 9305, height = 0),
            "Desert Desert Treasure Smoke Mage" to Tile(x = 3315, z = 9378, height = 0),
            "Desert Smokey Slayer dungeon" to Tile(x = 3353, z = 9394, height = 0),
            "Desert Smokey Slayer dungeon main part" to Tile(x = 3303, z = 4354, height = 0),
            "Desert Kalphite Queen Dungeon" to Tile(x = 3499, z = 9492, height = 0),
            "Desert Kalphite back enterance (lower level)" to Tile(x = 3553, z = 9503, height = 0),
            "Desert Spirits of the Elid river top" to Tile(x = 3363, z = 9551, height = 0),
            "Desert Eagle Cave" to Tile(x = 3426, z = 9570, height = 0),
            "Mos Le Harmless bar basement" to Tile(x = 3666, z = 9394, height = 0),
            "Mos Le Harmless Cave horror dungeon start" to Tile(x = 3740, z = 9375, height = 0),
            "Mos Le Harmless Cave horror dungeon exit to tree island" to Tile(x = 3814, z = 9465, height = 0),
            "Harmony underwater Dungeon (pretty sure, don't have mapdata)" to Tile(x = 3796, z = 9254, height = 0),
            "Mage Training Arena" to Tile(x = 3365, z = 9644, height = 0),
            "Shades of Morton Dungeon" to Tile(x = 3496, z = 9674, height = 0),
            "Barrows Dungeon" to Tile(x = 3552, z = 9696, height = 0),
            "Canafis Dungeon" to Tile(x = 3479, z = 9841, height = 0),
            "Mort Myre/Canafis Dungeon outside blood altar" to Tile(x = 3700, z = 9685, height = 0),
            "Ectofuntus" to Tile(x = 3687, z = 9888, height = 0),
            "Aid of Myre quest series base" to Tile(x = 3628, z = 9642, height = 0),
            "Skullball & agility" to Tile(x = 3548, z = 9865, height = 0),
            "Mausoleum Dungeon" to Tile(x = 3503, z = 9970, height = 0),
            "Columbarium" to Tile(x = 3423, z = 9949, height = 0),
            "Priest in Peril quest" to Tile(x = 3421, z = 9887, height = 0),
            "Zaros Altar" to Tile(x = 3503, z = 10069, height = 0),
            "Port Phatsmaty Bar underground" to Tile(x = 2680, z = 9958, height = 0),
            "Resource Dungeon -Chaos Druid/Edgeville (10) -" to Tile(x = 991, z = 4585, height = 0),
            "Resource Dungeon -Dwarven Mine w/ bank(15) -" to Tile(x = 1041, z = 4575, height = 0),
            "Resource Dungeon - Hill Giants/Edgeville (20) -" to Tile(x = 1134, z = 4590, height = 0),
            "Resource Dungeon - Lesser Demons/Karamja (25) -" to Tile(x = 1186, z = 4598, height = 0),
            "Resource Dungeon - Fire Giants/Waterfall(35) -" to Tile(x = 1256, z = 4592, height = 0),
            "Resource Dungeon -Magic trees/Varrock Sewers (65) -" to Tile(x = 1313, z = 4590, height = 0),
            "Resource Dungeon -Hellhounds/Taverly(55) -" to Tile(x = 1394, z = 4588, height = 0),
            "Resource Dungeon  Runite Rocks/Mining Guild(45)-" to Tile(x = 1052, z = 4521, height = 0),
            "Resource Dungeon Blue D ragons/Taverly Dungeon(60) -" to Tile(x = 1000, z = 4522, height = 0),
            "Resource Dungeon Black Demon/Chaos Tunnels(70) -" to Tile(x = 1110, z = 4460, height = 0),
            "Resource Dungeon Metal Dragons/ Brimhaven(80) -" to Tile(x = 1140, z = 4500, height = 0),
            "Resource Dungeon Implings/Alkharid(75)" to Tile(x = 1181, z = 4515, height = 0),
            "Resource Dungeon Frost Dragons/Asgarnia Ice Dungeon(85)" to Tile(x = 1297, z = 4510, height = 0),
            "'Rum'-geon (braindeath isle) (not positive, missing mapdata)" to Tile(x = 1053, z = 5005, height = 0),
            "Scape-rune party-room" to Tile(x = 2085, z = 4463, height = 0),
            "Keldagrim Ratpits" to Tile(x = 1953, z = 4704, height = 0),
            "Dragonkin Castle (robert the strong cutscene)" to Tile(x = 1891, z = 4527, height = 0),
            "Draynor Christmas Event Factory" to Tile(x = 2006, z = 4440, height = 0),
            "Pyramid Plunder" to Tile(x = 1958, z = 4449, height = 0),
            "Waterbirth Dungeon -> Ladder to DKs" to Tile(x = 1911, z = 4366, height = 0),
            "While Guthix Sleeps Movaro(spellcheck) base" to Tile(x = 2005, z = 4401, height = 0),
            "While Guthix Sleeps Movaro(spellcheck) base2" to Tile(x = 2074, z = 4383, height = 0),
            "Lost goblin Plane Yubisk(spellcheck)" to Tile(x = 2198, z = 4258, height = 0),
            "Braindeath Island" to Tile(x = 2121, z = 5090, height = 0),
            "Stronghold of Security ladder floor 1" to Tile(x = 1860, z = 5244, height = 0),
            "Stronghold of Security chest floor 1" to Tile(x = 1907, z = 5221, height = 0),
            "Stronghold of Security ladder floor 2" to Tile(x = 2042, z = 5245, height = 0),
            "Stronghold of Security chest floor 2" to Tile(x = 2020, z = 5215, height = 0),
            "Stronghold of Security ladder floor 3" to Tile(x = 2122, z = 5251, height = 0),
            "Stronghold of Security chest floor 3" to Tile(x = 2144, z = 5281, height = 0),
            "Stronghold of Security ladder floor 4" to Tile(x = 2358, z = 5215, height = 0),
            "Stronghold of Security chest floor 4" to Tile(x = 2344, z = 5213, height = 0),
            "Between a Rock area 1" to Tile(x = 2367, z = 4961, height = 0),
            "Between a Rock area 2" to Tile(x = 2560, z = 4960, height = 0),
            "Runecrafting Altars Blood?" to Tile(x = 2464, z = 4895, height = 0),
            "Runecrafting Altar Air?" to Tile(x = 2400, z = 4836, height = 0),
            "Runecrafting Altar Law" to Tile(x = 2464, z = 4829, height = 0),
            "Runecrafting Altar Body" to Tile(x = 2520, z = 4832, height = 0),
            "Runecrafting Altar Fire" to Tile(x = 2580, z = 4843, height = 0),
            "Runecrafting Altar Earth" to Tile(x = 2659, z = 4839, height = 0),
            "Runecrafting Mind Altar" to Tile(x = 2788, z = 4839, height = 0),
            "Runecrafting Altar Nature" to Tile(x = 2844, z = 4828, height = 0),
            "Runecrafting Altar Death" to Tile(x = 2203, z = 4836, height = 0),
            "Runecrafting Altar Cosmic" to Tile(x = 2203, z = 4836, height = 0),
            "Runecrafting Altar Chaos" to Tile(x = 2271, z = 4840, height = 0),
            "Rune Essence Mine" to Tile(x = 2830, z = 4526, height = 0),
            "Runecrafting Altar Water" to Tile(x = 3486, z = 4836, height = 0),
            "Abyssal Teleport Random" to Tile(x = 2332, z = 4772, height = 0),
            "Abyssal Space Runecraft/Dark Mage" to Tile(x = 3038, z = 4833, height = 0),
            "The Abyss/Abyssal Plane" to Tile(x = 3057, z = 4881, height = 0),
            "King Black Dragon Lair" to Tile(x = 2275, z = 4680, height = 0),
            "Sabbots Cave" to Tile(x = 2269, z = 4756, height = 0),
            "Mage Bank Bank" to Tile(x = 2538, z = 4716, height = 0),
            "Mage Bank Statues" to Tile(x = 2507, z = 4722, height = 0),
            "Freaky Forester Random" to Tile(x = 2603, z = 4775, height = 0),
            "ShipYard Seperate Area" to Tile(x = 2562, z = 4578, height = 0),
            "Karamja Seperate Area" to Tile(x = 2531, z = 4559, height = 0),
            "Tyras Catapult seperate area" to Tile(x = 2315, z = 4593, height = 0),
            "Zanaris Secret Base" to Tile(x = 2273, z = 4433, height = 0),
            "Zanaris Bank" to Tile(x = 2381, z = 4458, height = 0),
            "Zanaris Market Place" to Tile(x = 2483, z = 4454, height = 0),
            "Zanaris Chicken Lair" to Tile(x = 2452, z = 4476, height = 0),
            "Zanaris Secret area" to Tile(x = 2453, z = 4396, height = 0),
            "Juliet Dungeon" to Tile(x = 2324, z = 4642, height = 0),
            "Part of Kalphite Hive I think" to Tile(x = 2347, z = 4385, height = 0),
            "Zanik Quest area I believe" to Tile(x = 2335, z = 4329, height = 0),
            "Bandos Throne Room 1" to Tile(x = 2344, z = 4275, height = 0),
            "Bandos Throne Room 2" to Tile(x = 2339, z = 4243, height = 0),
            "Dorgesh-kaan bank" to Tile(x = 2446, z = 4325, height = 0),
            "Monkey Maddness Jungle Demon Area" to Tile(x = 2651, z = 4573, height = 0),
            "Monkey Maddness Zombie Dungeon" to Tile(x = 2758, z = 9103, height = 0),
            "Monkey Maddness dungeon" to Tile(x = 2798, z = 9198, height = 0),
            "Monkey Maddness dungeon2" to Tile(x = 2787, z = 9168, height = 0),
            "Wilderness Spirit Farm Basement" to Tile(x = 2840, z = 4317, height = 0),
            "Wilderness Spirit Realm Rogue Castle" to Tile(x = 2724, z = 4323, height = 0),
            "Wilderness Spirit Realm Graveyard" to Tile(x = 2779, z = 4326, height = 0),
            "Wilderness Spirit Realm Flying Axe hut" to Tile(x = 2784, z = 4384, height = 0),
            "Wilderness Spirit Realm Dark Knight Fortress/Tormented Wraith" to Tile(x = 2848, z = 4382, height = 0),
            "Wilderness Spirit Realm ???" to Tile(x = 2834, z = 4259, height = 0),
            "Wilderness Spirit Realm Farm" to Tile(x = 2933, z = 4292, height = 0),
            "Wilderness Spirit Realm Chaos Altar" to Tile(x = 3296, z = 4187, height = 0),
            "Wilderness Spirit Realm North-East Coast Ruins" to Tile(x = 3230, z = 4186, height = 0),
            "Goblin Altar Room" to Tile(x = 2784, z = 4251, height = 0),
            "Daggonath Kings" to Tile(x = 2900, z = 4449, height = 0),
            "Corporeal Beast quest dungeon area" to Tile(x = 2985, z = 4386, height = 0),
            "Corporeal Beast area" to Tile(x = 2977, z = 4384, height = 0),
            "Tower of Life" to Tile(x = 2841, z = 4266, height = 0),
            "Enchanted Valley" to Tile(x = 3038, z = 4507, height = 0),
            "Yanille seperate area" to Tile(x = 2927, z = 4714, height = 0),
            "Maze Random" to Tile(x = 2912, z = 4577, height = 0),
            "ZMI altar" to Tile(x = 3318, z = 4809, height = 0),
            "Camo Random" to Tile(x = 3152, z = 4822, height = 0),
            "Unstable Foundations tutorial quest three headed green dragon area" to Tile(x = 3292, z = 4942, height = 0),
            "Chaos Tunnel quest enterance (east of varrock)" to Tile(x = 3150, z = 5233, height = 0),
            "Pheonix Lair" to Tile(x = 3535, z = 5197, height = 0),
            "Treasure chest island random event" to Tile(x = 3290, z = 4703, height = 0),
            "Evil Bob Random Island" to Tile(x = 3423, z = 4780, height = 0),
            "Uzer ruins desert demon/evil dave" to Tile(x = 3554, z = 4964, height = 0),
            "Cockroach Dungeon chest" to Tile(x = 3161, z = 4258, height = 0),
            "Maggies Caravan thingy" to Tile(x = 3229, z = 4451, height = 0),
            "Maggie quest thing" to Tile(x = 3295, z = 4511, height = 0),
            "Under Rimmington witch hut place" to Tile(x = 3227, z = 4521, height = 0),
            "Under Rimmington witch hut part" to Tile(x = 3168, z = 4518, height = 0),
            "Soul Wars Mini-game" to Tile(x = 3610, z = 5403, height = 0),
            "Muspah Cave" to Tile(x = 3414, z = 5541, height = 0),
            "Muspah Cave 2" to Tile(x = 3486, z = 5543, height = 0),
            "Circus" to Tile(x = 3548, z = 5599, height = 0),
            "Ice Strykewyrm Cave" to Tile(x = 3434, z = 5651, height = 0),
            "Spirit Shard area" to Tile(x = 3690, z = 5560, height = 0),
            "Draynor Seperate" to Tile(x = 3861, z = 5468, height = 0),
            "Lumbridge Catacombs" to Tile(x = 3865, z = 5525, height = 0),
            "Lumbridge Catacombs training area" to Tile(x = 3971, z = 5563, height = 0),
            "Ritual Of the Mahjarrhat Kethsa" to Tile(x = 4014, z = 5709, height = 0),
            "Verac Additional Dungeon" to Tile(x = 4071, z = 5713, height = 0),
            "Kethsa basement" to Tile(x = 4128, z = 5727, height = 0),
            "Recruitment Drive area?" to Tile(x = 4110, z = 5750, height = 0),
            "Glacor cave" to Tile(x = 4184, z = 5733, height = 0),
            "Rimmington Basement I believe??? could be part of thieves guild quest" to Tile(x = 4615, z = 5942, height = 0),
            "Thieves Guild part I" to Tile(x = 4663, z = 5900, height = 0),
            "Thieves Guild part II" to Tile(x = 4763, z = 5902, height = 0),
            "Thieves Guild part III" to Tile(x = 4640, z = 5792, height = 0),
            "Thieves Guild part IV" to Tile(x = 4761, z = 5773, height = 0),
            "Sea Slug Rubium place" to Tile(x = 2640, z = 4922, height = 0),
            "Mansion Quest area" to Tile(x = 2846, z = 5086, height = 0),
            "Gorak Plane" to Tile(x = 3040, z = 5346, height = 0),
            "Sorrcoress Garden" to Tile(x = 2909, z = 5468, height = 0),
            "While Guthix Sleeps Stone of Jas" to Tile(x = 2605, z = 5728, height = 0),
            "Chaos Tunnels" to Tile(x = 3248, z = 5489, height = 0),
            "Little Island" to Tile(x = 3628, z = 5720, height = 0),
            "Fist of Guthix" to Tile(x = 1663, z = 5708, height = 0),
            "LavaFLow Mine" to Tile(x = 2182, z = 5664, height = 0),
            "Mole" to Tile(x = 1752, z = 5137, height = 0),
            "Kuradel Slayer Dungeon" to Tile(x = 1656, z = 5258, height = 0),
            "Varrock Grand Exchange" to Tile(x = 3165, z = 3482, height = 0),
            "Varrock Sawmill" to Tile(x = 3310, z = 3502, height = 0),
            "Varrock Digsite area" to Tile(x = 3368, z = 3420, height = 0),
            "Varrock Digsite Building" to Tile(x = 3360, z = 3342, height = 0),
            "Draynor Manor" to Tile(x = 3109, z = 3361, height = 0),
            "Draynor Bank" to Tile(x = 3092, z = 3242, height = 0),
            "Draynor Wizards Tower" to Tile(x = 3108, z = 3160, height = 0),
            "Port Sarim Bar" to Tile(x = 3048, z = 3256, height = 0),
            "Port Sarim Dock" to Tile(x = 3027, z = 3219, height = 0),
            "Port Sarim Church" to Tile(x = 2997, z = 3178, height = 0),
            "Rimmington Maze" to Tile(x = 2932, z = 3244, height = 0),
            "Falador West Bank" to Tile(x = 2945, z = 3370, height = 0),
            "Falador East Bank" to Tile(x = 3013, z = 3355, height = 0),
            "Falador Partyroom" to Tile(x = 3043, z = 3375, height = 0),
            "Falador Artisans Workshop" to Tile(x = 3046, z = 3340, height = 0),
            "Falador Ice Mountain" to Tile(x = 3007, z = 3476, height = 0),
            "Falador Goblin Village" to Tile(x = 2956, z = 3506, height = 0),
            "Taverley Herblore Shop" to Tile(x = 2899, z = 3428, height = 0),
            "Taverley Druid Circle" to Tile(x = 2926, z = 3484, height = 0),
            "Burthorpe Games room" to Tile(x = 2899, z = 3563, height = 0),
            "Burthorpe Turael" to Tile(x = 2928, z = 3546, height = 0),
            "Burthorpe White Wolf Mountain" to Tile(x = 2851, z = 3498, height = 0),
            "Catherby bank" to Tile(x = 2808, z = 3440, height = 0),
            "Catherby fishing" to Tile(x = 2857, z = 3430, height = 0),
            "Catherby island" to Tile(x = 2841, z = 3424, height = 0),
            "Seers Village Flax" to Tile(x = 2742, z = 3445, height = 0),
            "Seers Village Bank" to Tile(x = 2724, z = 3491, height = 0),
            "Seers Village Camelot Castle" to Tile(x = 2757, z = 3486, height = 0),
            "Seers Village Sinclair Mansion" to Tile(x = 2741, z = 3564, height = 0),
            "Seers Village McGrubors Wood" to Tile(x = 2655, z = 3485, height = 0),
            "Seers Village Coal Trucks" to Tile(x = 2721, z = 3383, height = 0),
            "Hemenster Fishing Contest" to Tile(x = 2638, z = 3438, height = 0),
            "Hemenster Sorcerers Tower" to Tile(x = 2702, z = 3404, height = 0),
            "Hemenster Temple of Ikov" to Tile(x = 2678, z = 3402, height = 0),
            "Ardougne East Ardougne South Bank" to Tile(x = 2653, z = 3284, height = 0),
            "Ardougne East Ardougne North Bank" to Tile(x = 2615, z = 3333, height = 0),
            "Ardougne East Ardougne stalls" to Tile(x = 2661, z = 3306, height = 0),
            "Ardougne East Ardougne castle" to Tile(x = 2576, z = 3298, height = 0),
            "Ardougne West Ardougne square" to Tile(x = 2528, z = 3310, height = 0),
            "Ardougne West Ardougne underground pass" to Tile(x = 2445, z = 3316, height = 0),
            "Ardougne West Ardougne Training Camp" to Tile(x = 2521, z = 3368, height = 0),
            "Ardougne West Ardougne Chaos Tower" to Tile(x = 2562, z = 3356, height = 0),
            "Witchhaven Church" to Tile(x = 2723, z = 3283, height = 0),
            "Witchhaven Fishing Platform" to Tile(x = 2766, z = 3276, height = 0),
            "Gnome Stronghold Gate" to Tile(x = 2460, z = 3394, height = 0),
            "Gnome Stronghold Agility Course" to Tile(x = 2478, z = 3426, height = 0),
            "Gnome Stronghold Grand Tree" to Tile(x = 2465, z = 3488, height = 0),
            "Gnome Stronghold Gnomeball" to Tile(x = 2395, z = 3490, height = 0),
            "Gnome Stronghold Eagles Peak" to Tile(x = 2328, z = 3498, height = 0),
            "Barbarian Outpost Agility Course" to Tile(x = 2552, z = 3554, height = 0),
            "Barbarian Outpost Barbarian Assault" to Tile(x = 2535, z = 3567, height = 0),
            "Barbarian Outpost Fish Flingers" to Tile(x = 2583, z = 3530, height = 0),
            "Barbarian Outpost Lighthouse" to Tile(x = 2507, z = 3640, height = 0),
            "Piscatoris bank" to Tile(x = 2330, z = 3689, height = 0),
            "Piscatoris falconer" to Tile(x = 2373, z = 3611, height = 0),
            "Piscatoris pheonix lair" to Tile(x = 2294, z = 3625, height = 0),
            "Castlewars bank" to Tile(x = 2442, z = 3089, height = 0),
            "Castlewars Zamorak" to Tile(x = 2374, z = 3129, height = 0),
            "Castlewars Saradomin" to Tile(x = 2423, z = 3079, height = 0),
            "Castlewars center" to Tile(x = 2399, z = 3103, height = 0),
            "Treegnome Village" to Tile(x = 2532, z = 3167, height = 0),
            "Treegnome Village battlefield" to Tile(x = 2515, z = 3245, height = 0),
            "ZMI Altar" to Tile(x = 2455, z = 3238, height = 0),
            "Port Khazard Fight Arena" to Tile(x = 2601, z = 3162, height = 0),
            "Port Khazard Port" to Tile(x = 2658, z = 3158, height = 0),
            "Port Khazard Tower of Life" to Tile(x = 2649, z = 3218, height = 0),
            "Yanille bank" to Tile(x = 2611, z = 3093, height = 0),
            "Yanille watchtower" to Tile(x = 2546, z = 3115, height = 0),
            "Ogre area city" to Tile(x = 2519, z = 3041, height = 0),
            "Ogre area Zogre area" to Tile(x = 2481, z = 3047, height = 0),
            "Ogre Area ogress city" to Tile(x = 2574, z = 2850, height = 0),
            "Ogre Area Mobilising Armies" to Tile(x = 2413, z = 2838, height = 0),
            "Soulwars main area" to Tile(x = 1889, z = 3177, height = 0),
            "Soulwars middle" to Tile(x = 1885, z = 3228, height = 0),
            "Soulwars Saradomin Avatar" to Tile(x = 1807, z = 3214, height = 0),
            "Soulwars Zamorak avatar" to Tile(x = 1967, z = 3250, height = 0),
            "Isafdar Lletya" to Tile(x = 2343, z = 3170, height = 0),
            "Isafdar Elf Camp" to Tile(x = 2202, z = 3253, height = 0),
            "Ape Atoll Castle" to Tile(x = 2792, z = 2785, height = 0),
            "Ape Atoll agility course" to Tile(x = 2761, z = 2728, height = 0),
            "Ape Atoll Crash Island" to Tile(x = 2899, z = 2725, height = 0),
            "Ape Atoll boat" to Tile(x = 2800, z = 2709, height = 0),
            "Void Outpost" to Tile(x = 2659, z = 2658, height = 0),
            "Void Outpost pest control" to Tile(x = 2656, z = 2593, height = 0),
            "Void Outpost small island" to Tile(x = 2860, z = 2584, height = 0),
            "Karamja Shilo Village Bank" to Tile(x = 2852, z = 2955, height = 0),
            "Karamja Kharzai Jungle" to Tile(x = 2778, z = 2917, height = 0),
            "Karamja Kharzai Jungle Jadinko area" to Tile(x = 2956, z = 2914, height = 0),
            "Karamja Kharzai Jungle Jadinko area island" to Tile(x = 2976, z = 2911, height = 0),
            "Karamja Cairn Isle" to Tile(x = 2765, z = 2980, height = 0),
            "Karamja Tai Bwo Wannai" to Tile(x = 2790, z = 3063, height = 0),
            "Karamja Shipyard" to Tile(x = 2956, z = 3024, height = 0),
            "Karamja Banana Plantation" to Tile(x = 2913, z = 3172, height = 0),
            "Karamja Volcano" to Tile(x = 2848, z = 3166, height = 0),
            "Karamja Brimhaven" to Tile(x = 2785, z = 3185, height = 0),
            "Karamja Moss Giant Isle" to Tile(x = 2696, z = 3211, height = 0),
            "Lunar Island bank" to Tile(x = 2101, z = 3919, height = 0),
            "Lunar island altar" to Tile(x = 2151, z = 3862, height = 0),
            "Lunar Island pirates cove" to Tile(x = 2206, z = 3810, height = 0),
            "Rocking Out Jail Island" to Tile(x = 3032, z = 2981, height = 0),
            "Fremennik Isles Neitiznot bank" to Tile(x = 2336, z = 3807, height = 0),
            "Fremennik Isles Jatizso bank" to Tile(x = 2416, z = 3801, height = 0),
            "Fremennik Isles north part" to Tile(x = 2371, z = 3890, height = 0),
            "Miscellania Castle" to Tile(x = 2504, z = 3860, height = 0),
            "Etcteria bank" to Tile(x = 2619, z = 3895, height = 0),
            "Crandor" to Tile(x = 2835, z = 3274, height = 0),
            "Entrana" to Tile(x = 2827, z = 3344, height = 0),
            "Rellekka stalls" to Tile(x = 2645, z = 3674, height = 0),
            "Rellekka Waterbirth Island" to Tile(x = 2545, z = 3755, height = 0),
            "Rellekka Mountain Camp" to Tile(x = 2788, z = 3673, height = 0),
            "Rellekka Hunter area" to Tile(x = 2728, z = 3773, height = 0),
            "Troll stronghold trollhiem" to Tile(x = 2883, z = 3673, height = 0),
            "Troll stronghold death plateau" to Tile(x = 2859, z = 3591, height = 0),
            "Troll Stronghold Ice Path" to Tile(x = 2862, z = 3809, height = 0),
            "Troll Stronghold Trollweiss Mountain" to Tile(x = 2791, z = 3853, height = 0),
            "Troll Stronghold Wilderness Castle" to Tile(x = 2913, z = 3933, height = 0),
            "Troll stronghold Godwars Dungeon" to Tile(x = 2894, z = 3758, height = 0),
            "Kharidian Desert Al Kharid Castle" to Tile(x = 3291, z = 3173, height = 0),
            "Kharidian Desert Al Kharid Bank" to Tile(x = 3267, z = 3166, height = 0),
            "Kharidian Desert Al Kharid mine" to Tile(x = 3300, z = 3307, height = 0),
            "Kharidian Desert Al Kharid  Duel Arena" to Tile(x = 3363, z = 3268, height = 0),
            "Kharidian Desert Al Kharid Mage Trainning Arena" to Tile(x = 3362, z = 3317, height = 0),
            "Kharidian Desert Citharede" to Tile(x = 3398, z = 3177, height = 0),
            "Kharidian Desert Mining Camp" to Tile(x = 3293, z = 3027, height = 0),
            "Kharidian Desert Bandit Camp" to Tile(x = 3176, z = 2986, height = 0),
            "Kharidian Desert Kalphite Hive" to Tile(x = 3234, z = 3108, height = 0),
            "Kharidian Desert Pollnivneach" to Tile(x = 3358, z = 2982, height = 0),
            "Kharidian Desert Pyramid" to Tile(x = 3232, z = 2916, height = 0),
            "Kharidian Desert Quarry" to Tile(x = 3174, z = 2916, height = 0),
            "Kharidian Desert Agility Pyramid" to Tile(x = 3345, z = 2827, height = 0),
            "Kharidian Desert Nardah" to Tile(x = 2425, z = 2919, height = 0),
            "Kharidian Desert Uzer" to Tile(x = 3485, z = 3089, height = 0),
            "Kharidian Desert Sophanem" to Tile(x = 3309, z = 2792, height = 0),
            "Kharidian Desert Swamp" to Tile(x = 3510, z = 2932, height = 0),
            "Mos Le Harmless" to Tile(x = 3682, z = 2975, height = 0),
            "Harmony" to Tile(x = 3801, z = 2826, height = 0),
            "Dragontooth Island" to Tile(x = 3807, z = 3556, height = 0),
            "Port Phasmatys bank" to Tile(x = 3690, z = 3467, height = 0),
            "Port Phasmatys Ectofuntus" to Tile(x = 3658, z = 3521, height = 0),
            "Canifis" to Tile(x = 3496, z = 3490, height = 0),
            "Barrows" to Tile(x = 3564, z = 3300, height = 0),
            "Mort'ton" to Tile(x = 3487, z = 3288, height = 0),
            "Burgh De Rott" to Tile(x = 3494, z = 3235, height = 0),
            "Meiyerditch Vampire  city" to Tile(x = 3645, z = 3368, height = 0),
            "Wilderness Agility Arena" to Tile(x = 2997, z = 3953, height = 0),
            "Wilderness Mage Arena" to Tile(x = 3104, z = 3932, height = 0),
            "Wilderness Rogue Castle" to Tile(x = 3293, z = 3927, height = 0),
            "Wilderness Lava maze" to Tile(x = 3084, z = 3866, height = 0),
            "Wilderness King Black Dragon KBD lair" to Tile(x = 3015, z = 3849, height = 0),
            "Wilderness Red Dragon Island" to Tile(x = 3199, z = 3829, height = 0),
            "Wilderness Dark Knight Fortress" to Tile(x = 3026, z = 3632, height = 0),
            "Wilderness Altar" to Tile(x = 2951, z = 3822, height = 0),
            "Wilderness Bandit Camp" to Tile(x = 3037, z = 3691, height = 0),
            "Wilderness Demonic Ruins" to Tile(x = 3297, z = 3888, height = 0),
            "Guilds Mages Guild" to Tile(x = 2591, z = 3087, height = 0),
            "Guilds Legend Guild" to Tile(x = 2728, z = 3368, height = 0),
            "Guilds Ranging Guild" to Tile(x = 2668, z = 3427, height = 0),
            "Guilds Fishing Guild" to Tile(x = 2596, z = 3410, height = 0),
            "Guilds Warriors Guild" to Tile(x = 2867, z = 3540, height = 0),
            "Guilds Heroes Guild" to Tile(x = 2897, z = 3510, height = 0),
            "Guilds Prayer/Monastery" to Tile(x = 3052, z = 3491, height = 0),
            "Guilds Cooking" to Tile(x = 3144, z = 3449, height = 0),
            "Guilds Crafting" to Tile(x = 2933, z = 3282, height = 0),
            "Guilds Champions" to Tile(x = 3192, z = 3357, height = 0),
        )
    }

    init {
        // Portal object IDs from RSCM - using nexus portals and magic portals
        val portalObjects = listOf(
            "object.portal_nexus_33410",   // 33410 - Crystalline Portal Nexus (Regular) - PRIMARY
            "object.magic_portal",         // 2156
            "object.magic_portal_2157",    // 2157
            "object.portal_4525",          // 4525
            // "object.carving",              // 22706 - Gilded Portal Nexus (carving) - REMOVED: No available options
            "object.portal_nexus_33354",   // 33354 - Portal Nexus from POH (backup)
        )

        // Common portal interaction options - try all common OSRS portal options
        // Including POH portal nexus options like "Ring-configure"
        val portalOptions = listOf(
            "Ring-configure",      // POH portal nexus configure option
            "configure",           // Standard configure option
            "enter",               // Standard enter option
            "teleport",            // Standard teleport option
            "use",                 // Standard use option
            "operate",             // Standard operate option
            "activate",            // Standard activate option
            "quick-start",         // Quick start option
            "Tree",                // POH portal nexus tree option
            "Ring-Zanaris",        // POH portal nexus ring option
            "Ring-last-destination (AIP)" // POH portal nexus last destination
        )

        portalObjects.forEach { portalId ->
            var optionBound = false

            // First, try to get available options for this object
            val availableOptions = try {
                val objDef = getObject(getRSCM(portalId))
                objDef.actions.filterNotNull().filter { action -> action.length > 0 }
            } catch (e: Exception) {
                emptyList()
            }

            // Try common portal interaction options (similar to obelisk plugin pattern)
            for (option in portalOptions) {
                try {
                    // Check if the object has this option before trying to bind
                    if (objHasOption(portalId, option)) {
                        onObjOption(obj = portalId, option = option) {
                            if (!player.lock.canTeleport()) {
                                player.message("You cannot teleport right now.")
                                return@onObjOption
                            }

                            player.queue(TaskPriority.STRONG) {
                                openTeleportMenu(player)
                            }
                        }
                        optionBound = true
                        break // Found a working option, no need to try others
                    }
                } catch (e: Exception) {
                    // Option not available or binding failed, try next
                    continue
                }
            }

            // Right-click examine
            try {
                if (objHasOption(portalId, "examine")) {
                    onObjOption(obj = portalId, option = "examine") {
                        player.message("A mystical portal that can teleport you to many locations.")
                    }
                }
            } catch (e: Exception) {
                // Examine option not available, skip it
            }
        }
    }

    /**
     * Opens the paginated teleport menu for the player
     * This is a suspend function that runs within a QueueTask context
     * Uses a full-screen interface (187) with the crystalline portal nexus item display
     */
    private suspend fun QueueTask.openTeleportMenu(p: Player) {
        val options = TELEPORT_LOCATIONS.map { it.first }
        val title = "Crystalline Portal Nexus"

        p.openInterface(187, InterfaceDestination.MAIN_SCREEN)
        p.runClientScript(CommonClientScripts.INTERFACE_MENU, title, options.joinToString("|"), 22707)
        p.setInterfaceEvents(interfaceId = 187, component = 3, from = 0, to = options.size, setting = 1)

        terminateAction = { p.closeInterface(187) }
        waitReturnValue()
        terminateAction!!(this)

        val selected = (requestReturnValue as? ResumePauseButton)?.sub ?: -1

        if (selected < 0) {
            return // Player closed menu
        }

        val selectedOption = options[selected]
        val location = TELEPORT_LOCATIONS.find { it.first == selectedOption }
        if (location != null) {
            p.prepareForTeleport()
            p.moveTo(location.second)
            p.message("You teleport to ${location.first}.")
        }
    }
}

