package org.alter.plugins.content.npcs.dragons

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
import org.alter.plugins.content.combat.formula.DragonfireFormula
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy

class DragonCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // All basic dragons
        onNpcCombat("npc.green_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_261") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_262") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_263") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_264") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_8073") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_8076") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.green_dragon_8082") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.blue_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_266") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_267") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_268") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_269") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_8074") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_8077") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.blue_dragon_8083") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.red_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_248") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_249") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_250") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_251") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_8075") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_8078") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.red_dragon_8079") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.black_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_253") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_254") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_255") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_256") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_257") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_258") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_259") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_8084") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.black_dragon_8085") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.bronze_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.bronze_dragon_271") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.iron_dragon") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.iron_dragon_273") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.iron_dragon_8080") { npc.queue { npc.dragonCombat(this) } }
        
        onNpcCombat("npc.steel_dragon_274") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.steel_dragon_275") { npc.queue { npc.dragonCombat(this) } }
        onNpcCombat("npc.steel_dragon_8086") { npc.queue { npc.dragonCombat(this) } }
        
        // Brutal dragons
        onNpcCombat("npc.brutal_green_dragon_8081") { npc.queue { npc.brutalDragonCombat(this) } }
        onNpcCombat("npc.brutal_red_dragon_8087") { npc.queue { npc.brutalDragonCombat(this) } }
        onNpcCombat("npc.brutal_black_dragon_8092") { npc.queue { npc.brutalDragonCombat(this) } }
        onNpcCombat("npc.brutal_black_dragon_8093") { npc.queue { npc.brutalDragonCombat(this) } }
        
        // Baby dragons - they use melee only (no fire breath)
        onNpcCombat("npc.baby_blue_dragon") { npc.queue { npc.babyDragonCombat(this) } }
        onNpcCombat("npc.baby_blue_dragon_242") { npc.queue { npc.babyDragonCombat(this) } }
        onNpcCombat("npc.baby_blue_dragon_243") { npc.queue { npc.babyDragonCombat(this) } }
        onNpcCombat("npc.baby_red_dragon_244") { npc.queue { npc.babyDragonCombat(this) } }
        onNpcCombat("npc.baby_red_dragon_245") { npc.queue { npc.babyDragonCombat(this) } }
        onNpcCombat("npc.baby_red_dragon_246") { npc.queue { npc.babyDragonCombat(this) } }
    }

    /**
     * Regular dragon combat - mix of melee and fire breath
     */
    private suspend fun Npc.dragonCombat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 2, projectile = true) && isAttackDelayReady()) {
                // 50% chance for fire breath, 50% for melee
                if (this.world.chance(1, 2)) {
                    this.dragonFireBreath(target)
                } else {
                    this.dragonMeleeAttack(target)
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
     * Brutal dragon combat - more aggressive with multiple attack types
     */
    private suspend fun Npc.brutalDragonCombat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 3, projectile = true) && isAttackDelayReady()) {
                // 60% chance for fire breath, 40% for melee (more aggressive)
                if (this.world.chance(3, 5)) {
                    this.dragonFireBreath(target, brutal = true)
                } else {
                    this.dragonMeleeAttack(target)
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
     * Baby dragon combat - melee only
     */
    private suspend fun Npc.babyDragonCombat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                this.dragonMeleeAttack(target)
                postAttackLogic(target)
            }
            it.wait(1)
            target = getCombatTarget() ?: break
        }

        resetFacePawn()
        removeCombatTarget()
    }

    /**
     * Dragon melee attack
     */
    private fun Npc.dragonMeleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(combatDef.attackAnimation) // Use default attack animation from combat def
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            target.hit(this.world.random(maxHit), type = HitType.HIT, delay = 1)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }

    /**
     * Dragon fire breath attack
     */
    private fun Npc.dragonFireBreath(target: Pawn, brutal: Boolean = false) {
        // Create fire projectile
        val projectile = createProjectile(
            target, 
            gfx = 393, // Fire breath projectile graphic
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(81) // Dragon fire breath animation
        this.world.spawn(projectile)
        
        // Calculate damage based on dragon type - much higher damage without protection
        val maxHit = when {
            brutal -> 90 // Brutal dragons hit very hard
            else -> when (combatDef.hitpoints) {
                in 1..50 -> 30   // Baby/weak dragons
                in 51..100 -> 50  // Green/Blue dragons  
                in 101..150 -> 65 // Red/Black dragons
                else -> 75        // Strong dragons (Bronze/Iron/Steel)
            }
        }
        
        dealHit(
            target = target,
            formula = DragonfireFormula(maxHit = maxHit, minHit = 8),
            delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1,
        )
    }
}