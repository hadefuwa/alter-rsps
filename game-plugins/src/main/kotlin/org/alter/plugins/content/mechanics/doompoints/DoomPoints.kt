package org.alter.plugins.content.mechanics.doompoints

import org.alter.api.ext.message
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.api.Skills

/**
 * Doom Points System
 * 
 * Players can trade high-value items to the Doomsayer NPC for Doom Points,
 * which can then be used to unlock permanent account perks.
 */
object DoomPoints {
    // Persistent attribute keys
    val DOOM_POINTS_ATTR = AttributeKey<Int>("doom_points")
    val DAMAGE_MULTIPLIER_PERK = AttributeKey<Int>("doom_perk_damage_mult")
    val DROP_RATE_PERK = AttributeKey<Int>("doom_perk_drop_rate")
    val PASSIVE_XP_PERK = AttributeKey<Boolean>("doom_perk_passive_xp")
    val COIN_MULTIPLIER_PERK = AttributeKey<Int>("doom_perk_coin_mult")
    val SLAYER_POINTS_PERK = AttributeKey<Int>("doom_perk_slayer_points")
    
    /**
     * Get the player's current Doom Points
     */
    fun getDoomPoints(player: Player): Int {
        return player.attr[DOOM_POINTS_ATTR] ?: 0
    }
    
    /**
     * Add Doom Points to a player
     */
    fun addDoomPoints(player: Player, amount: Int) {
        val current = getDoomPoints(player)
        player.attr[DOOM_POINTS_ATTR] = current + amount
        player.message("You have been awarded $amount Doom Point${if (amount == 1) "" else "s"}. Total: ${current + amount}")
    }
    
    /**
     * Remove Doom Points from a player (returns false if insufficient points)
     */
    fun removeDoomPoints(player: Player, amount: Int): Boolean {
        val current = getDoomPoints(player)
        if (current < amount) {
            return false
        }
        player.attr[DOOM_POINTS_ATTR] = current - amount
        return true
    }
    
    /**
     * Get the damage multiplier percentage (e.g., 5 = 5% increase)
     */
    fun getDamageMultiplier(player: Player): Int {
        return player.attr[DAMAGE_MULTIPLIER_PERK] ?: 0
    }
    
    /**
     * Get the drop rate multiplier percentage (e.g., 10 = 10% increase)
     */
    fun getDropRateMultiplier(player: Player): Int {
        return player.attr[DROP_RATE_PERK] ?: 0
    }
    
    /**
     * Check if passive XP perk is active
     */
    fun hasPassiveXpPerk(player: Player): Boolean {
        return player.attr[PASSIVE_XP_PERK] ?: false
    }
    
    /**
     * Get the coin multiplier percentage (e.g., 20 = 20% more coins)
     */
    fun getCoinMultiplier(player: Player): Int {
        return player.attr[COIN_MULTIPLIER_PERK] ?: 0
    }
    
    /**
     * Get the slayer points bonus percentage (e.g., 50 = 50% more slayer points)
     */
    fun getSlayerPointsBonus(player: Player): Int {
        return player.attr[SLAYER_POINTS_PERK] ?: 0
    }
    
    /**
     * Apply passive XP to the player's lowest skill
     */
    fun applyPassiveXp(player: Player) {
        if (!hasPassiveXpPerk(player)) return
        
        // Find the lowest skill (23 total skills: 0-22)
        var lowestSkill = Skills.ATTACK
        var lowestLevel = player.getSkills().getBaseLevel(Skills.ATTACK)
        
        for (skill in 0..22) {
            val level = player.getSkills().getBaseLevel(skill)
            if (level < lowestLevel) {
                lowestLevel = level
                lowestSkill = skill
            }
        }
        
        // Grant passive XP (1 XP per game tick, adjust as needed)
        player.addXp(lowestSkill, 1.0)
    }
    
