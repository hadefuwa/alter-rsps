package org.alter.plugins.content.areas.revenantcaves

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.PlayerInfo
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.Item
import org.alter.game.model.item.ItemAttribute
import org.alter.game.model.timer.TimeConstants
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.move.moveTo
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.combat.removeCombatTarget
import org.alter.plugins.content.combat.canAttackMelee
import org.alter.plugins.content.combat.combatRaycast
import org.alter.plugins.content.combat.canEngageCombat
import org.alter.plugins.content.combat.isAttackDelayReady
import org.alter.plugins.content.combat.postAttackLogic
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.dealHit
import org.alter.game.model.queue.QueueTask
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
    
    /**
     * Set to track players who need the delay timer set (to avoid ConcurrentModificationException)
     */
    private val pendingDelayTimerPlayers = mutableSetOf<Player>()
    
    /**
     * Set to track players who need timers removed (to avoid ConcurrentModificationException)
     */
    private val pendingTimerRemovals = mutableMapOf<Player, MutableSet<TimerKey>>()
    
    /**
     * Map to track players that need timers set (to avoid ConcurrentModificationException)
     * Key: TimerKey, Value: Pair of (Player, timer value)
     */
    private val pendingPlayerTimers = mutableMapOf<TimerKey, MutableList<Pair<Player, Int>>>()
    
    /**
     * Map to track NPCs that need timers set (to avoid ConcurrentModificationException)
     * Key: TimerKey, Value: Pair of (NPC, timer value)
     */
    private val pendingNpcTimers = mutableMapOf<TimerKey, MutableList<Pair<Npc, Int>>>()
    
    companion object {
        /**
         * Revenant Caves area bounds
         * Z coordinates above 10000 indicate revenant caves
         */
        private fun isInRevenantCaves(tile: Tile): Boolean {
            return tile.z >= 10000 && tile.z <= 10300 && tile.x >= 3100 && tile.x <= 3300
        }
        
        /**
         * Timer key for tracking when a player was last targeted by a revenant (3 second delay)
         * This prevents multiple revenants from attacking the same player simultaneously
         */
        val REVENANT_TARGET_DELAY_TIMER = TimerKey()
        
        
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

        /**
         * Timer to check for Amulet of Avarice aggression
         */
        val AVARICE_AGGRO_TIMER = TimerKey()
    }
    
    init {
        /**
         * Revenant Single-Combat Enforcement with 3-second delay
         * Prevents multiple revenants from attacking the same player
         * Adds a 3-second delay when a revenant starts targeting a player to give them time to walk into melee range
         */
        onGlobalNpcSpawn {
            if (isRevenant(npc) && isInRevenantCaves(npc.tile)) {
                // Override aggression check to enforce single-combat for revenants with delay
                val originalAggroCheck = npc.aggroCheck
                npc.aggroCheck = { n, p ->
                    // First check the original aggression logic
                    if (originalAggroCheck != null && !originalAggroCheck(n, p)) {
                        false
                    } else {
                        // Only enforce single-combat in revenant caves
                        if (!isInRevenantCaves(n.tile)) {
                            true
                        } else {
                            // Check if another revenant is already attacking this player
                            val otherRevenantAttacking = world.npcs.any { otherNpc ->
                                otherNpc != n && 
                                isRevenant(otherNpc) && 
                                otherNpc.isAlive() && 
                                otherNpc.getCombatTarget() == p &&
                                isInRevenantCaves(otherNpc.tile)
                            }
                            
                            // Check if player has a recent revenant target delay (3 seconds = 300 cycles)
                            val hasRecentTargetDelay = p.timers.has(RevenantManagementPlugin.REVENANT_TARGET_DELAY_TIMER)
                            
                            // Only allow attack if:
                            // 1. No other revenant is attacking this player
                            // 2. No recent target delay (3 seconds haven't passed since last revenant targeted them)
                            val canAttack = !otherRevenantAttacking && !hasRecentTargetDelay
                            
                            // If we're allowing the attack, mark player for delay timer (set in separate timer to avoid ConcurrentModificationException)
                            if (canAttack) {
                                this@RevenantManagementPlugin.pendingDelayTimerPlayers.add(p)
                            }
                            
                            canAttack
                        }
                    }
                }
            }
        }
        
        /**
         * Global timer to set delay timers for players who were just targeted by revenants
         * This runs every tick and processes all pending players to avoid ConcurrentModificationException
         * We use a world-level timer that processes all players
         */
        val REVENANT_DELAY_TIMER_SETTER = TimerKey()
        
        // Start a global timer that runs every tick to process pending timers
        // Use world timers - they execute in world.cycle() which runs AFTER PlayerCycleTask
        // We'll collect the data in the timer handler, then use a world queue task
        // that executes in QueueHandlerTask (which runs BEFORE PlayerCycleTask)
        // This ensures timer modifications happen before timer cycles
        onWorldInit {
            world.timers[REVENANT_DELAY_TIMER_SETTER] = 1
        }
        
        // Register world timer handler - this executes when the timer reaches 0 in world.cycle()
        // World timers execute AFTER player cycles in the current cycle
        // We use submitGameThreadJob to execute at the START of the NEXT cycle,
        // BEFORE QueueHandlerTask and PlayerCycleTask, ensuring timer modifications
        // happen before any timer cycles
        onTimer(REVENANT_DELAY_TIMER_SETTER) {
            // Collect data to process (do this synchronously while we're in world.cycle())
            val playersToProcess = this@RevenantManagementPlugin.pendingDelayTimerPlayers.toList() // Create a copy
            this@RevenantManagementPlugin.pendingDelayTimerPlayers.clear()
            val playerTimersToProcess = this@RevenantManagementPlugin.pendingPlayerTimers.toMap() // Create a copy
            this@RevenantManagementPlugin.pendingPlayerTimers.clear()
            val timerRemovalsToProcess = this@RevenantManagementPlugin.pendingTimerRemovals.toMap() // Create a copy
            this@RevenantManagementPlugin.pendingTimerRemovals.clear()
            val npcTimersToProcess = this@RevenantManagementPlugin.pendingNpcTimers.toMap() // Create a copy
            this@RevenantManagementPlugin.pendingNpcTimers.clear()
            
            // Submit as game thread job - executes at START of NEXT cycle, BEFORE all tasks
            // This ensures timer modifications happen before timer cycles, avoiding ConcurrentModificationException
            if (playersToProcess.isNotEmpty() || playerTimersToProcess.isNotEmpty() || timerRemovalsToProcess.isNotEmpty() || npcTimersToProcess.isNotEmpty()) {
                world.getService(org.alter.game.service.GameService::class.java)?.submitGameThreadJob {
                    // Set delay timers for players
                    playersToProcess.forEach { p: Player ->
                        if (p.isOnline) {
                            p.timers[REVENANT_TARGET_DELAY_TIMER] = 300 // 3 seconds = 300 cycles
                        }
                    }
                    
                    // Set other player timers
                    playerTimersToProcess.forEach { (timerKey, playerTimerPairs) ->
                        playerTimerPairs.forEach { (player, timerValue) ->
                            if (player.isOnline) {
                                player.timers[timerKey] = timerValue
                            }
                        }
                    }
                    
                    // Remove player timers
                    timerRemovalsToProcess.forEach { (player, timerKeys) ->
                        if (player.isOnline) {
                            timerKeys.forEach { timerKey ->
                                player.timers.remove(timerKey)
                            }
                        }
                    }
                    
                    // Set NPC timers (or remove if timerValue is -1)
                    npcTimersToProcess.forEach { (timerKey, npcTimerPairs) ->
                        npcTimerPairs.forEach { (npc, timerValue) ->
                            if (npc.isAlive()) {
                                if (timerValue == -1) {
                                    // -1 indicates removal
                                    npc.timers.remove(timerKey)
                                } else {
                                    npc.timers[timerKey] = timerValue
                                }
                            }
                        }
                    }
                }
            }
            
            // Reset timer to run again next tick
            world.timers[REVENANT_DELAY_TIMER_SETTER] = 1
        }
        
        /**
         * Revenant Combat Plugin
         * Handles melee, ranged, and magic attacks for all revenants
         */
        onNpcCombat("npc.revenant_imp") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_goblin") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_pyrefiend") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_hobgoblin") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_cyclops") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_hellhound") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_demon") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_ork") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_dark_beast") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_knight") { npc.queue { revenantCombat(npc, this) } }
        onNpcCombat("npc.revenant_dragon") { npc.queue { revenantCombat(npc, this) } }
        
        /**
         * Amulet of Avarice - Make all revenants aggressive when worn
         * Skull player when equipping in revenant caves
         */
        onEquipToSlot(EquipmentType.AMULET.id) {
            val amulet = player.getEquipment(EquipmentType.AMULET)
            if (amulet?.id == getRSCM("item.amulet_of_avarice")) {
                // Skull player permanently
                player.skull(SkullIcon.WHITE, Int.MAX_VALUE)
                player.message("The amulet of avarice has skulled you!")
                
                // Start aggro timer immediately if in rev caves
                if (isInRevenantCaves(player.tile)) {
                    player.timers[AVARICE_AGGRO_TIMER] = 1
                }
            }
        }
        
        onUnequipFromSlot(EquipmentType.AMULET.id) {
            val amulet = player.getEquipment(EquipmentType.AMULET)
            if (amulet?.id == getRSCM("item.amulet_of_avarice")) {
                // Unskull player
                player.skullIcon = -1
                player.message("You are no longer skulled by the amulet of avarice.")
            }
        }
        
        /**
         * Amulet of Avarice - Skull player when entering revenant caves with it equipped
         */
        onLogin {
            val player = ctx as Player
            
            val hasAvarice = player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice")
            if (hasAvarice) {
                // Skull player permanently if they have the amulet
                if (!player.hasSkullIcon(SkullIcon.WHITE)) {
                    player.skull(SkullIcon.WHITE, Int.MAX_VALUE)
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
         * Use DAMAGE_TAKE_MULTIPLIER to reduce damage to 0 when bracelet has charges AND absorption is enabled
         */
        onEquipToSlot(EquipmentType.GLOVES.id) {
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                // Use ATTACHED_ITEM_ID as a flag for absorption: 1 = enabled, 0 = disabled (default to enabled if missing)
                val absorptionEnabled = (bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1) == 1
                
                if (charges > 0 && absorptionEnabled && isInRevenantCaves(player.tile)) {
                    // Set damage multiplier to 0.5 for 50% damage reduction from revenant attacks
                    player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.5
                }
            }
        }
        
        onUnequipFromSlot(EquipmentType.GLOVES.id) {
            // Remove damage multiplier when unequipping
            player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
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
                // Stop timer if player left the area - defer removal to avoid ConcurrentModificationException
                pendingTimerRemovals.getOrPut(player) { mutableSetOf() }.add(ETHEREUM_CONSUME_TIMER)
                player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                return@onTimer
            }
            
            // Only run timer if player has bracelet equipped
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                // Use ATTACHED_ITEM_ID as a flag for absorption: 1 = enabled, 0 = disabled (default to enabled if missing)
                val absorptionEnabled = (bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1) == 1
                
                if (charges > 0 && absorptionEnabled) {
                    // Update damage multiplier for 50% damage reduction from revenant attacks
                    player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.5
                } else {
                    // No charges or absorption disabled, remove protection
                    player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                }
                // Continue timer if player has bracelet - defer setting to avoid ConcurrentModificationException
                pendingPlayerTimers.getOrPut(ETHEREUM_CONSUME_TIMER) { mutableListOf() }.add(Pair(player, 5))
            } else {
                // Player doesn't have bracelet, remove any protection and stop timer - defer removal
                player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                pendingTimerRemovals.getOrPut(player) { mutableSetOf() }.add(ETHEREUM_CONSUME_TIMER)
            }
        }
        
        
        // Start timer for players in revenant caves (on login) - only if they have bracelet
        onLogin {
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                val bracelet = player.getEquipment(EquipmentType.GLOVES)
                if (bracelet?.id == getRSCM("item.bracelet_of_ethereum")) {
                    player.timers[ETHEREUM_CONSUME_TIMER] = 1
                }
            }
        }
        
        // Global timer to check all players and start timers if they enter revenant caves
        val REVENANT_CAVES_CHECK_TIMER = TimerKey()
        onTimer(REVENANT_CAVES_CHECK_TIMER) {
            // This timer runs for all players, check if they're in revenant caves
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                // Start AVARICE_AGGRO_TIMER if not already running - defer setting to avoid ConcurrentModificationException
                if (!player.timers.has(AVARICE_AGGRO_TIMER)) {
                    pendingPlayerTimers.getOrPut(AVARICE_AGGRO_TIMER) { mutableListOf() }.add(Pair(player, 1))
                }
                
                // Only start ETHEREUM_CONSUME_TIMER if player has bracelet equipped - defer setting
                val bracelet = player.getEquipment(EquipmentType.GLOVES)
                if (bracelet?.id == getRSCM("item.bracelet_of_ethereum") && !player.timers.has(ETHEREUM_CONSUME_TIMER)) {
                    pendingPlayerTimers.getOrPut(ETHEREUM_CONSUME_TIMER) { mutableListOf() }.add(Pair(player, 1))
                } else if (bracelet?.id != getRSCM("item.bracelet_of_ethereum")) {
                    // Player doesn't have bracelet, make sure timer is stopped and multiplier is removed - defer removal
                    pendingTimerRemovals.getOrPut(player) { mutableSetOf() }.add(ETHEREUM_CONSUME_TIMER)
                    player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                }
            } else {
                // Player left revenant caves, stop timers and remove multiplier - defer removal
                pendingTimerRemovals.getOrPut(player) { mutableSetOf() }.add(ETHEREUM_CONSUME_TIMER)
                player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
            }
            // Run every 10 ticks to check for players entering the area - defer setting to avoid ConcurrentModificationException
            pendingPlayerTimers.getOrPut(REVENANT_CAVES_CHECK_TIMER) { mutableListOf() }.add(Pair(player, 10))
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
         * Use Ether on Uncharged Bracelet
         */
        onItemOnItem("item.revenant_ether", "item.bracelet_of_ethereum_uncharged") {
            val etherIndex = player.inventory.getItemIndex(getRSCM("item.revenant_ether"), true)
            val ether = player.inventory[etherIndex] ?: return@onItemOnItem
            
            val braceletIndex = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum_uncharged"), true)
            val bracelet = player.inventory[braceletIndex] ?: return@onItemOnItem
            
            if (ether.amount < 250) {
                player.message("You need at least 250 revenant ether to activate the bracelet.")
                return@onItemOnItem
            }
            
            // Consume 250 ether for activation
            val etherToAdd = ether.amount - 250
            val chargesToAdd = etherToAdd.coerceAtMost(16000)
            
            player.inventory.remove(ether.id, 250 + chargesToAdd)
            player.inventory.remove(bracelet.id, 1)
            
            val chargedBracelet = Item(getRSCM("item.bracelet_of_ethereum"))
            chargedBracelet.putAttr(ItemAttribute.CHARGES, chargesToAdd)
            // Use ATTACHED_ITEM_ID as a flag for absorption: 1 = enabled, 0 = disabled
            chargedBracelet.putAttr(ItemAttribute.ATTACHED_ITEM_ID, 1)
            
            player.inventory.add(chargedBracelet)
            player.message("You activate the bracelet with 250 ether" + (if (chargesToAdd > 0) " and add $chargesToAdd charges" else "") + ".")
        }

        /**
         * Use Ether on Charged Bracelet
         */
        onItemOnItem("item.revenant_ether", "item.bracelet_of_ethereum") {
            val etherIndex = player.inventory.getItemIndex(getRSCM("item.revenant_ether"), true)
            val ether = player.inventory[etherIndex] ?: return@onItemOnItem
            
            val braceletSlot = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum"), true)
            val bracelet = player.inventory[braceletSlot] ?: return@onItemOnItem
            
            val currentCharges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
            val space = 16000 - currentCharges
            
            if (space <= 0) {
                player.message("Your bracelet is already fully charged.")
                return@onItemOnItem
            }
            
            val amountToAdd = ether.amount.coerceAtMost(space)
            
            player.inventory.remove(ether.id, amountToAdd)
            bracelet.putAttr(ItemAttribute.CHARGES, currentCharges + amountToAdd)
            player.message("You add $amountToAdd charges to the bracelet.")
        }

        /**
         * Toggle Absorption
         */
        val toggleAbsorption: Plugin.() -> Unit = {
            val invIndex = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum"), false)
            val bracelet = if (invIndex != -1) player.inventory[invIndex] else player.getEquipment(EquipmentType.GLOVES)
            
            if (bracelet != null && bracelet.id == getRSCM("item.bracelet_of_ethereum")) {
                // Use ATTACHED_ITEM_ID as a flag for absorption: 1 = enabled, 0 = disabled (default to enabled if missing)
                val currentVal = bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1
                val newStatus = currentVal == 0
                bracelet.putAttr(ItemAttribute.ATTACHED_ITEM_ID, if (newStatus) 1 else 0)
                
                player.message("Absorption is now ${if (newStatus) "enabled" else "disabled"}.")
                
                // Update equipment stats if equipped
                if (player.hasEquipped(EquipmentType.GLOVES, "item.bracelet_of_ethereum")) {
                     val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                     if (newStatus && charges > 0 && isInRevenantCaves(player.tile)) {
                         player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.5
                     } else {
                         player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                     }
                }
            }
        }
        
        // Register for inventory
        onItemOption("item.bracelet_of_ethereum", "Toggle-absorption", toggleAbsorption)
        
        // Register for equipped item (note: equipped version uses space instead of hyphen)
        onEquipmentOption("item.bracelet_of_ethereum", "Toggle absorption") {
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            if (bracelet != null && bracelet.id == getRSCM("item.bracelet_of_ethereum")) {
                // Use ATTACHED_ITEM_ID as a flag for absorption: 1 = enabled, 0 = disabled (default to enabled if missing)
                val currentVal = bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1
                val newStatus = currentVal == 0
                bracelet.putAttr(ItemAttribute.ATTACHED_ITEM_ID, if (newStatus) 1 else 0)
                
                player.message("Absorption is now ${if (newStatus) "enabled" else "disabled"}.")
                
                // Update equipment stats if equipped
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                if (newStatus && charges > 0 && isInRevenantCaves(player.tile)) {
                    player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.5
                } else {
                    player.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
                }
            }
        }

        /**
         * Uncharge (Dismantle)
         */
        onItemOption("item.bracelet_of_ethereum", "Uncharge") {
            val slot = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum"), false)
            if (slot == -1) {
                player.message("You must unequip the bracelet to dismantle it.")
                return@onItemOption
            }
            
            val bracelet = player.inventory[slot] ?: return@onItemOption
            val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
            
            player.inventory.remove(bracelet.id, 1)
            if (charges > 0) {
                player.inventory.add(getRSCM("item.revenant_ether"), charges)
            }
            
            player.message("You dismantle the bracelet and retrieve $charges ether. The bracelet is destroyed.")
        }

        /**
         * Check/Check charges
         */
        val checkCharges: Plugin.() -> Unit = {
            val invIndex = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum"), false)
            val bracelet = if (invIndex != -1) player.inventory[invIndex] else player.getEquipment(EquipmentType.GLOVES)
            
            if (bracelet != null && bracelet.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                val absorptionVal = bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1
                val absorption = absorptionVal == 1
                
                player.message("Charges: $charges. Absorption: ${if (absorption) "Enabled" else "Disabled"}.")
            }
        }
        
        // Register for inventory
        onItemOption("item.bracelet_of_ethereum", "Check", checkCharges)
        
        // Register for equipped item
        onEquipmentOption("item.bracelet_of_ethereum", "Check") {
            val bracelet = player.getEquipment(EquipmentType.GLOVES)
            
            if (bracelet != null && bracelet.id == getRSCM("item.bracelet_of_ethereum")) {
                val charges = bracelet.getAttr(ItemAttribute.CHARGES) ?: 0
                val absorptionVal = bracelet.getAttr(ItemAttribute.ATTACHED_ITEM_ID) ?: 1
                val absorption = absorptionVal == 1
                
                player.message("Charges: $charges. Absorption: ${if (absorption) "Enabled" else "Disabled"}.")
            }
        }
        
        /**
         * Amulet of Avarice - Note all drops in revenant caves
         * This is handled by modifying the drop system to note items
         * We'll store a flag that the loot drop plugin can check
         */
        // Note: Drop noting will be handled in NpcLootDropPlugin by checking for amulet
        
        /**
         * Amulet of Avarice - Always lost on death
         */
        onPlayerDeath {
            val player = ctx as Player
            val avariceId = getRSCM("item.amulet_of_avarice")
            val amulet = player.getEquipment(EquipmentType.AMULET)
            
            if (amulet?.id == avariceId) {
                // Remove from equipment
                player.equipment[EquipmentType.AMULET.id] = null
                player.equipment.dirty = true
                
                // Drop on ground
                val killer = player.damageMap.getMostDamage() as? Player
                val groundItem = GroundItem(avariceId, 1, player.tile, killer ?: player)
                
                if (killer != null) {
                    groundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                    groundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                } else {
                    // If died to NPC/Environment, make visible to everyone immediately?
                    // Or just player? Standard is player then public.
                    groundItem.timeUntilPublic = 0 // Visible to everyone immediately if no killer?
                    groundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                }
                
                world.spawn(groundItem)
                player.message("Your Amulet of Avarice has been lost!")
            }
        }
        
        /**
         * Periodic check to make revenants aggressive to players with Amulet of Avarice
         */
        onTimer(AVARICE_AGGRO_TIMER) {
            val player = ctx as Player
            if (!isInRevenantCaves(player.tile)) {
                // Stop timer if player left the area - defer removal to avoid ConcurrentModificationException
                pendingTimerRemovals.getOrPut(player) { mutableSetOf() }.add(AVARICE_AGGRO_TIMER)
                return@onTimer
            }
            
            if (player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice")) {
                // Check if player has charged bracelet of ethereum (blocks aggression)
                val bracelet = player.getEquipment(EquipmentType.GLOVES)
                val hasChargedBracelet = bracelet?.id == getRSCM("item.bracelet_of_ethereum") && 
                                        (bracelet.getAttr(ItemAttribute.CHARGES) ?: 0) > 0
                
                if (!hasChargedBracelet) {
                    // Make nearby revenants aggressive
                    world.npcs.forEach { npc ->
                        if (isRevenant(npc) && npc.tile.isWithinRadius(player.tile, 15)) {
                            if (npc.getCombatTarget() != player && npc.lock.canAttack()) {
                                npc.attack(player)
                            }
                        }
                    }
                }
            }
            
            // Check every 3 ticks - defer setting to avoid ConcurrentModificationException
            pendingPlayerTimers.getOrPut(AVARICE_AGGRO_TIMER) { mutableListOf() }.add(Pair(player, 3))
        }
        
        // Start aggro timer for players in revenant caves (on login)
        onLogin {
            val player = ctx as Player
            if (isInRevenantCaves(player.tile)) {
                player.timers[AVARICE_AGGRO_TIMER] = 1
            }
        }
        // The REVENANT_CAVES_CHECK_TIMER will also handle starting it when entering the area
        
        /**
         * Revenant Combat Style Switching
         * Make all revenants switch between melee, ranged, and magic combat styles
         */
        val REVENANT_COMBAT_STYLE_TIMER = TimerKey()
        
        // Data class to hold combat style configuration
        data class RevenantCombatStyle(
            val combatClass: CombatClass,
            val combatStyle: CombatStyle,
            val attackStyle: AttackStyle,
            val spell: CombatSpell?
        )
        
        // Start timer for revenants when they spawn and set initial combat style
        onGlobalNpcSpawn {
            if (isRevenant(npc)) {
                // Set initial random combat style
                val styles = listOf(
                    RevenantCombatStyle(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE, null),
                    RevenantCombatStyle(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE, null),
                    RevenantCombatStyle(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE, CombatSpell.WIND_STRIKE)
                )
                val initialStyle = styles.random()
                npc.combatClass = initialStyle.combatClass
                npc.combatStyle = initialStyle.combatStyle
                npc.attackStyle = initialStyle.attackStyle
                // Set spell for magic attacks
                if (initialStyle.spell != null) {
                    npc.attr[Combat.CASTING_SPELL] = initialStyle.spell
                } else {
                    npc.attr.remove(Combat.CASTING_SPELL)
                }
                
                // Start timer to switch styles periodically
                npc.timers[REVENANT_COMBAT_STYLE_TIMER] = 1
            }
        }
        
        // Timer to switch revenant combat styles
        onTimer(REVENANT_COMBAT_STYLE_TIMER) {
            val npc = ctx as Npc
            if (!isRevenant(npc) || !npc.isAlive()) {
                // Defer removal to avoid ConcurrentModificationException (NPC timers handled separately, but safer to defer)
                pendingNpcTimers.getOrPut(REVENANT_COMBAT_STYLE_TIMER) { mutableListOf() }.add(Pair(npc, -1)) // -1 indicates removal
                return@onTimer
            }
            
            // Switch combat style periodically to make revenants use all 3 attack styles
            val styles = listOf(
                RevenantCombatStyle(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE, null),
                RevenantCombatStyle(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE, null),
                RevenantCombatStyle(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE, CombatSpell.WIND_STRIKE)
            )
            
            // Randomly select a style (can repeat, making it unpredictable)
            val selectedStyle = styles.random()
            npc.combatClass = selectedStyle.combatClass
            npc.combatStyle = selectedStyle.combatStyle
            npc.attackStyle = selectedStyle.attackStyle
            // Set or remove spell based on combat class
            if (selectedStyle.spell != null) {
                npc.attr[Combat.CASTING_SPELL] = selectedStyle.spell
            } else {
                npc.attr.remove(Combat.CASTING_SPELL)
            }
            
            // Run every 4-6 ticks (randomized to make switching less predictable)
            // This ensures revenants switch styles multiple times during combat
            // Defer timer setting to avoid ConcurrentModificationException
            val timerValue = world.random(4..6)
            pendingNpcTimers.getOrPut(REVENANT_COMBAT_STYLE_TIMER) { mutableListOf() }.add(Pair(npc, timerValue))
        }
        
        /**
         * Revenant Tile Protection
         * Prevent revenants from standing on the same tile as each other
         */
        val REVENANT_TILE_PROTECTION_TIMER = TimerKey()
        
        // Start timer for revenants when they spawn
        onGlobalNpcSpawn {
            if (isRevenant(npc)) {
                // Start tile protection timer if not already running
                if (!npc.timers.has(REVENANT_TILE_PROTECTION_TIMER)) {
                    npc.timers[REVENANT_TILE_PROTECTION_TIMER] = 2 // Check every 2 ticks
                }
            }
        }
        
        // Timer to check and prevent revenants from standing on the same tile
        onTimer(REVENANT_TILE_PROTECTION_TIMER) {
            val npc = ctx as Npc
            if (!isRevenant(npc) || !npc.isAlive()) {
                // Defer removal to avoid ConcurrentModificationException (NPC timers handled separately, but safer to defer)
                pendingNpcTimers.getOrPut(REVENANT_TILE_PROTECTION_TIMER) { mutableListOf() }.add(Pair(npc, -1)) // -1 indicates removal
                return@onTimer
            }
            
            val npcTile = npc.tile
            
            // Check all other revenants to see if any are on the same tile
            world.npcs.forEach { otherNpc ->
                if (otherNpc != npc && isRevenant(otherNpc) && otherNpc.isAlive()) {
                    if (otherNpc.tile.sameAs(npcTile)) {
                        // Another revenant is on the same tile, move this one away
                        val adjacentTiles = listOf(
                            npcTile.transform(1, 0),   // East
                            npcTile.transform(-1, 0),  // West
                            npcTile.transform(0, 1),   // North
                            npcTile.transform(0, -1),  // South
                            npcTile.transform(1, 1),   // Northeast
                            npcTile.transform(-1, 1),  // Northwest
                            npcTile.transform(1, -1),  // Southeast
                            npcTile.transform(-1, -1)  // Southwest
                        )
                        
                        // Find a walkable adjacent tile that doesn't have another revenant on it
                        val targetTile = adjacentTiles.firstOrNull { tile ->
                            // Check if tile is walkable
                            val chunk = npc.world.chunks.get(tile, createIfNeeded = false)
                            if (chunk == null) return@firstOrNull false
                            
                            val isWalkable = npc.world.reachStrategy.reached(
                                flags = npc.world.collision,
                                level = tile.height,
                                srcX = npcTile.x,
                                srcZ = npcTile.z,
                                destX = tile.x,
                                destZ = tile.z,
                                destWidth = 1,
                                destLength = 1,
                                srcSize = 1,
                                locShape = -2
                            )
                            
                            if (!isWalkable) return@firstOrNull false
                            
                            // Check if another revenant is already on this tile
                            val hasOtherRevenant = world.npcs.any { checkNpc ->
                                checkNpc != npc && isRevenant(checkNpc) && checkNpc.isAlive() && checkNpc.tile.sameAs(tile)
                            }
                            
                            !hasOtherRevenant
                        }
                        
                        if (targetTile != null) {
                            npc.moveTo(targetTile)
                        }
                    }
                }
            }
            
            // Run every 2 ticks to check for tile conflicts
            // Defer timer setting to avoid ConcurrentModificationException
            pendingNpcTimers.getOrPut(REVENANT_TILE_PROTECTION_TIMER) { mutableListOf() }.add(Pair(npc, 2))
        }
        
        /**
         * Revenant Single-Combat Enforcement Timer
         * Stops revenants that try to attack a player already being attacked by another revenant
         * This handles the race condition where multiple revenants check simultaneously
         */
        val REVENANT_SINGLE_COMBAT_TIMER = TimerKey()
        
        // Start timer for revenants when they spawn
        onGlobalNpcSpawn {
            if (isRevenant(npc) && isInRevenantCaves(npc.tile)) {
                if (!npc.timers.has(REVENANT_SINGLE_COMBAT_TIMER)) {
                    npc.timers[REVENANT_SINGLE_COMBAT_TIMER] = 1 // Check every tick
                }
            }
        }
        
        // Timer to enforce single-combat for revenants (with delay check)
        onTimer(REVENANT_SINGLE_COMBAT_TIMER) {
            val npc = ctx as Npc
            if (!isRevenant(npc) || !npc.isAlive() || !isInRevenantCaves(npc.tile)) {
                // Defer removal to avoid ConcurrentModificationException (NPC timers handled separately, but safer to defer)
                pendingNpcTimers.getOrPut(REVENANT_SINGLE_COMBAT_TIMER) { mutableListOf() }.add(Pair(npc, -1)) // -1 indicates removal
                return@onTimer
            }
            
            val target = npc.getCombatTarget()
            if (target is Player && isInRevenantCaves(target.tile)) {
                // Check if another revenant is already attacking this player
                val otherRevenantAttacking = world.npcs.any { otherNpc ->
                    otherNpc != npc && 
                    isRevenant(otherNpc) && 
                    otherNpc.isAlive() && 
                    otherNpc.getCombatTarget() == target &&
                    isInRevenantCaves(otherNpc.tile)
                }
                
                // Check if player has a recent revenant target delay (3 seconds = 300 cycles)
                val hasRecentTargetDelay = target.timers.has(RevenantManagementPlugin.REVENANT_TARGET_DELAY_TIMER)
                
                if (otherRevenantAttacking || hasRecentTargetDelay) {
                    // Another revenant is already attacking this player, or delay is active, stop this one
                    npc.removeCombatTarget()
                    npc.resetFacePawn()
                    npc.interruptQueues()
                }
            }
            
            // Run every tick to check for conflicts
            // Defer timer setting to avoid ConcurrentModificationException
            pendingNpcTimers.getOrPut(REVENANT_SINGLE_COMBAT_TIMER) { mutableListOf() }.add(Pair(npc, 1))
        }
    }
    
    /**
     * Revenant Combat Handler
     * Handles melee, ranged, and magic attacks based on the current combat class
     * Applies damage multiplier to increase revenant damage output
     */
    private suspend fun revenantCombat(npc: Npc, it: QueueTask) {
        var target = npc.getCombatTarget() ?: return
        
        // Apply damage multiplier for revenants (10.0x damage - doubled from 5.0x)
        // This makes revenants hit significantly harder
        npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = 10.0
        
        // Loop while in combat
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Check if we can attack (in range and attack delay ready)
            val canAttack = when (npc.combatClass) {
                CombatClass.MELEE -> {
                    if (npc.canAttackMelee(it, target, moveIfNeeded = true) && npc.isAttackDelayReady()) {
                        // Melee attack - ensure combat style is valid for melee (STAB, SLASH, or CRUSH)
                        // If combat style is MAGIC or RANGED from a previous switch, default to SLASH
                        val meleeStyle = when (npc.combatStyle) {
                            CombatStyle.STAB, CombatStyle.SLASH, CombatStyle.CRUSH -> npc.combatStyle
                            else -> CombatStyle.SLASH // Default to SLASH if invalid style
                        }
                        npc.prepareAttack(CombatClass.MELEE, meleeStyle, npc.attackStyle)
                        npc.animate(npc.combatDef.attackAnimation)
                        npc.dealHit(target, MeleeCombatFormula, delay = 1)
                        true
                    } else {
                        false
                    }
                }
                CombatClass.RANGED -> {
                    if (npc.combatRaycast(target, RangedCombatStrategy.getAttackRange(npc), projectile = true) && npc.isAttackDelayReady()) {
                        // Ranged attack - create projectile
                        val projectile = npc.createProjectile(
                            target,
                            gfx = 249, // Arrow projectile
                            startHeight = 43,
                            endHeight = 31,
                            delay = 51,
                            angle = 10,
                            steepness = 11
                        )
                        npc.prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
                        npc.animate(426) // Ranged attack animation
                        npc.world.spawn(projectile)
                        val hitDelay = RangedCombatStrategy.getHitDelay(npc.getFrontFacingTile(target), target.getCentreTile())
                        npc.dealHit(target, RangedCombatFormula, delay = hitDelay - 1)
                        true
                    } else {
                        false
                    }
                }
                CombatClass.MAGIC -> {
                    if (npc.combatRaycast(target, MagicCombatStrategy.getAttackRange(npc), projectile = true) && npc.isAttackDelayReady()) {
                        // Magic attack - get spell from attribute or use default
                        val spell = npc.attr[Combat.CASTING_SPELL] ?: CombatSpell.WIND_STRIKE
                        val projectile = npc.createProjectile(
                            target,
                            gfx = spell.projectile,
                            startHeight = 43,
                            endHeight = if (spell.projectilEndHeight != -1) spell.projectilEndHeight else 31,
                            delay = 51,
                            angle = 15,
                            steepness = 127
                        )
                        npc.prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
                        npc.animate(spell.castAnimation)
                        npc.world.spawn(projectile)
                        val hitDelay = MagicCombatStrategy.getHitDelay(npc.getFrontFacingTile(target), target.getCentreTile())
                        npc.dealHit(target, MagicCombatFormula, delay = hitDelay - 1)
                        // Show impact graphic
                        spell.impactGfx?.let { impact ->
                            target.graphic(impact.id, impact.height, delay = hitDelay - 1)
                        }
                        true
                    } else {
                        false
                    }
                }
                else -> {
                    // Fallback to melee
                    if (npc.canAttackMelee(it, target, moveIfNeeded = true) && npc.isAttackDelayReady()) {
                        npc.prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
                        npc.animate(npc.combatDef.attackAnimation)
                        npc.dealHit(target, MeleeCombatFormula, delay = 1)
                        true
                    } else {
                        false
                    }
                }
            }
            
            // Post attack logic (handles retaliation, etc.)
            if (canAttack) {
                npc.postAttackLogic(target)
            }
            
            // Wait before next attack cycle
            it.wait(1)
            
            // Update target (in case it changed)
            target = npc.getCombatTarget() ?: break
        }
        
        // Clean up when combat ends
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}

