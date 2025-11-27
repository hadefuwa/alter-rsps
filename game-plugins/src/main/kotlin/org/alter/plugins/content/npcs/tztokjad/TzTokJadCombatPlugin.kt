package org.alter.plugins.content.npcs.tztokjad

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.api.PrayerIcon
import org.alter.game.model.attr.KILLER_ATTR
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference
import kotlin.random.Random

/**
 * TzTok-Jad Combat Plugin
 * 
 * TzTok-Jad alternates between Ranged and Magic attacks:
 * - Ranged Attack: Front legs raised (Animation 2652)
 * - Magic Attack: Back legs raised (Animation 2656)
 * 
 * Players must watch Jad's animations to know which prayer to use:
 * - Front legs up = Protect from Missiles (Ranged)
 * - Back legs up = Protect from Magic (Magic)
 */
class TzTokJadCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Animation IDs for Jad's attacks
         */
        private const val RANGED_ATTACK_ANIM = 2652  // Front legs raised
        private const val MAGIC_ATTACK_ANIM = 2656   // Back legs raised
        
        /**
         * Graphics for Jad's attacks
         */
        private const val RANGED_PROJECTILE_GFX = 451  // Ranged projectile
        private const val MAGIC_PROJECTILE_GFX = 157   // Magic fireball
        
        /**
         * Chance to use ranged vs magic (50/50 split)
         */
        private const val RANGED_CHANCE = 50
        
        /**
         * Damage range when player is not praying correctly
         */
        private const val MIN_DAMAGE = 50
        private const val MAX_DAMAGE = 110
    }
    
    init {
        /**
         * Handle Jad's combat - alternates between ranged and magic attacks
         */
        onNpcCombat("npc.tztokjad") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        /**
         * Restore Jad's health immediately when he kills a player
         */
        onPlayerPreDeath {
            val player = ctx as Player
            val killer = player.attr[KILLER_ATTR]?.get()
            
            // Check if the killer is Jad
            if (killer is Npc && killer.id == getRSCM("npc.tztokjad")) {
                // Restore Jad's health to full immediately
                val maxHp = killer.getMaxHp()
                killer.setCurrentHp(maxHp)
            }
        }
    }
    
    /**
     * Main combat loop for Jad
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Jad attacks from range, so use distance = 10 (or whatever range is appropriate)
            if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                // Randomly choose between ranged and magic attack (50/50)
                val useRanged = this.world.random(100) < RANGED_CHANCE
                
                if (useRanged) {
                    rangedAttack(target)
                } else {
                    magicAttack(target)
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
     * Ranged attack - Front legs raised
     */
    private fun Npc.rangedAttack(target: Pawn) {
        // Prepare ranged attack
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        
        // Animate with front legs raised (ranged animation)
        animate(RANGED_ATTACK_ANIM)
        
        // Create ranged projectile
        val projectile = createProjectile(
            target,
            gfx = RANGED_PROJECTILE_GFX,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay
        val hitDelay = RangedCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal damage after projectile hits
        this.world.queue {
            wait(hitDelay - 1)
            
            if (RangedCombatFormula.getAccuracy(this@rangedAttack, target) >= this@rangedAttack.world.randomDouble()) {
                // Check if player is praying correctly (Protect from Missiles)
                val damage = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
                    // Player is praying correctly - no damage
                    0
                } else {
                    // Player is not praying correctly - deal 50-110 damage
                    this@rangedAttack.world.random(MIN_DAMAGE..MAX_DAMAGE)
                }
                
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 451, height = 0) // Impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }
    
    /**
     * Magic attack - Back legs raised
     */
    private fun Npc.magicAttack(target: Pawn) {
        // Prepare magic attack
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_WAVE
        
        // Animate with back legs raised (magic animation)
        animate(MAGIC_ATTACK_ANIM)
        
        // Create magic projectile (fireball)
        val projectile = createProjectile(
            target,
            gfx = MAGIC_PROJECTILE_GFX,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay
        val hitDelay = MagicCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal damage after projectile hits
        this.world.queue {
            wait(hitDelay - 1)
            
            if (MagicCombatFormula.getAccuracy(this@magicAttack, target) >= this@magicAttack.world.randomDouble()) {
                // Check if player is praying correctly (Protect from Magic)
                val damage = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                    // Player is praying correctly - no damage
                    0
                } else {
                    // Player is not praying correctly - deal 50-110 damage
                    this@magicAttack.world.random(MIN_DAMAGE..MAX_DAMAGE)
                }
                
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 157, height = 0) // Fire impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
        
        // Clear spell attribute after attack
        attr.remove(Combat.CASTING_SPELL)
    }
}