    /**
     * Item trade-in values (item ID -> Doom Points)
     */
    val TRADE_IN_VALUES = mapOf(
        // Tier 1: Low-value items (1-5 points)
        995 to 1,      // 1M coins = 1 point (per million)
        
        // Tier 2: Medium-value items (10-50 points)
        11834 to 10,   // Bandos chestplate
        11836 to 10,   // Bandos tassets
        11832 to 15,   // Bandos boots
        6585 to 20,    // Amulet of fury
        11773 to 25,   // Berserker ring
        6737 to 30,    // Berserker ring (i)
        
        // Tier 3: High-value items (50-100 points)
        13576 to 50,   // Dragon warhammer
        11785 to 60,   // Armadyl crossbow
        12002 to 75,   // Occult necklace
        19481 to 80,   // Heavy ballista
        13652 to 90,   // Dragon claws
        
        // Tier 4: Very high-value items (100-250 points)
        11802 to 100,  // Armadyl godsword
        11804 to 100,  // Bandos godsword
        11806 to 100,  // Saradomin godsword
        11808 to 100,  // Zamorak godsword
        20997 to 150,  // Twisted bow
        21021 to 150,  // Elder maul
        21003 to 200,  // Kodai wand
        
        // Tier 5: Ultra rare items (250-500 points)
        13239 to 250,  // Primordial boots
        13235 to 250,  // Eternal boots
        13237 to 250,  // Pegasian boots
        22324 to 300,  // Sanguinesti staff
        22322 to 350,  // Scythe of vitur
        21015 to 400,  // Ancestral robe top
        21018 to 400,  // Ancestral robe bottom
        
        // Tier 6: Legendary items (500+ points)
        20784 to 500,  // Dragon claws (ornament kit)
        21295 to 600,  // Infernal cape
        13652 to 700,  // Elysian spirit shield
        22613 to 800,  // Avernic defender
        25739 to 1000, // Holy scythe of vitur
    )
    
    /**
     * Perk definitions (name, cost, description)
     */
    data class Perk(
        val name: String,
        val cost: Int,
        val description: String,
        val maxLevel: Int = 1,
        val unlock: (Player, Int) -> Unit
    )
    
    val AVAILABLE_PERKS = listOf(
        Perk(
            name = "Damage Multiplier",
            cost = 100,
            description = "Increase all damage dealt by 5% per level (max 25%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[DAMAGE_MULTIPLIER_PERK] = level * 5
            player.message("Damage multiplier increased to ${level * 5}%!")
        },
        
        Perk(
            name = "Increased Drop Rate",
            cost = 150,
            description = "Increase rare drop rates by 10% per level (max 50%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[DROP_RATE_PERK] = level * 10
            player.message("Drop rate multiplier increased to ${level * 10}%!")
        },
        
        Perk(
            name = "Passive XP",
            cost = 200,
            description = "Gain passive XP to your lowest skill over time",
            maxLevel = 1
        ) { player, _ ->
            player.attr[PASSIVE_XP_PERK] = true
            player.message("Passive XP perk unlocked! Your lowest skill will gain XP over time.")
        },
        
        Perk(
            name = "Coin Multiplier",
            cost = 120,
            description = "Increase coin drops by 20% per level (max 100%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[COIN_MULTIPLIER_PERK] = level * 20
            player.message("Coin multiplier increased to ${level * 20}%!")
        },
        
        Perk(
            name = "Slayer Points Bonus",
            cost = 180,
            description = "Increase slayer points earned by 50% per level (max 250%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[SLAYER_POINTS_PERK] = level * 50
            player.message("Slayer points bonus increased to ${level * 50}%!")
        }
    )
    
    /**
     * Get the current level of a perk
     */
    fun getPerkLevel(player: Player, perkIndex: Int): Int {
        return when (perkIndex) {
            0 -> (player.attr[DAMAGE_MULTIPLIER_PERK] ?: 0) / 5
            1 -> (player.attr[DROP_RATE_PERK] ?: 0) / 10
            2 -> if (player.attr[PASSIVE_XP_PERK] == true) 1 else 0
            3 -> (player.attr[COIN_MULTIPLIER_PERK] ?: 0) / 20
            4 -> (player.attr[SLAYER_POINTS_PERK] ?: 0) / 50
            else -> 0
        }
    }
}
