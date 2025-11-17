package org.alter.plugins.content.npcs.crazyarchaeologist

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
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.poison.poison

class CrazyArchaeologistCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.crazy_archaeologist") {
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
                
                // Special attacks
                if (attackCount >= 4 && this.world.chance(1, 3)) {
                    teleportAttack(target)
                    attackCount = 0
                } else if (attackCount >= 6 && this.world.chance(1, 4)) {
                    bookRainAttack(target)
                    attackCount = 0
                } else {
                    // Regular magic attacks with different book projectiles
                    when (this.world.random(4)) {
                        0 -> bookAttack(target, BookType.NORMAL)
                        1 -> bookAttack(target, BookType.EXPLOSIVE) 
                        2 -> bookAttack(target, BookType.POISON)
                        3 -> bookAttack(target, BookType.FREEZE)
                        else -> bookAttack(target, BookType.NORMAL)
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

    private fun Npc.bookAttack(target: Pawn, bookType: BookType) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3353) // Book throwing animation
        
        val projectileGfx = when (bookType) {
            BookType.NORMAL -> 1259     // Regular book projectile
            BookType.EXPLOSIVE -> 1260  // Explosive book (red)
            BookType.POISON -> 1261     // Poison book (green)
            BookType.FREEZE -> 1262     // Freeze book (blue)
        }
        
        val projectile = createProjectile(
            target, 
            gfx = projectileGfx, 
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
            formula = MagicCombatFormula(maxHit = 28),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                applyBookEffect(target, bookType)
            }
        }
        
        if (hit.blocked()) {
            target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
        }
    }
    
    private fun Npc.applyBookEffect(target: Pawn, bookType: BookType) {
        when (bookType) {
            BookType.EXPLOSIVE -> {
                // Create an explosion around the target
                target.graphic(id = 157, height = 0, delay = 1) // Explosion graphic
                // Damage surrounding players if in multi-combat
                if (target is Player) {
                    target.message("The book explodes around you!")
                }
            }
            BookType.POISON -> {
                if (this.world.chance(1, 4)) {
                    target.poison(initialDamage = 6) {
                        if (target is Player) {
                            target.message("You have been poisoned by the ancient tome!")
                        }
                    }
                }
            }
            BookType.FREEZE -> {
                if (this.world.chance(1, 4)) {
                    target.freeze(cycles = 4) {
                        if (target is Player) {
                            target.message("You are frozen by ancient magic!")
                        }
                    }
                }
            }
            BookType.NORMAL -> {
                // No special effect, just damage
            }
        }
    }
    
    private suspend fun Npc.teleportAttack(target: Pawn) {
        // Teleport attack - brings the target next to the archaeologist
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3353) // Book animation
        
        // Special teleport projectile
        val projectile = createProjectile(
            target, 
            gfx = 1576, // Teleport book projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        // Teleport the target after a delay
        world.spawn(
            QueueTask(
                world, 
                delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
            ) {
                if (target is Player && !target.isDead()) {
                    // Find a tile next to the archaeologist to teleport the player
                    val archaeologistTile = this@teleportAttack.tile
                    val surroundingTiles = mutableListOf<Tile>()
                    
                    // Get tiles in a 3x3 area around the archaeologist
                    for (x in -1..1) {
                        for (z in -1..1) {
                            if (x == 0 && z == 0) continue // Skip the archaeologist's tile
                            val tile = archaeologistTile.transform(x, z)
                            if (tile.isWithinDistance(archaeologistTile, 2) && world.collision.canTraverse(tile, EntityType.PLAYER)) {
                                surroundingTiles.add(tile)
                            }
                        }
                    }
                    
                    if (surroundingTiles.isNotEmpty()) {
                        val teleportTile = surroundingTiles.random()
                        target.graphic(id = 1577, height = 0, delay = 0) // Teleport out graphic
                        target.moveTo(teleportTile)
                        target.graphic(id = 1578, height = 0, delay = 1) // Teleport in graphic
                        target.message("The Crazy Archaeologist teleports you to him!")
                        
                        // Deal some damage from the teleport
                        target.hit(this@teleportAttack.world.random(1, 8), type = HitType.HIT, delay = 1)
                    }
                }
            }
        )
    }
    
    private suspend fun Npc.bookRainAttack(target: Pawn) {
        // Book rain attack - multiple books fall from the sky in the area
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(3353) // Book animation
        
        if (target is Player) {
            target.message("The Crazy Archaeologist summons a rain of explosive books!")
        }
        
        // Create multiple books around the target area
        val targetTile = target.tile
        val affectedTiles = mutableListOf<Tile>()
        
        // Get tiles in a 5x5 area around the target
        for (x in -2..2) {
            for (z in -2..2) {
                val tile = targetTile.transform(x, z)
                affectedTiles.add(tile)
            }
        }
        
        // Spawn projectiles for each tile
        affectedTiles.forEach { tile ->
            val projectile = createProjectile(
                tile, 
                gfx = 1260, // Explosive book
                startHeight = 100, 
                endHeight = 0, 
                delay = 60 + world.random(20), // Staggered arrival
                angle = 0, 
                steepness = 0
            )
            world.spawn(projectile)
        }
        
        // After delay, damage anyone in the affected area
        world.spawn(
            QueueTask(world, delay = 6) {
                affectedTiles.forEach { tile ->
                    // Show explosion graphic
                    world.spawn(Graphic(id = 157, tile = tile, height = 0))
                    
                    // Damage any players on this tile
                    world.getPlayersInChunk(tile.chunkCoords).forEach { player ->
                        if (player.tile == tile && !player.isDead()) {
                            player.hit(this@bookRainAttack.world.random(8, 16), type = HitType.HIT, delay = 0)
                        }
                    }
                }
            }
        )
    }
    
    private enum class BookType {
        NORMAL,
        EXPLOSIVE, 
        POISON,
        FREEZE
    }
}