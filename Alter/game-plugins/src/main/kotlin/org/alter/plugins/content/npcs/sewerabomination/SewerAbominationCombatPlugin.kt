package org.alter.plugins.content.npcs.sewerabomination

import org.alter.api.*
import org.alter.api.Skills
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
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.rscm.RSCM.getRSCM

/**
 * Sewer Abomination - A corrupted creature lurking in the Varrock sewers
 *
 * Attack patterns:
 * - Melee slam (25% chance) - Heavy melee damage with knockback effect
 * - Toxic spit (20% chance) - Ranged poison attack
 * - Sewer gas (15% chance) - Magic poison cloud
 * - Bite (15% chance) - Quick melee with guaranteed poison
 * - Acid wave (15% chance) - Multi-hit magic attack
 * - Plague breath (10% chance) - Draining attack that lowers stats
 *
 * Boss Mechanics:
 * - Spawns melee minion at 70% HP
 * - Spawns ranged minion at 35% HP
 * - Spawns mage minion at 10% HP
 * - Minions are aggressive and will attack players automatically
 */
class SewerAbominationCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Using Cerberus NPC model (5862) as the Sewer Abomination
        onNpcCombat("npc.cerberus") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        // Track minion spawns to avoid duplicates
        var meleeMinionSpawned = false
        var rangedMinionSpawned = false
        var mageMinionSpawned = false

        while (canEngageCombat(target)) {
            facePawn(target)

            // Check HP thresholds and spawn minions
            val hpPercent = (getCurrentHp().toDouble() / getMaxHp().toDouble()) * 100

            // Spawn melee minion at 70% HP
            if (!meleeMinionSpawned && hpPercent <= 70) {
                spawnMinion("melee")
                meleeMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted melee fighter!")
                }
            }

            // Spawn ranged minion at 35% HP
            if (!rangedMinionSpawned && hpPercent <= 35) {
                spawnMinion("ranged")
                rangedMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted archer!")
                }
            }

            // Spawn mage minion at 10% HP
            if (!mageMinionSpawned && hpPercent <= 10) {
                spawnMinion("mage")
                mageMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted sorcerer!")
                }
            }

            if (moveToAttackRange(it, target, distance = 4, projectile = true) && isAttackDelayReady()) {
                // Randomly select attack based on weighted chances
                val roll = this.world.random(100)
                when {
                    roll < 25 -> this.slamAttack(target)        // 25% - Heavy melee
                    roll < 45 -> this.toxicSpitAttack(target)   // 20% - Poison ranged
                    roll < 60 -> this.sewerGasAttack(target)    // 15% - Gas cloud
                    roll < 75 -> this.biteAttack(target)        // 15% - Quick poison bite
                    roll < 90 -> this.acidWaveAttack(target)    // 15% - Multi-hit magic
                    else -> this.plagueBreathAttack(target)     // 10% - Stat drain
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
     * Heavy melee slam attack - high damage melee with ground shake effect
     */
    private fun Npc.slamAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(426) // Giant/troll slam animation
        graphic(id = 157, height = 0) // Ground shake

        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            val damage = this.world.random(maxHit + 5)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0) // Impact graphic

            if (target is Player && damage > 10) {
                target.message("The abomination's slam shakes the ground!")
            }
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }

    /**
     * Toxic spit - ranged attack with poison
     */
    private fun Npc.toxicSpitAttack(target: Pawn) {
        val projectile = createProjectile(
            target,
            gfx = 288, // Poison projectile
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )

        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(422) // Ranged attack animation
        this.world.spawn(projectile)

        val hitDelay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())

        // Deal damage and potentially poison
        this.world.queue {
            wait(hitDelay - 1)

            if (MagicCombatFormula.getAccuracy(this@toxicSpitAttack, target) >= this@toxicSpitAttack.world.randomDouble()) {
                target.hit(this@toxicSpitAttack.world.random(18), type = HitType.HIT)
                target.graphic(id = 289, height = 0) // Poison splash

                // 50% chance to poison
                if (this@toxicSpitAttack.world.chance(1, 2)) {
                    target.poison(initialDamage = 6) {
                        if (target is Player) {
                            target.message("You have been poisoned by toxic spit!")
                        }
                    }
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Sewer gas - Magic poison attack with gas cloud effect
     */
    private fun Npc.sewerGasAttack(target: Pawn) {
        val projectile = createProjectile(
            target,
            gfx = 114, // Gas/poison cloud
            startHeight = 43,
            endHeight = 0,
            delay = 51,
            angle = 10,
            steepness = 11
        )

        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(1979) // Special animation
        this.world.spawn(projectile)

        val hitDelay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())

        // Deal damage and potentially poison
        this.world.queue {
            wait(hitDelay - 1)

            if (MagicCombatFormula.getAccuracy(this@sewerGasAttack, target) >= this@sewerGasAttack.world.randomDouble()) {
                target.hit(this@sewerGasAttack.world.random(15), type = HitType.HIT)

                // 75% chance to poison
                if (this@sewerGasAttack.world.chance(3, 4)) {
                    target.poison(initialDamage = 4) {
                        if (target is Player) {
                            target.message("The sewer gas poisons you!")
                        }
                    }
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Bite attack - quick melee with guaranteed poison
     */
    private fun Npc.biteAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.AGGRESSIVE)
        animate(423) // Bite/attack animation

        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            // Ensure damage is at least 1 to avoid negative/zero bounds
            val damage = maxOf(1, maxHit - 5)
            target.hit(this.world.random(damage), type = HitType.HIT, delay = 1)

            // Always poison on successful hit
            target.poison(initialDamage = 7) {
                if (target is Player) {
                    target.message("The abomination's bite poisons you!")
                }
            }
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }

    /**
     * Acid wave - Multi-hit magic attack that hits 2-3 times
     */
    private fun Npc.acidWaveAttack(target: Pawn) {
        val projectile = createProjectile(
            target,
            gfx = 165, // Green acid projectile
            startHeight = 43,
            endHeight = 31,
            delay = 41,
            angle = 16,
            steepness = 11
        )

        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.AGGRESSIVE)
        animate(1162) // Wave animation
        graphic(id = 166, height = 0) // Casting graphic
        this.world.spawn(projectile)

        val hitDelay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())

        // Deal multiple hits
        this.world.queue {
            wait(hitDelay - 1)

            if (MagicCombatFormula.getAccuracy(this@acidWaveAttack, target) >= this@acidWaveAttack.world.randomDouble()) {
                // First hit
                val hit1 = this@acidWaveAttack.world.random(12)
                target.hit(hit1, type = HitType.HIT)
                target.graphic(id = 167, height = 0) // Acid splash

                if (target is Player) {
                    target.message("Acid burns you!")
                }

                wait(2)

                // Second hit (50% chance)
                if (this@acidWaveAttack.world.chance(1, 2)) {
                    val hit2 = this@acidWaveAttack.world.random(10)
                    target.hit(hit2, type = HitType.HIT)
                    target.graphic(id = 167, height = 0)

                    wait(2)

                    // Third hit (25% chance)
                    if (this@acidWaveAttack.world.chance(1, 4)) {
                        val hit3 = this@acidWaveAttack.world.random(8)
                        target.hit(hit3, type = HitType.HIT)
                        target.graphic(id = 167, height = 0)
                    }
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Plague breath - Draining attack that temporarily lowers stats
     */
    private fun Npc.plagueBreathAttack(target: Pawn) {
        val projectile = createProjectile(
            target,
            gfx = 144, // Purple/dark cloud
            startHeight = 43,
            endHeight = 0,
            delay = 51,
            angle = 10,
            steepness = 11
        )

        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.DEFENSIVE)
        animate(811) // Dragon breath animation
        this.world.spawn(projectile)

        val hitDelay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())

        this.world.queue {
            wait(hitDelay - 1)

            if (MagicCombatFormula.getAccuracy(this@plagueBreathAttack, target) >= this@plagueBreathAttack.world.randomDouble()) {
                // Deal moderate damage
                val damage = this@plagueBreathAttack.world.random(16)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 145, height = 100) // Dark cloud splash

                // Drain stats for players
                if (target is Player) {
                    target.message("The plague breath weakens you!")

                    // Drain attack by 1-3 levels
                    val attackDrain = this@plagueBreathAttack.world.random(3) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.ATTACK, value = 0 - attackDrain, capValue = 0)

                    // Drain strength by 1-3 levels
                    val strengthDrain = this@plagueBreathAttack.world.random(3) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.STRENGTH, value = 0 - strengthDrain, capValue = 0)

                    // Drain defence by 1-3 levels
                    val defenceDrain = this@plagueBreathAttack.world.random(3) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.DEFENCE, value = 0 - defenceDrain, capValue = 0)
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Spawns a minion near the boss based on type
     * Minions are temporary and will not respawn after death
     */
    private fun Npc.spawnMinion(type: String) {
        // Visual effect at boss location
        this.graphic(id = 86, height = 100) // Purple spawn effect on boss

        val target = getCombatTarget() ?: return
        if (target !is Player) return

        // Determine which NPC to spawn based on type
        val npcName = when (type) {
            "melee" -> "npc.zombie"      // Melee fighter
            "ranged" -> "npc.archer"     // Ranged attacker
            "mage" -> "npc.dark_wizard"  // Magic user
            else -> return
        }

        // Find a spawn location near the boss (1-2 tiles away)
        val bossTile = this.tile
        val spawnOffsetX = world.random(-2..2) // -2 to 2
        val spawnOffsetZ = world.random(-2..2) // -2 to 2
        val spawnTile = bossTile.transform(spawnOffsetX, spawnOffsetZ)

        // Create and spawn the minion
        val minion = Npc(getRSCM(npcName), spawnTile, world)
        minion.respawns = false // Minions don't respawn - they're temporary
        minion.walkRadius = 0 // Minions stay in place
        minion.setActive(true)

        // Spawn the minion in the world
        world.spawn(minion)

        // Make the minion attack the player immediately
        minion.attack(target)

        // Show effect to player
        target.graphic(id = 86, height = 0)
    }
}
