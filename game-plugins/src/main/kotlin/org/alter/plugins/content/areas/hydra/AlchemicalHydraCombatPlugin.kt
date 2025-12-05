package org.alter.plugins.content.areas.hydra

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
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
import org.alter.api.cfg.Graphic
import org.alter.api.cfg.Sound
import org.alter.api.cfg.Animation
import org.alter.game.model.entity.Projectile
import org.alter.game.model.entity.AreaSound
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Alchemical Hydra Combat Plugin
 * 
 * Implements the Alchemical Hydra's unique phase-based combat mechanics:
 * - Phase 1 (100-75% HP): Green carapace - Poison blob attacks
 * - Phase 2 (75-50% HP): Blue carapace - Lightning orb attacks
 * - Phase 3 (50-25% HP): Red carapace - Fire wall attacks
 * - Phase 4 (25-0% HP): Grey carapace - Enraged phase with faster attacks
 * 
 * The Hydra alternates between Ranged and Magic attacks throughout all phases.
 */
class AlchemicalHydraCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private val PHASE_ATTR = AttributeKey<HydraPhase>("hydra_phase")
        private val ATTACK_COUNT_ATTR = AttributeKey<Int>("hydra_attack_count")
        private val LAST_ATTACK_STYLE_ATTR = AttributeKey<CombatClass>("hydra_last_attack_style")
        private val SPECIAL_ATTACK_COUNT_ATTR = AttributeKey<Int>("hydra_special_attack_count")
        
        // Phase thresholds (as percentages of max HP)
        private const val PHASE_1_THRESHOLD = 75 // 100-75%
        private const val PHASE_2_THRESHOLD = 50 // 75-50%
        private const val PHASE_3_THRESHOLD = 25 // 50-25%
        // Phase 4 is 25-0%
        
        // Special attack intervals
        private const val PHASE_1_SPECIAL_INTERVAL = 3 // Every 3 attacks
        private const val PHASE_2_SPECIAL_INTERVAL = 4 // Every 4 attacks
        private const val PHASE_3_SPECIAL_INTERVAL = 5 // Every 5 attacks
        private const val PHASE_4_SPECIAL_INTERVAL = 9 // Every 9 attacks (after first 3)
        private const val PHASE_4_FIRST_SPECIAL = 3 // First special at 3 attacks
    }

    enum class HydraPhase {
        POISON,      // Phase 1: Green carapace (100-75% HP)
        LIGHTNING,   // Phase 2: Blue carapace (75-50% HP)
        FLAME,       // Phase 3: Red carapace (50-25% HP)
        ENRAGED      // Phase 4: Grey carapace (25-0% HP)
    }

    init {
        onNpcCombat("npc.alchemical_hydra") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Initialize phase on spawn
        onNpcSpawn("npc.alchemical_hydra") {
            npc.attr[PHASE_ATTR] = HydraPhase.POISON
            npc.attr[ATTACK_COUNT_ATTR] = 0
            npc.attr[LAST_ATTACK_STYLE_ATTR] = CombatClass.RANGED
            npc.attr[SPECIAL_ATTACK_COUNT_ATTR] = 0
            // Play spawn sound
            world.spawn(AreaSound(npc.tile, Sound.HYDRA_BOSS_STAGE_1_HIT, radius = 10, volume = 5))
        }
        
        // Play death sound
        onNpcDeath("npc.alchemical_hydra") {
            world.spawn(AreaSound(npc.tile, Sound.HYDRA_BOSS_STAGE_1_DEATH, radius = 15, volume = 5))
            world.queue {
                wait(3)
                world.spawn(AreaSound(npc.tile, Sound.HYDRA_BOSS_STAGE_4_DEATH, radius = 15, volume = 5))
            }
        }
    }

    /**
     * Main combat loop for the Alchemical Hydra
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Update phase based on HP
            updatePhase()
            val currentPhase = attr[PHASE_ATTR] ?: HydraPhase.POISON
            
            // Determine attack distance (ranged/magic = 10 tiles)
            if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                val attackCount = (attr[ATTACK_COUNT_ATTR] ?: 0) + 1
                attr[ATTACK_COUNT_ATTR] = attackCount
                
                // Check if special attack should be used
                val shouldUseSpecial = when (currentPhase) {
                    HydraPhase.POISON -> attackCount % PHASE_1_SPECIAL_INTERVAL == 0
                    HydraPhase.LIGHTNING -> attackCount % PHASE_2_SPECIAL_INTERVAL == 0
                    HydraPhase.FLAME -> attackCount % PHASE_3_SPECIAL_INTERVAL == 0
                    HydraPhase.ENRAGED -> {
                        val specialCount = attr[SPECIAL_ATTACK_COUNT_ATTR] ?: 0
                        if (specialCount == 0) {
                            attackCount >= PHASE_4_FIRST_SPECIAL
                        } else {
                            attackCount % PHASE_4_SPECIAL_INTERVAL == 0
                        }
                    }
                }
                
                if (shouldUseSpecial) {
                    // Use phase-specific special attack
                    when (currentPhase) {
                        HydraPhase.POISON -> poisonBlobAttack(target)
                        HydraPhase.LIGHTNING -> lightningOrbAttack(target)
                        HydraPhase.FLAME -> fireWallAttack(target)
                        HydraPhase.ENRAGED -> {
                            poisonBlobAttack(target) // Enraged phase uses poison blobs
                            val specialCount = (attr[SPECIAL_ATTACK_COUNT_ATTR] ?: 0) + 1
                            attr[SPECIAL_ATTACK_COUNT_ATTR] = specialCount
                        }
                    }
                } else {
                    // Regular attack - alternate between Ranged and Magic
                    val lastStyle = attr[LAST_ATTACK_STYLE_ATTR] ?: CombatClass.RANGED
                    val nextStyle = if (lastStyle == CombatClass.RANGED) CombatClass.MAGIC else CombatClass.RANGED
                    
                    // In enraged phase, alternate every attack. In other phases, alternate every 3 attacks
                    val styleToUse = if (currentPhase == HydraPhase.ENRAGED) {
                        nextStyle
                    } else {
                        if (attackCount % 3 == 1) CombatClass.RANGED else CombatClass.MAGIC
                    }
                    
                    when (styleToUse) {
                        CombatClass.RANGED -> rangedAttack(target, currentPhase)
                        CombatClass.MAGIC -> magicAttack(target, currentPhase)
                        else -> rangedAttack(target, currentPhase)
                    }
                    
                    attr[LAST_ATTACK_STYLE_ATTR] = styleToUse
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
     * Updates the Hydra's phase based on current HP percentage
     */
    private fun Npc.updatePhase() {
        val maxHp = getMaxHp()
        val currentHp = getCurrentHp()
        val hpPercent = (currentHp * 100) / maxHp
        
        val newPhase = when {
            hpPercent > PHASE_1_THRESHOLD -> HydraPhase.POISON
            hpPercent > PHASE_2_THRESHOLD -> HydraPhase.LIGHTNING
            hpPercent > PHASE_3_THRESHOLD -> HydraPhase.FLAME
            else -> HydraPhase.ENRAGED
        }
        
        val currentPhase = attr[PHASE_ATTR] ?: HydraPhase.POISON
        if (newPhase != currentPhase) {
            attr[PHASE_ATTR] = newPhase
            attr[ATTACK_COUNT_ATTR] = 0 // Reset attack count on phase change
            attr[SPECIAL_ATTACK_COUNT_ATTR] = 0
            
            // Play phase transition animation and sounds
            when (newPhase) {
                HydraPhase.LIGHTNING -> {
                    world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_2_SPAWN, radius = 15, volume = 5))
                    animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_LIGHTNING_START)
                    world.queue {
                        wait(2)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_2_SPARK, radius = 15, volume = 5))
                        wait(1)
                        animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_LIGHTNING_END)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_NEUTRALIZE, radius = 15, volume = 5))
                    }
                }
                HydraPhase.FLAME -> {
                    world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_FIRE_SPAWN, radius = 15, volume = 5))
                    animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_FLAME_START)
                    world.queue {
                        wait(2)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_FIRE_STACK, radius = 15, volume = 5))
                        wait(1)
                        animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_FLAME_END)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_NEUTRALIZE, radius = 15, volume = 5))
                    }
                }
                HydraPhase.ENRAGED -> {
                    world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_4_SPAWN, radius = 15, volume = 5))
                    animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_ENRAGED_START)
                    world.queue {
                        wait(2)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_4_DEATH_RUMBLE, radius = 15, volume = 5))
                        wait(1)
                        animate(Animation.ALCHEMICAL_HYDRA_TRANSFORM_TO_ENRAGED_END)
                        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_4_DEATH_ROCKS, radius = 15, volume = 5))
                    }
                    forceChat("The Alchemical Hydra becomes enraged!")
                }
                else -> {}
            }
        }
    }

    /**
     * Regular Ranged attack
     */
    private fun Npc.rangedAttack(target: Pawn, phase: HydraPhase) {
        val animation = when (phase) {
            HydraPhase.POISON -> Animation.ALCHEMICAL_HYDRA_POISON_PHASE_RANGED
            HydraPhase.LIGHTNING -> Animation.ALCHEMICAL_HYDRA_LIGHTNING_PHASE_RANGED
            HydraPhase.FLAME -> Animation.ALCHEMICAL_HYDRA_FLAME_PHASE_RANGED
            HydraPhase.ENRAGED -> Animation.ALCHEMICAL_HYDRA_ENRAGED_PHASE_RANGED
        }
        
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(animation)
        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_1_RANGED_ATTACK, radius = 10, volume = 5))
        
        val projectile = createProjectile(
            target,
            gfx = Graphic.RUNE_ARROW_PROJECTILE, // Ranged projectile
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
            maxHit = if (phase == HydraPhase.ENRAGED) 26 else 20,
            landHit = RangedCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = Graphic.RUNE_ARROW_DRAWBACK, height = 0, delay = 1)
                world.spawn(AreaSound(target.tile, Sound.HYDRA_BOSS_RANGED_HIT, radius = 5, volume = 5))
            } else {
                target.graphic(id = Graphic.SPLASH, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    /**
     * Regular Magic attack
     */
    private fun Npc.magicAttack(target: Pawn, phase: HydraPhase) {
        val animation = when (phase) {
            HydraPhase.POISON -> Animation.ALCHEMICAL_HYDRA_POISON_PHASE_MAGIC
            HydraPhase.LIGHTNING -> Animation.ALCHEMICAL_HYDRA_LIGHTNING_PHASE_MAGIC
            HydraPhase.FLAME -> Animation.ALCHEMICAL_HYDRA_FLAME_PHASE_MAGIC
            HydraPhase.ENRAGED -> Animation.ALCHEMICAL_HYDRA_ENRAGED_PHASE_MAGIC
        }
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(animation)
        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_1_MAGIC_ATTACK, radius = 10, volume = 5))
        
        val projectile = createProjectile(
            target,
            gfx = Graphic.FIRE_STRIKE_PROJECTILE, // Magic projectile
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
            maxHit = if (phase == HydraPhase.ENRAGED) 26 else 20,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = Graphic.FIRE_STRIKE_HIT, height = 0, delay = 1)
                world.spawn(AreaSound(target.tile, Sound.HYDRA_BOSS_MAGIC_HIT, radius = 5, volume = 5))
            } else {
                target.graphic(id = Graphic.SPLASH, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    /**
     * Phase 1 Special Attack: Poison Blob
     * Spits multiple poison blobs - one targeting the player and others around
     */
    private fun Npc.poisonBlobAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.ALCHEMICAL_HYDRA_POISON_PHASE_POISON)
        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_1_SPECIAL_ATTACK, radius = 15, volume = 5))
        
        if (target is Player) {
            target.message("<col=ff0000>The Alchemical Hydra spits poison blobs!</col>")
        }
        
        val targetTile = target.tile
        
        // Create poison blobs in a pattern around the target
        val blobOffsets = listOf(
            Pair(0, 0),      // Direct hit on target
            Pair(-2, -2),     // Top-left
            Pair(2, -2),      // Top-right
            Pair(-2, 2),      // Bottom-left
            Pair(2, 2)        // Bottom-right
        )
        
        world.queue {
            wait(2)
            // Play additional poison sound effects
            world.spawn(AreaSound(tile, Sound.HYDRA_GEYSER, radius = 15, volume = 5))
            wait(1) // Delay for blob landing
            
            blobOffsets.forEach { (x, z) ->
                val blobTile = targetTile.transform(x, z)
                
                // Create poison pool graphic
                world.spawn(TileGraphic(id = Graphic.BIG_SPLASH, tile = blobTile, height = 0, delay = 0))
                world.spawn(AreaSound(blobTile, Sound.HYDRA_BOSS_POOL_LAND, radius = 5, volume = 5))
                
                // Check for players standing on or near the blob
                world.players.forEach { player ->
                    val distance = player.tile.getDistance(blobTile)
                    if (distance <= 1 && player.getCurrentHp() > 0) {
                        // Deal poison damage
                        val damage = world.random(8) + 4 // 4-12 damage
                        player.hit(damage, type = HitType.HIT, delay = 0)
                        player.graphic(id = Graphic.BIG_SPLASH, height = 0, delay = 0)
                        world.spawn(AreaSound(player.tile, Sound.HYDRA_BOSS_STAGE_1_HIT, radius = 5, volume = 5))
                        
                        // Apply poison
                        player.poison(initialDamage = 6) {
                            player.message("You have been poisoned by the Hydra's toxic blob!")
                        }
                        
                        if (player == target) {
                            player.message("<col=ff0000>You are hit by a poison blob!</col>")
                        }
                    }
                }
            }
        }
    }

    /**
     * Phase 2 Special Attack: Lightning Orbs
     * Summons four lightning orbs from corners that converge on the player
     */
    private fun Npc.lightningOrbAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.ALCHEMICAL_HYDRA_LIGHTNING_PHASE_LIGHTNING)
        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_2_SPAWN, radius = 15, volume = 5))
        
        if (target is Player) {
            target.message("<col=00ffff>The Alchemical Hydra summons lightning orbs!</col>")
        }
        
        val targetTile = target.tile
        val bossTile = this.tile
        
        // Calculate corner positions relative to boss
        val corners = listOf(
            bossTile.transform(-5, -5), // Top-left corner
            bossTile.transform(5, -5),  // Top-right corner
            bossTile.transform(-5, 5),  // Bottom-left corner
            bossTile.transform(5, 5)    // Bottom-right corner
        )
        
        world.queue {
            // Spawn orbs at corners
            corners.forEach { corner ->
                world.spawn(TileGraphic(id = Graphic.ZAP, tile = corner, height = 0, delay = 0))
                world.spawn(AreaSound(corner, Sound.HYDRA_BOSS_STAGE_2_SPARK, radius = 5, volume = 5))
            }
            
            wait(1)
            // Play additional lightning sounds
            world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_STAGE_2_SPARK_PART_2, radius = 15, volume = 5))
            wait(2) // Orbs move toward target
            
            // Orbs converge on target location
            corners.forEach { corner ->
                val projectile = Projectile.Builder()
                    .setStart(corner)
                    .setTarget(targetTile)
                    .setGfx(Graphic.ZAP) // Lightning projectile
                    .setHeights(startHeight = 43, endHeight = 31)
                    .setSlope(angle = 15, steepness = 127)
                    .setTimes(delay = 30, lifespan = 100)
                    .build()
                world.spawn(projectile)
            }
            
            wait(2) // Wait for projectiles to reach target
            
            // Check if player is at target location
            world.players.forEach { player ->
                val distance = player.tile.getDistance(targetTile)
                if (distance <= 2 && player.getCurrentHp() > 0) {
                    val damage = world.random(5) + 15 // 15-20 damage
                    player.hit(damage, type = HitType.HIT, delay = 0)
                    player.graphic(id = Graphic.ZAP, height = 0, delay = 0)
                    world.spawn(AreaSound(player.tile, Sound.HYDRA_BOSS_STAGE_2_SPARK, radius = 5, volume = 5))
                    player.freeze(3) // Bind for 3 ticks
                    
                    if (player == target) {
                        player.message("<col=00ffff>You are struck by lightning and bound!</col>")
                    }
                }
            }
        }
    }

    /**
     * Phase 3 Special Attack: Fire Walls
     * Moves to center, breathes fire walls, then launches a tracking fire wall
     */
    private suspend fun Npc.fireWallAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.ALCHEMICAL_HYDRA_BREATHE_FIRE)
        world.spawn(AreaSound(tile, Sound.HYDRA_BOSS_FIRE_SPAWN, radius = 15, volume = 5))
        
        if (target is Player) {
            target.message("<col=ff6600>The Alchemical Hydra breathes fire!</col>")
        }
        
        val bossTile = this.tile
        val centerTile = bossTile // Assume boss is near center, or calculate center
        
        // Move boss to center (if needed)
        // For simplicity, we'll use the boss's current position
        
        world.queue {
            wait(2)
            
            // Create side fire walls
            val sideWalls = listOf(
                centerTile.transform(-3, 0),
                centerTile.transform(3, 0)
            )
            
            sideWalls.forEach { wallTile ->
                for (z in -2..2) {
                    val wallSegment = wallTile.transform(0, z)
                    world.spawn(TileGraphic(id = Graphic.FIRE_STRIKE_HIT, tile = wallSegment, height = 0, delay = 0))
                    world.spawn(AreaSound(wallSegment, Sound.HYDRA_BOSS_FIRE_STACK, radius = 3, volume = 5))
                    
                    // Check for players in fire wall
                    world.players.forEach { player ->
                        if (player.tile.sameAs(wallSegment) && player.getCurrentHp() > 0) {
                            val damage = world.random(5) + 20 // 20-25 damage
                            player.hit(damage, type = HitType.HIT, delay = 0)
                            player.graphic(id = Graphic.FIRE_STRIKE_HIT, height = 0, delay = 0)
                            if (player == target) {
                                player.message("<col=ff6600>You are burned by the fire wall!</col>")
                            }
                        }
                    }
                }
            }
            
            wait(2)
            
            // Create tracking fire wall that follows player
            if (target is Player) {
                var currentPos = centerTile
                var steps = 0
                val maxSteps = 8
                
                while (steps < maxSteps && target.getCurrentHp() > 0 && !target.isDead()) {
                    val direction = Direction.calculateAttackDirection(currentPos, target.tile)
                    val nextPos = currentPos.step(direction, 1) ?: break
                    
                    world.spawn(TileGraphic(id = Graphic.FIRE_STRIKE_HIT, tile = nextPos, height = 0, delay = 0))
                    
                    // Check if player is hit
                    if (target.tile.getDistance(nextPos) <= 1) {
                        val damage = world.random(5) + 20 // 20-25 damage
                        target.hit(damage, type = HitType.HIT, delay = 0)
                        target.graphic(id = Graphic.FIRE_STRIKE_HIT, height = 0, delay = 0)
                        target.message("<col=ff6600>The tracking fire wall hits you!</col>")
                    }
                    
                    currentPos = nextPos
                    steps++
                    wait(1)
                }
            }
        }
    }
}
