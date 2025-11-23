package org.alter.plugins.content.npcs.kalphitequeen

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.AttributeKey
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
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.rscm.RSCM.getRSCM

/**
 * Kalphite Queen Combat Plugin
 * 
 * Handles the Kalphite Queen's combat mechanics for both forms:
 * - First Form (Crawling): Uses melee and magic attacks
 * - Second Form (Flying): Uses ranged and magic attacks
 * - Special spike attack that can hit multiple targets
 * 
 * Note: Form transformation is handled by KalphiteQueenPhasePlugin
 */
class KalphiteQueenCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Handle Form 1 combat
        onNpcCombat("npc.kalphite_queen_963") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Handle Form 2 combat (using both RSCM name and direct ID check)
        // Try RSCM name first
        try {
            onNpcCombat("npc.kalphite_queen_964") {
                npc.queue {
                    npc.combat(this)
                }
            }
        } catch (e: Exception) {
            // Form 2 NPC not in RSCM, handle via ID check in combat method
        }
        
        // Also handle NPC ID 964 directly (in case it exists but isn't in RSCM)
        // We'll check for ID 964 in the combat method itself
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        var attackCount = 0

        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Determine current form by checking the form attribute
            // This works even if both forms use the same NPC ID
            val isForm2 = KalphiteQueenPhasePlugin.isForm2(this)
            val currentForm = isForm2
            
            // Check if target is in range (including same tile for melee)
            val isSameTile = this.tile.sameAs(target.tile)
            val targetDistance = this.tile.getDistance(target.tile)
            val attackRange = if (currentForm) 10 else 1
            
            // For crawling form, allow attacks when on same tile or adjacent
            // For flying form, use normal range check
            val inRange = if (!currentForm) {
                // Crawling form: can attack if same tile or adjacent (distance <= 1)
                isSameTile || targetDistance <= 1
            } else {
                // Flying form: use normal range check (but also allow same tile)
                isSameTile || targetDistance <= attackRange
            }
            
            // Try to move to attack range if not in range, or if in range proceed to attack
            val canAttack = if (inRange) {
                // Already in range - for same tile, we don't need raycast check
                if (isSameTile) {
                    true
                } else {
                    // Check line of sight for adjacent/ranged attacks
                    this.hasLineOfSightTo(target, projectile = true, maximumDistance = attackRange)
                }
            } else {
                moveToAttackRange(it, target, distance = attackRange, projectile = true)
            }
            
            if (canAttack && isAttackDelayReady()) {
                attackCount++
                
                // Special spike attack (can occur in both forms)
                if (attackCount >= 5 && this.world.chance(1, 4)) {
                    spikeAttack(target)
                    attackCount = 0
                } else {
                    // Regular attacks based on form
                    if (currentForm) {
                        // Flying form: ranged and magic attacks
                        when (this.world.random(3)) {
                            0 -> rangedAttack(target)
                            1 -> magicAttack(target)
                            2 -> magicAttack(target) // Magic is more common in flying form
                            else -> rangedAttack(target)
                        }
                    } else {
                        // Crawling form: melee and magic attacks
                        // Can use melee if target is on same tile or adjacent
                        if (targetDistance <= 1 && this.world.chance(1, 2)) {
                            meleeAttack(target)
                        } else {
                            magicAttack(target)
                        }
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
     * Melee attack (crawling form only)
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
     * Magic attack (both forms)
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
     * Ranged attack (flying form only)
     */
    private fun Npc.rangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(6245) // Kalphite Queen ranged attack
        
        val projectile = createProjectile(
            target, 
            gfx = 473, // Ranged projectile
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
            maxHit = 31,
            landHit = RangedCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 474, height = 0, delay = 1) // Ranged hit graphic
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

