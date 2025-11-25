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
import org.alter.plugins.content.mechanics.doompoints.DoomPoints
import org.alter.rscm.RSCM.getRSCM

/**
 * Dark Merchant NPC Plugin
 * 
 * An NPC in Varrock who accepts high-value items in exchange for Doom Points.
 * Players can then spend Doom Points to unlock permanent account perks.
 * 
 * Using "Mysterious Old Man" NPC (ID 410) as the Dark Merchant
 */
class DarkMerchantPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Spawn the Dark Merchant in Varrock (near the fountain)
        spawnNpc("npc.mysterious_old_man", x = 3210, z = 3424, walkRadius = 2, direction = Direction.SOUTH)
        
        // Use numeric option IDs since the NPC has no string options defined
        // Option 1 = first click (talk-to)
        onNpcOption("npc.mysterious_old_man", option = 1, lineOfSightDistance = 4) {
            player.queue { dialog(player) }
        }
        
        // Option 2 = second click (trade)
        onNpcOption("npc.mysterious_old_man", option = 2, lineOfSightDistance = 4) {
            player.queue { showTradeMenu(player) }
        }
        
        // Option 3 = third click (perks)
        onNpcOption("npc.mysterious_old_man", option = 3, lineOfSightDistance = 4) {
            player.queue { showPerksMenu(player) }
        }
        
        // Command to check doom points
        onCommand("doompoints") {
            val points = DoomPoints.getDoomPoints(player)
            player.message("You have $points Doom Point${if (points == 1) "" else "s"}.")
        }
    }
    
    private suspend fun QueueTask.dialog(player: Player) {
        val currentPoints = DoomPoints.getDoomPoints(player)
        
        chatNpc(
            player,
            "Greetings, ${player.username}. I am the Dark Merchant,<br>keeper of forbidden knowledge and ancient power.",
            animation = 567
        )
        
        chatNpc(
            player,
            "You currently possess $currentPoints Doom Point${if (currentPoints == 1) "" else "s"}.",
            animation = 554
        )
        
        chatPlayer(player, "What do you do here?", animation = 588)
        
        chatNpc(
            player,
            "I offer power beyond your wildest dreams... for a price.<br>Bring me items of great value, and I shall grant you<br>Doom Points.",
            animation = 554
        )
        
        chatNpc(
            player,
            "With these points, you may unlock permanent perks that<br>will enhance your abilities forever.",
            animation = 567
        )
        
        when (options(
            player,
            "What items do you accept?",
            "Show me the perks.",
            "I'd like to trade items.",
            "Nevermind."
        )) {
            1 -> showItemList(player)
            2 -> showPerksMenu(player)
            3 -> showTradeMenu(player)
            4 -> chatPlayer(player, "I'll think about it.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.showItemList(player: Player) {
        chatNpc(
            player,
            "I accept many powerful items. The more valuable the<br>item, the more Doom Points you shall receive.",
            animation = 567
        )
        
        chatNpc(
            player,
            "Common items like coins are worth little, but rare<br>equipment like godswords, twisted bows, and ancestral<br>robes are worth much more.",
            animation = 554
        )
        
        chatPlayer(player, "How many points do specific items give?", animation = 588)
        
        chatNpc(
            player,
            "That depends on their power. Godswords grant 100 points,<br>twisted bows 150, scythes 350, and the rarest items<br>can grant 1000 or more!",
            animation = 567
        )
        
        when (options(player, "I'd like to trade.", "Tell me about the perks.", "Goodbye.")) {
            1 -> showTradeMenu(player)
            2 -> showPerksMenu(player)
            3 -> chatPlayer(player, "Goodbye.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.showPerksMenu(player: Player) {
        val currentPoints = DoomPoints.getDoomPoints(player)
        
        chatNpc(
            player,
            "You currently have $currentPoints Doom Point${if (currentPoints == 1) "" else "s"}.<br>Here are the perks you can unlock:",
            animation = 567
        )
        
        // Build perk options
        val perkOptions = DoomPoints.AVAILABLE_PERKS.mapIndexed { index, perk ->
            val currentLevel = DoomPoints.getPerkLevel(player, index)
            val nextLevel = currentLevel + 1
            
            if (currentLevel >= perk.maxLevel) {
                "${perk.name} [MAX LEVEL]"
            } else {
                "${perk.name} (${perk.cost} points) [Level $currentLevel/${perk.maxLevel}]"
            }
        } + "Check my active perks" + "Nevermind"
        
        val choice = options(player, *perkOptions.toTypedArray())
        
        when {
            choice <= DoomPoints.AVAILABLE_PERKS.size -> {
                val perkIndex = choice - 1
                val perk = DoomPoints.AVAILABLE_PERKS[perkIndex]
                val currentLevel = DoomPoints.getPerkLevel(player, perkIndex)
                
                if (currentLevel >= perk.maxLevel) {
                    chatNpc(player, "You have already maxed out this perk!", animation = 554)
                    showPerksMenu(player)
                } else {
                    showPerkDetails(player, perkIndex)
                }
            }
            choice == DoomPoints.AVAILABLE_PERKS.size + 1 -> showActivePerks(player)
            else -> chatPlayer(player, "I'll think about it.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.showPerkDetails(player: Player, perkIndex: Int) {
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
                    showPerksMenu(player)
                } else {
                    chatNpc(
                        player,
                        "You do not have enough Doom Points!<br>You need ${perk.cost} but only have $currentPoints.",
                        animation = 554
                    )
                    showPerksMenu(player)
                }
            }
            2 -> showPerksMenu(player)
            3 -> chatPlayer(player, "Nevermind.", animation = 588)
        }
    }
    
    private suspend fun QueueTask.showActivePerks(player: Player) {
        val damageBonus = DoomPoints.getDamageMultiplier(player)
        val dropBonus = DoomPoints.getDropRateMultiplier(player)
        val coinBonus = DoomPoints.getCoinMultiplier(player)
        val slayerBonus = DoomPoints.getSlayerPointsBonus(player)
        val hasPassiveXp = DoomPoints.hasPassiveXpPerk(player)
        
        chatNpc(player, "Here are your currently active perks:", animation = 567)
        
        player.message("=== Active Doom Perks ===")
        if (damageBonus > 0) player.message("Damage Multiplier: +$damageBonus%")
        if (dropBonus > 0) player.message("Drop Rate Increase: +$dropBonus%")
        if (coinBonus > 0) player.message("Coin Multiplier: +$coinBonus%")
        if (slayerBonus > 0) player.message("Slayer Points Bonus: +$slayerBonus%")
        if (hasPassiveXp) player.message("Passive XP: Active")
        
        if (damageBonus == 0 && dropBonus == 0 && coinBonus == 0 && slayerBonus == 0 && !hasPassiveXp) {
            player.message("No perks unlocked yet.")
        }
        
        showPerksMenu(player)
    }
    
    private suspend fun QueueTask.showTradeMenu(player: Player) {
        chatNpc(
            player,
            "Show me what you have, and I shall tell you its worth<br>in Doom Points.",
            animation = 567
        )
        
        // Check inventory for tradeable items
        val tradeableItems = mutableListOf<Pair<Int, Int>>() // slot, item id
        
        for (slot in 0 until player.inventory.capacity) {
            val item = player.inventory[slot]
            if (item != null) {
                val itemId = item.id
                if (DoomPoints.TRADE_IN_VALUES.containsKey(itemId)) {
                    tradeableItems.add(Pair(slot, itemId))
                }
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
        val itemOptions = tradeableItems.map { (slot, itemId) ->
            val item = player.inventory[slot]!!
            val itemDef = getItem(itemId)
            val itemName = itemDef.name
            val points = DoomPoints.TRADE_IN_VALUES[itemId] ?: 0
            val amount = item.amount
            
            // Special handling for coins (1M coins = 1 point)
            if (itemId == 995) {
                val millions = amount / 1_000_000
                if (millions > 0) {
                    "$itemName (${millions}M = $millions points)"
                } else {
                    "$itemName (need 1M+ for points)"
                }
            } else {
                "$itemName ($points points each, $amount in inventory)"
            }
        } + "Nevermind"
        
        val choice = options(player, *itemOptions.toTypedArray())
        
        if (choice <= tradeableItems.size) {
            val (slot, itemId) = tradeableItems[choice - 1]
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
