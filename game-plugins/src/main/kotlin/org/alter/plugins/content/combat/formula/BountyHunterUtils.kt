package org.alter.plugins.content.combat.formula

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.model.entity.Player
import org.alter.game.model.Tile
import org.alter.rscm.RSCM.getRSCM

/**
 * Utility functions for Bounty Hunter set bonuses and Boss Island detection
 */
object BountyHunterUtils {
    
    /**
     * Boss Island center coordinates - matches BossIslandSpawnPlugin
     */
    private const val ISLAND_CENTER_X = 3423
    private const val ISLAND_CENTER_Z = 4089
    private const val ISLAND_HEIGHT = 0
    
    /**
     * Radius around the island center to consider as "Boss Island"
     */
    private const val ISLAND_RADIUS = 35
    
    /**
     * Bounty Hunter hat tier items (tier 1-6)
     */
    private val BOUNTY_HUNTER_HATS = arrayOf(
        "item.bounty_hunter_hat_tier_1",
        "item.bounty_hunter_hat_tier_2",
        "item.bounty_hunter_hat_tier_3",
        "item.bounty_hunter_hat_tier_4",
        "item.bounty_hunter_hat_tier_5",
        "item.bounty_hunter_hat_tier_6"
    )
    
    /**
     * Bounty Hunter weapon items - these need to be identified
     * TODO: Add actual bounty hunter weapon item IDs when known
     * Common bounty hunter weapons in OSRS include:
     * - Rune scimitar (bounty hunter variant)
     * - Rune longsword (bounty hunter variant)
     * - Rune dagger (bounty hunter variant)
     * - Rune 2h sword (bounty hunter variant)
     * - Rune crossbow (bounty hunter variant)
     * - Rune mace (bounty hunter variant)
     * - Rune warhammer (bounty hunter variant)
     */
    private val BOUNTY_HUNTER_WEAPONS = arrayOf<String>(
        // Placeholder - will need to be populated with actual item IDs
        // Example format: "item.bounty_hunter_rune_scimitar_tier_1", etc.
    )
    
    /**
     * Bounty Hunter armour pieces - these need to be identified
     * TODO: Add actual bounty hunter armour item IDs when known
     * Common bounty hunter armour in OSRS includes:
     * - Rune platebody (bounty hunter variant)
     * - Rune platelegs (bounty hunter variant)
     * - Rune full helm (bounty hunter variant)
     * - Rune kiteshield (bounty hunter variant)
     */
    private val BOUNTY_HUNTER_ARMOUR = arrayOf<String>(
        // Placeholder - will need to be populated with actual item IDs
        // Example format: "item.bounty_hunter_rune_platebody_tier_1", etc.
    )
    
    /**
     * Check if a player is currently on Boss Island
     */
    fun isOnBossIsland(player: Player): Boolean {
        val tile = player.tile
        
        // Check height
        if (tile.height != ISLAND_HEIGHT) {
            return false
        }
        
        // Check distance from center
        val distanceX = kotlin.math.abs(tile.x - ISLAND_CENTER_X)
        val distanceZ = kotlin.math.abs(tile.z - ISLAND_CENTER_Z)
        val distance = kotlin.math.max(distanceX, distanceZ)
        
        return distance <= ISLAND_RADIUS
    }
    
    /**
     * Check if a player has a Bounty Hunter hat equipped (any tier)
     */
    fun hasBountyHunterHat(player: Player): Boolean {
        return player.hasEquipped(EquipmentType.HEAD, *BOUNTY_HUNTER_HATS)
    }
    
    /**
     * Check if a player has a Bounty Hunter weapon equipped
     * For now, we'll check if the weapon name contains "bounty" or if they have a hat
     * This is a flexible approach that works even if exact item IDs aren't known
     */
    fun hasBountyHunterWeapon(player: Player): Boolean {
        // If weapons are defined, check those
        if (BOUNTY_HUNTER_WEAPONS.isNotEmpty()) {
            return player.hasEquipped(EquipmentType.WEAPON, *BOUNTY_HUNTER_WEAPONS)
        }
        
        // Otherwise, check if weapon name contains "bounty" (case insensitive)
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return false
        val weaponName = weapon.getDef().name.lowercase()
        return weaponName.contains("bounty")
    }
    
    /**
     * Check if a player has Bounty Hunter armour equipped
     * Checks chest and legs slots for bounty hunter items
     */
    fun hasBountyHunterArmour(player: Player): Boolean {
        // If armour is defined, check those
        if (BOUNTY_HUNTER_ARMOUR.isNotEmpty()) {
            val hasChest = BOUNTY_HUNTER_ARMOUR.any { armour ->
                try {
                    player.hasEquipped(EquipmentType.CHEST, armour)
                } catch (e: Exception) {
                    false
                }
            }
            val hasLegs = BOUNTY_HUNTER_ARMOUR.any { armour ->
                try {
                    player.hasEquipped(EquipmentType.LEGS, armour)
                } catch (e: Exception) {
                    false
                }
            }
            return hasChest && hasLegs
        }
        
        // Otherwise, check if armour names contain "bounty" (case insensitive)
        val chest = player.getEquipment(EquipmentType.CHEST)
        val legs = player.getEquipment(EquipmentType.LEGS)
        
        val hasChest = chest?.getDef()?.name?.lowercase()?.contains("bounty") ?: false
        val hasLegs = legs?.getDef()?.name?.lowercase()?.contains("bounty") ?: false
        
        return hasChest && hasLegs
    }
    
    /**
     * Check if a player has a full Bounty Hunter set equipped
     * Full set requires: Hat + Weapon + Armour (chest + legs)
     */
    fun hasFullBountyHunterSet(player: Player): Boolean {
        return hasBountyHunterHat(player) && 
               hasBountyHunterWeapon(player) && 
               hasBountyHunterArmour(player)
    }
    
    /**
     * Check if a player has any Bounty Hunter item equipped
     * Used to determine if they should hit 0s outside boss island
     */
    fun hasAnyBountyHunterItem(player: Player): Boolean {
        return hasBountyHunterHat(player) || 
               hasBountyHunterWeapon(player) || 
               hasBountyHunterArmour(player)
    }
}

