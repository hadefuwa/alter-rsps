package org.alter.plugins.content.npcs.chaoselemental

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

class ChaosElementalCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.chaos_elemental") {
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
                
                // Special chaos attacks  
                if (attackCount >= 4 && this.world.chance(1, 3)) {
                    chaosUnequipAttack(target)
                    attackCount = 0
                } else if (attackCount >= 6 && this.world.chance(1, 4)) {
                    chaosPortalAttack(target)
                    attackCount = 0
                } else if (attackCount >= 8 && this.world.chance(1, 5)) {
                    chaosStormAttack(target)
                    attackCount = 0
                } else {
                    // Regular chaos attacks - mix of different elemental spells
                    when (this.world.random(4)) {
                        0 -> chaosBoltAttack(target)      // Magic chaos bolt
                        1 -> chaosWindAttack(target)      // Wind disruption
                        2 -> chaosFireAttack(target)      // Fire chaos
                        3 -> chaosIceAttack(target)       // Ice chaos
                        else -> chaosBoltAttack(target)
                    }
                }
                
                // Random chaos taunts during combat  
                if (attackCount >= 3 && this.world.chance(1, 5) && target is Player) {
                    when (this.world.random(6)) {
                        0 -> target.message("Chaos Elemental: Embrace the chaos!")
                        1 -> target.message("Chaos Elemental: Order shall crumble!")
                        2 -> target.message("Chaos Elemental: Your equipment betrays you!")
                        3 -> target.message("Chaos Elemental: Chaos reigns supreme!")
                        4 -> target.message("Chaos Elemental: Reality bends to my will!")
                        5 -> target.message("Chaos Elemental: Feel the power of disorder!")
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

    private fun Npc.chaosBoltAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos Elemental attack animation
        
        val projectile = createProjectile(
            target, 
            gfx = 130, // Chaos bolt projectile
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
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 131, height = 0, delay = 1) // Chaos hit graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosWindAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos Elemental attack animation
        
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
            maxHit = 24,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 19, height = 0, delay = 1) // Wind hit graphic
                if (target is Player) {
                    target.message("The chaotic winds disrupt your concentration!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosFireAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos Elemental attack animation
        
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
            maxHit = 26,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 35, height = 0, delay = 1) // Fire hit graphic
                if (target is Player) {
                    target.message("Chaotic flames sear your flesh!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.chaosIceAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos Elemental attack animation
        
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
            maxHit = 25,
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
    
    private suspend fun Npc.chaosUnequipAttack(target: Pawn) {
        // Chaos Elemental's signature attack - unequips player's items
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos casting animation
        
        // Chaos unequip projectile
        val projectile = createProjectile(
            target, 
            gfx = 130, // Chaos projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        world.queue {
            wait(delay)
            
            if (target is Player && target.getCurrentHp() > 0) {
                target.graphic(id = 131, height = 0, delay = 0) // Chaos graphic
                target.message("The Chaos Elemental's magic disrupts your equipment!")
                
                // Deal damage first
                target.hit(this@chaosUnequipAttack.world.random(20) + 15, type = HitType.HIT, delay = 0)
                
                // Then attempt to unequip items (simplified - just message for now)
                if (this@chaosUnequipAttack.world.chance(1, 2)) {
                    target.message("Your equipment feels unstable!")
                    // In a full implementation, this would actually unequip random items
                    // For now, we'll just provide the thematic experience
                }
            }
        }
    }
    
    private suspend fun Npc.chaosPortalAttack(target: Pawn) {
        // Portal attack - teleports target to random nearby location
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos casting animation
        
        if (target is Player) {
            target.message("The Chaos Elemental opens a portal beneath you!")
        }
        
        // Portal graphic at target location
        world.spawn(TileGraphic(id = 86, tile = target.tile, height = 0))
        
        world.queue {
            wait(2)
            
            if (target is Player && target.getCurrentHp() > 0) {
                // Deal moderate damage
                target.hit(this@chaosPortalAttack.world.random(18) + 12, type = HitType.HIT, delay = 0)
                
                // Teleport effect (simplified - just graphic and message)
                target.graphic(id = 86, height = 0, delay = 0)
                target.message("Chaotic energy warps space around you!")
                
                // In a full implementation, this could teleport the player to a nearby tile
                // For now, we'll provide the thematic experience without actual teleportation
            }
        }
    }

    private suspend fun Npc.chaosStormAttack(target: Pawn) {
        // Chaos storm attack - area of effect chaos magic
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3144) // Chaos casting animation
        
        if (target is Player) {
            target.message("The Chaos Elemental summons a storm of pure chaos!")
        }
        
        val stormCenter = target.tile
        
        // Create storm graphics in 5x5 area
        for (x in -2..2) {
            for (z in -2..2) {
                val tile = stormCenter.transform(x, z)
                world.spawn(TileGraphic(id = 131, tile = tile, height = 0, delay = 10 + (x + z) * 2))
            }
        }
        
        // Multiple waves of damage
        repeat(3) { wave ->
            world.queue {
                wait(wave * 2 + 3)
                
                // Damage all players in storm area
                world.players.forEach { player ->
                    if (player.tile.getDistance(stormCenter) <= 2 && player.getCurrentHp() > 0) {
                        player.hit(
                            this@chaosStormAttack.world.random(12) + 8, 
                            type = HitType.HIT, 
                            delay = 0
                        )
                        player.graphic(id = 131, height = 0, delay = 0)
                        
                        if (wave == 0) {
                            player.message("You are caught in the chaos storm!")
                        }
                    }
                }
            }
        }
    }
}