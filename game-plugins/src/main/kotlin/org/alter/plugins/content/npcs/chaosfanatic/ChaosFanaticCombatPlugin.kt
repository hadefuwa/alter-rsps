package org.alter.plugins.content.npcs.chaosfanatic

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
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.game.model.move.moveTo

class ChaosFanaticCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.chaos_fanatic") {
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
            target.message("Chaos Fanatic: Embrace the chaos of magic!")
        }

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                attackCount++
                
                // Special chaos attacks
                if (attackCount >= 4 && this.world.chance(1, 3)) {
                    chaosOrbAttack(target)
                    attackCount = 0
                } else if (attackCount >= 6 && this.world.chance(1, 4)) {
                    chaosStormAttack(target)
                    attackCount = 0
                } else {
                    // Regular chaos magic attacks
                    when (this.world.random(4)) {
                        0 -> chaosFireSpellAttack(target)
                        1 -> chaosIceSpellAttack(target)
                        2 -> chaosWindSpellAttack(target)
                        3 -> chaosEarthSpellAttack(target)
                        else -> chaosFireSpellAttack(target)
                    }
                }
                
                // Random chaos taunts
                if (attackCount >= 3 && this.world.chance(1, 5) && target is Player) {
                    when (this.world.random(5)) {
                        0 -> target.message("Chaos Fanatic: Feel the raw power of chaos!")
                        1 -> target.message("Chaos Fanatic: Magic bends to my chaotic will!")
                        2 -> target.message("Chaos Fanatic: Order is an illusion!")
                        3 -> target.message("Chaos Fanatic: Chaos magic flows through me!")
                        4 -> target.message("Chaos Fanatic: Your spells are nothing compared to chaos!")
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

    private fun Npc.chaosFireSpellAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos magic animation
        
        val projectile = createProjectile(
            target, 
            gfx = 34, // Fire spell projectile
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
            maxHit = 25,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 35, height = 0, delay = 1) // Fire hit graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosIceSpellAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos magic animation
        
        val projectile = createProjectile(
            target, 
            gfx = 368, // Ice spell projectile
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
            maxHit = 23,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 369, height = 0, delay = 1) // Ice hit graphic
                if (target is Player) {
                    target.message("Chaotic ice freezes your soul!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosWindSpellAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos magic animation
        
        val projectile = createProjectile(
            target, 
            gfx = 18, // Wind spell projectile
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
            maxHit = 21,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 19, height = 0, delay = 1) // Wind hit graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosEarthSpellAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos magic animation
        
        val projectile = createProjectile(
            target, 
            gfx = 42, // Earth spell projectile
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
            maxHit = 27,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 43, height = 0, delay = 1) // Earth hit graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }
    
    private suspend fun Npc.chaosOrbAttack(target: Pawn) {
        // Chaos orb attack - creates magical explosion
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos casting animation
        
        // Chaos orb projectile
        val projectile = createProjectile(
            target, 
            gfx = 28, // Chaos orb projectile
            startHeight = 43, 
            endHeight = 0, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        // Orb explodes after delay
        world.queue {
            wait(RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()))
            
            if (target is Player && target.getCurrentHp() > 0) {
                val targetTile = target.tile
                
                // Create chaos explosion graphic
                world.spawn(TileGraphic(id = 157, tile = targetTile, height = 0))
                target.message("Chaos Fanatic hurls a chaotic orb at you!")
                
                // Explosion damage
                target.hit(this@chaosOrbAttack.world.random(20) + 15, type = HitType.HIT, delay = 0)
                
                // Damage nearby players in 3x3 area
                for (x in -1..1) {
                    for (z in -1..1) {
                        val tile = targetTile.transform(x, z)
                        world.players.forEach { player ->
                            if (player.tile == tile && player.getCurrentHp() > 0 && player != target) {
                                player.graphic(id = 157, height = 0, delay = 0)
                                player.hit(this@chaosOrbAttack.world.random(15) + 8, type = HitType.HIT, delay = 0)
                                player.message("You are caught in the chaotic explosion!")
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.chaosStormAttack(target: Pawn) {
        // Chaos storm attack - area of effect magic
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3337) // Chaos casting animation
        
        if (target is Player) {
            target.message("Chaos Fanatic summons a storm of pure chaotic energy!")
        }
        
        val stormCenter = target.tile
        
        // Create storm graphics in 5x5 area
        for (x in -2..2) {
            for (z in -2..2) {
                val tile = stormCenter.transform(x, z)
                world.spawn(TileGraphic(id = 157, tile = tile, height = 0, delay = 15 + (x + z) * 3))
            }
        }
        
        // Multiple waves of chaotic damage
        repeat(3) { wave ->
            world.queue {
                wait(wave * 3 + 4)
                
                // Damage all players in storm area
                world.players.forEach { player ->
                    if (player.tile.getDistance(stormCenter) <= 2 && player.getCurrentHp() > 0) {
                        player.hit(
                            this@chaosStormAttack.world.random(14) + 10, 
                            type = HitType.HIT, 
                            delay = 0
                        )
                        player.graphic(id = 157, height = 0, delay = 0)
                        
                        if (wave == 0) {
                            player.message("You are engulfed by the chaos storm!")
                        }
                    }
                }
            }
        }
    }
}