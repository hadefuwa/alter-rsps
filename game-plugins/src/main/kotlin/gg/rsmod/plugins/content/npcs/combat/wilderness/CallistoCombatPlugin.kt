package gg.rsmod.plugins.content.npcs.combat.wilderness

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
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.strategy.MeleeCombatStrategy

/**
 * @author Alycia <https://github.com/alycii>
 * Callisto Combat Plugin - The Bear Wilderness Boss
 * 
 * Callisto is a massive bear with powerful area-of-effect attacks:
 * - Shockwave Attack: Damages all nearby players
 * - Bear Swipe: High damage melee attack
 * - Ground Slam: Creates traps that stun players
 * - Roar: Fear effect that can force players to move
 * 
 * Combat Level: 470, Hitpoints: 1000
 * Location: Callisto's Den (Multi-combat wilderness)
 */

class CallistoCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.callisto") {
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
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                attackCount++
                
                // Determine attack based on combat cycle and HP
                val attackType = when {
                    getCurrentHp() <= getMaxHp() * 0.3 -> "enraged" // Below 30% HP
                    this.world.chance(1, 4) -> "special" // 25% chance for special attack
                    else -> "normal"
                }
                
                when (attackType) {
                    "enraged" -> {
                        // Enraged phase - more frequent special attacks
                        when (this.world.random(3)) {
                            0 -> shockwaveAttack(target)
                            1 -> groundSlamAttack(target)
                            2 -> bearRoarAttack(target)
                        }
                    }
                    "special" -> {
                        // Random special attack
                        when (this.world.random(4)) {
                            0 -> shockwaveAttack(target)
                            1 -> groundSlamAttack(target)
                            2 -> bearRoarAttack(target)
                            3 -> bearSwipeAttack(target)
                        }
                    }
                    else -> {
                        // Normal melee attack
                        normalBearAttack(target)
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

    private fun Npc.normalBearAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*Growls menacingly*")
        animate(4925) // Bear attack animation
        
        val hit = dealHit(
            target = target,
            formula = MeleeCombatFormula,
            delay = 1
        )
        
        if (hit.hit.hitmarks.sumOf { it.damage } > 25 && target is Player) {
            target.forceChat("Oof!")
        }
    }

    private suspend fun Npc.shockwaveAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*REARS UP FOR MASSIVE ATTACK*")
        animate(4927) // Shockwave animation
        graphic(157) // Ground shockwave graphic
        
        world.queue {
            wait(3)
            
            // Find all players within 3 tiles
            val nearbyPlayers = mutableListOf<Player>()
            world.players.forEach { player ->
                if (player.tile.getDistance(this@shockwaveAttack.tile) <= 3 && player.isAlive()) {
                    nearbyPlayers.add(player)
                }
            }
            
            nearbyPlayers.forEach { player: Player ->
                val distance = this@shockwaveAttack.tile.getDistance(player.tile)
                val damage = when {
                    distance <= 1 -> this@shockwaveAttack.world.random(25..50) // Point blank: 25-50 damage
                    distance == 2 -> this@shockwaveAttack.world.random(15..30) // Close: 15-30 damage
                    else -> this@shockwaveAttack.world.random(5..15) // Far: 5-15 damage
                }
                
                // Spawn ground graphic at player location
                world.spawn(TileGraphic(player.tile, id = 157, height = 100, delay = 0))
                
                player.hit(damage, type = HitType.HIT, delay = 1)
                player.message("The ground shakes violently beneath you!")
                
                // Chance to knock player back
                if (this@shockwaveAttack.world.chance(30, 100)) {
                    val knockbackTile = Tile(
                        player.tile.x + this@shockwaveAttack.world.random(-2..2),
                        player.tile.z + this@shockwaveAttack.world.random(-2..2),
                        player.tile.height
                    )
                    player.moveTo(knockbackTile)
                    player.message("You are knocked back by the force!")
                }
            }
        }
    }

    private suspend fun Npc.groundSlamAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*SLAMS THE GROUND*")
        animate(4926) // Ground slam animation
        
        world.queue {
            wait(2)
            
            // Create 3-5 trap tiles around the target area
            val trapCount = this@groundSlamAttack.world.random(3..5)
            repeat(trapCount) { 
                val trapTile = Tile(
                    target.tile.x + this@groundSlamAttack.world.random(-3..3),
                    target.tile.z + this@groundSlamAttack.world.random(-3..3),
                    target.tile.height
                )
                
                // Spawn warning graphic first
                world.spawn(TileGraphic(trapTile, id = 130, height = 80, delay = 0)) // Warning graphic
                
                // Check for players on trap tiles after delay
                world.queue {
                    wait(2)
                    
                    val playersOnTrap = mutableListOf<Player>()
                    world.players.forEach { player ->
                        if (player.tile == trapTile && player.isAlive()) {
                            playersOnTrap.add(player)
                        }
                    }
                    
                    playersOnTrap.forEach { player: Player ->
                        val damage = this@groundSlamAttack.world.random(15..25) // 15-25 damage
                        player.hit(damage, type = HitType.HIT, delay = 0)
                        player.message("You are caught in Callisto's trap!")
                        
                        // Stun player briefly
                        player.stun(3) // 1.8 second stun
                        player.graphic(80) // Stun graphic
                    }
                    
                    // Spawn actual trap graphic
                    world.spawn(TileGraphic(trapTile, id = 131, height = 0, delay = 0)) // Trap graphic
                }
            }
        }
    }

    private fun Npc.bearSwipeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.ACCURATE)
        forceChat("*SWIPES WITH MASSIVE CLAWS*")
        animate(4925) // Swipe animation
        graphic(245) // Claw slash graphic
        
        val hit = dealHit(
            target = target,
            formula = MeleeCombatFormula,
            delay = 1
        ) { hit ->
            if (hit.hit.hitmarks.sumOf { it.damage } > 30 && target is Player) {
                target.forceChat("Argh!")
                target.message("Callisto's claws leave deep wounds!")
                
                // Apply bleeding effect (small damage over time)
                target.queue {
                    repeat(5) { // 5 ticks of bleeding
                        wait(2)
                        if (target.isAlive()) {
                            val bleedDamage = this@bearSwipeAttack.world.random(1..3) // 1-3 bleed damage
                            target.hit(bleedDamage, type = HitType.POISON, delay = 0)
                            target.message("You bleed from Callisto's claws.")
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.bearRoarAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*LETS OUT A TERRIFYING ROAR*")
        animate(4928) // Roar animation
        graphic(158) // Fear aura graphic
        
        world.queue {
            wait(2)
            
            // Affect all players within 5 tiles
            val nearbyPlayers = mutableListOf<Player>()
            world.players.forEach { player ->
                if (this@bearRoarAttack.tile.getDistance(player.tile) <= 5 && player.isAlive()) {
                    nearbyPlayers.add(player)
                }
            }
            
            nearbyPlayers.forEach { player: Player ->
                val fearChance = when (this@bearRoarAttack.tile.getDistance(player.tile)) {
                    0, 1 -> 80 // 80% chance up close
                    2, 3 -> 60 // 60% chance at medium range
                    else -> 40 // 40% chance at long range
                }
                
                if (this@bearRoarAttack.world.chance(fearChance, 100)) {
                    player.message("Callisto's roar fills you with terror!")
                    player.graphic(1843) // Fear graphic
                    
                    // Force player to run away
                    val escapeDirection = when (this@bearRoarAttack.world.random(4)) {
                        0 -> Tile(player.tile.x + 3, player.tile.z, player.tile.height)
                        1 -> Tile(player.tile.x - 3, player.tile.z, player.tile.height)
                        2 -> Tile(player.tile.x, player.tile.z + 3, player.tile.height)
                        else -> Tile(player.tile.x, player.tile.z - 3, player.tile.height)
                    }
                    
                    player.moveTo(escapeDirection)
                    player.message("You flee in terror!")
                    
                    // Reduce combat stats temporarily
                    val reduction = this@bearRoarAttack.world.random(3..7)
                    player.getSkills().alterCurrentLevel(0, -reduction) // Attack
                    player.getSkills().alterCurrentLevel(1, -reduction) // Strength
                    player.getSkills().alterCurrentLevel(2, -reduction) // Defence
                    player.message("Fear reduces your combat effectiveness!")
                } else {
                    player.message("You resist Callisto's intimidating roar.")
                }
            }
        }
    }
}