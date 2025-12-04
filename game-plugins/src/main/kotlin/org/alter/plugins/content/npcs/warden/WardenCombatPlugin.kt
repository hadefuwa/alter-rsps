package org.alter.plugins.content.npcs.warden

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.combat.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.api.ProjectileType
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.api.cfg.Sound
import org.alter.game.model.entity.AreaSound

/**
 * Warden Combat Plugin
 * 
 * The Warden switches protection prayers every 50 damage taken.
 * It uses TWO prayers at a time. When the Warden is praying a protection prayer,
 * it CANNOT attack with that attack type.
 * 
 * Example: If Warden has "Protect from Magic" active, it cannot use Magic attacks.
 * If it has "Protect from Melee" active, it cannot use Melee attacks.
 * If it has "Protect from Missiles" active, it cannot use Ranged attacks.
 * 
 * Since the Warden has 2 prayers active, it is blocked from 2 attack types,
 * leaving only 1 attack type available.
 */
class WardenCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private val DAMAGE_TRACKER_ATTR = AttributeKey<Int>()
        private val LAST_HP_ATTR = AttributeKey<Int>()
        private val PRAYER_INDEX_ATTR = AttributeKey<Int>()  // First prayer (0=Magic, 1=Missiles, 2=Melee)
        private val PRAYER_INDEX_2_ATTR = AttributeKey<Int>()  // Second prayer (0=Magic, 1=Missiles, 2=Melee)
        private val COMBAT_ACTIVE_ATTR = AttributeKey<Boolean>()  // Prevent multiple combat loops
        private const val DAMAGE_THRESHOLD = 50
    }

    init {
        // Initialize attributes when warden spawns
        onNpcSpawn("npc.tumekens_warden_11756") {
            val npc = ctx as Npc
            npc.attr[DAMAGE_TRACKER_ATTR] = 0
            npc.attr[LAST_HP_ATTR] = npc.getMaxHp()
            npc.attr[PRAYER_INDEX_ATTR] = 2 // Start with Protect from Melee (first prayer) - Phase 0
            npc.attr[PRAYER_INDEX_2_ATTR] = 1 // Start with Protect from Missiles (second prayer) - Phase 0
            npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id // Display first prayer
        }

        onNpcCombat("npc.tumekens_warden_11756") {
            val npc = ctx as Npc
            val target = npc.getCombatTarget() ?: return@onNpcCombat
            
            // Prevent multiple combat loops from running simultaneously
            if (npc.attr[COMBAT_ACTIVE_ATTR] == true) {
                return@onNpcCombat
            }
            
            npc.attr[COMBAT_ACTIVE_ATTR] = true
            npc.queue {
                try {
                    wardenCombat(target)
                } finally {
                    npc.attr[COMBAT_ACTIVE_ATTR] = false
                }
            }
        }
    }

    private suspend fun QueueTask.wardenCombat(initialTarget: Pawn) {
        val npc = ctx as Npc
        var target = initialTarget
        
        // Initialize attributes if not already set
        if (!npc.attr.has(DAMAGE_TRACKER_ATTR)) {
            npc.attr[DAMAGE_TRACKER_ATTR] = 0
            npc.attr[LAST_HP_ATTR] = npc.getMaxHp()
            npc.attr[PRAYER_INDEX_ATTR] = 2 // First prayer: Melee - Phase 0
            npc.attr[PRAYER_INDEX_2_ATTR] = 1 // Second prayer: Missiles - Phase 0
            npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id // Display first prayer
        }
        
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Track damage taken
            val currentHp = npc.getCurrentHp()
            val lastHp = npc.attr[LAST_HP_ATTR] ?: npc.getMaxHp()
            val damageTaken = lastHp - currentHp
            
            if (damageTaken > 0) {
                // Update damage tracker
                val currentDamage = npc.attr[DAMAGE_TRACKER_ATTR] ?: 0
                val newDamage = currentDamage + damageTaken
                npc.attr[DAMAGE_TRACKER_ATTR] = newDamage
                npc.attr[LAST_HP_ATTR] = currentHp
                
                // Check if we've reached the damage threshold
                if (newDamage >= DAMAGE_THRESHOLD) {
                    switchPrayer(npc)
                    // Reset damage tracker (keep any overflow)
                    npc.attr[DAMAGE_TRACKER_ATTR] = newDamage - DAMAGE_THRESHOLD
                }
            } else {
                // Update last HP even if no damage was taken (in case of healing)
                npc.attr[LAST_HP_ATTR] = currentHp
            }
            
            // Determine which attack type is allowed based on active prayers
            // If Warden is praying a protection prayer, it CANNOT use that attack type
            val prayer1 = npc.attr[PRAYER_INDEX_ATTR] ?: 2
            val prayer2 = npc.attr[PRAYER_INDEX_2_ATTR] ?: 1
            
            // Check which attack types are blocked by prayers
            // 0 = Magic, 1 = Missiles (Ranged), 2 = Melee
            val hasMagicPrayer = prayer1 == 0 || prayer2 == 0
            val hasMissilesPrayer = prayer1 == 1 || prayer2 == 1
            val hasMeleePrayer = prayer1 == 2 || prayer2 == 2
            
            // Determine which attack type is allowed (the one that's NOT blocked)
            val attackType = when {
                !hasMagicPrayer -> 0 // Magic not blocked -> use Magic
                !hasMissilesPrayer -> 1 // Ranged not blocked -> use Ranged
                !hasMeleePrayer -> 2 // Melee not blocked -> use Melee
                else -> 0 // Default to Magic if all are blocked (shouldn't happen)
            }
            
            // Use different distances based on attack type
            val (distance, projectile) = when (attackType) {
                0 -> Pair(10, true)  // Magic: 10 tiles, with projectile
                1 -> Pair(10, true)  // Ranged: 10 tiles, with projectile
                2 -> Pair(2, false)  // Melee: 2 tiles, no projectile
                else -> Pair(10, true)
            }
            
            if (npc.moveToAttackRange(this, target, distance = distance, projectile = projectile) && npc.isAttackDelayReady()) {
                when (attackType) {
                    0 -> {
                        npc.wardenMagicAttack(target)
                    }
                    1 -> {
                        npc.wardenRangedAttack(target)
                    }
                    2 -> {
                        npc.wardenMeleeAttack(target)
                    }
                }
                // Set attack delay after attacking
                Combat.postAttack(npc, target)
            }
            
            wait(1)
            val newTarget = npc.getCombatTarget() ?: break
            target = newTarget
        }
        
        // When combat ends (player teleports away or leaves), restore full health
        npc.setCurrentHp(npc.getMaxHp())
        npc.attr[DAMAGE_TRACKER_ATTR] = 0
        npc.attr[LAST_HP_ATTR] = npc.getMaxHp()
        
        // Clear prayer icon when combat ends
        npc.prayerIcon = -1
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
    
    /**
     * Switches to the next protection prayer pair in the cycle
     * Cycles through: (Melee+Missiles) -> (Magic+Melee) -> (Missiles+Magic) -> (Melee+Missiles)...
     */
    private fun switchPrayer(npc: Npc) {
        val currentIndex = npc.attr[PRAYER_INDEX_ATTR] ?: 2
        val currentIndex2 = npc.attr[PRAYER_INDEX_2_ATTR] ?: 1
        
        // Calculate next prayer pair
        // Phase 0: (2,1) Melee + Missiles
        // Phase 1: (0,2) Magic + Melee
        // Phase 2: (1,0) Missiles + Magic
        // Cycle: (2,1) -> (0,2) -> (1,0) -> (2,1)...
        val (nextIndex, nextIndex2) = when {
            currentIndex == 2 && currentIndex2 == 1 -> Pair(0, 2) // Phase 0 -> Phase 1: (2,1) -> (0,2)
            currentIndex == 0 && currentIndex2 == 2 -> Pair(1, 0) // Phase 1 -> Phase 2: (0,2) -> (1,0)
            currentIndex == 1 && currentIndex2 == 0 -> Pair(2, 1) // Phase 2 -> Phase 0: (1,0) -> (2,1)
            // Handle reversed order
            currentIndex == 1 && currentIndex2 == 2 -> Pair(0, 2) // (1,2) -> (0,2)
            currentIndex == 2 && currentIndex2 == 0 -> Pair(1, 0) // (2,0) -> (1,0)
            currentIndex == 0 && currentIndex2 == 1 -> Pair(2, 1) // (0,1) -> (2,1)
            else -> Pair(2, 1) // Default to Phase 0
        }
        
        npc.attr[PRAYER_INDEX_ATTR] = nextIndex
        npc.attr[PRAYER_INDEX_2_ATTR] = nextIndex2
        
        // Play prayer switch sound
        npc.world.spawn(AreaSound(npc.tile, id = Sound.ALTAR_PRAY, radius = 10, volume = 5))
        
        // Display the first prayer icon and announce both prayers
        val prayer1Name = when (nextIndex) {
            0 -> "Protect from Magic"
            1 -> "Protect from Missiles"
            2 -> "Protect from Melee"
            else -> "Unknown"
        }
        val prayer2Name = when (nextIndex2) {
            0 -> "Protect from Magic"
            1 -> "Protect from Missiles"
            2 -> "Protect from Melee"
            else -> "Unknown"
        }
        
        // Set the displayed prayer icon to the first prayer
        npc.prayerIcon = when (nextIndex) {
            0 -> PrayerIcon.PROTECT_FROM_MAGIC.id
            1 -> PrayerIcon.PROTECT_FROM_MISSILES.id
            2 -> PrayerIcon.PROTECT_FROM_MELEE.id
            else -> -1
        }
        
        npc.forceChat("The Warden switches to $prayer1Name and $prayer2Name!")
    }
    
    /**
     * Standard melee attack
     */
    private fun Npc.wardenMeleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(422) // Melee attack animation
        
        // Play attack sound
        val combatDef = this.combatDef
        if (combatDef.defaultAttackSoundArea) {
            world.spawn(AreaSound(this.tile, combatDef.defaultAttackSound, combatDef.defaultAttackSoundRadius, combatDef.defaultAttackSoundVolume))
        } else if (target is Player) {
            target.playSound(combatDef.defaultAttackSound, combatDef.defaultAttackSoundVolume)
        }
        
        // Use standard combat formula
        val accuracy = MeleeCombatFormula.getAccuracy(this, target)
        val maxHit = MeleeCombatFormula.getMaxHit(this, target)
        
        if (accuracy >= this.world.randomDouble()) {
            val damage = this.world.random(maxHit + 1)
            target.hit(damage, type = HitType.HIT, delay = 1)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Standard magic attack
     */
    private fun Npc.wardenMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_BLAST
        animate(422) // Magic attack animation
        
        // Play attack sound
        val combatDef = this.combatDef
        if (combatDef.defaultAttackSoundArea) {
            world.spawn(AreaSound(this.tile, combatDef.defaultAttackSound, combatDef.defaultAttackSoundRadius, combatDef.defaultAttackSoundVolume))
        } else if (target is Player) {
            target.playSound(combatDef.defaultAttackSound, combatDef.defaultAttackSoundVolume)
        }
        
        // Create projectile for magic attack
        val projectile = createProjectile(target, gfx = 1465, startHeight = 43, endHeight = 31, delay = 51, angle = 16, steepness = 64)
        world.spawn(projectile)
        
        // Calculate hit delay based on distance
        val hitDelay = RangedCombatStrategy.getHitDelay(this.getFrontFacingTile(target), target.getCentreTile())
        
        // Use standard combat formula
        val accuracy = MagicCombatFormula.getAccuracy(this, target)
        val maxHit = MagicCombatFormula.getMaxHit(this, target)
        
        if (accuracy >= this.world.randomDouble()) {
            val damage = this.world.random(maxHit + 1)
            target.hit(damage, type = HitType.HIT, delay = hitDelay)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = hitDelay)
        }
        
        attr.remove(Combat.CASTING_SPELL)
    }
    
    /**
     * Standard ranged attack
     */
    private fun Npc.wardenRangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(426) // Ranged attack animation
        
        // Play attack sound
        val combatDef = this.combatDef
        if (combatDef.defaultAttackSoundArea) {
            world.spawn(AreaSound(this.tile, combatDef.defaultAttackSound, combatDef.defaultAttackSoundRadius, combatDef.defaultAttackSoundVolume))
        } else if (target is Player) {
            target.playSound(combatDef.defaultAttackSound, combatDef.defaultAttackSoundVolume)
        }
        
        // Create projectile for ranged attack
        val projectile = createProjectile(target, gfx = 15, startHeight = 40, endHeight = 36, delay = 41, angle = 15, steepness = 11)
        world.spawn(projectile)
        
        // Calculate hit delay based on distance
        val hitDelay = RangedCombatStrategy.getHitDelay(this.getFrontFacingTile(target), target.getCentreTile())
        
        // Use standard combat formula
        val accuracy = RangedCombatFormula.getAccuracy(this, target)
        val maxHit = RangedCombatFormula.getMaxHit(this, target)
        
        if (accuracy >= this.world.randomDouble()) {
            val damage = this.world.random(maxHit + 1)
            target.hit(damage, type = HitType.HIT, delay = hitDelay)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = hitDelay)
        }
    }
}
