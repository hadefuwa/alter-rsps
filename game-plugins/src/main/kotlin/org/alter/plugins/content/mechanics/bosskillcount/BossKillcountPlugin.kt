package org.alter.plugins.content.mechanics.bosskillcount

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import java.util.concurrent.ConcurrentHashMap

/**
 * Boss Killcount Plugin
 * 
 * Tracks and displays boss killcounts for players.
 * Killcounts are stored in player attributes and persist across sessions.
 */
class BossKillcountPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Attribute key for storing boss killcounts
         * Format: Map<String, Int> where key is boss name (lowercase) and value is killcount
         */
        val BOSS_KILLCOUNT_ATTR = AttributeKey<MutableMap<String, Int>>(persistenceKey = "boss_killcount")
        
        /**
         * Minimum combat level to be considered a boss
         */
        private const val MIN_BOSS_COMBAT_LEVEL = 100
        
        /**
         * List of specific NPCs that are considered bosses regardless of combat level
         * Using RSCM names for consistency
         */
        private val BOSS_NPCS = setOf(
            "npc.venenatis",
            "npc.callisto",
            "npc.vetion",
            "npc.scorpia",
            "npc.king_black_dragon",
            "npc.kalphite_queen_963",
            "npc.kalphite_queen_964",
            "npc.cerberus",
            "npc.crazy_archaeologist",
            "npc.chaos_fanatic",
            "npc.chaos_elemental",
            "npc.obor",
            "npc.bryophyta",
            "npc.general_graardor",
            "npc.commander_zilyana",
            "npc.kree_arra",
            "npc.kril_tsutsaroth",
            "npc.zulrah",
            "npc.vorkath",
            "npc.corporeal_beast",
            "npc.nightmare",
            "npc.phosanis_nightmare",
            "npc.sarachnis",
            "npc.grotesque_guardians",
            "npc.hydra",
            "npc.alchemical_hydra",
            "npc.skotizo",
            "npc.kraken",
            "npc.thermonuclear_smoke_devil",
            "npc.dagannoth_rex",
            "npc.dagannoth_prime",
            "npc.dagannoth_supreme",
            "npc.giant_mole",
            "npc.kalphite_queen",
            "npc.kree_arra",
            "npc.kril_tsutsaroth",
            "npc.commander_zilyana",
            "npc.general_graardor"
        )
        
        /**
         * Cached set of boss NPC IDs for fast lookup
         */
        private val bossNpcIds = mutableSetOf<Int>()
    }
    
    init {
        // Cache boss NPC IDs at initialization
        BOSS_NPCS.forEach { bossName ->
            try {
                val npcId = getRSCM(bossName)
                if (npcId != -1) {
                    bossNpcIds.add(npcId)
                }
            } catch (e: Exception) {
                // NPC might not exist in RSCM, skip it
            }
        }
        
        // Track boss kills
        onAnyNpcDeath {
            val npc = ctx as Npc
            
            // Check if this NPC is a boss
            if (!isBoss(npc)) {
                return@onAnyNpcDeath
            }
            
            // Find the player who dealt the most damage
            val killer = npc.damageMap.getMostDamage() as? Player ?: return@onAnyNpcDeath
            
            // Increment killcount
            incrementBossKillcount(killer, npc)
        }
    }
    
    /**
     * Checks if an NPC is considered a boss
     */
    private fun isBoss(npc: Npc): Boolean {
        // Check if NPC ID is in boss list
        if (bossNpcIds.contains(npc.id)) {
            return true
        }
        
        // Check if NPC combat level meets minimum requirement
        val combatLevel = npc.def.combatLevel
        if (combatLevel >= MIN_BOSS_COMBAT_LEVEL) {
            return true
        }
        
        // Check if NPC name contains "boss" keywords (case-insensitive)
        val npcName = npc.def.name.lowercase()
        val bossKeywords = listOf("boss", "king", "queen", "lord", "general", "commander", "kree", "zilyana", "graardor", "kril")
        if (bossKeywords.any { npcName.contains(it) }) {
            return true
        }
        
        return false
    }
    
    /**
     * Increments the killcount for a boss and displays it to the player
     */
    private fun incrementBossKillcount(player: Player, npc: Npc) {
        // Get or create killcount map
        val killcounts = player.attr[BOSS_KILLCOUNT_ATTR] ?: run {
            val newMap = ConcurrentHashMap<String, Int>()
            player.attr[BOSS_KILLCOUNT_ATTR] = newMap
            newMap
        }
        
        // Get boss name (use lowercase for consistency)
        val bossName = npc.def.name
        
        // Increment killcount
        val currentKc = killcounts.getOrDefault(bossName.lowercase(), 0)
        val newKc = currentKc + 1
        killcounts[bossName.lowercase()] = newKc
        
        // Display killcount in chatbox
        player.message("<col=ff6600>Your $bossName killcount is now: $newKc</col>")
    }
    
    /**
     * Gets the killcount for a specific boss
     */
    fun getBossKillcount(player: Player, bossName: String): Int {
        val killcounts = player.attr[BOSS_KILLCOUNT_ATTR] ?: return 0
        return killcounts.getOrDefault(bossName.lowercase(), 0)
    }
    
    /**
     * Gets all boss killcounts for a player
     */
    fun getAllBossKillcounts(player: Player): Map<String, Int> {
        return player.attr[BOSS_KILLCOUNT_ATTR] ?: emptyMap()
    }
}

