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
    val PASSIVE_XP_PERK = AttributeKey<Int>("doom_perk_passive_xp")
    val COIN_MULTIPLIER_PERK = AttributeKey<Int>("doom_perk_coin_mult")
    val SLAYER_POINTS_PERK = AttributeKey<Int>("doom_perk_slayer_points")
    val SELECT_SLAYER_TASK_UNLOCK = AttributeKey<Boolean>("doom_unlock_select_slayer_task")
    val INCREASED_BANK_UNLOCK = AttributeKey<Boolean>("doom_unlock_increased_bank")
    val REMOTE_BANK_UNLOCK = AttributeKey<Boolean>("doom_unlock_remote_bank")
    
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
        return (player.attr[PASSIVE_XP_PERK] ?: 0) > 0
    }
    
    /**
     * Get the passive XP perk level (0 = inactive, 1-5 = active levels)
     */
    fun getPassiveXpPerkLevel(player: Player): Int {
        val value = player.attr[PASSIVE_XP_PERK]
        return when (value) {
            is Int -> value
            is Boolean -> if (value) 1 else 0  // Convert old Boolean format to Int
            else -> 0
        }
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
     * Try to apply passive XP to the player's lowest skill with a chance based on perk level
     * Called whenever the player gains XP from any skill
     * 
     * @param player The player who gained XP
     * @param xpGained The amount of XP that was just gained (used for chance calculation)
     * @return true if passive XP was granted, false otherwise
     */
    fun tryApplyPassiveXp(player: Player, xpGained: Double): Boolean {
        val perkLevel = getPassiveXpPerkLevel(player)
        if (perkLevel == 0) return false
        
        // Chance increases with perk level: 5% per level (5% at level 1, 10% at level 2, etc.)
        // Base chance is 5% per level, so level 5 = 25% chance
        val chancePercent = perkLevel * 5
        val roll = player.world.random(100)
        
        if (roll >= chancePercent) {
            return false // Didn't roll the chance
        }
        
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
        
        // Grant passive XP (10% of the XP that was just gained, minimum 1 XP)
        val passiveXp = maxOf(1.0, xpGained * 0.10)
        player.addXp(lowestSkill, passiveXp)
        return true
    }
    
    /**
     * Item trade-in values (item ID -> Doom Points)
     */
    val TRADE_IN_VALUES = mapOf(
        // Tier 1: Low-value items (1-5 points)
        //995 to 1,      // 1M coins = 1 point (per million)
        
        // Tier 2: Medium-value items (10-50 points)
        11834 to 10,   // Bandos chestplate
        11836 to 10,   // Bandos tassets
        6585 to 20,    // Amulet of fury
        11773 to 25,   // Berserker ring
        6737 to 30,    // Berserker ring (i)
        
        // Vesta's Armour Set (10 points each)
        13887 to 10,   // Vesta's chainbody
        13893 to 10,   // Vesta's plateskirt
        13899 to 10,   // Vesta's longsword
        13905 to 10,   // Vesta's spear
        22610 to 10,   // Vesta's spear (variant)
        22616 to 10,   // Vesta's chainbody (variant)
        22619 to 10,   // Vesta's plateskirt (variant)
        23615 to 10,   // Vesta's longsword (variant 2)
        
        // Statius's Armour Set (10 points each)
        13884 to 10,   // Statius's full helm
        13890 to 10,   // Statius's platebody
        13896 to 10,   // Statius's platelegs
        13902 to 10,   // Statius's warhammer
        22622 to 10,   // Statius's warhammer (variant)
        22625 to 10,   // Statius's full helm (variant)
        22626 to 10,   // Statius's full helm (noted)
        22628 to 10,   // Statius's platebody (variant)
        22631 to 10,   // Statius's platelegs (variant)
        23620 to 10,   // Statius's warhammer (variant 2)
        
        // Zuriel's Armour Set (10 points each)
        13885 to 10,   // Zuriel's hood
        13891 to 10,   // Zuriel's robe top
        13897 to 10,   // Zuriel's robe bottom
        13903 to 10,   // Zuriel's staff
        22647 to 10,   // Zuriel's staff (variant)
        22650 to 10,   // Zuriel's hood (variant)
        22653 to 10,   // Zuriel's robe top (variant)
        22656 to 10,   // Zuriel's robe bottom (variant)
        23617 to 10,   // Zuriel's staff (variant 2)
        
        // Morrigan's Armour Set (10 points each)
        13886 to 10,   // Morrigan's coif
        13892 to 10,   // Morrigan's leather body
        13898 to 10,   // Morrigan's leather chaps
        22634 to 10,   // Morrigan's throwing axe
        22636 to 1,   // Morrigan's javelin
        22638 to 10,   // Morrigan's coif (variant)
        22641 to 10,   // Morrigan's leather body (variant)
        22644 to 10,   // Morrigan's leather chaps (variant)
        23619 to 1,   // Morrigan's javelin (variant 2)
        
        // Tier 3: High-value items (50-100 points)
        13576 to 50,   // Dragon warhammer
        11785 to 60,   // Armadyl crossbow
        12002 to 75,   // Occult necklace
        19481 to 80,   // Heavy ballista
        13652 to 90,   // Dragon claws
        21012 to 80,  // Dragon Hunter Crossbow
        24422 to 80,  // Nightmare Staff
        
        // Tier 4: Very high-value items (100-250 points)
        11802 to 100,  // Armadyl godsword
        11804 to 100,  // Bandos godsword
        11806 to 100,  // Saradomin godsword
        11808 to 100,  // Zamorak godsword
        20997 to 150,  // Twisted bow
        21003 to 150,  // Elder maul (fixed ID)
        21006 to 200,  // Kodai wand
        21015 to 100,  // Dinh's Bulwark
        22327 to 100,  // Justiciar Chestguard
        22328 to 100,  // Justiciar Legguards
        22326 to 100,  // Justiciar Faceguard
        26235 to 100,  // Nex's Zaryte Vambraces (fixed ID)
        26374 to 100,  // Zaryte Crossbow
        24417 to 100,  // Inquisitor's Mace
        24419 to 100,  // Inquisitor's Great Helm
        24420 to 100,  // Inquisitor's Hauberk
        24421 to 100,  // Inquisitor's Plateskirt
        24551 to 100,  // Blade of Saeldor (charged)
        27226 to 100,  // Masori Mask (F)
        27229 to 100,  // Masori Chestplate (F)
        27232 to 100,  // Masori Chaps (F)
        
        // 3rd Age Items (100-150 points)
        10334 to 100,  // 3rd Age Range Coif
        10330 to 100,  // 3rd Age Range Body
        10332 to 100,  // 3rd Age Range Legs
        10348 to 100,  // 3rd Age Melee Platebody
        10346 to 100,  // 3rd Age Melee Platelegs
        10350 to 100,  // 3rd Age Melee Full Helmet
        12422 to 100,  // 3rd Age Wand
        12437 to 100,  // 3rd Age Cloak
        10344 to 100,  // 3rd Age Amulet
        10352 to 100,  // 3rd Age Kiteshield
        12424 to 100,  // 3rd Age Bow
        20014 to 100,  // 3rd Age Pickaxe
        12426 to 100,  // 3rd Age Longsword
        20011 to 100,  // 3rd Age Axe
        23336 to 150,  // 3rd Age Druidic Robe Top
        23339 to 150,  // 3rd Age Druidic Robe Bottom
        23345 to 150,  // 3rd Age Druidic Cloak
        
        // Tier 5: Ultra rare items (250-500 points)
        13239 to 50,  // Primordial boots
        13235 to 50,  // Eternal boots
        13237 to 50,  // Pegasian boots
        13231 to 50,  // Primordial crystal
        13227 to 50,  // Eternal crystal
        13229 to 50,  // Pegasian crystal
        22323 to 200,  // Sanguinesti staff (fixed ID)
        
        // Scythe of Vitur Variants
        22325 to 350,  // Scythe of vitur
        22486 to 200,  // Scythe of vitur (uncharged)
        22664 to 350,  // Scythe of vitur (variant)
        
        21021 to 200,  // Ancestral robe top (fixed ID)
        21024 to 200,  // Ancestral robe bottom (fixed ID)
        22324 to 200,  // Ghrazi Rapier
        24511 to 200,  // Harmonised Orb
        24514 to 200,  // Volatile Orb
        24517 to 200,  // Eldritch Orb
        27251 to 200,  // Elidinis' Ward (F)
        
        // Torva Armour Set (400 points each)
        26382 to 200,  // Torva Full Helm
        26384 to 200,  // Torva Platebody
        26386 to 200,  // Torva Platelegs
        
        // Torva Armour Set (Damaged) (300 points each)
        26376 to 200,  // Torva Full Helm (Damaged)
        26378 to 200,  // Torva Platebody (Damaged)
        26380 to 200,  // Torva Platelegs (Damaged)
        
        // Sanguine Torva Armour Set (500 points each)
        28254 to 200,  // Sanguine Torva Full Helm
        28256 to 200,  // Sanguine Torva Platebody
        28258 to 200,  // Sanguine Torva Platelegs
        
        // Torva Armour Set (Variant) (400 points each)
        30302 to 200,  // Torva Full Helm (Variant)
        30303 to 200,  // Torva Platebody (Variant)
        30304 to 200,  // Torva Platelegs (Variant)
        
        24423 to 200,  // Harmonised Nightmare Staff
        
        // Tier 6: Legendary items (500+ points)
        20784 to 200,  // Dragon claws (ornament kit)
        21295 to 200,  // Infernal cape
        12817 to 200,  // Elysian spirit shield (fixed ID)
        22613 to 200,  // Avernic defender
        
        // Scythe of Vitur - Special Variants
        25736 to 200, // Holy scythe of vitur
        25738 to 200,  // Holy scythe of vitur (uncharged)
        25739 to 200, // Sanguine scythe of vitur
        25741 to 200,  // Sanguine scythe of vitur (uncharged)
        28543 to 200,  // Corrupted scythe of vitur
        28545 to 200,  // Corrupted scythe of vitur (uncharged)
        
        27277 to 1200, // Tumeken's Shadow
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
            description = "Increase all damage dealt by 25% per level (max 125%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[DAMAGE_MULTIPLIER_PERK] = level * 25
            player.message("Damage multiplier increased to ${level * 25}%!")
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
            description = "Chance to gain passive XP to your lowest skill when gaining XP (5% per level, max 25%)",
            maxLevel = 5
        ) { player, level ->
            player.attr[PASSIVE_XP_PERK] = level
            val chance = level * 5
            player.message("Passive XP perk level $level unlocked! You now have a $chance% chance to gain passive XP when training.")
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
        },
        
        Perk(
            name = "Slayer Task Selector",
            cost = 1000,
            description = "Unlock the ability to select your own slayer task at any Slayer Master.",
            maxLevel = 1
        ) { player, level ->
            player.attr[SELECT_SLAYER_TASK_UNLOCK] = true
            player.message("You have unlocked the ability to select your own slayer tasks!")
        },
        
        Perk(
            name = "Increased Bank Storage",
            cost = 1000,
            description = "Increase your bank capacity from 800 to 1200 slots.",
            maxLevel = 1
        ) { player, level ->
            player.attr[INCREASED_BANK_UNLOCK] = true
            if (player.bank.capacity < 1200) {
                player.bank.resize(1200)
            }
            player.message("You have unlocked increased bank storage! Capacity is now 1200.")
        },
        
        Perk(
            name = "Remote Banking",
            cost = 1000,
            description = "Unlock the ability to open your bank from anywhere using ::bank.",
            maxLevel = 1
        ) { player, level ->
            player.attr[REMOTE_BANK_UNLOCK] = true
            player.message("You have unlocked remote banking! Type ::bank to use it.")
        }
    )
    
    /**
     * Get the current level of a perk
     */
    fun getPerkLevel(player: Player, perkIndex: Int): Int {
        return when (perkIndex) {
            0 -> (player.attr[DAMAGE_MULTIPLIER_PERK] ?: 0) / 25
            1 -> (player.attr[DROP_RATE_PERK] ?: 0) / 10
            2 -> player.attr[PASSIVE_XP_PERK] ?: 0
            3 -> (player.attr[COIN_MULTIPLIER_PERK] ?: 0) / 20
            4 -> (player.attr[SLAYER_POINTS_PERK] ?: 0) / 50
            5 -> if (player.attr[SELECT_SLAYER_TASK_UNLOCK] == true) 1 else 0
            6 -> if (player.attr[INCREASED_BANK_UNLOCK] == true) 1 else 0
            7 -> if (player.attr[REMOTE_BANK_UNLOCK] == true) 1 else 0
            else -> 0
        }
    }
}
