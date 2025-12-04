package org.alter.plugins.content.items.chainmace

import org.alter.api.EquipmentType
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.item.ItemAttribute
import org.alter.game.info.PlayerInfo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.dealHit
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.combat.specialattack.SpecialAttacks
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.combat.CombatConfigs

/**
 * Chainmace Plugin
 * 
 * Features:
 * - Charging system with revenant ether (1 ether = 1 charge, max 16,000 charges)
 * - Consumes 1 ether per attack when charged
 * - Right-click options to check/uncharge the chainmace
 * - Item interactions for adding charges
 * - Ursine chainmace special attack (50% energy, increases accuracy and max hit)
 * - Charges lost on death in wilderness
 */
class ChainmacePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Charging constants
        const val MAX_CHARGES = 16000 // Maximum charges for chainmace
        
        // Chainmace variants
        private val CHAINMACE_VARIANTS = listOf(
            "viggoras_chainmace",
            "viggoras_chainmace_u",
            "ursine_chainmace",
            "ursine_chainmace_u"
        )
        
        // Helper methods for chainmace variants
        fun isChainmaceVariant(itemId: Int): Boolean {
            return CHAINMACE_VARIANTS.any { getRSCM("item.$it") == itemId }
        }
        
        fun getChargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.viggoras_chainmace_u") -> getRSCM("item.viggoras_chainmace")
                getRSCM("item.ursine_chainmace_u") -> getRSCM("item.ursine_chainmace")
                else -> itemId // Already charged
            }
        }
        
        fun getUnchargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.viggoras_chainmace") -> getRSCM("item.viggoras_chainmace_u")
                getRSCM("item.ursine_chainmace") -> getRSCM("item.ursine_chainmace_u")
                else -> itemId // Already uncharged
            }
        }
    }

    init {
        // Register charging handlers
        registerChargingHandlers()
        
        // Register right-click options
        registerRightClickOptions()
        
        // Register combat logic for ether consumption
        registerCombatLogic()
        
        // Register Ursine chainmace special attack
        registerSpecialAttack()
        
        // Handle charge loss on death in wilderness
        registerDeathHandler()
    }
    
    private fun registerChargingHandlers() {
        val unchargedVariants = listOf(
            "viggoras_chainmace_u",
            "ursine_chainmace_u"
        )
        
        val chargedVariants = listOf(
            "viggoras_chainmace",
            "ursine_chainmace"
        )
        
        // Handle charging uncharged chainmaces
        unchargedVariants.forEach { uncharged ->
            onItemOnItem("item.revenant_ether", "item.$uncharged") {
                handleChainmaceCharging(player, uncharged)
            }
        }
        
        // Handle adding charges to already charged chainmaces
        chargedVariants.forEach { charged ->
            onItemOnItem("item.revenant_ether", "item.$charged") {
                handleChainmaceAddCharges(player, charged)
            }
        }
    }
    
    private fun handleChainmaceCharging(player: Player, unchargedVariant: String) {
        val etherId = getRSCM("item.revenant_ether")
        val chainmaceUnchargedId = getRSCM("item.$unchargedVariant")
        val chainmaceChargedId = getChargedVariant(chainmaceUnchargedId)
        
        // Check if player has revenant ether
        val etherCount = player.inventory.getItemCount(etherId)
        if (etherCount < 1) {
            player.message("You need revenant ether to charge the chainmace.")
            return
        }
        
        // Find the uncharged chainmace
        if (player.inventory.getItemCount(chainmaceUnchargedId) < 1) {
            player.message("You need an uncharged chainmace.")
            return
        }
        
        // Calculate how much ether we can add (up to max charges)
        val etherToUse = etherCount.coerceAtMost(MAX_CHARGES)
        
        // Remove items
        player.inventory.remove(etherId, etherToUse)
        player.inventory.remove(chainmaceUnchargedId, 1)
        
        // Add charged chainmace (forceNoStack to ensure it doesn't stack, since it will have attributes)
        val addResult = player.inventory.add(chainmaceChargedId, 1, forceNoStack = true)
        if (addResult.hasSucceeded()) {
            // Find the item we just added and set charges
            // Items with attributes don't stack, so we should find it
            val chainmaceIndex = player.inventory.getItemIndex(chainmaceChargedId, false)
            if (chainmaceIndex != -1) {
                val newItem = player.inventory[chainmaceIndex]
                newItem?.putAttr(ItemAttribute.CHARGES, etherToUse)
            }
            
            player.animate(832) // Charging animation
            player.graphic(363) // Charging graphic
            player.message("You charge the chainmace with $etherToUse revenant ether.")
        } else {
            // Refund ether if we couldn't add the item
            player.inventory.add(etherId, etherToUse)
            player.message("Could not add charged chainmace to inventory.")
        }
    }
    
    private fun handleChainmaceAddCharges(player: Player, chargedVariant: String) {
        val etherId = getRSCM("item.revenant_ether")
        val chainmaceChargedId = getRSCM("item.$chargedVariant")
        
        // Check if player has a charged chainmace
        val chainmaceIndex = player.inventory.getItemIndex(chainmaceChargedId, false)
        if (chainmaceIndex == -1) {
            player.message("You need a charged chainmace.")
            return
        }
        
        val chainmace = player.inventory[chainmaceIndex] ?: return
        val currentCharges = chainmace.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (currentCharges >= MAX_CHARGES) {
            player.message("Your chainmace is already fully charged.")
            return
        }
        
        // Calculate how much ether we can add
        val etherCount = player.inventory.getItemCount(etherId)
        val chargesNeeded = MAX_CHARGES - currentCharges
        val etherToUse = etherCount.coerceAtMost(chargesNeeded)
        
        if (etherToUse == 0) {
            player.message("You need revenant ether to add charges.")
            return
        }
        
        // Remove ether
        player.inventory.remove(etherId, etherToUse)
        
        // Update charges
        val newCharges = currentCharges + etherToUse
        chainmace.putAttr(ItemAttribute.CHARGES, newCharges)
        
        player.animate(832) // Charging animation
        player.graphic(363) // Charging graphic
        player.message("You add $etherToUse revenant ether to the chainmace. It now has $newCharges charges.")
    }
    
    private fun registerRightClickOptions() {
        val chargedVariants = listOf(
            "viggoras_chainmace",
            "ursine_chainmace"
        )
        
        chargedVariants.forEach { variant ->
            onItemOption("item.$variant", "check") {
                val chainmaceIndex = player.inventory.getItemIndex(getRSCM("item.$variant"), false)
                if (chainmaceIndex == -1) {
                    // Check equipped chainmace
                    val equipped = player.getEquipment(EquipmentType.WEAPON)
                    if (equipped?.id == getRSCM("item.$variant")) {
                        val charges = equipped.getAttr(ItemAttribute.CHARGES) ?: 0
                        player.message("Your chainmace has $charges revenant ether charges remaining.")
                    } else {
                        player.message("You need to have the chainmace in your inventory or equipped.")
                    }
                } else {
                    val chainmace = player.inventory[chainmaceIndex]
                    val charges = chainmace?.getAttr(ItemAttribute.CHARGES) ?: 0
                    player.message("Your chainmace has $charges revenant ether charges remaining.")
                }
            }
            
            onItemOption("item.$variant", "uncharge") {
                handleChainmaceUncharge(player, variant)
            }
        }
    }
    
    private fun handleChainmaceUncharge(player: Player, chargedVariant: String) {
        val chainmaceChargedId = getRSCM("item.$chargedVariant")
        val chainmaceUnchargedId = getUnchargedVariant(chainmaceChargedId)
        
        player.queue {
            chatPlayer(player, "Are you sure you want to uncharge the chainmace?")
            chatPlayer(player, "This will destroy all charges and cannot be undone.")
            
            when (options(player, "Yes, uncharge it.", "No, keep it charged.")) {
                1 -> {
                    // Check inventory first
                    val inventoryIndex = player.inventory.getItemIndex(chainmaceChargedId, false)
                    if (inventoryIndex != -1) {
                        player.inventory.remove(chainmaceChargedId, 1)
                        player.inventory.add(chainmaceUnchargedId, 1)
                        player.message("You uncharge the chainmace. All charges have been lost.")
                        return@queue
                    }
                    
                    // Check if equipped
                    val equipped = player.getEquipment(EquipmentType.WEAPON)
                    if (equipped?.id == chainmaceChargedId) {
                        player.equipment[EquipmentType.WEAPON.id] = null
                        val addResult = player.inventory.add(chainmaceUnchargedId, 1)
                        if (addResult.hasSucceeded()) {
                            player.message("You uncharge the chainmace. All charges have been lost.")
                        } else {
                            // Put it back if we can't add to inventory
                            player.equipment[EquipmentType.WEAPON.id] = equipped
                            player.message("You don't have enough inventory space.")
                        }
                    } else {
                        player.message("You need to have the chainmace in your inventory or equipped.")
                    }
                }
                2 -> {
                    player.message("You decide to keep the chainmace charged.")
                }
            }
        }
    }
    
    private fun registerCombatLogic() {
        CHAINMACE_VARIANTS.forEach { variant ->
            setItemCombatLogic("item.$variant") {
                handleChainmaceCombat(player)
            }
        }
    }
    
    private fun handleChainmaceCombat(player: Player) {
        val target = player.getCombatTarget() ?: return
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return
        
        // Check if chainmace is charged (has charges attribute)
        val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (charges > 0) {
            // Consume 1 ether per attack
            val newCharges = charges - 1
            if (newCharges <= 0) {
                // Convert to uncharged variant
                val unchargedId = getUnchargedVariant(weapon.id)
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    // If we can't add to inventory, drop it
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your chainmace has run out of charges.")
            } else {
                // Update charges
                weapon.putAttr(ItemAttribute.CHARGES, newCharges)
                PlayerInfo(player).syncAppearance()
            }
        } else {
            // Uncharged chainmace - can still attack but no bonuses
            // Normal combat continues
        }
        
        // Execute standard combat logic
        val strategy = CombatConfigs.getCombatStrategy(player)
        strategy.attack(player, target)
    }
    
    private fun registerSpecialAttack() {
        // Ursine chainmace special attack: 50% energy, increases accuracy and max hit
        SpecialAttacks.register("item.ursine_chainmace", 50) {
            val target = this.target ?: return@register
            
            val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return@register
            val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
            
            if (charges <= 0) {
                player.message("Your chainmace needs to be charged to use its special attack.")
                return@register
            }
            
            player.animate(245) // Ursine chainmace attack animation
            player.graphic(1834) // Special attack graphic
            
            // Special attack: 1.25x accuracy, 1.15x max hit
            val maxHit = MeleeCombatFormula.getMaxHit(player, target, specialAttackMultiplier = 1.15)
            val accuracy = MeleeCombatFormula.getAccuracy(player, target, specialAttackMultiplier = 1.25)
            val landHit = accuracy >= world.randomDouble()
            
            player.dealHit(target = target, maxHit = maxHit, landHit = landHit, delay = 1)
            
            // Consume 1 ether for special attack
            val newCharges = charges - 1
            if (newCharges <= 0) {
                // Convert to uncharged variant
                val unchargedId = getUnchargedVariant(weapon.id)
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your chainmace has run out of charges.")
            } else {
                weapon.putAttr(ItemAttribute.CHARGES, newCharges)
                PlayerInfo(player).syncAppearance()
            }
        }
    }
    
    private fun registerDeathHandler() {
        onPlayerDeath {
            val player = this.player
            
            // Check if player is in wilderness
            if (player.tile.getWildernessLevel() <= 0) {
                return@onPlayerDeath
            }
            
            // Check if player has a charged chainmace equipped
            val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return@onPlayerDeath
            if (!isChainmaceVariant(weapon.id)) {
                return@onPlayerDeath
            }
            
            val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
            if (charges > 0) {
                // Convert to uncharged variant on death in wilderness
                val unchargedId = getUnchargedVariant(weapon.id)
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your chainmace lost all charges upon death in the wilderness.")
            }
        }
    }
}

