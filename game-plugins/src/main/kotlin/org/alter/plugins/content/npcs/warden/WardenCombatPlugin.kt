package org.alter.plugins.content.npcs.warden

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.combat.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.mechanics.prayer.PrayerIcon

/**
 * Warden Combat Plugin
 * 
 * The Warden switches protection prayers every 50 damage taken.
 * It cycles through: Protect from Magic -> Protect from Missiles -> Protect from Melee
 * 
 * Attack pattern (repeats continuously):
 * - 2 magic attacks (10 tile range)
 * - 2 ranged attacks (10 tile range)
 * - 2 melee attacks (2 tile range, only if adjacent to player)
 * - Then repeats from magic
 */
class WardenCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private val DAMAGE_TRACKER_ATTR = AttributeKey<Int>()
        private val LAST_HP_ATTR = AttributeKey<Int>()
        private val PRAYER_INDEX_ATTR = AttributeKey<Int>()
        private val ATTACK_COUNT_ATTR = AttributeKey<Int>()
        private val ATTACK_PHASE_ATTR = AttributeKey<Int>()
        private const val DAMAGE_THRESHOLD = 50
        private const val MELEE_MAX_HIT = 60
        private const val MAGIC_MAX_HIT = 55
        private const val RANGED_MAX_HIT = 57
        private const val MAGIC_ATTACKS = 2
        private const val RANGED_ATTACKS = 2
        private const val MELEE_ATTACKS = 2
    }

    init {
        // Initialize attributes when warden spawns
        onNpcSpawn("npc.tumekens_warden_11756") {
            val npc = ctx as Npc
            npc.attr[DAMAGE_TRACKER_ATTR] = 0
            npc.attr[LAST_HP_ATTR] = npc.getMaxHp()
            npc.attr[PRAYER_INDEX_ATTR] = 0 // Start with Protect from Magic
            npc.attr[ATTACK_COUNT_ATTR] = 0
            npc.attr[ATTACK_PHASE_ATTR] = 0 // 0 = Magic, 1 = Ranged, 2 = Melee
            npc.prayerIcon = PrayerIcon.PROTECT_FROM_MAGIC.id
        }

        onNpcCombat("npc.tumekens_warden_11756") {
            val npc = ctx as Npc
            val target = npc.getCombatTarget() ?: return@onNpcCombat
            
            npc.queue {
                wardenCombat(target)
            }
        }
    }

    private suspend fun QueueTask.wardenCombat(initialTarget: Pawn) {
        val npc = ctx as Npc
        var target = initialTarget
        
        // Initialize attributes if not already set
        if (!npc.attr.has(DAMAGE_TRACKER_ATTR)) {
            npc.attr[DAMAGE_TRACKER_ATTR] = 0
            npc.attr[LAST_HP_ATTR] = npc.getMaxHp()
            npc.attr[PRAYER_INDEX_ATTR] = 0
            npc.attr[ATTACK_COUNT_ATTR] = 0
            npc.attr[ATTACK_PHASE_ATTR] = 0
            npc.prayerIcon = PrayerIcon.PROTECT_FROM_MAGIC.id
        }
        
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Track damage taken
            val currentHp = npc.getCurrentHp()
            val lastHp = npc.attr[LAST_HP_ATTR] ?: npc.getMaxHp()
            val damageTaken = lastHp - currentHp
            
            if (damageTaken > 0) {
                // Update damage tracker
                val currentDamage = npc.attr[DAMAGE_TRACKER_ATTR] ?: 0
                val newDamage = currentDamage + damageTaken
                npc.attr[DAMAGE_TRACKER_ATTR] = newDamage
                npc.attr[LAST_HP_ATTR] = currentHp
                
                // Check if we've reached the damage threshold
                if (newDamage >= DAMAGE_THRESHOLD) {
                    switchPrayer(npc)
                    // Reset damage tracker (keep any overflow)
                    npc.attr[DAMAGE_TRACKER_ATTR] = newDamage - DAMAGE_THRESHOLD
                }
            } else {
                // Update last HP even if no damage was taken (in case of healing)
                npc.attr[LAST_HP_ATTR] = currentHp
            }
            
            // Perform combat with attack pattern: 2 magic -> 2 ranged -> 2 melee (if adjacent)
            var attackPhase = npc.attr[ATTACK_PHASE_ATTR] ?: 0
            var attackCount = npc.attr[ATTACK_COUNT_ATTR] ?: 0
            
            // Determine which attack type to use based on phase
            val attackType = when (attackPhase) {
                0 -> { // Magic phase
                    if (attackCount >= MAGIC_ATTACKS) {
                        // Move to ranged phase
                        attackPhase = 1
                        attackCount = 0
                        npc.attr[ATTACK_PHASE_ATTR] = attackPhase
                        npc.attr[ATTACK_COUNT_ATTR] = attackCount
                        1 // Ranged
                    } else {
                        0 // Magic
                    }
                }
                1 -> { // Ranged phase
                    if (attackCount >= RANGED_ATTACKS) {
                        // Check if we can do melee (if adjacent)
                        val distanceToTarget = npc.tile.getDistance(target.tile)
                        if (distanceToTarget <= 2) {
                            // Move to melee phase
                            attackPhase = 2
                            attackCount = 0
                            npc.attr[ATTACK_PHASE_ATTR] = attackPhase
                            npc.attr[ATTACK_COUNT_ATTR] = attackCount
                            2 // Melee
                        } else {
                            // Not adjacent, restart with magic
                            attackPhase = 0
                            attackCount = 0
                            npc.attr[ATTACK_PHASE_ATTR] = attackPhase
                            npc.attr[ATTACK_COUNT_ATTR] = attackCount
                            0 // Magic
                        }
                    } else {
                        1 // Ranged
                    }
                }
                2 -> { // Melee phase
                    if (attackCount >= MELEE_ATTACKS) {
                        // Restart cycle with magic (repeats)
                        attackPhase = 0
                        attackCount = 0
                        npc.attr[ATTACK_PHASE_ATTR] = attackPhase
                        npc.attr[ATTACK_COUNT_ATTR] = attackCount
                        0 // Magic - cycle repeats
                    } else {
                        // Check if still in melee range
                        val distanceToTarget = npc.tile.getDistance(target.tile)
                        if (distanceToTarget > 2) {
                            // Too far, restart with magic
                            attackPhase = 0
                            attackCount = 0
                            npc.attr[ATTACK_PHASE_ATTR] = attackPhase
                            npc.attr[ATTACK_COUNT_ATTR] = attackCount
                            0 // Magic
                        } else {
                            2 // Melee
                        }
                    }
                }
                else -> 0
            }
            
            // Use different distances based on attack type
            val (distance, projectile) = when (attackType) {
                0 -> Pair(10, true)  // Magic: 10 tiles, with projectile
                1 -> Pair(10, true)  // Ranged: 10 tiles, with projectile
                2 -> Pair(2, false)  // Melee: 2 tiles, no projectile
                else -> Pair(10, true)
            }
            
            if (npc.moveToAttackRange(this, target, distance = distance, projectile = projectile) && npc.isAttackDelayReady()) {
                when (attackType) {
                    0 -> {
                        npc.wardenMagicAttack(target)
                        attackCount++
                        npc.attr[ATTACK_COUNT_ATTR] = attackCount
                    }
                    1 -> {
                        npc.wardenRangedAttack(target)
                        attackCount++
                        npc.attr[ATTACK_COUNT_ATTR] = attackCount
                    }
                    2 -> {
                        npc.wardenMeleeAttack(target)
                        attackCount++
                        npc.attr[ATTACK_COUNT_ATTR] = attackCount
                    }
                }
            }
            
            wait(1)
            val newTarget = npc.getCombatTarget() ?: break
            target = newTarget
        }
        
        // Clear prayer icon when combat ends
        npc.prayerIcon = -1
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
    
    /**
     * Switches to the next protection prayer in the cycle
     */
    private fun switchPrayer(npc: Npc) {
        val currentIndex = npc.attr[PRAYER_INDEX_ATTR] ?: 0
        val nextIndex = (currentIndex + 1) % 3
        
        npc.attr[PRAYER_INDEX_ATTR] = nextIndex
        
        when (nextIndex) {
            0 -> {
                npc.prayerIcon = PrayerIcon.PROTECT_FROM_MAGIC.id
                npc.forceChat("The Warden switches to Protect from Magic!")
            }
            1 -> {
                npc.prayerIcon = PrayerIcon.PROTECT_FROM_MISSILES.id
                npc.forceChat("The Warden switches to Protect from Missiles!")
            }
            2 -> {
                npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id
                npc.forceChat("The Warden switches to Protect from Melee!")
            }
        }
    }
    
    /**
     * Custom melee attack with max hit of 60
     * If target has Protect from Melee prayer active, max hit is reduced to 3
     */
    private fun Npc.wardenMeleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(422) // Melee attack animation
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            // Check if target has Protect from Melee prayer active
            val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
                3  // Max hit reduced to 3 through protection prayer
            } else {
                MELEE_MAX_HIT
            }
            val damage = this.world.random(maxHit + 1)
            target.hit(damage, type = HitType.HIT, delay = 1)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Custom magic attack with max hit of 55
     * If target has Protect from Magic prayer active, max hit is reduced to 3
     */
    private fun Npc.wardenMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_BLAST
        animate(422) // Magic attack animation
        
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
        
        // Check if target has Protect from Magic prayer active
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
            3  // Max hit reduced to 3 through protection prayer
        } else {
            MAGIC_MAX_HIT
        }
        
        this.world.queue {
            wait(hitDelay - 1)
            
            if (MagicCombatFormula.getAccuracy(this@wardenMagicAttack, target) >= this@wardenMagicAttack.world.randomDouble()) {
                val damage = this@wardenMagicAttack.world.random(maxHit + 1)
                target.hit(damage, type = HitType.HIT)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
        
        attr.remove(Combat.CASTING_SPELL)
    }
    
    /**
     * Custom ranged attack with max hit of 57
     * If target has Protect from Missiles prayer active, max hit is reduced to 3
     */
    private fun Npc.wardenRangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(426) // Ranged attack animation
        
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
        
        // Check if target has Protect from Missiles prayer active
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
            3  // Max hit reduced to 3 through protection prayer
        } else {
            RANGED_MAX_HIT
        }
        
        this.world.queue {
            wait(hitDelay - 1)
            
            if (RangedCombatFormula.getAccuracy(this@wardenRangedAttack, target) >= this@wardenRangedAttack.world.randomDouble()) {
                val damage = this@wardenRangedAttack.world.random(maxHit + 1)
                target.hit(damage, type = HitType.HIT)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }
}
