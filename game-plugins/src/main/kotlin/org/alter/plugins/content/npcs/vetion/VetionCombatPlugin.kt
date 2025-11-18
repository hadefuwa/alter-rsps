package org.alter.plugins.content.npcs.vetion

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
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.game.model.move.moveTo

class VetionCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.vetion") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        var attackCount = 0

        // Opening taunt
        if (target is Player) {
            target.message("Vet'ion: You dare disturb the eternal rest of the dead!")
        }

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                attackCount++
                
                // Special skeletal attacks
                if (attackCount >= 4 && this.world.chance(1, 3)) {
                    summonHellhoundsAttack(target)
                    attackCount = 0
                } else if (attackCount >= 6 && this.world.chance(1, 4)) {
                    boneBarrageAttack(target)
                    attackCount = 0
                } else if (attackCount >= 8 && this.world.chance(1, 5)) {
                    earthShakeAttack(target)
                    attackCount = 0
                } else {
                    // Regular skeletal attacks - melee focused with magic variants
                    when (this.world.random(4)) {
                        0 -> skeletalClawAttack(target)   // Melee claw swipe
                        1 -> boneThrowAttack(target)      // Ranged bone projectile 
                        2 -> necromanticStrikeAttack(target) // Magic death strike
                        3 -> skeletalBiteAttack(target)   // Melee bite attack
                        else -> skeletalClawAttack(target)
                    }
                }
                
                // Random skeletal taunts during combat
                if (attackCount >= 3 && this.world.chance(1, 5) && target is Player) {
                    when (this.world.random(6)) {
                        0 -> target.message("Vet'ion: Death comes for all mortals!")
                        1 -> target.message("Vet'ion: Your bones will join my collection!")
                        2 -> target.message("Vet'ion: The skeletal army awakens!")
                        3 -> target.message("Vet'ion: Feel the power of undeath!")
                        4 -> target.message("Vet'ion: Your flesh will rot from your bones!")
                        5 -> target.message("Vet'ion: The bone yard claims another victim!")
                    }
                }
                
                postAttackLogic(target)
            }
            it.wait(1)
            target = getCombatTarget() ?: break
        }

        resetFacePawn()
        removeCombatTarget()
    }

    private fun Npc.skeletalClawAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(5485) // Vet'ion attack animation
        
        dealHit(
            target = target,
            maxHit = 32,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1) // Slash graphic
                if (target is Player) {
                    target.message("Vet'ion's skeletal claws tear into you!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.boneThrowAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(5485) // Vet'ion attack animation
        
        val projectile = createProjectile(
            target, 
            gfx = 9, // Bone projectile 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        dealHit(
            target = target,
            maxHit = 28,
            landHit = RangedCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 10, height = 0, delay = 1) // Bone hit graphic
                if (target is Player) {
                    target.message("A skeletal bone strikes you!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.necromanticStrikeAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(5485) // Vet'ion attack animation
        
        val projectile = createProjectile(
            target, 
            gfx = 100, // Dark magic projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        dealHit(
            target = target,
            maxHit = 30,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 101, height = 0, delay = 1) // Dark magic hit graphic
                if (target is Player) {
                    target.message("Necromantic energy courses through you!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.skeletalBiteAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(5485) // Vet'ion attack animation
        
        dealHit(
            target = target,
            maxHit = 29,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1) // Bite graphic
                if (target is Player) {
                    target.message("Vet'ion's skeletal jaws snap at you!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }
    
    private suspend fun Npc.summonHellhoundsAttack(target: Pawn) {
        // Summon skeletal hellhounds attack (simplified for this implementation)
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(5485) // Vet'ion summoning animation
        
        if (target is Player) {
            target.message("Vet'ion: Rise, my skeletal hounds!")
        }
        
        // Create summoning effects around Vet'ion
        val vetionTile = this.tile
        for (x in -2..2) {
            for (z in -2..2) {
                if (kotlin.math.abs(x) + kotlin.math.abs(z) == 2) { // Diamond pattern
                    val tile = vetionTile.transform(x, z)
                    world.spawn(TileGraphic(id = 86, tile = tile, height = 0, delay = 20))
                }
            }
        }
        
        // After delay, deal area damage representing hellhound attacks
        world.queue {
            wait(3)
            
            // Damage players near Vet'ion representing hellhound attacks
            world.players.forEach { player ->
                if (player.tile.getDistance(vetionTile) <= 4 && player.getCurrentHp() > 0) {
                    player.hit(this@summonHellhoundsAttack.world.random(18) + 12, type = HitType.HIT, delay = 0)
                    player.message("Skeletal hellhounds emerge and attack you!")
                }
            }
        }
    }
    
    private suspend fun Npc.boneBarrageAttack(target: Pawn) {
        // Bone barrage attack - multiple bone projectiles
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(5485) // Vet'ion attack animation
        
        if (target is Player) {
            target.message("Vet'ion unleashes a barrage of ancient bones!")
        }
        
        // Launch 4 bone projectiles in sequence
        repeat(4) { i ->
            world.queue {
                wait(i + 1) // Staggered timing
                
                val projectile = createProjectile(
                    target, 
                    gfx = 9, // Bone projectile
                    startHeight = 43, 
                    endHeight = 31, 
                    delay = 30, 
                    angle = 15, 
                    steepness = 127
                )
                
                world.spawn(projectile)
                
                // Each bone has chance to hit
                wait(2)
                if (target.getCurrentHp() > 0 && this@boneBarrageAttack.world.chance(3, 4)) {
                    target.hit(this@boneBarrageAttack.world.random(15) + 10, type = HitType.HIT, delay = 0)
                    target.graphic(id = 10, height = 0, delay = 0)
                    
                    if (i == 0 && target is Player) {
                        target.message("Ancient bones pummel you!")
                    }
                }
            }
        }
    }

    private suspend fun Npc.earthShakeAttack(target: Pawn) {
        // Earth shake attack - ground-based area damage
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(5485) // Vet'ion casting animation
        
        if (target is Player) {
            target.message("Vet'ion: The very earth trembles before me!")
        }
        
        val shakeCenter = this.tile
        
        // Create earthquake graphics in expanding circles
        for (radius in 1..3) {
            world.queue {
                wait(radius)
                
                for (x in -radius..radius) {
                    for (z in -radius..radius) {
                        if (kotlin.math.abs(x) + kotlin.math.abs(z) == radius) {
                            val tile = shakeCenter.transform(x, z)
                            world.spawn(TileGraphic(id = 99, tile = tile, height = 0, delay = 0))
                        }
                    }
                }
            }
        }
        
        // Deal damage to all players in area after earthquake buildup
        world.queue {
            wait(4)
            
            world.players.forEach { player ->
                if (player.tile.getDistance(shakeCenter) <= 3 && player.getCurrentHp() > 0) {
                    player.hit(this@earthShakeAttack.world.random(22) + 15, type = HitType.HIT, delay = 0)
                    player.message("The ground shakes violently beneath your feet!")
                }
            }
        }
    }
}