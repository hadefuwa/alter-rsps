package org.alter.plugins.content.items.scytheofvitur

import org.alter.api.EquipmentType
import org.alter.api.cfg.Graphic
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Npc
import org.alter.game.model.item.ItemAttribute
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.dealHit
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.skills.slayer.Slayer
import dev.openrune.cache.CacheManager.getNpc
import org.alter.rscm.RSCM.getRSCM

/**
 * Scythe of Vitur Plugin
 * 
 * Features:
 * - Charging system with vials of blood + blood runes
 * - Right-click options to check/uncharge the scythe
 * - Item interactions for adding charges
 * - Dynamic examine text showing current charges
 */
class ScytheOfViturPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Charging constants
        const val BLOOD_RUNES_PER_VIAL = 300
        const val CHARGES_PER_VIAL = 100
        const val MAX_CHARGES = 20000
        
        // Multi-hit damage percentages
        const val FIRST_HIT_PERCENTAGE = 1.0   // 100%
        const val SECOND_HIT_PERCENTAGE = 0.5  // 50%
        const val THIRD_HIT_PERCENTAGE = 0.25  // 25%
        
        // Minimum target size for multi-hits
        const val MIN_MULTI_HIT_SIZE = 2
        
        // All Scythe of Vitur variants
        private val SCYTHE_VARIANTS = listOf(
            "scythe_of_vitur",
            "scythe_of_vitur_uncharged", 
            "holy_scythe_of_vitur",
            "holy_scythe_of_vitur_uncharged",
            "sanguine_scythe_of_vitur",
            "sanguine_scythe_of_vitur_uncharged",
            "corrupted_scythe_of_vitur",
            "scythe_of_vitur_22664"
        )
        
        // Helper methods for scythe variants
        fun isScytheVariant(itemId: Int): Boolean {
            return SCYTHE_VARIANTS.any { getRSCM("item.$it") == itemId }
        }
        
        fun getChargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.scythe_of_vitur_uncharged") -> getRSCM("item.scythe_of_vitur")
                getRSCM("item.holy_scythe_of_vitur_uncharged") -> getRSCM("item.holy_scythe_of_vitur")
                getRSCM("item.sanguine_scythe_of_vitur_uncharged") -> getRSCM("item.sanguine_scythe_of_vitur")
                else -> itemId // Already charged or corrupted variant
            }
        }
        
        fun getUnchargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.scythe_of_vitur") -> getRSCM("item.scythe_of_vitur_uncharged")
                getRSCM("item.holy_scythe_of_vitur") -> getRSCM("item.holy_scythe_of_vitur_uncharged")
                getRSCM("item.sanguine_scythe_of_vitur") -> getRSCM("item.sanguine_scythe_of_vitur_uncharged")
                else -> itemId // Already uncharged or corrupted variant
            }
        }
        
        /**
         * Checks if a player has a slayer task for the given NPC
         * @param player The player to check
         * @param npc The NPC that was killed
         * @return true if the player has a slayer task for this NPC type, false otherwise
         */
        fun isOnSlayerTaskFor(player: Player, npc: Npc): Boolean {
            val taskNpcId = player.attr[Slayer.SLAYER_TASK_ATTR] ?: return false
            
            // Get the task NPC definition to compare names
            val taskNpcDef = try {
                getNpc(taskNpcId)
            } catch (e: Exception) {
                // If we can't get the task NPC definition, just compare IDs
                null
            }
            
            // Check if the killed NPC matches the assigned NPC ID
            // Also check by name to handle NPC variants (e.g., crawling_hand_448 vs crawling_hand_453)
            val idMatches = npc.id == taskNpcId
            val nameMatches = taskNpcDef != null && npc.name.lowercase() == taskNpcDef.name.lowercase()
            
            // Special case: If task is a TzHaar NPC, allow any TzHaar NPC to count
            val tzhaarMatches = if (taskNpcDef != null) {
                val taskNameLower = taskNpcDef.name.lowercase()
                val killedNameLower = npc.name.lowercase()
                // Check if both are TzHaar NPCs (name contains "tzhaar")
                (taskNameLower.contains("tzhaar") || taskNameLower.contains("tz-haar")) &&
                (killedNameLower.contains("tzhaar") || killedNameLower.contains("tz-haar"))
            } else {
                false
            }
            
            return idMatches || nameMatches || tzhaarMatches
        }
    }

    init {
        // Register charging handlers for all scythe variants
        registerChargingHandlers()
        
        // Register right-click options for all scythe variants
        registerRightClickOptions()
        
        // Register combat logic for all scythe variants
        registerCombatLogic()
    }
    
    private fun registerChargingHandlers() {
        val unchargedVariants = listOf(
            "scythe_of_vitur_uncharged",
            "holy_scythe_of_vitur_uncharged", 
            "sanguine_scythe_of_vitur_uncharged"
        )
        
        val chargedVariants = listOf(
            "scythe_of_vitur",
            "holy_scythe_of_vitur",
            "sanguine_scythe_of_vitur"
        )
        
        // Handle charging uncharged scythes
        unchargedVariants.forEach { uncharged ->
            onItemOnItem("item.vial_of_blood", "item.$uncharged") {
                handleScytheCharging(player, uncharged)
            }
        }
        
        // Handle adding charges to already charged scythes
        chargedVariants.forEach { charged ->
            onItemOnItem("item.vial_of_blood", "item.$charged") {
                handleScytheAddCharges(player, charged)
            }
        }
    }
    
    private fun handleScytheCharging(player: Player, unchargedVariant: String) {
        val bloodRuneId = getRSCM("item.blood_rune")
        val vialOfBloodId = getRSCM("item.vial_of_blood")
        val scytheUnchargedId = getRSCM("item.$unchargedVariant")
        val scytheChargedId = getChargedVariant(scytheUnchargedId)
        
        // Check if player has blood runes
        if (player.inventory.getItemCount(bloodRuneId) < BLOOD_RUNES_PER_VIAL) {
            player.message("You need $BLOOD_RUNES_PER_VIAL blood runes to charge the scythe.")
            return
        }
        
        // Find the uncharged scythe
        if (player.inventory.getItemCount(scytheUnchargedId) < 1) {
            player.message("You need an uncharged scythe of vitur.")
            return
        }
        
        // Remove items
        player.inventory.remove(vialOfBloodId, 1)
        player.inventory.remove(bloodRuneId, BLOOD_RUNES_PER_VIAL)
        player.inventory.remove(scytheUnchargedId, 1)
        
        // Add charged scythe with 100 charges
        // Use forceNoStack to ensure we can set attributes on this specific item
        val addResult = player.inventory.add(scytheChargedId, 1, forceNoStack = true)
        if (addResult.hasSucceeded()) {
            // Find the item we just added and set charges
            val scytheIndex = player.inventory.getItemIndex(scytheChargedId, false)
            if (scytheIndex != -1) {
                val newItem = player.inventory[scytheIndex]
                newItem?.putAttr(ItemAttribute.CHARGES, CHARGES_PER_VIAL)
            }
            
            player.animate(832) // Charging animation
            player.graphic(363) // Use a generic charging graphic
            player.message("You charge the scythe with blood. It now has $CHARGES_PER_VIAL charges.")
        } else {
            // Refund items if failed
            player.inventory.add(vialOfBloodId, 1)
            player.inventory.add(bloodRuneId, BLOOD_RUNES_PER_VIAL)
            player.inventory.add(scytheUnchargedId, 1)
            player.message("Could not add charged scythe to inventory.")
        }
    }
    
    private fun handleScytheAddCharges(player: Player, chargedVariant: String) {
        val bloodRuneId = getRSCM("item.blood_rune")
        val vialOfBloodId = getRSCM("item.vial_of_blood")
        val scytheChargedId = getRSCM("item.$chargedVariant")
        
        // Check if player has a charged scythe
        val scytheIndex = player.inventory.getItemIndex(scytheChargedId, false)
        if (scytheIndex == -1) {
            player.message("You need a charged scythe of vitur.")
            return
        }
        
        val scythe = player.inventory[scytheIndex] ?: return
        val currentCharges = scythe.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (currentCharges >= MAX_CHARGES) {
            player.message("Your scythe is already fully charged.")
            return
        }
        
        // Calculate how many vials we can use
        val vialsInInventory = player.inventory.getItemCount(vialOfBloodId)
        val runesInInventory = player.inventory.getItemCount(bloodRuneId)
        
        // Calculate max vials we can afford with runes
        val maxVialsByRunes = runesInInventory / BLOOD_RUNES_PER_VIAL
        
        // Calculate max vials we can add without exceeding max charges
        val chargesSpace = MAX_CHARGES - currentCharges
        val maxVialsBySpace = (chargesSpace + CHARGES_PER_VIAL - 1) / CHARGES_PER_VIAL // Ceiling division
        
        // Determine actual vials to use (1 per interaction for now, or max possible?)
        // User asked for "charging 1 per blood vial instead of 100 charges", implying they want 100 charges per vial.
        // Let's just use 1 vial per interaction to keep it simple, or maybe all?
        // The previous code used 1 vial. Let's stick to 1 vial per interaction for safety, 
        // or we can make it smart. Let's do 1 vial for now to match the "Use Item on Item" behavior typically.
        
        val vialsToUse = 1
        val runesNeeded = vialsToUse * BLOOD_RUNES_PER_VIAL
        
        if (vialsInInventory < vialsToUse) {
            player.message("You need a vial of blood.")
            return
        }
        
        if (runesInInventory < runesNeeded) {
            player.message("You need $runesNeeded blood runes to add charges.")
            return
        }
        
        val chargesToAdd = vialsToUse * CHARGES_PER_VIAL
        val newCharges = (currentCharges + chargesToAdd).coerceAtMost(MAX_CHARGES)
        val actualChargesAdded = newCharges - currentCharges
        
        if (actualChargesAdded <= 0) {
             player.message("Your scythe is already fully charged.")
             return
        }

        // Remove items
        player.inventory.remove(vialOfBloodId, vialsToUse)
        player.inventory.remove(bloodRuneId, runesNeeded)
        
        // Update charges
        scythe.putAttr(ItemAttribute.CHARGES, newCharges)
        
        player.animate(832) // Charging animation
        player.graphic(363) // Use a generic charging graphic
        player.message("You add $actualChargesAdded charges to the scythe. It now has $newCharges charges.")
    }
    
    private fun registerRightClickOptions() {
        val chargedVariants = listOf(
            "scythe_of_vitur",
            "holy_scythe_of_vitur",
            "sanguine_scythe_of_vitur",
            "corrupted_scythe_of_vitur"
        )
        
        chargedVariants.forEach { variant ->
            // Inventory check - usually option 3 or 4
            onItemOption("item.$variant", "Check") { checkCharges(player, variant) }
            
            // Equipment check - usually option 2
            onEquipmentOption("item.$variant", "Check") { checkChargesEquipped(player, variant) }
            
            // Only non-corrupted variants can be uncharged
            if (variant != "corrupted_scythe_of_vitur") {
                onItemOption("item.$variant", "Uncharge") {
                    handleScytheUncharge(player, variant)
                }
            }
        }
    }

    private fun checkCharges(player: Player, variant: String) {
        val scytheIndex = player.inventory.getItemIndex(getRSCM("item.$variant"), false)
        if (scytheIndex != -1) {
            val scythe = player.inventory[scytheIndex]
            val charges = scythe?.getAttr(ItemAttribute.CHARGES) ?: 0
            player.message("Your scythe has $charges charges remaining.")
        } else {
            player.message("You need to have the scythe in your inventory.")
        }
    }

    private fun checkChargesEquipped(player: Player, variant: String) {
        val equipped = player.getEquipment(EquipmentType.WEAPON)
        if (equipped?.id == getRSCM("item.$variant")) {
            val charges = equipped.getAttr(ItemAttribute.CHARGES) ?: 0
            player.message("Your scythe has $charges charges remaining.")
        }
    }
    
    private fun handleScytheUncharge(player: Player, chargedVariant: String) {
        val scytheChargedId = getRSCM("item.$chargedVariant")
        val scytheUnchargedId = getUnchargedVariant(scytheChargedId)
        
        player.queue {
            chatPlayer(player, "Are you sure you want to uncharge the scythe?")
            chatPlayer(player, "This will destroy all charges and cannot be undone.")
            
            when (options(player, "Yes, uncharge it.", "No, keep it charged.")) {
                1 -> {
                    // Remove charged scythe and add uncharged one
                    // Check inventory
                    val inventoryIndex = player.inventory.getItemIndex(scytheChargedId, false)
                    if (inventoryIndex != -1) {
                        player.inventory.remove(scytheChargedId, 1)
                        player.inventory.add(scytheUnchargedId, 1)
                        player.message("You uncharge the scythe. All charges have been lost.")
                    } else {
                        // Check equipment? Usually uncharge is only from inventory
                        player.message("You must have the scythe in your inventory to uncharge it.")
                    }
                }
                2 -> {
                    player.message("You decide to keep the scythe charged.")
                }
            }
        }
    }
    
    private fun registerCombatLogic() {
        SCYTHE_VARIANTS.forEach { variant ->
            setItemCombatLogic("item.$variant") {
                handleScytheCombat(player)
            }
        }
    }
    
    private fun handleScytheCombat(player: Player) {
        val target = player.getCombatTarget() ?: return
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return
        
        // Debug logging
        val targetName = if (target is Npc) target.name else "Player"

        
        // Play attack animation
        player.animate(8056)
        
        // Handle charges
        // Corrupted scythe doesn't use charges
        val isCorrupted = weapon.id == getRSCM("item.corrupted_scythe_of_vitur")
        var hasCharges = false
        
        if (!isCorrupted) {
            val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
            if (charges > 0) {
                weapon.putAttr(ItemAttribute.CHARGES, charges - 1)
                hasCharges = true
            } else {
                // Out of charges
                // If it's a charged variant, revert to uncharged
                if (isScytheVariant(weapon.id) && !weapon.getDef().name.contains("uncharged")) {
                     val unchargedId = getUnchargedVariant(weapon.id)
                     player.equipment[EquipmentType.WEAPON.id] = null
                     val addResult = player.inventory.add(unchargedId, 1)
                     if (!addResult.hasSucceeded()) {
                         world.spawn(org.alter.game.model.entity.GroundItem(unchargedId, 1, player.tile, player))
                     }
                     player.message("Your scythe has run out of charges.")
                     return
                }
            }
        } else {
            hasCharges = true
        }
        
        // Check for Sanguine variant (for healing)
        val isSanguine = weapon.id == getRSCM("item.sanguine_scythe_of_vitur")
        
        // Check if target is large enough (2x2 or bigger)
        if (target.getSize() < MIN_MULTI_HIT_SIZE) {

            // Normal single hit for small targets
            val maxHit = MeleeCombatFormula.getMaxHit(player, target)
            val accuracy = MeleeCombatFormula.getAccuracy(player, target)
            val landHit = accuracy >= world.randomDouble()
            
            player.dealHit(
                target = target,
                maxHit = maxHit,
                landHit = landHit,
                delay = 1
            )
            return
        }
        

        
        // Multi-hit for large targets (2x2+)
        val baseMaxHit = MeleeCombatFormula.getMaxHit(player, target)
        val accuracy = MeleeCombatFormula.getAccuracy(player, target)
        
        // First hit: 100% damage
        val firstHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = baseMaxHit,
            landHit = firstHit,
            delay = 1
        )
        
        // Second hit: 50% damage (delay slightly for visual effect)
        val secondMaxHit = (baseMaxHit * SECOND_HIT_PERCENTAGE).toInt()
        val secondHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = secondMaxHit,
            landHit = secondHit,
            delay = 2
        )
        
        // Third hit: 25% damage
        val thirdMaxHit = (baseMaxHit * THIRD_HIT_PERCENTAGE).toInt()
        val thirdHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = thirdMaxHit,
            landHit = thirdHit,
            delay = 3
        )
        
        // 4th Hit Logic (Blood Hit)
        // Only triggers if charged AND all 3 normal hits landed successfully
        if (hasCharges && firstHit && secondHit && thirdHit) {
             // 4th hit: 20% of max hit (small percentage bonus)
             val fourthMaxHit = (baseMaxHit * 0.2).toInt()
             val fourthHit = accuracy >= world.randomDouble()
             
             // Delay 3 to match the 3rd hit or slightly after? 
             // Using delay 3 to keep it part of the burst.
             val hitResult = player.dealHit(
                target = target,
                maxHit = fourthMaxHit,
                landHit = fourthHit,
                delay = 3
             )
             
             // Healing for Sanguine Scythe
             if (isSanguine && fourthHit) {
                 // Heal for 50% of the damage dealt by the 4th hit
                 val damage = hitResult.hit.hitmarks.sumOf { it.damage }
                 if (damage > 0) {
                     val healAmount = damage / 2
                     if (healAmount > 0) {
                         player.heal(healAmount)
                     }
                 }
             }
        }
        
        // Visual effects for multi-hit
        player.graphic(1834, delay = 1) // Scythe multi-hit effect
    }
}