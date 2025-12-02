package org.alter.plugins.content.npcs.scorpia

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
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.game.model.move.moveTo

class ScorpiaCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.scorpia") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        var attackCount = 0

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                attackCount++
                
                // Special scorpion attacks
                if (attackCount >= 4 && this.world.chance(1, 3)) {
                    venomBombAttack(target)
                    attackCount = 0
                } else if (attackCount >= 6 && this.world.chance(1, 4)) {
                    stingerBarrageAttack(target)
                    attackCount = 0
                } else if (attackCount >= 8 && this.world.chance(1, 5)) {
                    spawnOffspringAttack(target)
                    attackCount = 0
                } else {
                    // Regular scorpion attacks - mix of ranged and magic
                    when (this.world.random(3)) {
                        0 -> stingerAttack(target)     // Ranged poison stinger
                        1 -> venomSpitAttack(target)   // Magic venom spit
                        2 -> clawSwipeAttack(target)   // Melee claw attack
                        else -> stingerAttack(target)
                    }
                }
                
                // Random scorpion taunts during combat
                if (attackCount >= 3 && this.world.chance(1, 5) && target is Player) {
                    when (this.world.random(5)) {
                        0 -> target.message("Scorpia: Feel the sting of death!")
                        1 -> target.message("Scorpia: My venom will consume you!")
                        2 -> target.message("Scorpia: You dare enter my domain?!")
                        3 -> target.message("Scorpia: My children will feast on your bones!")
                        4 -> target.message("Scorpia: The desert claims another victim!")
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

    private fun Npc.stingerAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(6254) // Scorpia attack animation
        
        val projectile = createProjectile(
            target, 
            gfx = 663, // Scorpia's offspring ranged projectile
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
                target.graphic(id = 664, height = 0, delay = 1) // Poison hit graphic
                // High chance to poison with stinger
                if (this.world.chance(2, 3)) {
                    target.poison(initialDamage = 8) {
                        if (target is Player) {
                            target.message("You have been poisoned by Scorpia's stinger!")
                        }
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.venomSpitAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(6254) // Scorpia attack animation
        
        val projectile = createProjectile(
            target, 
            gfx = 165, // Green venom projectile
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
            maxHit = 26,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 289, height = 0, delay = 1) // Venom splash graphic
                // Chance to apply stronger poison
                if (this.world.chance(1, 3)) {
                    target.poison(initialDamage = 10) {
                        if (target is Player) {
                            target.message("Scorpia's venom burns through your veins!")
                        }
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.clawSwipeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(6255) // Scorpia claw attack animation
        
        dealHit(
            target = target,
            maxHit = 30,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1) // Slash graphic
                if (target is Player) {
                    target.message("Scorpia's claw tears into you!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }
    
    private suspend fun Npc.venomBombAttack(target: Pawn) {
        // Venom bomb attack - creates toxic area around target
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(6254) // Scorpia casting animation
        
        // Venom bomb projectile
        val projectile = createProjectile(
            target, 
            gfx = 165, // Venom projectile
            startHeight = 43, 
            endHeight = 0, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        // Bomb explodes after delay, creating toxic area
        world.queue {
            wait(RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()))
            
            if (target is Player && target.getCurrentHp() > 0) {
                val targetTile = target.tile
                
                // Create toxic pool graphic
                world.spawn(TileGraphic(id = 289, tile = targetTile, height = 0))
                target.message("Scorpia hurls a venom bomb at you!")
                
                // Initial explosion damage
                target.hit(this@venomBombAttack.world.random(18) + 12, type = HitType.HIT, delay = 0)
                
                // Poison anyone in 3x3 area around impact
                for (x in -1..1) {
                    for (z in -1..1) {
                        val tile = targetTile.transform(x, z)
                        world.players.forEach { player ->
                            if (player.tile == tile && player.getCurrentHp() > 0) {
                                player.graphic(id = 289, height = 0, delay = 0)
                                player.poison(initialDamage = 6) {
                                    player.message("You are caught in the toxic venom cloud!")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun Npc.stingerBarrageAttack(target: Pawn) {
        // Stinger barrage attack - multiple stingers in sequence
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(6254) // Scorpia attack animation
        
        if (target is Player) {
            target.message("Scorpia unleashes a barrage of venomous stingers!")
        }
        
        // Launch 5 stingers in sequence
        repeat(5) { i ->
            world.queue {
                wait(i + 1) // Staggered timing
                
                val projectile = createProjectile(
                    target, 
                    gfx = 663, // Stinger projectile
                    startHeight = 43, 
                    endHeight = 31, 
                    delay = 30, 
                    angle = 15, 
                    steepness = 127
                )
                
                world.spawn(projectile)
                
                // Each stinger has chance to hit and poison
                wait(2)
                if (target.getCurrentHp() > 0 && this@stingerBarrageAttack.world.chance(3, 4)) {
                    target.hit(this@stingerBarrageAttack.world.random(12) + 8, type = HitType.HIT, delay = 0)
                    
                    if (this@stingerBarrageAttack.world.chance(1, 3)) {
                        target.poison(initialDamage = 4) {
                            if (target is Player) {
                                target.message("A stinger finds its mark!")
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.spawnOffspringAttack(target: Pawn) {
        // Spawn offspring attack - summons scorpia minions (simplified for this implementation)
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(6259) // Scorpia spawn animation
        
        if (target is Player) {
            target.message("Scorpia calls upon her offspring to aid her!")
        }
        
        // Create spawn effect graphics around Scorpia
        val scorpiaTile = this.tile
        for (x in -1..1) {
            for (z in -1..1) {
                if (x == 0 && z == 0) continue // Skip Scorpia's tile
                val tile = scorpiaTile.transform(x, z)
                world.spawn(TileGraphic(id = 86, tile = tile, height = 0, delay = 20))
            }
        }
        
        // After delay, deal area damage representing offspring attacks
        world.queue {
            wait(3)
            
            // Damage players near Scorpia representing offspring attacks
            world.players.forEach { player ->
                if (player.tile.getDistance(scorpiaTile) <= 3 && player.getCurrentHp() > 0) {
                    player.hit(this@spawnOffspringAttack.world.random(15) + 10, type = HitType.HIT, delay = 0)
                    player.message("Scorpia's offspring swarm around you!")
                }
            }
        }
    }
}