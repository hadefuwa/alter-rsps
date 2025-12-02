package org.alter.plugins.content.npcs.kreearra

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
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy

/**
 * Kree'arra Combat Plugin
 * 
 * Kree'arra is a powerful ranged boss with devastating attacks:
 * - Default Attack: High-damage ranged attacks
 * - Whirlwind Attack: Devastating area-of-effect ranged attack that hits all nearby players
 * - Magic Attack: Can use magic attacks as well
 * 
 * This boss is MUCH harder than standard - beware!
 */
class KreeArraCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Animation IDs for Kree'arra's attacks
         */
        private const val RANGED_ATTACK_ANIM = 6977  // Kree'arra ranged attack animation
        private const val MAGIC_ATTACK_ANIM = 6976  // Kree'arra magic attack animation
        private const val WHIRLWIND_ATTACK_ANIM = 6977  // Same as ranged, but with special effects
        
        /**
         * Graphics for Kree'arra's attacks
         */
        private const val RANGED_PROJECTILE_GFX = 1176  // Arrow/projectile graphic
        private const val MAGIC_PROJECTILE_GFX = 157  // Magic projectile
        private const val WHIRLWIND_GFX = 1177  // Whirlwind effect graphic
        
        /**
         * Damage ranges - MUCH HIGHER than standard
         */
        private const val RANGED_MAX_HIT = 70  // Very high ranged damage
        private const val MAGIC_MAX_HIT = 50  // High magic damage
        private const val WHIRLWIND_DAMAGE_MIN = 30  // Minimum whirlwind damage
        private const val WHIRLWIND_DAMAGE_MAX = 60  // Maximum whirlwind damage
        
        /**
         * Attack chances
         */
        private const val WHIRLWIND_ATTACK_CHANCE = 30  // 30% chance for whirlwind attack
        private const val MAGIC_ATTACK_CHANCE = 20  // 20% chance for magic attack
        
        /**
         * Whirlwind attack radius
         */
        private const val WHIRLWIND_RADIUS = 5  // Hits all players within 5 tiles
    }
    
    init {
        /**
         * Handle Kree'arra's combat
         */
        onNpcCombat("npc.kreearra_3162") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Set combat class to RANGED when Kree'arra spawns
        onNpcSpawn("npc.kreearra_3162") {
            val npc = ctx as Npc
            npc.combatClass = CombatClass.RANGED
        }
    }
    
    /**
     * Main combat loop for Kree'arra
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Kree'arra attacks from range (distance 7-10)
            if (moveToAttackRange(it, target, distance = 7, projectile = true) && isAttackDelayReady()) {
                // Randomly choose attack type
                val roll = this.world.random(100)
                
                when {
                    roll < WHIRLWIND_ATTACK_CHANCE -> {
                        // Devastating whirlwind attack - area of effect
                        whirlwindAttack(target)
                    }
                    roll < (WHIRLWIND_ATTACK_CHANCE + MAGIC_ATTACK_CHANCE) -> {
                        // Magic attack
                        magicAttack(target)
                    }
                    else -> {
                        // Default high-damage ranged attack
                        rangedAttack(target)
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
     * Regular ranged attack - very high damage
     */
    private fun Npc.rangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(RANGED_ATTACK_ANIM)
        
        // Create ranged projectile
        val projectile = createProjectile(
            target,
            gfx = RANGED_PROJECTILE_GFX,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay
        val hitDelay = RangedCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal damage after projectile hits
        this.world.queue {
            wait(hitDelay - 1)
            
            // Calculate actual max hit from formula
            val formulaMaxHit = RangedCombatFormula.getMaxHit(this@rangedAttack, target)
            
            // Check if target has Protect from Missiles prayer active
            val baseMaxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
                // Reduced damage through protection prayer, but still significant
                RANGED_MAX_HIT / 2  // 35 max hit with prayer
            } else {
                RANGED_MAX_HIT  // 70 max hit without prayer
            }
            
            // Use the higher of formula max hit or our base max hit (ensure we always have damage)
            val maxHit = maxOf(formulaMaxHit, baseMaxHit)
            
            if (RangedCombatFormula.getAccuracy(this@rangedAttack, target) >= this@rangedAttack.world.randomDouble()) {
                val damage = this@rangedAttack.world.random(maxHit + 1)
                // Ensure minimum damage of 1 if accuracy passes
                val finalDamage = maxOf(1, damage)
                target.hit(finalDamage, type = HitType.HIT, delay = 1)
                target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
            }
        }
    }
    
    /**
     * Magic attack
     */
    private fun Npc.magicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(MAGIC_ATTACK_ANIM)
        
        // Create magic projectile
        val projectile = createProjectile(
            target,
            gfx = MAGIC_PROJECTILE_GFX,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay
        val hitDelay = RangedCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal magic damage
        this.world.queue {
            wait(hitDelay - 1)
            
            // Check if target has Protect from Magic prayer active
            val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                MAGIC_MAX_HIT / 2  // Reduced damage with prayer
            } else {
                MAGIC_MAX_HIT
            }
            
            if (MagicCombatFormula.getAccuracy(this@magicAttack, target) >= this@magicAttack.world.randomDouble()) {
                val damage = minOf(this@magicAttack.world.random(maxHit + 1), maxHit)
                target.hit(damage, type = HitType.HIT, delay = 1)
                target.graphic(id = MAGIC_PROJECTILE_GFX, height = 0, delay = 1) // Impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
            }
        }
    }
    
    /**
     * WHIRLWIND ATTACK - Devastating area-of-effect ranged attack
     * 
     * Kree'arra spins and fires projectiles in all directions, hitting all players
     * within a 5-tile radius. This attack is extremely dangerous!
     */
    private fun Npc.whirlwindAttack(mainTarget: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(WHIRLWIND_ATTACK_ANIM)
        
        // Show warning message to main target
        if (mainTarget is Player) {
            mainTarget.message("Kree'arra unleashes a devastating whirlwind of ranged attacks!")
        }
        
        // Create whirlwind visual effect at Kree'arra's location
        graphic(id = WHIRLWIND_GFX, height = 0)
        
        // Spawn multiple projectiles in a circle pattern
        val centerTile = this.tile
        val projectileDirections = listOf(
            Pair(0, 1),   // North
            Pair(1, 1),   // Northeast
            Pair(1, 0),   // East
            Pair(1, -1),  // Southeast
            Pair(0, -1),  // South
            Pair(-1, -1), // Southwest
            Pair(-1, 0),  // West
            Pair(-1, 1)   // Northwest
        )
        
        // Fire projectiles in all directions
        projectileDirections.forEach { (dx, dz) ->
            val targetTile = centerTile.transform(dx * WHIRLWIND_RADIUS, dz * WHIRLWIND_RADIUS)
            val distance = centerTile.getChebyshevDistance(targetTile)
            val projectile = createProjectile(
                targetTile,
                gfx = RANGED_PROJECTILE_GFX,
                startHeight = 43,
                endHeight = 31,
                delay = 51,
                angle = 10,
                lifespan = 51 + (distance * 5)  // Calculate lifespan based on distance
            )
            this.world.spawn(projectile)
        }
        
        // After a short delay, damage all players in the area
        world.queue {
            wait(2)
            
            // Find all players within whirlwind radius
            val nearbyPlayers = mutableListOf<Player>()
            world.players.forEach { player ->
                if (player.tile.getDistance(this@whirlwindAttack.tile) <= WHIRLWIND_RADIUS && player.isAlive()) {
                    nearbyPlayers.add(player)
                }
            }
            
            // Damage all players in the area
            nearbyPlayers.forEach { player ->
                val distance = this@whirlwindAttack.tile.getDistance(player.tile)
                
                // Spawn impact graphic at player location
                world.spawn(TileGraphic(player.tile, id = 254, height = 100, delay = 0))
                
                // Calculate damage based on distance and prayer protection
                val baseDamage = if (!player.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
                    // No prayer protection - full damage based on distance
                    when {
                        distance <= 1 -> this@whirlwindAttack.world.random(WHIRLWIND_DAMAGE_MIN..WHIRLWIND_DAMAGE_MAX) // Point blank: 30-60 damage
                        distance == 2 -> this@whirlwindAttack.world.random(25..50) // Close: 25-50 damage
                        distance == 3 -> this@whirlwindAttack.world.random(20..40) // Medium: 20-40 damage
                        distance == 4 -> this@whirlwindAttack.world.random(15..30) // Far: 15-30 damage
                        else -> this@whirlwindAttack.world.random(10..25) // Very far: 10-25 damage
                    }
                } else {
                    // Player has Protect from Missiles - reduced damage but still significant
                    when {
                        distance <= 1 -> this@whirlwindAttack.world.random(15..30) // Point blank: 15-30 damage (reduced)
                        distance == 2 -> this@whirlwindAttack.world.random(12..25) // Close: 12-25 damage
                        distance == 3 -> this@whirlwindAttack.world.random(10..20) // Medium: 10-20 damage
                        distance == 4 -> this@whirlwindAttack.world.random(8..15) // Far: 8-15 damage
                        else -> this@whirlwindAttack.world.random(5..12) // Very far: 5-12 damage
                    }
                }
                
                player.hit(baseDamage, type = HitType.HIT, delay = 1)
                
                // Warning message for players hit
                if (distance <= 2) {
                    player.message("You are caught in Kree'arra's whirlwind attack!")
                }
            }
        }
    }
}

