package org.alter.plugins.content.npcs.vardorvis

import org.alter.api.*
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.plugins.content.mechanics.prayer.Prayer
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.combat.PawnHit
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
 * - Ranged Barrage: Multiple ranged projectiles (with head pop-out effect)
 * - Charge Attack: Powerful melee charge
 * - Spike Attack: Melee stab attack that heals Vardorvis for damage dealt
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
                    when (this.world.random(5)) {
                        0 -> groundSlamAttack(target)
                        1 -> rangedBarrageAttack(target)
                        2 -> chargeAttack(target)
                        3 -> darkMagicBoltAttack(target)
                        4 -> spikeAttack(target)
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
        
        // Check if player has Protect from Melee
        val hasProtectMelee = target is Player && Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)
        val accuracy = MeleeCombatFormula.getAccuracy(this, target)
        val landHit = accuracy >= world.randomDouble()
        
        if (landHit) {
            if (hasProtectMelee) {
                // Player is praying melee - deal normal formula damage (reduced by prayer)
                val maxHit = MeleeCombatFormula.getMaxHit(this, target)
                dealHit(
                    target = target,
                    maxHit = maxHit,
                    landHit = true,
                    delay = 1
                ) { hit: PawnHit ->
                    if (target is Player) {
                        target.graphic(100) // Hit graphic
                        target.message("Your Protect from Melee reduces Vardorvis's damage!")
                    }
                }
            } else {
                // Player is NOT praying melee - guaranteed 30-45 damage
                val damage = world.random(30..45)
                target.hit(damage, type = HitType.HIT, delay = 1)
                if (target is Player) {
                    target.graphic(100) // Hit graphic
                    target.message("Vardorvis strikes you for $damage damage!")
                }
            }
        } else {
            // Miss
            target.hit(0, type = HitType.BLOCK, delay = 1)
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
        ) { hit: PawnHit ->
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
        // Check if player has Protect from Melee
        val hasProtectMelee = target is Player && Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)
        val accuracy = MeleeCombatFormula.getAccuracy(this, target)
        val landHit = accuracy >= world.randomDouble()
        
        if (landHit) {
            if (hasProtectMelee) {
                // Player is praying melee - deal normal formula damage (reduced by prayer)
                val maxHit = MeleeCombatFormula.getMaxHit(this, target) + 10 // Bonus damage
                dealHit(
                    target = target,
                    maxHit = maxHit,
                    landHit = true,
                    delay = 2
                ) { hit: PawnHit ->
                    if (target is Player) {
                        target.message("Your Protect from Melee reduces Vardorvis's ground slam damage!")
                    }
                }
            } else {
                // Player is NOT praying melee - guaranteed 30-45 damage
                val damage = world.random(30..45)
                target.hit(damage, type = HitType.HIT, delay = 2)
                if (target is Player) {
                    target.message("Vardorvis's ground slam crushes you for $damage damage!")
                }
            }
        } else {
            // Miss
            target.hit(0, type = HitType.BLOCK, delay = 2)
        }
    }

    /**
     * Ranged barrage - multiple projectiles
     */
    private fun Npc.rangedBarrageAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        
        forceChat("*Vardorvis unleashes a barrage of projectiles!*")
        animate(422)
        
        // Head pop out effect - show head graphic above Vardorvis
        // First show graphic on NPC
        graphic(100, height = 0, delay = 0) // Head pop out graphic on NPC
        world.queue {
            wait(1)
            // Show head pop out effect on tile above Vardorvis
            world.spawn(TileGraphic(
                id = 100, // Head graphic
                tile = this@rangedBarrageAttack.tile,
                height = 200, // Higher up to show head popping out
                delay = 0
            ))
            // Also show on adjacent tiles for more dramatic effect
            world.spawn(TileGraphic(
                id = 100,
                tile = this@rangedBarrageAttack.tile.transform(1, 0),
                height = 150,
                delay = 1
            ))
            world.spawn(TileGraphic(
                id = 100,
                tile = this@rangedBarrageAttack.tile.transform(-1, 0),
                height = 150,
                delay = 1
            ))
        }
        
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
                
                // Check if player has Protect from Missiles prayer active
                val hasProtectMissiles = target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)
                
                if (RangedCombatFormula.getAccuracy(this@rangedBarrageAttack, target) >= world.randomDouble()) {
                    val baseMaxHit = RangedCombatFormula.getMaxHit(this@rangedBarrageAttack, target)
                    
                    // Apply protection prayer reduction
                    val maxHit = if (hasProtectMissiles) {
                        // Reduce damage by 60% (40% gets through) when praying
                        (baseMaxHit * 0.4).toInt()
                    } else {
                        baseMaxHit
                    }
                    
                    val damage = world.random(maxHit + 1)
                    target.hit(damage, type = HitType.HIT, delay = 3 + i)
                    
                    if (target is Player) {
                        target.graphic(9) // Hit graphic
                        if (hasProtectMissiles && damage > 0) {
                            target.message("Your Protect from Missiles reduces Vardorvis's damage!")
                        } else if (hasProtectMissiles && damage == 0) {
                            target.message("Your Protect from Missiles completely blocks Vardorvis's attack!")
                        }
                    }
                } else {
                    target.hit(damage = 0, type = HitType.BLOCK, delay = 3 + i)
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
        
        // Check if player has Protect from Melee
        val hasProtectMelee = target is Player && Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)
        val accuracy = MeleeCombatFormula.getAccuracy(this, target)
        val landHit = accuracy >= world.randomDouble()
        
        if (landHit) {
            if (hasProtectMelee) {
                // Player is praying melee - deal normal formula damage (reduced by prayer)
                val maxHit = MeleeCombatFormula.getMaxHit(this, target) + 15 // Extra damage for charge
                dealHit(
                    target = target,
                    maxHit = maxHit,
                    landHit = true,
                    delay = 2
                ) { hit: PawnHit ->
                    if (target is Player) {
                        target.graphic(100) // Impact graphic
                        target.message("Your Protect from Melee reduces Vardorvis's charge damage!")
                    }
                }
            } else {
                // Player is NOT praying melee - guaranteed 30-45 damage
                val damage = world.random(30..45)
                target.hit(damage, type = HitType.HIT, delay = 2)
                if (target is Player) {
                    target.graphic(100) // Impact graphic
                    target.message("Vardorvis's charge sends you reeling for $damage damage!")
                }
            }
        } else {
            // Miss
            target.hit(0, type = HitType.BLOCK, delay = 2)
        }
    }

    /**
     * Spike attack - Vardorvis heals for damage dealt
     */
    private fun Npc.spikeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.AGGRESSIVE)
        
        forceChat("*Vardorvis lunges forward with deadly spikes!*")
        animate(422) // Attack animation
        
        // Play spike attack sound
        world.spawn(AreaSound(this.tile, Sound.DEMON_CHAMPION_ATTACK, radius = 12, volume = 60))
        
        // Show spike graphic effect
        graphic(100) // Spike effect on Vardorvis
        world.spawn(TileGraphic(
            id = 86, // Spike graphic on ground
            tile = target.tile,
            height = 0,
            delay = 1
        ))
        
        // Check if player has Protect from Melee
        val hasProtectMelee = target is Player && Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)
        val accuracy = MeleeCombatFormula.getAccuracy(this, target)
        val landHit = accuracy >= world.randomDouble()
        
        if (landHit) {
            if (hasProtectMelee) {
                // Player is praying melee - deal normal formula damage (reduced by prayer)
                val maxHit = MeleeCombatFormula.getMaxHit(this, target) + 20 // Extra damage for spike attack
                val damage = world.random(maxHit + 1) // 0 to maxHit
                
                if (damage > 0) {
                    target.hit(damage, type = HitType.HIT, delay = 2)
                    
                    // Heal Vardorvis after hit lands
                    world.queue {
                        wait(2) // Wait for hit to land
                        
                        val currentHp = this@spikeAttack.getCurrentHp()
                        val maxHp = this@spikeAttack.getMaxHp()
                        val healAmount = minOf(damage, maxHp - currentHp) // Don't heal beyond max HP
                        
                        if (healAmount > 0) {
                            this@spikeAttack.setCurrentHp(minOf(currentHp + healAmount, maxHp))
                            this@spikeAttack.graphic(436) // Healing graphic
                        }
                    }
                    
                    if (target is Player) {
                        target.graphic(100) // Impact graphic on player
                        target.message("Your Protect from Melee reduces Vardorvis's spike attack! He heals for $damage damage!")
                    }
                } else {
                    target.hit(0, type = HitType.BLOCK, delay = 2)
                    if (target is Player) {
                        target.message("Vardorvis's spike attack glances off!")
                    }
                }
            } else {
                // Player is NOT praying melee - guaranteed 30-45 damage
                val damage = world.random(30..45)
                target.hit(damage, type = HitType.HIT, delay = 2)
                
                // Heal Vardorvis after hit lands
                world.queue {
                    wait(2) // Wait for hit to land
                    
                    val currentHp = this@spikeAttack.getCurrentHp()
                    val maxHp = this@spikeAttack.getMaxHp()
                    val healAmount = minOf(damage, maxHp - currentHp) // Don't heal beyond max HP
                    
                    if (healAmount > 0) {
                        this@spikeAttack.setCurrentHp(minOf(currentHp + healAmount, maxHp))
                        
                        // Show healing graphic
                        this@spikeAttack.graphic(436) // Healing graphic
                    }
                }
                
                if (target is Player) {
                    target.graphic(100) // Impact graphic on player
                    target.message("Vardorvis's spike attack drains your life force for $damage damage! He heals for $damage damage!")
                }
            }
        } else {
            // Miss
            target.hit(0, type = HitType.BLOCK, delay = 2)
            if (target is Player) {
                target.message("Vardorvis's spike attack misses!")
            }
        }
    }
}

