package org.alter.plugins.content.npcs.nex

import org.alter.api.*
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
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * Nex Combat Plugin
 * 
 * Nex is a 5-phase boss with unique mechanics for each phase.
 * Each phase has a minion that must be killed to proceed.
 * 
 * Phases:
 * 1. Smoke (Fumus minion) - Poison and prayer drain
 * 2. Shadow (Umbra minion) - Shadow damage and darkness
 * 3. Blood (Cruor minion) - Blood healing and sacrifice
 * 4. Ice (Glacies minion) - Freeze and ice prison
 * 5. Zaros (final phase) - Turmoil and wrath
 */
class NexCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute keys
        private val PHASE_ATTR = AttributeKey<NexPhase>("nex_phase")
        private val MINION_ATTR = AttributeKey<WeakReference<Npc>>("nex_minion")
        private val LAST_SPECIAL_ATTR = AttributeKey<Int>("nex_last_special_tick")
        private val BLOOD_SACRIFICE_ATTR = AttributeKey<Tile>("nex_blood_sacrifice_tile")
        
        /**
         * Phase enum
         */
        enum class NexPhase {
            SMOKE,      // Phase 1: Fumus minion
            SHADOW,     // Phase 2: Umbra minion
            BLOOD,      // Phase 3: Cruor minion
            ICE,        // Phase 4: Glacies minion
            ZAROS       // Phase 5: Final phase
        }
        
        /**
         * Special attack intervals
         */
        private const val SPECIAL_ATTACK_INTERVAL = 15 // Every 15 ticks
        
        /**
         * Damage ranges
         */
        private const val MELEE_MAX_HIT = 50
        private const val MAGIC_MAX_HIT = 40
        private const val RANGED_MAX_HIT = 40
    }
    
    init {
        /**
         * Handle Nex's combat
         */
        onNpcCombat("npc.nex") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Initialize phase when Nex spawns
        onNpcSpawn("npc.nex") {
            val npc = ctx as Npc
            npc.attr[PHASE_ATTR] = NexPhase.SMOKE
            npc.attr[LAST_SPECIAL_ATTR] = 0
            // Spawn first phase minion (Fumus)
            spawnPhaseMinion(npc, NexPhase.SMOKE)
        }
        
        // Check for minion deaths to progress phases
        onAnyNpcDeath {
            val minion = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.fumus"),
                getRSCM("npc.umbra"),
                getRSCM("npc.cruor"),
                getRSCM("npc.glacies")
            )
            
            if (minion.id in minionIds && !minion.respawns) {
                // Find Nex and progress to next phase
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.nex")) {
                        val minionRef = boss.attr[MINION_ATTR]?.get()
                        if (minionRef == minion) {
                            progressToNextPhase(boss)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Main combat loop for Nex
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        // Initialize phase if not set
        if (attr[PHASE_ATTR] == null) {
            attr[PHASE_ATTR] = NexPhase.SMOKE
            spawnPhaseMinion(this, NexPhase.SMOKE)
        }
        
        val currentPhase = attr[PHASE_ATTR] ?: NexPhase.SMOKE
        
        // Apply global passive for current phase
        applyPhasePassive(currentPhase, target)
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Move to attack range
            if (moveToAttackRange(it, target, distance = 7, projectile = true) && isAttackDelayReady()) {
                val currentTick = this.world.currentCycle
                val lastSpecial = attr[LAST_SPECIAL_ATTR] ?: 0
                val ticksSinceSpecial = currentTick - lastSpecial
                
                // Use special attack if interval has passed
                if (ticksSinceSpecial >= SPECIAL_ATTACK_INTERVAL) {
                    usePhaseSpecialAttack(currentPhase, target)
                    attr[LAST_SPECIAL_ATTR] = currentTick
                } else {
                    // Regular attack based on phase
                    usePhaseRegularAttack(currentPhase, target)
                }
                
                postAttackLogic(target)
            }
            
            it.wait(1)
            target = getCombatTarget() ?: break
        }
        
        resetFacePawn()
        removeCombatTarget()
    }
    
    /**
     * Regular attack based on phase
     */
    private fun Npc.usePhaseRegularAttack(phase: NexPhase, target: Pawn) {
        when (phase) {
            NexPhase.SMOKE -> {
                // Smoke phase: Magic attacks with poison chance
                magicAttack(target, canPoison = true)
            }
            NexPhase.SHADOW -> {
                // Shadow phase: Ranged attacks
                rangedAttack(target)
            }
            NexPhase.BLOOD -> {
                // Blood phase: Melee attacks
                meleeAttack(target)
            }
            NexPhase.ICE -> {
                // Ice phase: Magic attacks with freeze chance
                magicAttack(target, canFreeze = true)
            }
            NexPhase.ZAROS -> {
                // Zaros phase: Mix of all attack types
                val roll = this.world.random(100)
                when {
                    roll < 40 -> meleeAttack(target)
                    roll < 70 -> magicAttack(target)
                    else -> rangedAttack(target)
                }
            }
        }
    }
    
    /**
     * Special attack based on phase
     */
    private fun Npc.usePhaseSpecialAttack(phase: NexPhase, target: Pawn) {
        when (phase) {
            NexPhase.SMOKE -> smokeSpecialAttack(target)
            NexPhase.SHADOW -> shadowSpecialAttack(target)
            NexPhase.BLOOD -> bloodSpecialAttack(target)
            NexPhase.ICE -> iceSpecialAttack(target)
            NexPhase.ZAROS -> zarosSpecialAttack(target)
        }
    }
    
    /**
     * Apply global passive effects for current phase
     */
    private fun applyPhasePassive(phase: NexPhase, target: Pawn) {
        if (target !is Player) return
        
        when (phase) {
            NexPhase.SMOKE -> {
                // Smoke passive: Slow prayer drain over time
                val currentPrayer = target.getSkills().getCurrentLevel(Skills.PRAYER)
                if (currentPrayer > 0 && this.world.chance(1, 10)) {
                    target.getSkills().alterCurrentLevel(skill = Skills.PRAYER, value = -1, capValue = 0)
                }
            }
            NexPhase.SHADOW -> {
                // Shadow passive: Reduced visibility (no mechanical effect, just visual)
                // Could add stat reduction if needed
            }
            NexPhase.BLOOD -> {
                // Blood passive: Nex heals from damage dealt
                // Handled in damage dealing
            }
            NexPhase.ICE -> {
                // Ice passive: Movement speed reduction (handled in movement)
            }
            NexPhase.ZAROS -> {
                // Zaros passive: Stat boost for Nex (handled in combat formulas)
            }
        }
    }
    
    /**
     * Smoke Phase Special: Choke - Drains prayer and stats
     */
    private fun Npc.smokeSpecialAttack(target: Pawn) {
        forceChat("Choke!")
        animate(1979) // Magic special animation
        
        if (target is Player) {
            // Drain prayer
            val prayerDrain = this.world.random(5) + 5
            val currentPrayer = target.getSkills().getCurrentLevel(Skills.PRAYER)
            val drainAmount = minOf(prayerDrain, currentPrayer)
            if (drainAmount > 0) {
                target.getSkills().alterCurrentLevel(skill = Skills.PRAYER, value = -drainAmount, capValue = 0)
            }
            
            // Drain stats
            val attackDrain = this.world.random(3) + 2
            target.getSkills().alterCurrentLevel(skill = Skills.ATTACK, value = -attackDrain, capValue = 0)
            target.getSkills().alterCurrentLevel(skill = Skills.STRENGTH, value = -attackDrain, capValue = 0)
            target.getSkills().alterCurrentLevel(skill = Skills.DEFENCE, value = -attackDrain, capValue = 0)
            
            target.message("Nex chokes you, draining your prayer and stats!")
        }
    }
    
    /**
     * Shadow Phase Special: Embrace Darkness - Area damage
     */
    private fun Npc.shadowSpecialAttack(target: Pawn) {
        forceChat("Embrace Darkness!")
        animate(1979) // Magic special animation
        
        // Damage all players in range
        val bossTile = this.tile
        this.world.players.forEach { player ->
            if (player.initiated && !player.isDead() && player.tile.height == bossTile.height) {
                val distance = bossTile.getDistance(player.tile)
                if (distance <= 10) {
                    val damage = this.world.random(15) + 10
                    player.hit(damage, type = HitType.HIT, delay = 0)
                    player.message("Darkness embraces you!")
                }
            }
        }
    }
    
    /**
     * Blood Phase Special: Blood Sacrifice - High damage if player doesn't move
     */
    private fun Npc.bloodSpecialAttack(target: Pawn) {
        forceChat("Blood Sacrifice!")
        animate(1979) // Magic special animation
        
        if (target is Player) {
            target.message("Nex marks you for blood sacrifice! Move away!")
            
            // Mark target tile for sacrifice
            val sacrificeTile = target.tile
            target.attr[BLOOD_SACRIFICE_ATTR] = sacrificeTile
            
            // Deal damage after delay if player hasn't moved
            this.world.queue {
                wait(5)
                val markedTile = target.attr[BLOOD_SACRIFICE_ATTR]
                // Check if player is still on the same tile (hasn't moved)
                if (markedTile != null && target.tile.sameAs(markedTile)) {
                    val damage = this@bloodSpecialAttack.world.random(30) + 20
                    target.hit(damage, type = HitType.HIT, delay = 0)
                    target.message("Nex sacrifices you for blood!")
                    
                    // Heal Nex
                    val currentHp = this@bloodSpecialAttack.getCurrentHp()
                    val maxHp = this@bloodSpecialAttack.getMaxHp()
                    val healAmount = minOf(damage / 2, maxHp - currentHp)
                    this@bloodSpecialAttack.setCurrentHp(minOf(currentHp + healAmount, maxHp))
                } else if (markedTile != null) {
                    target.message("You moved away from the blood sacrifice!")
                }
                target.attr.remove(BLOOD_SACRIFICE_ATTR)
            }
        }
    }
    
    /**
     * Ice Phase Special: Contain This - Ice prison around Nex
     */
    private fun Npc.iceSpecialAttack(target: Pawn) {
        forceChat("Contain This!")
        animate(1979) // Magic special animation
        
        // Create ice prison effect around Nex
        val bossTile = this.tile
        for (x in -1..1) {
            for (z in -1..1) {
                if (x != 0 || z != 0) {
                    val iceTile = bossTile.transform(x, z)
                    // Show ice graphic
                    this.world.spawn(TileGraphic(id = 361, tile = iceTile, height = 0, delay = 0))
                }
            }
        }
        
        // Damage players in 3x3 area
        this.world.players.forEach { player ->
            if (player.initiated && !player.isDead() && player.tile.height == bossTile.height) {
                val distance = bossTile.getDistance(player.tile)
                if (distance <= 1) {
                    val damage = this.world.random(20) + 15
                    player.hit(damage, type = HitType.HIT, delay = 0)
                    player.message("You are contained by ice!")
                }
            }
        }
    }
    
    /**
     * Zaros Phase Special: Wrath - High damage to nearby players
     */
    private fun Npc.zarosSpecialAttack(target: Pawn) {
        forceChat("Wrath!")
        animate(1979) // Magic special animation
        
        // High damage to all nearby players
        val bossTile = this.tile
        this.world.players.forEach { player ->
            if (player.initiated && !player.isDead() && player.tile.height == bossTile.height) {
                val distance = bossTile.getDistance(player.tile)
                if (distance <= 8) {
                    val damage = this.world.random(40) + 30
                    player.hit(damage, type = HitType.HIT, delay = 0)
                    player.message("Nex's wrath strikes you!")
                }
            }
        }
    }
    
    /**
     * Melee attack
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(422) // Generic melee attack animation
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            val damage = minOf(this.world.random(maxHit + 1), MELEE_MAX_HIT)
            target.hit(damage, type = HitType.HIT, delay = 1)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Magic attack
     */
    private fun Npc.magicAttack(target: Pawn, canPoison: Boolean = false, canFreeze: Boolean = false) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_BLAST
        animate(711) // Generic magic attack animation
        
        val projectile = createProjectile(
            target,
            gfx = 157,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        val hitDelay = MagicCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        this.world.queue {
            wait(hitDelay - 1)
            
            if (MagicCombatFormula.getAccuracy(this@magicAttack, target) >= this@magicAttack.world.randomDouble()) {
                val maxHit = MagicCombatFormula.getMaxHit(this@magicAttack, target)
                val damage = minOf(this@magicAttack.world.random(maxHit + 1), MAGIC_MAX_HIT)
                target.hit(damage, type = HitType.HIT)
                
                if (canPoison && target is Player && this@magicAttack.world.chance(1, 4)) {
                    target.poison(initialDamage = 4) {
                        target.message("You have been poisoned by Nex's smoke!")
                    }
                }
                
                if (canFreeze && target is Player && this@magicAttack.world.chance(1, 5)) {
                    // Freeze effect (could add movement restriction)
                    target.message("You are frozen by Nex's ice!")
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
        
        attr.remove(Combat.CASTING_SPELL)
    }
    
    /**
     * Ranged attack
     */
    private fun Npc.rangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(426) // Generic ranged attack animation
        
        val projectile = createProjectile(
            target,
            gfx = 249,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        val hitDelay = RangedCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        this.world.queue {
            wait(hitDelay - 1)
            
            if (RangedCombatFormula.getAccuracy(this@rangedAttack, target) >= this@rangedAttack.world.randomDouble()) {
                val maxHit = RangedCombatFormula.getMaxHit(this@rangedAttack, target)
                val damage = minOf(this@rangedAttack.world.random(maxHit + 1), RANGED_MAX_HIT)
                target.hit(damage, type = HitType.HIT)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }
    
    /**
     * Spawn minion for current phase
     */
    private fun spawnPhaseMinion(boss: Npc, phase: NexPhase) {
        val bossTile = boss.tile
        val minionName = when (phase) {
            NexPhase.SMOKE -> "npc.fumus"
            NexPhase.SHADOW -> "npc.umbra"
            NexPhase.BLOOD -> "npc.cruor"
            NexPhase.ICE -> "npc.glacies"
            NexPhase.ZAROS -> return // No minion in Zaros phase
        }
        
        try {
            val spawnTile = bossTile.transform(2, 0) // Spawn east of Nex
            val minion = Npc(getRSCM(minionName), spawnTile, world)
            minion.respawns = false
            minion.walkRadius = 5
            minion.setActive(true)
            
            // Set combat class based on phase
            when (phase) {
                NexPhase.SMOKE -> minion.combatClass = CombatClass.MAGIC
                NexPhase.SHADOW -> minion.combatClass = CombatClass.RANGED
                NexPhase.BLOOD -> minion.combatClass = CombatClass.MELEE
                NexPhase.ICE -> minion.combatClass = CombatClass.MAGIC
                NexPhase.ZAROS -> {}
            }
            
            world.spawn(minion)
            boss.attr[MINION_ATTR] = WeakReference(minion)
            
            // Make minion aggressive
            minion.aggroCheck = { _, _ -> true }
        } catch (e: Exception) {
            println("Error spawning Nex minion: ${e.message}")
        }
    }
    
    /**
     * Progress to next phase when minion dies
     */
    private fun progressToNextPhase(boss: Npc) {
        val currentPhase = boss.attr[PHASE_ATTR] ?: NexPhase.SMOKE
        val nextPhase = when (currentPhase) {
            NexPhase.SMOKE -> NexPhase.SHADOW
            NexPhase.SHADOW -> NexPhase.BLOOD
            NexPhase.BLOOD -> NexPhase.ICE
            NexPhase.ICE -> NexPhase.ZAROS
            NexPhase.ZAROS -> return // Already in final phase
        }
        
        boss.attr[PHASE_ATTR] = nextPhase
        boss.forceChat("Now, the power of ${nextPhase.name}!")
        
        // Spawn next phase minion (if not Zaros)
        if (nextPhase != NexPhase.ZAROS) {
            spawnPhaseMinion(boss, nextPhase)
        } else {
            boss.forceChat("Fumus, Umbra, Cruor, Glacies... I am Nex!")
        }
    }
}

