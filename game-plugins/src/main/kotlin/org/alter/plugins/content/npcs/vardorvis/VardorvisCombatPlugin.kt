package org.alter.plugins.content.npcs.vardorvis

import org.alter.api.*
import org.alter.api.cfg.Sound
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

/**
 * Vardorvis Combat Plugin - The Ancient Warrior Boss
 * 
 * Vardorvis is an ancient warrior with powerful combat abilities:
 * - Ancient Slash: Melee slash attack
 * - Dark Magic Bolt: Magic projectile attack
 * - Ground Slam: Area-of-effect ground attack
 * - Ranged Barrage: Multiple ranged projectiles
 * - Charge Attack: Powerful melee charge
 * 
 * Combat Level: 700+, Hitpoints: 1500
 * Location: Vardorvis's Lair
 */
class VardorvisCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Attribute keys
        private val LAST_SPECIAL_ATTR = AttributeKey<Int>("vardorvis_last_special_tick")
        private val ATTACK_COUNT_ATTR = AttributeKey<Int>("vardorvis_attack_count")
        
        // Special attack intervals
        private const val SPECIAL_ATTACK_INTERVAL = 6 // Every 6 ticks
    }

    init {
        onNpcCombat("npc.vardorvis") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    /**
     * Main combat loop for Vardorvis
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        // Initialize attack count if not set
        if (attr[ATTACK_COUNT_ATTR] == null) {
            attr[ATTACK_COUNT_ATTR] = 0
        }
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Move to attack range
            if (moveToAttackRange(it, target, distance = 7, projectile = true) && isAttackDelayReady()) {
                val currentTick = this.world.currentCycle
                val lastSpecial = attr[LAST_SPECIAL_ATTR] ?: 0
                val ticksSinceSpecial = currentTick - lastSpecial
                val attackCount = (attr[ATTACK_COUNT_ATTR] ?: 0) + 1
                attr[ATTACK_COUNT_ATTR] = attackCount
                
                // Use special attack if interval has passed or every 5-8 attacks
                val useSpecial = ticksSinceSpecial >= SPECIAL_ATTACK_INTERVAL || 
                                (attackCount >= 5 && this.world.chance(1, 3))
                
                if (useSpecial) {
                    // Choose a random special attack
                    when (this.world.random(4)) {
                        0 -> groundSlamAttack(target)
                        1 -> rangedBarrageAttack(target)
                        2 -> chargeAttack(target)
                        3 -> darkMagicBoltAttack(target)
                    }
                    attr[LAST_SPECIAL_ATTR] = currentTick
                } else {
                    // Regular attack - mix of melee and magic
                    when (this.world.random(3)) {
                        0 -> ancientSlashAttack(target)
                        1 -> darkMagicBoltAttack(target)
                        2 -> ancientSlashAttack(target) // More melee attacks
                    }
                }
                
                postAttackLogic(target)
            }
            
            it.wait(1)
            target = getCombatTarget() ?: break
        }
        
        resetFacePawn()
        removeCombatTarget()
        attr.remove(ATTACK_COUNT_ATTR)
        attr.remove(LAST_SPECIAL_ATTR)
    }

    /**
     * Regular melee slash attack
     */
    private fun Npc.ancientSlashAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        
        animate(422) // Attack animation
        
        // Play attack sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_CHAMPION_ATTACK, radius = 10, volume = 50))
        
        val maxHit = MeleeCombatFormula.getMaxHit(this, target)
        dealHit(
            target = target,
            maxHit = maxHit,
            landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed() && target is Player) {
                // Visual feedback
                target.graphic(100) // Hit graphic
            }
        }
    }

    /**
     * Dark magic bolt attack
     */
    private fun Npc.darkMagicBoltAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        
        animate(422) // Magic attack animation
        
        // Play magic sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_ATTACK, radius = 10, volume = 50))
        
        // Create projectile
        val projectile = createProjectile(
            target,
            gfx = 100, // Dark magic graphic
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 15,
            steepness = 127
        )
        world.spawn(projectile)
        
        val maxHit = MagicCombatFormula.getMaxHit(this, target)
        dealHit(
            target = target,
            maxHit = maxHit,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 3
        ) { hit ->
            if (hit.landed() && target is Player) {
                target.graphic(101) // Magic hit graphic
            }
        }
    }

    /**
     * Ground slam - area of effect attack
     */
    private fun Npc.groundSlamAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        
        forceChat("*Vardorvis slams the ground with immense force!*")
        animate(836) // Powerful slam animation
        
        // Play ground slam sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_CHAMPION_ATTACK, radius = 15, volume = 60))
        
        // Show ground effect
        world.spawn(TileGraphic(
            id = 86, // Ground impact graphic
            tile = this.tile,
            height = 0,
            delay = 0
        ))
        
        // Area damage to all nearby players
        world.queue {
            wait(2) // Delay before damage
            world.players.forEach { player ->
                if (player.tile.getDistance(this@groundSlamAttack.tile) <= 3 && player.isAlive()) {
                    val damage = this@groundSlamAttack.world.random(20..40)
                    player.hit(damage, type = HitType.HIT, delay = 0)
                    player.graphic(86) // Impact graphic on player
                    if (player == target) {
                        player.message("Vardorvis's ground slam sends shockwaves through you!")
                    }
                }
            }
        }
        
        // Also hit the main target
        val maxHit = MeleeCombatFormula.getMaxHit(this, target) + 10 // Bonus damage
        dealHit(
            target = target,
            maxHit = maxHit,
            landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 2
        )
    }

    /**
     * Ranged barrage - multiple projectiles
     */
    private fun Npc.rangedBarrageAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        
        forceChat("*Vardorvis unleashes a barrage of projectiles!*")
        animate(422)
        
        // Play ranged sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_ATTACK, radius = 12, volume = 55))
        
        // Launch 3 projectiles in quick succession
        repeat(3) { i ->
            world.queue {
                wait(i + 1) // Stagger the projectiles
                
                val projectile = createProjectile(
                    target,
                    gfx = 9, // Bone/projectile graphic
                    startHeight = 43,
                    endHeight = 31,
                    delay = 51,
                    angle = 15,
                    steepness = 127
                )
                world.spawn(projectile)
                
                val maxHit = RangedCombatFormula.getMaxHit(this@rangedBarrageAttack, target)
                dealHit(
                    target = target,
                    maxHit = maxHit,
                    landHit = RangedCombatFormula.getAccuracy(this@rangedBarrageAttack, target) >= world.randomDouble(),
                    delay = 3 + i
                ) { hit ->
                    if (hit.landed() && target is Player) {
                        target.graphic(9) // Hit graphic
                    }
                }
            }
        }
    }

    /**
     * Charge attack - powerful melee charge
     */
    private fun Npc.chargeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        
        forceChat("*Vardorvis charges forward with devastating force!*")
        animate(422)
        
        // Play charge sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_CHAMPION_ATTACK, radius = 12, volume = 60))
        
        // Show charge graphic
        graphic(100) // Charge effect
        
        // Powerful hit
        val maxHit = MeleeCombatFormula.getMaxHit(this, target) + 15 // Extra damage for charge
        dealHit(
            target = target,
            maxHit = maxHit,
            landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 2
        ) { hit ->
            if (hit.landed() && target is Player) {
                target.graphic(100) // Impact graphic
                target.message("Vardorvis's charge sends you reeling!")
            }
        }
    }
}

