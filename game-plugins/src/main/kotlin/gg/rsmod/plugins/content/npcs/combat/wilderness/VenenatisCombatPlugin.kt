package org.alter.plugins.content.npcs.venenatis

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.game.model.timer.TimeConstants

/**
 * @author Alycia <https://github.com/alycii>
 * Venenatis Combat Plugin - The Spider Wilderness Boss
 * 
 * Venenatis is a massive venomous spider with web-based attacks:
 * - Web Projectile: Entangles and damages players
 * - Spawn Spiderlings: Summons smaller spider minions
 * - Venom Spit: Poison damage over time
 * - Web Trap: Creates sticky traps that slow players
 * - Web Stick: Sticks player in place and removes overhead prayers
 * 
 * Combat Level: 464, Hitpoints: 850
 * Location: Silk Chasm (Multi-combat wilderness)
 */

class VenenatisCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.venenatis") {
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
            if (moveToAttackRange(it, target, distance = 8, projectile = true) && isAttackDelayReady()) {
                attackCount++
                
                // Determine attack based on combat cycle and HP
                when {
                    getCurrentHp() <= getMaxHp() * 0.25 -> {
                        // Enraged phase - spawn more spiderlings and web traps
                        when (this.world.random(4)) {
                            0 -> spawnSpiderlingsAttack(target)
                            1 -> webTrapAttack(target)
                            2 -> venomSpitAttack(target)
                            3 -> webStickAttack(target)
                            else -> webProjectileAttack(target)
                        }
                        attackCount = 0
                    }
                    attackCount >= 4 && this.world.chance(1, 3) -> {
                        // Special attacks
                        when (this.world.random(5)) {
                            0 -> webProjectileAttack(target)
                            1 -> spawnSpiderlingsAttack(target)
                            2 -> venomSpitAttack(target)
                            3 -> webTrapAttack(target)
                            4 -> webStickAttack(target)
                            else -> webProjectileAttack(target)
                        }
                        attackCount = 0
                    }
                    else -> {
                        // Normal spider attack (alternates between styles)
                        when (this.world.random(3)) {
                            0 -> normalStabAttack(target)
                            1 -> normalRangedAttack(target)
                            2 -> normalMagicAttack(target)
                            else -> normalStabAttack(target)
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

    private fun Npc.normalStabAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.ACCURATE)
        forceChat("*Strikes with venomous fangs*")
        animate(5319) // Spider stab animation
        
        val hit = dealHit(
            target = target,
            formula = MeleeCombatFormula,
            delay = 1
        ) { hit ->
            if (hit.landed() && this.world.chance(3, 10)) {
                if (target is Player) {
                    target.message("You have been poisoned!")
                    target.poison(initialDamage = 4) {
                        target.message("The venom courses through your veins.")
                    }
                }
            }
        }
    }

    private fun Npc.normalRangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        forceChat("*Shoots web projectile*")
        animate(5320) // Spider ranged animation
        
        val projectile = createProjectile(
            target, 
            gfx = 1749, 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = delay
        ) { hit ->
            if (hit.landed() && this.world.chance(1, 4)) {
                if (target is Player) {
                    target.forceChat("I'm stuck!")
                }
            }
        }
    }

    private fun Npc.normalMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*Casts venom magic*")
        animate(5321) // Spider magic animation
        graphic(172) // Venom aura
        
        val projectile = createProjectile(
            target, 
            gfx = 1750, 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 173, height = 0, delay = hit.getClientHitDelay()) // Venom splash graphic
            }
        }
    }

    private suspend fun Npc.webProjectileAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*WEAVES A POWERFUL WEB*")
        animate(5320) // Web animation
        graphic(170) // Web casting graphic
        
        if (target is Player) {
            target.message("Venenatis weaves a powerful web attack!")
        }
        
        // Launch multiple web projectiles in a spread pattern
        val targets = world.players.forEach { player ->
            if (!player.tile.isWithinRadius(target.tile, 2) || !player.isAlive()) return@forEach
            if (player.tile.isWithinRadius(this.tile, 12)) {
                val projectile = createProjectile(
                    player, 
                    gfx = 1749, 
                    startHeight = 43, 
                    endHeight = 31, 
                    delay = 51, 
                    angle = 15, 
                    steepness = 127
                )
                
                world.spawn(projectile)
                
                val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(player), player.getCentreTile())
                
                // Apply web effect after delay
                this.world.queue {
                    wait(delay)
                    
                    if (player.isAlive()) {
                        val damage = this@webProjectileAttack.world.random(10..25) // 10-25 damage
                        player.hit(damage, type = HitType.HIT, delay = 0)
                        player.graphic(id = 171, height = 0, delay = 0) // Web entangle graphic
                        
                        // Entangle player
                        player.message("You are entangled in Venenatis's web!")
                        player.freeze(cycles = 5) {
                            player.message("You break free from the web!")
                        }
                        
                        // Reduce player's run energy (simplified)
                        if (player is Player) {
                            player.message("Your energy is drained by the web!")
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.spawnSpiderlingsAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*CALLS FORTH HER CHILDREN*")
        animate(5322) // Summoning animation
        graphic(174) // Spawning graphic
        
        if (target is Player) {
            target.message("Spiderlings emerge from the shadows to attack you!")
        }
        
        // Create spawn effects around the area
        val spiderlingCount = this.world.random(3..5)
        
        repeat(spiderlingCount) {
            val spawnTile = Tile(
                this.tile.x + this.world.random(-4..4),
                this.tile.z + this.world.random(-4..4),
                this.tile.height
            )
            
            // Create spawn effect and damage nearby players
            world.spawn(TileGraphic(spawnTile, id = 86, height = 100, delay = 0))
            
            // Damage players near spawn locations
            this.world.queue {
                wait(2)
                
                world.players.forEach { nearbyPlayer ->
                    if (nearbyPlayer.tile.isWithinRadius(spawnTile, 1) && nearbyPlayer.isAlive()) {
                        val damage = this@spawnSpiderlingsAttack.world.random(5..15)
                        nearbyPlayer.hit(damage, type = HitType.HIT, delay = 0)
                        nearbyPlayer.message("A spiderling bites you!")
                    }
                }
            }
        }
    }

    private suspend fun Npc.venomSpitAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*SPITS DEADLY VENOM*")
        animate(5321) // Venom animation
        graphic(175) // Venom charge graphic
        
        if (target is Player) {
            target.message("Venenatis spits deadly venom in your direction!")
        }
        
        // Create venom pools in a line towards the target
        val direction = this.tile.getDirection(target.tile)
        val distance = minOf(this.tile.getDistance(target.tile), 8)
        
        this.world.queue {
            wait(2)
            
            for (i in 1..distance) {
                val venomTile = when (direction) {
                    0 -> Tile(this@venomSpitAttack.tile.x, this@venomSpitAttack.tile.z + i, this@venomSpitAttack.tile.height) // North
                    1 -> Tile(this@venomSpitAttack.tile.x + i, this@venomSpitAttack.tile.z + i, this@venomSpitAttack.tile.height) // Northeast
                    2 -> Tile(this@venomSpitAttack.tile.x + i, this@venomSpitAttack.tile.z, this@venomSpitAttack.tile.height) // East
                    3 -> Tile(this@venomSpitAttack.tile.x + i, this@venomSpitAttack.tile.z - i, this@venomSpitAttack.tile.height) // Southeast
                    4 -> Tile(this@venomSpitAttack.tile.x, this@venomSpitAttack.tile.z - i, this@venomSpitAttack.tile.height) // South
                    5 -> Tile(this@venomSpitAttack.tile.x - i, this@venomSpitAttack.tile.z - i, this@venomSpitAttack.tile.height) // Southwest
                    6 -> Tile(this@venomSpitAttack.tile.x - i, this@venomSpitAttack.tile.z, this@venomSpitAttack.tile.height) // West
                    7 -> Tile(this@venomSpitAttack.tile.x - i, this@venomSpitAttack.tile.z + i, this@venomSpitAttack.tile.height) // Northwest
                    else -> Tile(this@venomSpitAttack.tile.x + i, this@venomSpitAttack.tile.z, this@venomSpitAttack.tile.height)
                }
                
                // Spawn venom pool
                world.spawn(TileGraphic(venomTile, id = 176, height = 0, delay = 0)) // Long-lasting venom pool
                
                // Check for players on venom
                world.players.forEach { player ->
                    if (player.tile == venomTile && player.isAlive()) {
                        val damage = this@venomSpitAttack.world.random(8..15) // 8-15 venom damage
                        player.hit(damage, type = HitType.POISON, delay = 0)
                        player.message("You step in a pool of deadly venom!")
                        player.poison(initialDamage = 6) {
                            player.message("The venom burns through your body!")
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.webTrapAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*LAYS WEB TRAPS*")
        animate(5323) // Web trap animation
        
        if (target is Player) {
            target.message("Venenatis lays deadly web traps around the area!")
        }
        
        // Create web traps in a 5x5 grid around the target
        val centerX = target.tile.x
        val centerZ = target.tile.z
        
        // Create 8-12 web traps randomly placed
        val trapCount = this.world.random(8..12)
        
        this.world.queue {
            wait(2)
            
            repeat(trapCount) {
                val trapTile = Tile(
                    centerX + this@webTrapAttack.world.random(-3..3),
                    centerZ + this@webTrapAttack.world.random(-3..3),
                    target.tile.height
                )
                
                // Warning phase
                world.spawn(TileGraphic(trapTile, id = 177, height = 0, delay = 0)) // Warning web graphic
                
                // Check for trapped players after delay
                queue {
                    wait(3)
                    
                    // Actual trap
                    world.spawn(TileGraphic(trapTile, id = 178, height = 0, delay = 0)) // Long-lasting web trap
                    
                    world.players.forEach { player ->
                        if (player.tile == trapTile && player.isAlive()) {
                            player.message("You are caught in a sticky web trap!")
                            player.freeze(cycles = 4) {
                                player.message("You break free from the web trap!")
                            }
                            player.graphic(id = 171, height = 0, delay = 0) // Web graphic on player
                            
                            // Small damage over time while trapped
                            player.queue {
                                repeat(4) {
                                    wait(1)
                                    if (player.isAlive()) {
                                        val trapDamage = this@webTrapAttack.world.random(1..3) // 1-3 damage per tick
                                        player.hit(trapDamage, type = HitType.POISON, delay = 0)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.webStickAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*SHOOTS STICKY WEB STRANDS*")
        animate(5320) // Web animation
        graphic(170) // Web casting graphic
        
        if (target is Player) {
            target.message("Venenatis shoots sticky web strands at you!")
        }
        
        // Create projectile
        val projectile = createProjectile(
            target, 
            gfx = 1749,  // Web projectile graphic
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        
        // Apply web stick effect after delay
        this.world.queue {
            wait(delay)
            
            if (target is Player && target.isAlive()) {
                val damage = this@webStickAttack.world.random(15..30) // 15-30 damage
                target.hit(damage, type = HitType.HIT, delay = 0)
                target.graphic(id = 171, height = 0, delay = 0) // Web entangle graphic
                
                // Stick player in place (freeze for 2 seconds)
                // 2 seconds ≈ 3-4 cycles (using TimeConstants conversion)
                val freezeCycles = TimeConstants.secondsToCycles(2) ?: 4
                target.message("You are stuck in Venenatis's sticky web!")
                target.freeze(cycles = freezeCycles) {
                    target.message("You break free from the sticky web!")
                }
                
                // Remove overhead prayers and disable them for 2 seconds
                Prayers.deactivateAll(target)
                val disableCycles = TimeConstants.secondsToCycles(2) ?: 4
                Prayers.disableOverheads(target, disableCycles)
                target.message("Your overhead prayer has been knocked off!")
                target.message("You cannot use overhead prayers for a short time.")
            } else if (target.isAlive()) {
                // For non-player targets, just deal damage
                val damage = this@webStickAttack.world.random(15..30)
                target.hit(damage, type = HitType.HIT, delay = 0)
            }
        }
    }

    // Helper function to get direction from one tile to another
    private fun Tile.getDirection(target: Tile): Int {
        val deltaX = target.x - this.x
        val deltaZ = target.z - this.z
        
        return when {
            deltaZ > 0 && deltaX == 0 -> 0 // North
            deltaZ > 0 && deltaX > 0 -> 1 // Northeast
            deltaZ == 0 && deltaX > 0 -> 2 // East
            deltaZ < 0 && deltaX > 0 -> 3 // Southeast
            deltaZ < 0 && deltaX == 0 -> 4 // South
            deltaZ < 0 && deltaX < 0 -> 5 // Southwest
            deltaZ == 0 && deltaX < 0 -> 6 // West
            deltaZ > 0 && deltaX < 0 -> 7 // Northwest
            else -> 2 // Default to East
        }
    }
}