package org.alter.plugins.content.npcs.kalphitequeen

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
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy

/**
 * Kalphite Queen Combat Plugin
 * 
 * Handles the Kalphite Queen's combat mechanics.
 * Modified to use a single combat pattern (Melee/Magic) throughout the fight.
 */
class KalphiteQueenCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Handle Combat
        onNpcCombat("npc.kalphite_queen_963") {
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
            
            val isSameTile = this.tile.sameAs(target.tile)
            val targetDistance = this.tile.getDistance(target.tile)
            
            // Always use "Crawling" form logic: attack if same tile or adjacent
            val inRange = isSameTile || targetDistance <= 1
            
            val canAttack = if (inRange) {
                if (isSameTile) {
                    true
                } else {
                    this.hasLineOfSightTo(target, projectile = true, maximumDistance = 1)
                }
            } else {
                moveToAttackRange(it, target, distance = 1, projectile = true)
            }
            
            if (canAttack && isAttackDelayReady()) {
                attackCount++
                
                // Special spike attack
                if (attackCount >= 5 && this.world.chance(1, 4)) {
                    spikeAttack(target)
                    attackCount = 0
                } else {
                    // Regular attacks: Melee and Magic
                    // Can use melee if target is on same tile or adjacent
                    if (targetDistance <= 1 && this.world.chance(1, 2)) {
                        meleeAttack(target)
                    } else {
                        magicAttack(target)
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


    /**
     * Melee attack
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(6240) // Kalphite Queen melee attack
        
        dealHit(
            target = target,
            maxHit = 31,
            landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1) // Slash graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    /**
     * Magic attack
     */
    private fun Npc.magicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(6244) // Kalphite Queen magic attack
        
        val projectile = createProjectile(
            target, 
            gfx = 280, // Magic projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = MagicCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        dealHit(
            target = target,
            maxHit = 31,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 281, height = 0, delay = 1) // Magic hit graphic
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    /**
     * Special spike attack - can hit multiple targets in area
     */
    private suspend fun Npc.spikeAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(6246) // Kalphite Queen spike attack animation
        
        if (target is Player) {
            target.message("The Kalphite Queen launches spikes from the ground!")
        }
        
        // Create spike graphics around target
        val targetTile = target.tile
        for (x in -1..1) {
            for (z in -1..1) {
                if (x == 0 && z == 0) continue // Skip target's tile
                val tile = targetTile.transform(x, z)
                world.spawn(TileGraphic(id = 278, tile = tile, height = 0, delay = 10))
            }
        }
        
        // Main spike hit on target
        world.queue {
            wait(2)
            
            if (target.getCurrentHp() > 0) {
                target.hit(this@spikeAttack.world.random(25) + 15, type = HitType.HIT, delay = 0)
                target.graphic(id = 279, height = 0, delay = 0) // Spike hit graphic
                
                if (target is Player) {
                    target.message("Spikes erupt from beneath you!")
                }
            }
            
            // Damage other players in 3x3 area
            wait(1)
            world.players.forEach { player ->
                if (player.tile.getDistance(targetTile) <= 1 && 
                    player != target && 
                    player.getCurrentHp() > 0) {
                    player.hit(this@spikeAttack.world.random(15) + 10, type = HitType.HIT, delay = 0)
                    player.graphic(id = 279, height = 0, delay = 0)
                    player.message("You are hit by the Kalphite Queen's spikes!")
                }
            }
        }
    }
}
