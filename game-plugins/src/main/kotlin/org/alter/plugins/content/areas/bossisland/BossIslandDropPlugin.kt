package org.alter.plugins.content.areas.bossisland

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.entity.*
import org.alter.game.model.timer.TimeConstants
import org.alter.game.plugin.*
import org.alter.game.model.weightedTableBuilder.roll
import org.alter.rscm.RSCM.getRSCM

/**
 * Boss Island Drop Enhancement Plugin
 * 
 * Provides tripled drop rates for all bosses spawned on Boss Island.
 * This plugin intercepts boss deaths on the island and modifies their
 * loot drops to provide 3x the normal rewards while preserving all
 * existing drop mechanics and integrations.
 * 
 * Features:
 * - Automatically detects bosses on Boss Island
 * - Triples the quantity of all dropped items
 * - Preserves original drop mechanics and probabilities
 * - Works with existing loot systems (shared loot, etc.)
 * - Integrates with NpcLootDropPlugin and SharedLootDropPlugin
 * - Special handling for coin drops and rare items
 * 
 * Location Detection: Bosses within the Boss Island region around (3423, 4089)
 * Integration: Hooks into the existing loot system after normal drops are generated
 */
class BossIslandDropPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Boss Island center coordinates
         */
        private const val ISLAND_CENTER_X = 3423
        private const val ISLAND_CENTER_Z = 4089
        private const val ISLAND_HEIGHT = 0
        
        /**
         * Radius around the island center to consider as "Boss Island"
         * This should encompass all boss spawn locations
         */
        private const val ISLAND_RADIUS = 35
        
        /**
         * Drop multiplier for Boss Island
         */
        private const val DROP_MULTIPLIER = 3
        
        /**
         * Set of boss NPC RSCM names that should get enhanced drops on the island
         */
        private val BOSS_ISLAND_NPCS = setOf(
            "npc.venenatis",
            "npc.callisto", 
            "npc.vetion",
            "npc.scorpia",
            "npc.king_black_dragon",
            "npc.kalphite_queen_963",
            "npc.kalphite_queen_964", // Form 2 (if it transforms)
            "npc.cerberus", // Sewer Abomination
            "npc.crazy_archaeologist",
            "npc.chaos_fanatic",
            "npc.chaos_elemental"
        )
        
        /**
         * Custom attribute key for Boss Island drop multiplier
         */
        val BOSS_ISLAND_DROP_MULTIPLIER = AttributeKey<Int>("boss_island_drop_multiplier")
    }
    
    /**
     * Cached set of NPC IDs for fast lookup
     */
    private val bossIslandNpcIds = mutableSetOf<Int>()
    
    init {
        // Cache NPC IDs at initialization
        BOSS_ISLAND_NPCS.forEach { rscmName ->
            try {
                val npcId = getRSCM(rscmName)
                bossIslandNpcIds.add(npcId)
                println("Boss Island Drops: Registered $rscmName (ID: $npcId) for enhanced drops")
            } catch (e: Exception) {
                println("Boss Island Drops: Warning - Could not find NPC $rscmName: ${e.message}")
            }
        }
        
        // Set up drop enhancement system
        setupDropEnhancement()
        
        println("Boss Island Drops: Initialized tripled drop system for ${bossIslandNpcIds.size} boss types")
        println("Boss Island Drops: Island location (${ISLAND_CENTER_X}, ${ISLAND_CENTER_Z}) with radius ${ISLAND_RADIUS}")
    }
    
    /**
     * Sets up the drop enhancement system
     */
    private fun setupDropEnhancement() {
        // Hook into any NPC death to detect Boss Island bosses
        // This sets the multiplier attribute so other plugins can use it
        onAnyNpcDeath {
            val npc = ctx as Npc
            
            // Check if this is a boss on Boss Island
            if (isBossOnIsland(npc)) {
                // Set the drop multiplier attribute for other plugins to use
                npc.attr[BOSS_ISLAND_DROP_MULTIPLIER] = DROP_MULTIPLIER
                
                // Get all players who dealt damage to the NPC
                val playersWhoDamaged = mutableListOf<Player>()
                
                npc.world.players.forEach { player ->
                    if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                        playersWhoDamaged.add(player)
                    }
                }
                
                if (playersWhoDamaged.isEmpty()) {
                    return@onAnyNpcDeath // No players dealt damage
                }
                
                // Message all players who participated
                playersWhoDamaged.forEach { player ->
                    player.message("<col=ff6600>Boss Island: This boss drops ${DROP_MULTIPLIER}x the normal loot + 3M coins!</col>")
                }
                
                println("Boss Island Drops: Set ${DROP_MULTIPLIER}x multiplier for ${npc.def.name} (ID: ${npc.id}) with ${playersWhoDamaged.size} damage dealers")
                
                // Generate guaranteed 3M coins and bonus loot for each player who dealt damage
                playersWhoDamaged.forEach { player ->
                    generateGuaranteedCoinsForPlayer(npc, player)
                    generateBonusLootForPlayer(npc, player)
                }
            }
        }
    }
    
    /**
     * Checks if an NPC is a boss located on Boss Island
     */
    private fun isBossOnIsland(npc: Npc): Boolean {
        // Check if it's a registered boss type
        if (!bossIslandNpcIds.contains(npc.id)) {
            return false
        }
        
        // Check if it's within the Boss Island area
        val npcTile = npc.tile
        if (npcTile.height != ISLAND_HEIGHT) {
            return false
        }
        
        val distanceX = kotlin.math.abs(npcTile.x - ISLAND_CENTER_X)
        val distanceZ = kotlin.math.abs(npcTile.z - ISLAND_CENTER_Z)
        val distance = kotlin.math.max(distanceX, distanceZ)
        
        return distance <= ISLAND_RADIUS
    }
    
    /**
     * Generates guaranteed 3M coins drop for a specific player who dealt damage
     */
    private fun generateGuaranteedCoinsForPlayer(npc: Npc, player: Player) {
        val coinsItemId = getRSCM("item.coins_995")
        val guaranteedCoins = GroundItem(
            item = coinsItemId,
            amount = 3000000, // 3 million coins
            tile = npc.tile,
            owner = player
        )
        
        // Set the same timers as normal drops
        guaranteedCoins.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
        guaranteedCoins.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
        guaranteedCoins.ownerShipType = 1
        
        npc.world.spawn(guaranteedCoins)
        println("Boss Island: Spawned guaranteed 3M coins for ${npc.def.name} kill for ${player.username}")
    }

    /**
     * Generates bonus loot for a specific player who dealt damage to the boss
     * This creates additional drops beyond the normal 1x drops
     */
    private fun generateBonusLootForPlayer(npc: Npc, player: Player) {
        val lootTables = npc.combatDef.LootTables
        
        if (lootTables.isNullOrEmpty()) {
            return // No loot tables configured
        }
        
        try {
            // Generate 2 additional sets of loot (since base plugin will generate 1x, we add 2x more for 3x total)
            repeat(DROP_MULTIPLIER - 1) {
                val bonusDrops = roll(player, lootTables)
                
                bonusDrops.forEach { groundItem ->
                    val bonusGroundItem = GroundItem(
                        item = groundItem.item,
                        amount = groundItem.amount,
                        tile = npc.tile,
                        owner = player
                    )
                    
                    // Set the same timers as normal drops
                    bonusGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                    bonusGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                    bonusGroundItem.ownerShipType = 1
                    
                    npc.world.spawn(bonusGroundItem)
                }
            }
            
            println("Boss Island Drops: Generated ${DROP_MULTIPLIER - 1} bonus loot sets for ${npc.def.name} for ${player.username}")
            
        } catch (e: Exception) {
            println("Boss Island Drops: Error generating bonus loot for ${npc.def.name} for ${player.username}: ${e.message}")
            e.printStackTrace()
        }
    }
}

/**
 * Extension function to get Boss Island drop multiplier from an NPC
 * This can be used by other plugins to check if an NPC has enhanced drops
 */
fun Npc.getBossIslandDropMultiplier(): Int {
    return this.attr[BossIslandDropPlugin.BOSS_ISLAND_DROP_MULTIPLIER] ?: 1
}

/**
 * Extension function to check if an NPC died on Boss Island
 */
fun Npc.isDiedOnBossIsland(): Boolean {
    return this.attr.has(BossIslandDropPlugin.BOSS_ISLAND_DROP_MULTIPLIER)
}