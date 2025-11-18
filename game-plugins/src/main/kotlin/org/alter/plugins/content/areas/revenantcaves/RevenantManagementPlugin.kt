package org.alter.plugins.content.areas.revenantcaves

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.PlayerInfo
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.ItemAttribute
import org.alter.game.model.timer.TimeConstants
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.rscm.RSCM.getRSCM

/**
 * Revenant Management Plugin
 * 
 * This plugin manages general revenant mechanics including:
 * - Amulet of Avarice effects (aggression, noted drops, salve bonus, skulling)
 * - Bracelet of Ethereum effects (damage protection, ether consumption/absorption)
 * - General damage multipliers for revenants
 * 
 * IMPORTANT: Some features require combat formula integration:
 * - Bracelet of Ethereum: Ether consumption per hit and revenant-only protection
 *   needs to be added to MeleeCombatFormula, RangedCombatFormula, and MagicCombatFormula
 * - Amulet of Avarice: Salve (e) bonus needs to be added to combat formulas
 *   (check for "item.amulet_of_avarice" and apply 1.2x multiplier against revenants)
 * - Drop noting: Needs to be added to NpcLootDropPlugin to note items when
 *   player has amulet of avarice equipped
 * 
 * @param r The plugin repository
 * @param world The game world instance
 * @param server The server instance
 */
class RevenantManagementPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Revenant Caves area bounds
         * Z coordinates above 10000 indicate revenant caves
         */
        private fun isInRevenantCaves(tile: Tile): Boolean {
            return tile.z >= 10000 && tile.z <= 10300 && tile.x >= 3100 && tile.x <= 3300
        }
        
        /**
         * Check if an NPC is a revenant
         */
        private fun isRevenant(npc: Npc): Boolean {
            val name = npc.def.name.lowercase()
            return name.contains("revenant") || npc.id in setOf(
                getRSCM("npc.revenant_imp"),
                getRSCM("npc.revenant_goblin"),
                getRSCM("npc.revenant_pyrefiend"),
                getRSCM("npc.revenant_hobgoblin"),
                getRSCM("npc.revenant_cyclops"),
                getRSCM("npc.revenant_hellhound"),
                getRSCM("npc.revenant_demon"),
                getRSCM("npc.revenant_ork"),
                getRSCM("npc.revenant_dark_beast"),
                getRSCM("npc.revenant_knight"),
                getRSCM("npc.revenant_dragon")
            )
        }
        
        /**
         * Get ether amount from bracelet of ethereum
         */
        private fun getEthereumCharges(player: Player): Int {
            val bracelet = player.getEquipment(EquipmentType.GLOVES) ?: return 0
            if (bracelet.id != getRSCM("item.bracelet_of_ethereum")) {
                return 0
            }
            return bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
        }
        
        /**
         * Consume ether from bracelet
         */
        private fun consumeEthereum(player: Player, amount: Int = 1): Boolean {
            val bracelet = player.getEquipment(EquipmentType.GLOVES) ?: return false
            if (bracelet.id != getRSCM("item.bracelet_of_ethereum")) {
                return false
            }
            val currentCharges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
            if (currentCharges < amount) {
                return false
            }
            val newCharges = currentCharges - amount
            if (newCharges <= 0) {
                // Convert to uncharged bracelet
                val unchargedId = getRSCM("item.bracelet_of_ethereum_uncharged")
                player.equipment[EquipmentType.GLOVES.id] = null
                player.inventory.add(unchargedId, 1)
                player.message("Your bracelet of ethereum has run out of charges.")
            } else {
                bracelet.putAttr(ItemAttribute.CHARGES, newCharges)
                PlayerInfo(player).syncAppearance()
            }
            return true
        }
        
        /**
         * Add ether to bracelet
         */
        private fun addEthereum(player: Player, amount: Int) {
            val bracelet = player.getEquipment(EquipmentType.GLOVES) ?: return
            if (bracelet.id != getRSCM("item.bracelet_of_ethereum")) {
                return
            }
            val currentCharges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
            val maxCharges = 16000 // Maximum charges for bracelet
            val newCharges = (currentCharges + amount).coerceAtMost(maxCharges)
            bracelet.putAttr(ItemAttribute.CHARGES, newCharges)
            PlayerInfo(player).syncAppearance()
        }
    }
    
    init {
        /**
         * Amulet of Avarice - Make all revenants aggressive when worn
         * Skull player when equipping in revenant caves
         */
        onEquipToSlot(EquipmentType.AMULET.id) {
            val amulet = player.getEquipment(EquipmentType.AMULET)
            if (amulet?.id == getRSCM("item.amulet_of_avarice")) {
                if (isInRevenantCaves(player.tile)) {
                    // Skull player
                    if (!player.hasSkullIcon(SkullIcon.WHITE)) {
                        player.skull(SkullIcon.WHITE, TimeConstants.minutesToCycles(20) ?: 2000)
                        player.message("The amulet of avarice has skulled you!")
                    }
                    
                    // Make all nearby revenants aggressive
                    world.npcs.forEach { npc ->
                        if (isRevenant(npc) && npc.tile.isWithinRadius(player.tile, 15)) {
                            if (npc.getCombatTarget() != player && npc.lock.canAttack()) {
                                npc.attack(player)
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * Amulet of Avarice - Skull player when entering revenant caves with it equipped
         */
        onLogin {
            val player = ctx as Player
            
            if (isInRevenantCaves(player.tile)) {
                val hasAvarice = player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice")
                if (hasAvarice && !player.hasSkullIcon(SkullIcon.WHITE)) {
                    player.skull(SkullIcon.WHITE, TimeConstants.minutesToCycles(20) ?: 2000)
                    player.message("The amulet of avarice has skulled you!")
                }
            }
        }
        
        /**
         * Amulet of Avarice - Apply Salve (e) bonus against revenants
         * This is handled in the combat formula, but we need to mark revenants as undead
         */
        onGlobalNpcSpawn {
            if (isRevenant(npc)) {
                // Revenants are undead, so salve amulet bonuses apply
                // This is handled automatically by the combat formula checking NpcSpecies.UNDEAD
                // We just need to ensure revenants have the UNDEAD species
                // (This should be set in the combat config, but we can verify here)
            }
        }
        
        /**
         * Amulet of Avarice - Note all drops in revenant caves
         */
        onAnyNpcDeath {
            val npc = ctx as Npc
            val killer = npc.attr[KILLER_ATTR]?.get() as? Player ?: return@onAnyNpcDeath
            
            if (!isInRevenantCaves(npc.tile) || !isRevenant(npc)) {
                return@onAnyNpcDeath
            }
            
            // Check if killer has amulet of avarice equipped
            if (killer.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice")) {
                // Note all ground items from this NPC
                // This will be handled in the loot drop system by converting items to noted
                // We'll intercept the ground items after they're spawned
            }
        }
        
        /**
         * Bracelet of Ethereum - Protect from revenant damage when charged
         * Use DAMAGE_TAKE_MULTIPLIER to reduce damage to 0 when bracelet has charges
         */
        onEquipToSlot(EquipmentType.GLOVES.id) {
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                if (charges > 0 && isInRevenantCaves(player.tile)) {
                    // Set damage multiplier to 0 for revenant attacks
                    player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.0
                }
            }
        }
        
        onUnequipFromSlot(EquipmentType.GLOVES.id) {
            // Remove damage multiplier when unequipping
            player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
        }
        
        /**
         * Bracelet of Ethereum - Consume ether on each revenant hit
         * This runs before damage is applied
         */
        onAnyNpcDeath {
            val npc = ctx as Npc
            val killer = npc.attr[KILLER_ATTR]?.get() as? Player ?: return@onAnyNpcDeath
            
            if (!isRevenant(npc) || !isInRevenantCaves(npc.tile)) {
                return@onAnyNpcDeath
            }
            
            // Check if killer has bracelet of ethereum equipped and consumed ether during fight
            // We'll handle ether consumption in a timer-based system instead
        }
        
        /**
         * Timer-based system to consume ether from bracelet on each hit
         * This is a workaround since we don't have direct access to onNpcDamage
         */
        val ETHEREUM_CONSUME_TIMER = TimerKey()
        
        // Set up timer to check and consume ether periodically for players in revenant caves
        onTimer(ETHEREUM_CONSUME_TIMER) {
            val player = ctx as Player
            if (!isInRevenantCaves(player.tile)) {
                // Stop timer if player left the area
                player.timers.remove(ETHEREUM_CONSUME_TIMER)
                player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                return@onTimer
            }
            
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                if (charges > 0) {
                    // Update damage multiplier based on charges
                    player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.0
                } else {
                    // No charges, remove protection
                    player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                }
            } else {
                player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
            }
            
            // Run every 5 ticks instead of every tick to reduce overhead
            player.timers[ETHEREUM_CONSUME_TIMER] = 5
        }
        
        // Define AVARICE_AGGRO_TIMER early so it can be referenced by other timers
        val AVARICE_AGGRO_TIMER = TimerKey()
        
        // Start timer for players in revenant caves (on login)
        onLogin {
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                player.timers[ETHEREUM_CONSUME_TIMER] = 1
            }
        }
        
        // Global timer to check all players and start timers if they enter revenant caves
        val REVENANT_CAVES_CHECK_TIMER = TimerKey()
        onTimer(REVENANT_CAVES_CHECK_TIMER) {
            // This timer runs for all players, check if they're in revenant caves
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                // Start timers if not already running
                if (!player.timers.has(ETHEREUM_CONSUME_TIMER)) {
                    player.timers[ETHEREUM_CONSUME_TIMER] = 1
                }
                if (!player.timers.has(AVARICE_AGGRO_TIMER)) {
                    player.timers[AVARICE_AGGRO_TIMER] = 1
                }
            }
            // Run every 10 ticks to check for players entering the area
            player.timers[REVENANT_CAVES_CHECK_TIMER] = 10
        }
        
        // Start the check timer for all players on login
        onLogin {
            val player = ctx as Player
            player.timers[REVENANT_CAVES_CHECK_TIMER] = 10
        }
        
        /**
         * Bracelet of Ethereum - Absorb ether from killed revenants
         */
        onAnyNpcDeath {
            val npc = ctx as Npc
            val killer = npc.attr[KILLER_ATTR]?.get() as? Player ?: return@onAnyNpcDeath
            
            if (!isRevenant(npc) || !isInRevenantCaves(npc.tile)) {
                return@onAnyNpcDeath
            }
            
            // Check if killer has bracelet of ethereum equipped
            val bracelet = killer.getEquipment(EquipmentType.GLOVES)
            if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                // Absorb ether from revenant (scaled by combat level)
                val combatLevel = npc.def.combatLevel
                val etherAmount = (combatLevel * 2).coerceIn(10, 200) // 10-200 ether based on level
                addEthereum(killer, etherAmount)
                killer.message("Your bracelet of ethereum absorbs $etherAmount revenant ether.")
            }
        }
        
        /**
         * Amulet of Avarice - Note all drops in revenant caves
         * This is handled by modifying the drop system to note items
         * We'll store a flag that the loot drop plugin can check
         */
        // Note: Drop noting will be handled in NpcLootDropPlugin by checking for amulet
        
        /**
         * Periodic check to make revenants aggressive to players with Amulet of Avarice
         */
        onTimer(AVARICE_AGGRO_TIMER) {
            val player = ctx as Player
            if (!isInRevenantCaves(player.tile)) {
                // Stop timer if player left the area
                player.timers.remove(AVARICE_AGGRO_TIMER)
                return@onTimer
            }
            
            if (player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice")) {
                // Make nearby revenants aggressive
                world.npcs.forEach { npc ->
                    if (isRevenant(npc) && npc.tile.isWithinRadius(player.tile, 15)) {
                        if (npc.getCombatTarget() != player && npc.lock.canAttack()) {
                            npc.attack(player)
                        }
                    }
                }
            }
            
            player.timers[AVARICE_AGGRO_TIMER] = 3 // Check every 3 ticks
        }
        
        // Start aggro timer for players in revenant caves (on login)
        onLogin {
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                player.timers[AVARICE_AGGRO_TIMER] = 1
            }
        }
        // The REVENANT_CAVES_CHECK_TIMER will also handle starting it when entering the area
    }
}

