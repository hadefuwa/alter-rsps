package org.alter.plugins.content.npcs.corporealbeast

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
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.game.model.move.moveTo
import org.alter.game.model.move.walkTo
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * Corporeal Beast Combat Plugin
 * 
 * Implements the Corporeal Beast's unique combat mechanics:
 * - Melee attacks (up to 3,000 damage)
 * - Magic attacks (high-damage, stat-draining, multi-hit projectiles)
 * - Dark Energy Core summoning
 * - Core tracking and management
 */
class CorporealBeastCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        private val CORES_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("corporeal_beast_cores")
        private val LAST_DAMAGE_ATTR = AttributeKey<Int>("corporeal_beast_last_damage")
        private val LAST_DAMAGE_TIME_ATTR = AttributeKey<Int>("corporeal_beast_last_damage_time")
        private const val CORE_SUMMON_THRESHOLD = 5000 // Damage threshold to trigger core
        private const val MAX_CORES = 10 // Maximum number of cores at once
    }

    init {
        onNpcCombat("npc.corporeal_beast") {
            npc.queue {
                npc.combat(this)
            }
        }

        // Clean up cores when boss dies
        onAnyNpcDeath {
            val npc = ctx as? Npc ?: return@onAnyNpcDeath
            if (npc.id == getRSCM("npc.corporeal_beast")) {
                cleanupCores(npc)
            }
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        var attackCount = 0

        // Initialize core tracking
        if (attr[CORES_ATTR] == null) {
            attr[CORES_ATTR] = mutableListOf<WeakReference<Npc>>()
        }

        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Update and manage cores
            updateCores()
            
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                attackCount++
                
                // Determine attack type
                val attackType = when {
                    attackCount >= 8 && world.chance(1, 4) -> "multi_hit_magic"
                    attackCount >= 6 && world.chance(1, 3) -> "stat_drain_magic"
                    attackCount >= 4 && world.chance(1, 5) -> "summon_core"
                    world.chance(1, 3) -> "high_damage_magic"
                    else -> "melee"
                }
                
                when (attackType) {
                    "melee" -> meleeAttack(target)
                    "high_damage_magic" -> highDamageMagicAttack(target)
                    "stat_drain_magic" -> statDrainMagicAttack(target)
                    "multi_hit_magic" -> multiHitMagicAttack(target)
                    "summon_core" -> summonDarkEnergyCore(this, target)
                }
                
                postAttackLogic(target)
            }
            it.wait(1)
            target = getCombatTarget() ?: break
        }

        resetFacePawn()
        removeCombatTarget()
    }

    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(1680) // Corporeal Beast melee animation
        
        dealHit(
            target = target,
            maxHit = 300, // Scaled down from 3000 for game balance (can be adjusted)
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1)
                if (target is Player) {
                    target.message("The Corporeal Beast swipes at you with immense force!")
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.highDamageMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(1681) // Corporeal Beast magic animation
        
        val projectile = createProjectile(
            target,
            gfx = 317, // Large magical orb
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
            maxHit = 450, // Scaled down from 4500 for game balance
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 318, height = 0, delay = 1) // Large magic hit graphic
                if (target is Player) {
                    // Check if Protect from Magic is active
                    if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                        target.message("Your protection prayer reduces the damage!")
                    } else {
                        target.message("A massive magical orb crashes into you!")
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private fun Npc.statDrainMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(1681) // Corporeal Beast magic animation
        
        val projectile = createProjectile(
            target,
            gfx = 316, // Smaller, transparent orb
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
            maxHit = 200, // Lower base damage
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 317, height = 0, delay = 1)
                if (target is Player) {
                    val magicLevel = target.getSkills().getCurrentLevel(Skills.MAGIC)
                    val prayerLevel = target.getSkills().getCurrentLevel(Skills.PRAYER)
                    
                    // Drain stats - if already at 0, deal extra damage
                    var extraDamage = 0
                    if (magicLevel > 0) {
                        val drain = world.random(3) + 1
                        target.getSkills().alterCurrentLevel(skill = Skills.MAGIC, value = -drain, capValue = 0)
                        target.message("Your Magic level has been drained!")
                    } else {
                        extraDamage += world.random(5) + 5
                    }
                    
                    if (prayerLevel > 0) {
                        val drain = world.random(5) + 5
                        target.getSkills().alterCurrentLevel(skill = Skills.PRAYER, value = -drain, capValue = 0)
                        target.message("Your Prayer level has been drained!")
                    } else {
                        extraDamage += world.random(10) + 10
                    }
                    
                    if (extraDamage > 0) {
                        target.hit(extraDamage, type = HitType.HIT, delay = 0)
                        target.message("The attack deals extra damage as your levels are already drained!")
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
            }
        }
    }

    private suspend fun Npc.multiHitMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(1681) // Corporeal Beast magic animation
        
        if (target is Player) {
            target.message("The Corporeal Beast launches a wavy, transparent orb!")
        }
        
        val projectile = createProjectile(
            target,
            gfx = 315, // Extremely transparent, wavy orb
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 15,
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = MagicCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        
        world.queue {
            wait(delay)
            
            if (target.getCurrentHp() > 0) {
                val targetTile = target.tile
                val hitMain = MagicCombatFormula.getAccuracy(this@multiHitMagicAttack, target) >= world.randomDouble()
                
                if (hitMain) {
                    // Main hit - high damage
                    target.hit(world.random(150) + 100, type = HitType.HIT, delay = 0)
                    target.graphic(id = 318, height = 0, delay = 0)
                    if (target is Player) {
                        target.message("The orb strikes you directly!")
                    }
                } else {
                    // Miss - splits into 5 smaller orbs
                    if (target is Player) {
                        target.message("The orb shatters into smaller fragments!")
                    }
                    
                    // Create 5 smaller orbs in 3x3 area
                    val offsets = listOf(
                        Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
                        Pair(0, -1), Pair(0, 1),
                        Pair(1, -1), Pair(1, 0), Pair(1, 1)
                    ).shuffled().take(5)
                    
                    offsets.forEach { (x, z) ->
                        val orbTile = targetTile.transform(x, z)
                        world.spawn(TileGraphic(id = 316, tile = orbTile, height = 0, delay = 0))
                        
                        // Check for players in 3x3 area around each orb
                        world.players.forEach { player ->
                            if (player.tile.getDistance(orbTile) <= 1 && player.getCurrentHp() > 0) {
                                if (world.chance(1, 2)) { // 50% chance to hit
                                    player.hit(world.random(30) + 20, type = HitType.HIT, delay = 0)
                                    player.graphic(id = 317, height = 0, delay = 0)
                                    if (player == target) {
                                        player.message("A fragment strikes you!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun Npc.summonDarkEnergyCore(boss: Npc, target: Pawn) {
        val cores = boss.attr[CORES_ATTR] as? MutableList<WeakReference<Npc>> ?: mutableListOf()
        val activeCores = cores.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        
        // Limit number of cores
        if (activeCores.size >= MAX_CORES) {
            return
        }
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(1682) // Corporeal Beast summoning animation
        
        if (target is Player) {
            target.message("The Corporeal Beast summons a Dark Energy Core!")
        }
        
        // Find spawn location near boss
        val bossTile = boss.tile
        val spawnOffsetX = world.random(-3..3)
        val spawnOffsetZ = world.random(-3..3)
        val spawnTile = bossTile.transform(spawnOffsetX, spawnOffsetZ)
        
        // Create and spawn the core
        val core = Npc(getRSCM("npc.dark_core"), spawnTile, world)
        core.respawns = false
        core.walkRadius = 0
        core.setActive(true)
        core.combatClass = CombatClass.MELEE
        
        world.spawn(core)
        
        // Add core to tracking list
        cores.add(WeakReference(core))
        boss.attr[CORES_ATTR] = cores
        
        // Make core move toward players and deal damage
        core.queue {
            var lastDamageCycle = world.currentCycle
            
            while (core.isSpawned() && core.isActive() && boss.isSpawned() && boss.isActive()) {
                // Find nearest player
                val validPlayers = mutableListOf<Player>()
                world.players.forEach { player ->
                    if (player.getCurrentHp() > 0 && player.tile.height == core.tile.height) {
                        validPlayers.add(player)
                    }
                }
                val nearestPlayer = validPlayers.minByOrNull { it.tile.getDistance(core.tile) }
                
                if (nearestPlayer != null) {
                    val distance = core.tile.getDistance(nearestPlayer.tile)
                    
                    // Move toward player if not adjacent
                    if (distance > 1) {
                        val direction = Direction.calculateAttackDirection(core.tile, nearestPlayer.tile)
                        val nextTile = core.tile.step(direction, 1)
                        if (nextTile != null && world.canTraverse(core.tile, direction, core)) {
                            core.walkTo(nextTile)
                        }
                    }
                    
                    // Deal damage if player is in 3x3 area
                    if (distance <= 1 && world.currentCycle - lastDamageCycle >= 1) {
                        val damage = world.random(4) // 0-3 damage (max hit 3)
                        nearestPlayer.hit(damage, type = HitType.HIT, delay = 0)
                        nearestPlayer.graphic(id = 319, height = 0, delay = 0)
                        
                        // Drain 20 prayer points
                        if (nearestPlayer is Player) {
                            val currentPrayer = nearestPlayer.getSkills().getCurrentLevel(Skills.PRAYER)
                            val drainAmount = minOf(20, currentPrayer)
                            nearestPlayer.getSkills().alterCurrentLevel(skill = Skills.PRAYER, value = -drainAmount, capValue = 0)
                            nearestPlayer.message("The Dark Energy Core drains your Prayer!")
                            
                            // Heal boss
                            val currentHp = boss.getCurrentHp()
                            val maxHp = boss.getMaxHp()
                            if (currentHp < maxHp) {
                                val healAmount = minOf(damage, maxHp - currentHp)
                                boss.setCurrentHp(minOf(currentHp + healAmount, maxHp))
                            }
                        }
                        
                        lastDamageCycle = world.currentCycle
                    }
                }
                
                wait(1)
            }
            
            // Remove core when done
            if (core.isSpawned()) {
                world.remove(core)
            }
        }
        
        // Auto-remove core after 2 minutes if still alive
        world.queue {
            wait(1200) // 2 minutes
            if (core.isSpawned() && core.isActive()) {
                world.remove(core)
                val updatedCores = boss.attr[CORES_ATTR] as? MutableList<WeakReference<Npc>> ?: mutableListOf()
                updatedCores.removeAll { it.get() == null || it.get() == core }
                boss.attr[CORES_ATTR] = updatedCores
            }
        }
    }

    private fun Npc.updateCores() {
        val cores = attr[CORES_ATTR] as? MutableList<WeakReference<Npc>> ?: return
        cores.removeAll { it.get() == null || !it.get()!!.isActive() || !it.get()!!.isSpawned() }
        attr[CORES_ATTR] = cores
    }

    private fun cleanupCores(boss: Npc) {
        val cores = boss.attr[CORES_ATTR] as? MutableList<WeakReference<Npc>> ?: return
        cores.mapNotNull { it.get() }.forEach { core ->
            if (core.isSpawned() && core.isActive()) {
                world.remove(core)
            }
        }
        cores.clear()
        boss.attr[CORES_ATTR] = cores
    }
}

