package org.alter.plugins.content.areas.varrock.npcs

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.InterfaceDestination
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.info.NpcInfo
import org.alter.game.model.Tile
import org.alter.plugins.content.mechanics.doompoints.DoomPoints
import org.alter.rscm.RSCM.getRSCM

/**
 * Doomsayer NPC Plugin
 * 
 * An NPC in Varrock who accepts high-value items in exchange for Doom Points.
 * Players can then spend Doom Points to unlock permanent account perks.
 */
class DoomsayerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val doomsayerNpc = "npc.wise_old_man"
    private val doomsayerTile = Tile(x = 3209, z = 3424, height = 0)
    
    init {
        // Spawn the Doomsayer in Varrock (near the fountain)
        spawnNpc(doomsayerNpc, x = 3209, z = 3424, walkRadius = 2, direction = Direction.SOUTH)
        println("DEBUG: Spawning Doomsayer with NPC: $doomsayerNpc at $doomsayerTile")
        
        // Set custom name for the Doomsayer when it spawns
        onNpcSpawn(doomsayerNpc) {
            println("DEBUG: NPC spawned: ${npc.id} at ${npc.tile}")
            if (npc.tile == doomsayerTile) {
                println("DEBUG: Setting custom name for Doomsayer")
                NpcInfo(npc).setTempName("Doomsayer")
            }
        }
        
        // Use only talk-to option and handle everything through dialogue
        onNpcOption(doomsayerNpc, option = "talk-to") {
            val npc = player.getInteractingNpc()
            player.message("DEBUG: Talk-to triggered on NPC ${npc.id} at ${npc.tile}")
            player.message("DEBUG: Starting mainMenu")
            player.queue { mainMenu(player) }
        }
        
        // Command to check doom points
        onCommand("doompoints") {
            val points = DoomPoints.getDoomPoints(player)
            player.message("You have $points Doom Point${if (points == 1) "" else "s"}.")
        }
    }
    
    private suspend fun QueueTask.mainMenu(player: Player) {
        player.message("DEBUG: mainMenu function called!")
        val currentPoints = DoomPoints.getDoomPoints(player)
        player.message("DEBUG: Current points: $currentPoints")
        
        chatNpc(
            player,
            "Greetings, ${player.username}. I am the Doomsayer,<br>keeper of forbidden knowledge and ancient power.",
            animation = 567
        )
        
        chatNpc(
            player,
            "You currently possess $currentPoints Doom Point${if (currentPoints == 1) "" else "s"}.",
            animation = 554
        )
        
        when (options(player, "Trade items for Doom Points", "View Tiered Upgrades", "View Permanent Upgrades", "Check my active perks", "Goodbye")) {
            1 -> showTradeMenu(player)
            2 -> showTieredPerksMenu(player)
            3 -> showPermanentPerksMenu(player)
            4 -> showActivePerks(player)
            5 -> chatPlayer(player, "Goodbye.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.dialog(player: Player) {
        chatPlayer(player, "What do you do here?", animation = 588)
        
        chatNpc(
            player,
            "I offer power beyond your wildest dreams... for a price.<br>Bring me items of great value, and I shall grant you<br>Doom Points.",
            animation = 567
        )
        
        chatNpc(
            player,
            "Doom Points are fragments of power extracted from potent artifacts.<br>The stronger the item, the more points it yields.",
            animation = 567
        )
        
        chatNpc(
            player,
            "You can spend these points on permanent upgrades to your character.<br>Each perk makes you stronger in different ways!",
            animation = 567
        )
        
        when (options(player, "How do I get Doom Points?", "What perks are available?", "Let me see the trade menu.", "I understand, thanks.")) {
            1 -> showItemList(player)
            2 -> mainMenu(player)
            3 -> showTradeMenu(player)
        }
    }
    
    private suspend fun QueueTask.showItemList(player: Player) {
        chatPlayer(player, "How many points do specific items give?", animation = 588)
        
        chatNpc(
            player,
            "That depends on their power. Godswords grant 100 points,<br>twisted bows 150, scythes 350, and the rarest items<br>can grant 1000 or more!",
            animation = 567
        )
        
        when (options(player, "I'd like to trade.", "Tell me about the perks.", "Goodbye.")) {
            1 -> showTradeMenu(player)
            2 -> mainMenu(player)
            3 -> chatPlayer(player, "Goodbye.", animation = 588)
        }
    }

    private suspend fun QueueTask.showTieredPerksMenu(player: Player) {
        val currentPoints = DoomPoints.getDoomPoints(player)
        
        chatNpc(
            player,
            "You currently have $currentPoints Doom Point${if (currentPoints == 1) "" else "s"}.<br>Here are the tiered upgrades you can unlock:",
            animation = 567
        )
        
        // Filter tiered perks and keep track of original index
        val tieredPerks = DoomPoints.AVAILABLE_PERKS.mapIndexed { index, perk -> index to perk }
            .filter { it.second.maxLevel > 1 }
            
        val optionsList = tieredPerks.map { (index, perk) ->
            val currentLevel = DoomPoints.getPerkLevel(player, index)
            if (currentLevel >= perk.maxLevel) {
                "${perk.name} (MAXED)"
            } else {
                "${perk.name} (Level: $currentLevel/${perk.maxLevel} - ${perk.cost} pts)"
            }
        }.toMutableList()
        
        optionsList.add("Main Menu")
        
        val choice = options(player, *optionsList.toTypedArray())
        
        if (choice <= tieredPerks.size) {
            val (originalIndex, perk) = tieredPerks[choice - 1]
            val currentLevel = DoomPoints.getPerkLevel(player, originalIndex)
            
            if (currentLevel >= perk.maxLevel) {
                chatNpc(player, "You have already maxed out this perk!", animation = 554)
                showTieredPerksMenu(player)
            } else {
                showPerkDetails(player, originalIndex, isTiered = true)
            }
        } else {
            mainMenu(player)
        }
    }

    private suspend fun QueueTask.showPermanentPerksMenu(player: Player) {
        val currentPoints = DoomPoints.getDoomPoints(player)
        
        chatNpc(
            player,
            "You currently have $currentPoints Doom Point${if (currentPoints == 1) "" else "s"}.<br>Here are the permanent upgrades you can unlock:",
            animation = 567
        )
        
        // Filter permanent perks and keep track of original index
        val permanentPerks = DoomPoints.AVAILABLE_PERKS.mapIndexed { index, perk -> index to perk }
            .filter { it.second.maxLevel == 1 }
            
        val optionsList = permanentPerks.map { (index, perk) ->
            val currentLevel = DoomPoints.getPerkLevel(player, index)
            if (currentLevel >= perk.maxLevel) {
                "${perk.name} (UNLOCKED)"
            } else {
                "${perk.name} (${perk.cost} pts)"
            }
        }.toMutableList()
        
        optionsList.add("Main Menu")
        
        val choice = options(player, *optionsList.toTypedArray())
        
        if (choice <= permanentPerks.size) {
            val (originalIndex, perk) = permanentPerks[choice - 1]
            val currentLevel = DoomPoints.getPerkLevel(player, originalIndex)
            
            if (currentLevel >= perk.maxLevel) {
                chatNpc(player, "You have already unlocked this upgrade!", animation = 554)
                showPermanentPerksMenu(player)
            } else {
                showPerkDetails(player, originalIndex, isTiered = false)
            }
        } else {
            mainMenu(player)
        }
    }
    
    private suspend fun QueueTask.showPerkDetails(player: Player, perkIndex: Int, isTiered: Boolean) {
        val perk = DoomPoints.AVAILABLE_PERKS[perkIndex]
        val currentLevel = DoomPoints.getPerkLevel(player, perkIndex)
        val nextLevel = currentLevel + 1
        val currentPoints = DoomPoints.getDoomPoints(player)
        
        chatNpc(player, perk.description, animation = 567)
        
        chatNpc(
            player,
            "This perk costs ${perk.cost} Doom Points.<br>You currently have $currentPoints points.",
            animation = 554
        )
        
        when (options(player, "Unlock this perk (${perk.cost} points)", "Go back", "Cancel")) {
            1 -> {
                if (DoomPoints.removeDoomPoints(player, perk.cost)) {
                    perk.unlock(player, nextLevel)
                    chatNpc(
                        player,
                        "The perk has been unlocked! Your power grows...",
                        animation = 567
                    )
                    if (isTiered) showTieredPerksMenu(player) else showPermanentPerksMenu(player)
                } else {
                    chatNpc(
                        player,
                        "You do not have enough Doom Points!<br>You need ${perk.cost} but only have $currentPoints.",
                        animation = 554
                    )
                    if (isTiered) showTieredPerksMenu(player) else showPermanentPerksMenu(player)
                }
            }
            2 -> if (isTiered) showTieredPerksMenu(player) else showPermanentPerksMenu(player)
            3 -> chatPlayer(player, "Nevermind.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.showActivePerks(player: Player) {
        val damageBonus = DoomPoints.getDamageMultiplier(player)
        val dropBonus = DoomPoints.getDropRateMultiplier(player)
        val coinBonus = DoomPoints.getCoinMultiplier(player)
        val slayerBonus = DoomPoints.getSlayerPointsBonus(player)
        val passiveXpLevel = DoomPoints.getPassiveXpPerkLevel(player)
        
        // New perks
        val slayerTaskSelector = DoomPoints.getPerkLevel(player, 5) > 0
        val increasedBank = DoomPoints.getPerkLevel(player, 6) > 0
        val remoteBank = DoomPoints.getPerkLevel(player, 7) > 0
        
        player.message("<col=ff0000>--- Active Doom Perks ---</col>")
        var hasPerks = false
        
        if (damageBonus > 0) { player.message("Damage Boost: +${damageBonus}%"); hasPerks = true }
        if (dropBonus > 0) { player.message("Drop Rate Boost: +${dropBonus}%"); hasPerks = true }
        if (coinBonus > 0) { player.message("Coin Drop Boost: +${coinBonus}%"); hasPerks = true }
        if (slayerBonus > 0) { player.message("Slayer XP Boost: +${slayerBonus}%"); hasPerks = true }
        if (passiveXpLevel > 0) { 
            val chance = passiveXpLevel * 5
            player.message("Passive XP: Level $passiveXpLevel ($chance% chance)"); hasPerks = true 
        }
        
        if (slayerTaskSelector) { player.message("Slayer Task Selector: Active"); hasPerks = true }
        if (increasedBank) { player.message("Increased Bank Storage: Active (1200 slots)"); hasPerks = true }
        if (remoteBank) { player.message("Remote Banking: Active (::bank)"); hasPerks = true }
        
        if (!hasPerks) {
            player.message("No perks unlocked yet.")
        }
        
        chatNpc(
            player,
            "I have listed your currently active perks in your chatbox.",
            animation = 567
        )
        
        when (options(player, "Main Menu")) {
            1 -> mainMenu(player)
        }
    }
    
    private suspend fun QueueTask.showTradeMenu(player: Player) {
        chatNpc(
            player,
            "I can sense the power within your possessions...<br>What would you like to trade for Doom Points?",
            animation = 567
        )
        
        // Find all tradeable items in inventory
        val tradeableItems = mutableListOf<Pair<Int, Int>>() // slot to itemId
        
        for (slot in 0 until player.inventory.capacity) {
            val item = player.inventory[slot] ?: continue
            val itemId = item.id
            
            // Check if item has doom point value
            val points = DoomPoints.TRADE_IN_VALUES[itemId]
            if (points != null && points > 0) {
                tradeableItems.add(slot to itemId)
            }
        }
        
        if (tradeableItems.isEmpty()) {
            chatNpc(
                player,
                "You have nothing of value to me. Return when you<br>possess items worthy of my attention.",
                animation = 554
            )
            return
        }
        
        // Build options for tradeable items
        // Calculate total points for all items
        var totalAllPoints = 0
        var totalAllItemsCount = 0
        
        for ((slot, itemId) in tradeableItems) {
            val item = player.inventory[slot]!!
            val points = DoomPoints.TRADE_IN_VALUES[itemId] ?: 0
            
            if (itemId == 995) { // Coins
                val millions = item.amount / 1_000_000
                totalAllPoints += millions
                if (millions > 0) totalAllItemsCount++
            } else {
                totalAllPoints += points * item.amount
                totalAllItemsCount++
            }
        }
        
        // Build options for tradeable items
        val itemOptions = mutableListOf<String>()
        
        if (totalAllPoints > 0) {
            itemOptions.add("Trade ALL items ($totalAllPoints points)")
        }
        
        tradeableItems.forEach { (slot, itemId) ->
            val item = player.inventory[slot]!!
            val itemDef = getItem(itemId)
            val itemName = itemDef.name
            val points = DoomPoints.TRADE_IN_VALUES[itemId] ?: 0
            val amount = item.amount
            
            if (amount > 1) {
                itemOptions.add("$itemName ($points points each, $amount in inventory)")
            } else {
                itemOptions.add("$itemName ($points points each, $amount in inventory)")
            }
        }
        itemOptions.add("Nevermind")
        
        val choice = options(player, *itemOptions.toTypedArray())
        
        if (totalAllPoints > 0 && choice == 1) {
            // Trade ALL items
            var tradedPoints = 0
            val itemsToRemove = mutableListOf<Pair<Int, Int>>() // itemId, amount
            
            for ((slot, itemId) in tradeableItems) {
                val item = player.inventory[slot]!!
                val points = DoomPoints.TRADE_IN_VALUES[itemId] ?: 0
                
                if (itemId == 995) {
                    val millions = item.amount / 1_000_000
                    if (millions > 0) {
                        itemsToRemove.add(itemId to millions * 1_000_000)
                        tradedPoints += millions
                    }
                } else {
                    itemsToRemove.add(itemId to item.amount)
                    tradedPoints += points * item.amount
                }
            }
            
            // Remove items and add points
            var success = true
            for ((id, amount) in itemsToRemove) {
                if (player.inventory.remove(id, amount).hasFailed()) {
                    success = false
                }
            }
            
            if (success) {
                DoomPoints.addDoomPoints(player, tradedPoints)
                chatNpc(
                    player,
                    "Excellent! I have taken all your offerings.<br>You have gained $tradedPoints Doom Points.",
                    animation = 567
                )
            } else {
                player.message("Something went wrong while trading items.")
            }
            
            showTradeMenu(player)
            return
        }
        
        // Adjust index based on whether "Trade ALL" option was added
        val indexOffset = if (totalAllPoints > 0) 2 else 1
        
        if (choice >= indexOffset && choice < itemOptions.size) {
            val (slot, itemId) = tradeableItems[choice - indexOffset]
            val item = player.inventory[slot]!!
            
            // Handle the trade
            handleTrade(player, slot, itemId, item.amount)
        } else {
            chatPlayer(player, "I'll come back later.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.handleTrade(player: Player, slot: Int, itemId: Int, amount: Int) {
        val item = player.inventory[slot] ?: return
        val itemDef = getItem(itemId)
        val itemName = itemDef.name
        val basePoints = DoomPoints.TRADE_IN_VALUES[itemId] ?: 0
        
        // Special handling for coins
        if (itemId == 995) {
            val millions = amount / 1_000_000
            if (millions <= 0) {
                chatNpc(
                    player,
                    "You need at least 1,000,000 coins to trade.",
                    animation = 554
                )
                showTradeMenu(player)
                return
            }
            
            when (options(
                player,
                "Trade all ${millions}M coins ($millions points)",
                "Trade a specific amount",
                "Cancel"
            )) {
                1 -> {
                    val coinsToRemove = millions * 1_000_000
                    if (player.inventory.remove(itemId, coinsToRemove).hasSucceeded()) {
                        DoomPoints.addDoomPoints(player, millions)
                        chatNpc(
                            player,
                            "Your offering is accepted. You have gained $millions<br>Doom Point${if (millions == 1) "" else "s"}.",
                            animation = 567
                        )
                    }
                    showTradeMenu(player)
                }
                2 -> {
                    // TODO: Implement custom amount input
                    chatNpc(player, "This feature is coming soon.", animation = 554)
                    showTradeMenu(player)
                }
                3 -> showTradeMenu(player)
            }
            return
        }
        
        // For regular items
        val totalPoints = basePoints * amount
        
        val optionsList = mutableListOf(
            "Trade 1 $itemName ($basePoints points)"
        )
        if (amount > 1) {
            optionsList.add("Trade all $amount $itemName ($totalPoints points)")
        }
        optionsList.add("Cancel")
        
        when (options(player, *optionsList.toTypedArray())) {
            1 -> {
                if (player.inventory.remove(itemId, 1).hasSucceeded()) {
                    DoomPoints.addDoomPoints(player, basePoints)
                    chatNpc(
                        player,
                        "Your offering is accepted. You have gained $basePoints<br>Doom Point${if (basePoints == 1) "" else "s"}.",
                        animation = 567
                    )
                }
                showTradeMenu(player)
            }
            2 -> {
                if (amount > 1) {
                    if (player.inventory.remove(itemId, amount).hasSucceeded()) {
                        DoomPoints.addDoomPoints(player, totalPoints)
                        chatNpc(
                            player,
                            "Your offering is accepted. You have gained $totalPoints<br>Doom Point${if (totalPoints == 1) "" else "s"}.",
                            animation = 567
                        )
                    }
                    showTradeMenu(player)
                } else {
                    showTradeMenu(player)
                }
            }
            else -> showTradeMenu(player)
        }
    }
}