package org.alter.plugins.content.npcs.commanderzilyana

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Graphic
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * Commander Zilyana Combat Plugin
 * 
 * Commander Zilyana is a fast melee boss with special mechanics:
 * - Very Fast Melee Attacks: Attacks every 2 ticks
 * - Long Movement Range: Chases player quickly
 * - Special Dash Attack: Every few ticks, unavoidable light damage
 * - No Magic/Ranged: Only uses melee attacks
 * 
 * Minions:
 * - Starlight (Melee)
 * - Growler (Magic)
 * - Bree (Range)
 */
class CommanderZilyanaCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute key to track minions spawned by this boss
        private val MINIONS_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("commander_zilyana_minions")
        private val LAST_DASH_ATTACK_ATTR = AttributeKey<Int>("last_dash_attack_tick")
        private val LAST_HEAL_ATTR = AttributeKey<Int>("last_heal_tick")
        private val DIVINE_PROTECTION_ATTR = AttributeKey<Boolean>("divine_protection_active")
        private val DIVINE_PROTECTION_END_ATTR = AttributeKey<Int>("divine_protection_end_tick")
        private val HAS_USED_DIVINE_PROTECTION_ATTR = AttributeKey<Boolean>("has_used_divine_protection")
        
        /**
         * Animation IDs for Zilyana's attacks
         */
        private const val MELEE_ATTACK_ANIM = Animation.COMMANDER_ZILYANA_MELEE_ATTACK
        
        /**
         * Graphics for Zilyana's attacks
         */
        private const val DASH_ATTACK_GFX = Graphic.COMMANDER_ZILYANA_MAGIC_HIT // Lightning/light graphic
        
        /**
         * Damage ranges
         */
        private const val MELEE_MAX_HIT = 31 // Increased damage
        private const val DASH_DAMAGE_MIN = 8
        private const val DASH_DAMAGE_MAX = 18
        private const val AOE_DAMAGE_MIN = 10
        private const val AOE_DAMAGE_MAX = 20
        private const val SMITE_DAMAGE_MIN = 15
        private const val SMITE_DAMAGE_MAX = 30
        
        /**
         * Healing mechanics
         */
        private const val HEAL_THRESHOLD_HP = 100 // Heal when HP drops below this
        private const val HEAL_AMOUNT_MIN = 25
        private const val HEAL_AMOUNT_MAX = 40
        private const val HEAL_COOLDOWN = 20 // 20 ticks between heals
        
        /**
         * Divine protection phase
         */
        private const val DIVINE_PROTECTION_THRESHOLD = 50 // Activate at 50 HP
        private const val DIVINE_PROTECTION_DURATION = 10 // 10 ticks of protection
        
        /**
         * Attack chances and timing
         */
        private const val DASH_ATTACK_CHANCE = 20 // 20% chance
        private const val AOE_ATTACK_CHANCE = 15 // 15% chance for area smite
        private const val SMITE_ATTACK_CHANCE = 25 // 25% chance for powerful smite
        private const val HEAL_CHANCE = 30 // 30% chance when low HP
    }
    
    init {
        /**
         * Handle Zilyana's combat
         */
        onNpcCombat("npc.commander_zilyana") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Spawn minions when Zilyana spawns
        onNpcSpawn("npc.commander_zilyana") {
            val npc = ctx as Npc
            // Initialize minion tracking
            if (npc.attr[MINIONS_ATTR] == null) {
                npc.attr[MINIONS_ATTR] = mutableListOf()
            }
            // Initialize combat attributes
            npc.attr[LAST_DASH_ATTACK_ATTR] = 0
            npc.attr[LAST_HEAL_ATTR] = 0
            npc.attr[DIVINE_PROTECTION_ATTR] = false
            npc.attr[DIVINE_PROTECTION_END_ATTR] = 0
            npc.attr[HAS_USED_DIVINE_PROTECTION_ATTR] = false
            // Spawn all three minions
            spawnMinions(npc)
        }
        
        // Clean up minions when boss dies
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.commander_zilyana")) {
                cleanupMinions(npc)
            }
        }
        
        // Clean up minions when minions die
        onAnyNpcDeath {
            val npc = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.starlight"),
                getRSCM("npc.growler"),
                getRSCM("npc.bree")
            )
            if (npc.id in minionIds && !npc.respawns) {
                // Remove this minion from its boss's minion list
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.commander_zilyana")) {
                        val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                        minions.removeAll { it.get() == null || it.get() == npc }
                        boss.attr[MINIONS_ATTR] = minions
                    }
                }
            }
        }
        
        // Configure minions to be aggressive to ALL players
        onNpcSpawn(npc = "npc.starlight") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MELEE
            }
        }
        
        onNpcSpawn(npc = "npc.growler") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MAGIC
            }
        }
        
        onNpcSpawn(npc = "npc.bree") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.RANGED
            }
        }
    }
    
    /**
     * Main combat loop for Commander Zilyana
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        // Initialize minion tracking if not already set
        if (attr[MINIONS_ATTR] == null) {
            attr[MINIONS_ATTR] = mutableListOf()
        }
        
        // Initialize all attributes if not set
        if (attr[LAST_DASH_ATTACK_ATTR] == null) {
            attr[LAST_DASH_ATTACK_ATTR] = 0
        }
        if (attr[LAST_HEAL_ATTR] == null) {
            attr[LAST_HEAL_ATTR] = 0
        }
        if (attr[DIVINE_PROTECTION_ATTR] == null) {
            attr[DIVINE_PROTECTION_ATTR] = false
        }
        if (attr[DIVINE_PROTECTION_END_ATTR] == null) {
            attr[DIVINE_PROTECTION_END_ATTR] = 0
        }
        if (attr[HAS_USED_DIVINE_PROTECTION_ATTR] == null) {
            attr[HAS_USED_DIVINE_PROTECTION_ATTR] = false
        }
        
        // Update minions
        updateMinions()
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            val currentTick = this.world.currentCycle
            val currentHp = getCurrentHp()
            
            // Check divine protection status
            updateDivineProtection(currentTick)
            
            // Check if we should activate divine protection (low HP, once per fight)
            if (!attr[HAS_USED_DIVINE_PROTECTION_ATTR]!! && currentHp <= DIVINE_PROTECTION_THRESHOLD) {
                activateDivineProtection(currentTick)
            }
            
            // Check if we should heal (low HP, on cooldown)
            val lastHealTick = attr[LAST_HEAL_ATTR] ?: 0
            val ticksSinceLastHeal = currentTick - lastHealTick
            if (currentHp < HEAL_THRESHOLD_HP && ticksSinceLastHeal >= HEAL_COOLDOWN && this.world.random(100) < HEAL_CHANCE) {
                divineHeal()
                attr[LAST_HEAL_ATTR] = currentTick
            }
            
            // Long movement range - chase player quickly
            if (moveToAttackRange(it, target, distance = 1, projectile = false)) {
                if (isAttackDelayReady()) {
                    // Choose attack based on random chance
                    val attackRoll = this.world.random(100)
                    
                    when {
                        attackRoll < AOE_ATTACK_CHANCE -> {
                            // Area-of-effect smite attack
                            areaSmiteAttack(target)
                        }
                        attackRoll < (AOE_ATTACK_CHANCE + SMITE_ATTACK_CHANCE) -> {
                            // Powerful single-target smite
                            smiteAttack(target)
                        }
                        attackRoll < (AOE_ATTACK_CHANCE + SMITE_ATTACK_CHANCE + DASH_ATTACK_CHANCE) -> {
                            // Dash attack
                            dashAttack(target)
                        }
                        else -> {
                            // Regular fast melee attack
                            meleeAttack(target)
                        }
                    }
                    
                    postAttackLogic(target)
                }
            }
            
            it.wait(1)
            target = getCombatTarget() ?: break
        }
        
        resetFacePawn()
        removeCombatTarget()
    }
    
    /**
     * Melee attack - very fast, standard melee damage
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            // Cap at MELEE_MAX_HIT (27)
            val damage = minOf(this.world.random(maxHit + 1), MELEE_MAX_HIT)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Dash attack - unavoidable light damage
     * This is Zilyana's special attack that cannot be blocked
     */
    private fun Npc.dashAttack(target: Pawn) {
        // Show dash animation and graphic
        animate(MELEE_ATTACK_ANIM)
        graphic(id = DASH_ATTACK_GFX, height = 0)
        
        // Deal unavoidable typeless damage
        val damage = this.world.random(DASH_DAMAGE_MIN..DASH_DAMAGE_MAX)
        
        // Always hits - unavoidable
        target.hit(damage, type = HitType.HIT, delay = 1)
        target.graphic(id = DASH_ATTACK_GFX, height = 0, delay = 1)
        
        if (target is Player && damage > 0) {
            target.message("Commander Zilyana dashes at you with divine speed!")
        }
    }
    
    /**
     * Powerful smite attack - high damage single target
     */
    private fun Npc.smiteAttack(target: Pawn) {
        animate(MELEE_ATTACK_ANIM)
        graphic(id = DASH_ATTACK_GFX, height = 100)
        
        // High damage smite attack
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val damage = this.world.random(SMITE_DAMAGE_MIN..SMITE_DAMAGE_MAX)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = DASH_ATTACK_GFX, height = 0, delay = 1)
            
            if (target is Player) {
                target.message("Commander Zilyana smites you with holy power!")
            }
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Area-of-effect smite - damages all nearby players
     */
    private fun Npc.areaSmiteAttack(target: Pawn) {
        animate(MELEE_ATTACK_ANIM)
        graphic(id = DASH_ATTACK_GFX, height = 150)
        
        // Find all players within 3 tiles
        val nearbyPlayers = mutableListOf<Player>()
        if (target is Player) {
            nearbyPlayers.add(target)
        }
        
        // Get other players in range
        this.world.players.forEach { player ->
            if (player != target && player.tile.isWithinRadius(this.tile, 3)) {
                nearbyPlayers.add(player)
            }
        }
        
        // Damage all nearby players
        nearbyPlayers.forEach { player ->
            val damage = this.world.random(AOE_DAMAGE_MIN..AOE_DAMAGE_MAX)
            player.hit(damage, type = HitType.HIT, delay = 1)
            player.graphic(id = DASH_ATTACK_GFX, height = 0, delay = 1)
            player.message("Commander Zilyana's divine light strikes you!")
        }
        
        if (nearbyPlayers.size > 1 && target is Player) {
            target.message("Commander Zilyana's area smite hits everyone nearby!")
        }
    }
    
    /**
     * Divine healing - Zilyana heals herself
     */
    private fun Npc.divineHeal() {
        val healAmount = this.world.random(HEAL_AMOUNT_MIN..HEAL_AMOUNT_MAX)
        val maxHp = combatDef.hitpoints
        val currentHp = getCurrentHp()
        val actualHeal = minOf(healAmount, maxHp - currentHp)
        
        if (actualHeal > 0) {
            setCurrentHp(currentHp + actualHeal)
            graphic(id = DASH_ATTACK_GFX, height = 100)
            
            // Notify nearby players
            this.world.players.forEach { player ->
                if (player.tile.isWithinRadius(this.tile, 15)) {
                    player.message("Commander Zilyana channels divine energy to heal herself!")
                }
            }
        }
    }
    
    /**
     * Activate divine protection - reduces damage taken
     */
    private fun Npc.activateDivineProtection(currentTick: Int) {
        attr[DIVINE_PROTECTION_ATTR] = true
        attr[DIVINE_PROTECTION_END_ATTR] = currentTick + DIVINE_PROTECTION_DURATION
        attr[HAS_USED_DIVINE_PROTECTION_ATTR] = true
        
        graphic(id = DASH_ATTACK_GFX, height = 100)
        
        // Notify nearby players
        this.world.players.forEach { player ->
            if (player.tile.isWithinRadius(this.tile, 15)) {
                player.message("Commander Zilyana is protected by divine light!")
            }
        }
    }
    
    /**
     * Update divine protection status
     */
    private fun Npc.updateDivineProtection(currentTick: Int) {
        val isProtected = attr[DIVINE_PROTECTION_ATTR] ?: false
        if (isProtected) {
            val endTick = attr[DIVINE_PROTECTION_END_ATTR] ?: 0
            if (currentTick >= endTick) {
                attr[DIVINE_PROTECTION_ATTR] = false
                
                // Notify nearby players
                this.world.players.forEach { player ->
                    if (player.tile.isWithinRadius(this.tile, 15)) {
                        player.message("Commander Zilyana's divine protection fades.")
                    }
                }
            }
        }
    }
    
    /**
     * Spawn all three minions
     */
    private fun spawnMinions(boss: Npc) {
        val bossTile = boss.tile
        
        // Spawn positions around Zilyana
        val spawnPositions = listOf(
            bossTile.transform(1, 0),   // East
            bossTile.transform(-1, 0),  // West
            bossTile.transform(0, 1)     // North
        )
        
        val minionTypes = listOf(
            "npc.starlight" to CombatClass.MELEE,
            "npc.growler" to CombatClass.MAGIC,
            "npc.bree" to CombatClass.RANGED
        )
        
        minionTypes.forEachIndexed { index, (npcName, combatClass) ->
            val spawnTile = spawnPositions.getOrNull(index) ?: bossTile.transform(0, -1)
            
            try {
                val minion = Npc(getRSCM(npcName), spawnTile, world)
                minion.respawns = false
                minion.walkRadius = 5
                minion.setActive(true)
                minion.combatClass = combatClass
                
                world.spawn(minion)
                
                // Add to minion list
                val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                minions.add(WeakReference(minion))
                boss.attr[MINIONS_ATTR] = minions
            } catch (e: Exception) {
                println("Error spawning minion: ${e.message}")
            }
        }
    }
    
    /**
     * Update and clean up minions
     */
    private fun Npc.updateMinions() {
        val minions = attr[MINIONS_ATTR] ?: return
        val activeMinions = minions.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        
        // Clean up dead/invalid minions
        minions.removeAll { it.get() == null || !it.get()!!.isActive() || !it.get()!!.isSpawned() }
        
        // Clean up minions that are too far from boss
        activeMinions.forEach { minion ->
            val distance = tile.getDistance(minion.tile)
            if (distance > 20) {
                world.remove(minion)
                minions.removeAll { it.get() == minion }
            }
        }
        
        attr[MINIONS_ATTR] = minions
    }
    
    /**
     * Cleans up all minions spawned by the boss when the boss dies
     */
    private fun cleanupMinions(boss: Npc) {
        val minions = boss.attr[MINIONS_ATTR] ?: return
        minions.mapNotNull { it.get() }.forEach { minion ->
            if (minion.isSpawned() && minion.isActive()) {
                world.remove(minion)
            }
        }
        boss.attr.remove(MINIONS_ATTR)
    }
}






