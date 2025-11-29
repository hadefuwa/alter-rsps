package org.alter.plugins.content.npcs.sewerabomination

import dev.openrune.cache.CacheManager.getItem
import dev.openrune.cache.CacheManager.itemSize
import org.alter.api.*
import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.KILLER_ATTR
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.model.timer.TimeConstants
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.rscm.RSCM.getRSCM
import org.alter.game.model.attr.AttributeKey
import org.alter.api.PrayerIcon
import java.lang.ref.WeakReference
import kotlin.random.Random

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

    companion object {
        // Attribute key to track minions spawned by this boss
        private val MINIONS_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("sewer_abomination_minions")
        private val SPAWNING_MINION_ATTR = AttributeKey<Boolean>("spawning_minion") // Prevent concurrent spawns
        private const val MAX_MINIONS = 3 // Maximum total minions allowed
        private const val MINION_CLEANUP_DISTANCE = 20 // Remove minions more than 20 tiles from boss
        private const val MINION_TIMEOUT_TICKS = 300 // Remove minions after 5 minutes (300 ticks)
        
        /**
         * Sewer Abomination spawn location (to differentiate from Cerberus which uses the same NPC ID)
         */
        private const val SEWER_ABOMINATION_SPAWN_X = 3237
        private const val SEWER_ABOMINATION_SPAWN_Z = 9866
        
        /**
         * Cerberus spawn location
         */
        private const val CERBERUS_SPAWN_X = 1240
        private const val CERBERUS_SPAWN_Z = 1253
        
        private const val LOCATION_TOLERANCE = 10 // Allow 10 tile radius from spawn point
        
        /**
         * Cerberus combat constants
         */
        private const val MELEE_ATTACK_CHANCE_NUMERATOR = 1
        private const val MELEE_ATTACK_CHANCE_DENOMINATOR = 4
        private const val MELEE_MIN_DAMAGE = 30
        private const val MELEE_MAX_DAMAGE = 60
    }
    
    /**
     * Checks if this NPC is the Sewer Abomination (not Cerberus)
     * Both use the same NPC ID (5862), so we check location
     */
    private fun Npc.isSewerAbomination(): Boolean {
        val distanceX = Math.abs(tile.x - SEWER_ABOMINATION_SPAWN_X)
        val distanceZ = Math.abs(tile.z - SEWER_ABOMINATION_SPAWN_Z)
        return distanceX <= LOCATION_TOLERANCE && distanceZ <= LOCATION_TOLERANCE
    }
    
    /**
     * Checks if this NPC is the actual Cerberus (not the Sewer Abomination)
     */
    private fun Npc.isActualCerberus(): Boolean {
        val distanceX = Math.abs(tile.x - CERBERUS_SPAWN_X)
        val distanceZ = Math.abs(tile.z - CERBERUS_SPAWN_Z)
        return distanceX <= LOCATION_TOLERANCE && distanceZ <= LOCATION_TOLERANCE
    }

    init {
        // Set Cerberus's default combat class to MAGIC (only for actual Cerberus)
        onNpcSpawn("npc.cerberus") {
            if (npc.isActualCerberus()) {
                npc.combatClass = CombatClass.MAGIC
            }
        }
        
        // Using Cerberus NPC model (5862) - handle both Sewer Abomination and Cerberus
        onNpcCombat("npc.cerberus") {
            npc.queue {
                // Route to appropriate combat logic based on location
                if (npc.isSewerAbomination()) {
                    npc.combat(this) // Sewer Abomination combat
                } else if (npc.isActualCerberus()) {
                    npc.cerberusCombat(this) // Cerberus combat
                }
            }
        }


        // Configure gnome minions to be aggressive to ALL players (like dark wizards)
        // This runs after NpcAggroPlugin sets the default aggroCheck, so we override it
        onNpcSpawn(npc = "npc.gnome_driver") {
            val npc = ctx as Npc
            // Only apply to minions spawned by the boss (they don't respawn)
            if (!npc.respawns) {
                // Override aggroCheck to be aggressive to ALL players regardless of combat level
                npc.aggroCheck = { _, _ -> true }
            }
        }
        
        onNpcSpawn(npc = "npc.gnome_archer") {
            val npc = ctx as Npc
            // Only apply to minions spawned by the boss (they don't respawn)
            if (!npc.respawns) {
                // Override aggroCheck to be aggressive to ALL players regardless of combat level
                npc.aggroCheck = { _, _ -> true }
            }
        }
        
        onNpcSpawn(npc = "npc.gnome_mage") {
            val npc = ctx as Npc
            // Only apply to minions spawned by the boss (they don't respawn)
            if (!npc.respawns) {
                // Override aggroCheck to be aggressive to ALL players regardless of combat level
                npc.aggroCheck = { _, _ -> true }
            }
        }

        // Custom combat handlers for minions to ensure they hit hard
        // Only apply to minions spawned by the boss (they don't respawn)
        onNpcCombat("npc.gnome_driver") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.MELEE)
                }
            }
        }
        
        onNpcCombat("npc.gnome_archer") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.RANGED)
                }
            }
        }
        
        onNpcCombat("npc.gnome_mage") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.MAGIC)
                }
            }
        }

        // Override unwanted NPC options for minions (they should only be attackable)
        // Only register handlers for options that actually exist on these NPCs
        // Archers should only have "Attack" option, so no need to override anything
        
        // Check if gnome_mage has "talk-to" option and override it if it exists
        if (npcHasOption("npc.gnome_mage", "talk-to")) {
            onNpcOption("npc.gnome_mage", option = "talk-to") {
                val npc = ctx as Npc
                // If this is a minion (doesn't respawn), redirect to attack instead
                if (!npc.respawns) {
                    // Redirect to attack instead
                    player.attack(npc)
                }
            }
        }

        // Minions will get random drops through the main NpcLootDropPlugin system
        // based on their combat level defined in CombatConfigPlugin.kt
        
        // Clean up minions when boss dies
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.cerberus")) {
                cleanupMinions(npc)
            }
        }
        
        // Clean up minions when minions die
        onAnyNpcDeath {
            val npc = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.gnome_driver"),
                getRSCM("npc.gnome_archer"),
                getRSCM("npc.gnome_mage")
            )
            if (npc.id in minionIds && !npc.respawns) {
                // Remove this minion from its boss's minion list
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.cerberus")) {
                        val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                        minions.removeAll { it.get() == null || it.get() == npc }
                        boss.attr[MINIONS_ATTR] = minions
                    }
                }
            }
        }
    }







    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        // Initialize minion tracking if not already set
        if (attr[MINIONS_ATTR] == null) {
            attr[MINIONS_ATTR] = mutableListOf()
        }

        // Track minion spawns per type to avoid duplicates
        val minions = attr[MINIONS_ATTR]!!
        val activeMinions = minions.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        
        // Clean up dead/invalid minions from the list
        minions.removeAll { it.get() == null || !it.get()!!.isActive() || !it.get()!!.isSpawned() }
        
        // Clean up minions that are too far from boss
        activeMinions.forEach { minion ->
            val distance = tile.getDistance(minion.tile)
            if (distance > MINION_CLEANUP_DISTANCE) {
                world.remove(minion)
                minions.removeAll { it.get() == minion }
            }
        }
        
        val meleeMinionSpawned = activeMinions.any { it.id == getRSCM("npc.gnome_driver") }
        val rangedMinionSpawned = activeMinions.any { it.id == getRSCM("npc.gnome_archer") }
        val mageMinionSpawned = activeMinions.any { it.id == getRSCM("npc.gnome_mage") }

        while (canEngageCombat(target)) {
            facePawn(target)

            // Check HP thresholds and spawn minions (only if we haven't reached max)
            val currentActiveMinions = attr[MINIONS_ATTR]!!.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
            
            if (currentActiveMinions.size < MAX_MINIONS) {
                val hpPercent = (getCurrentHp().toDouble() / getMaxHp().toDouble()) * 100

                // Spawn melee minion at 40% HP (delayed significantly)
                if (!meleeMinionSpawned && hpPercent <= 40 && currentActiveMinions.size < MAX_MINIONS) {
                    spawnMinion("melee")
                    if (target is Player) {
                        target.message("The abomination summons a corrupted melee fighter!")
                    }
                }

                // Spawn ranged minion at 20% HP (delayed significantly)
                if (!rangedMinionSpawned && hpPercent <= 20 && currentActiveMinions.size < MAX_MINIONS) {
                    spawnMinion("ranged")
                    if (target is Player) {
                        target.message("The abomination summons a corrupted archer!")
                    }
                }

                // Spawn mage minion at 5% HP (delayed significantly)
                if (!mageMinionSpawned && hpPercent <= 5 && currentActiveMinions.size < MAX_MINIONS) {
                    spawnMinion("mage")
                    if (target is Player) {
                        target.message("The abomination summons a corrupted sorcerer!")
                    }
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
            val damage = this.world.random(maxHit + 10)  // Reduced bonus damage for easier fight
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
                target.hit(this@toxicSpitAttack.world.random(30), type = HitType.HIT)  // Much lower damage for easier fight
                target.graphic(id = 289, height = 0) // Poison splash

                // 25% chance to poison (reduced for easier fight)
                if (this@toxicSpitAttack.world.chance(1, 4)) {
                    target.poison(initialDamage = 3) {  // Much lower poison damage
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
                target.hit(this@sewerGasAttack.world.random(25), type = HitType.HIT)  // Much lower damage for easier fight

                // 30% chance to poison (reduced for easier fight)
                if (this@sewerGasAttack.world.chance(3, 10)) {
                    target.poison(initialDamage = 2) {  // Much lower poison damage
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
            // Much lower damage bonus for easier fight
            val damage = maxOf(1, maxHit + 5)
            target.hit(this.world.random(damage), type = HitType.HIT, delay = 1)

            // Always poison on successful hit but with low damage
            target.poison(initialDamage = 2) {  // Much lower poison damage
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
                // First hit - much lower damage for easier fight
                val hit1 = this@acidWaveAttack.world.random(20)
                target.hit(hit1, type = HitType.HIT)
                target.graphic(id = 167, height = 0) // Acid splash

                if (target is Player) {
                    target.message("Acid burns you!")
                }

                wait(2)

                // Second hit (20% chance, much lower) - reduced damage
                if (this@acidWaveAttack.world.chance(1, 5)) {
                    val hit2 = this@acidWaveAttack.world.random(15)
                    target.hit(hit2, type = HitType.HIT)
                    target.graphic(id = 167, height = 0)

                    wait(2)

                    // Third hit (10% chance, much lower) - reduced damage
                    if (this@acidWaveAttack.world.chance(1, 10)) {
                        val hit3 = this@acidWaveAttack.world.random(10)
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
                // Deal low damage for easier fight
                val damage = this@plagueBreathAttack.world.random(20)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 145, height = 100) // Dark cloud splash

                // Minimal stat drain for easier fight
                if (target is Player) {
                    target.message("The plague breath weakens you slightly!")

                    // Drain attack by 1-2 levels (much reduced)
                    val attackDrain = this@plagueBreathAttack.world.random(2) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.ATTACK, value = 0 - attackDrain, capValue = 0)

                    // Drain strength by 1-2 levels (much reduced)
                    val strengthDrain = this@plagueBreathAttack.world.random(2) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.STRENGTH, value = 0 - strengthDrain, capValue = 0)

                    // Drain defence by 1-2 levels (much reduced)
                    val defenceDrain = this@plagueBreathAttack.world.random(2) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.DEFENCE, value = 0 - defenceDrain, capValue = 0)
                }
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Custom combat handler for minions to ensure they deal significant damage
     */
    private suspend fun minionCombat(npc: Npc, it: QueueTask, combatClass: CombatClass) {
        var target = npc.getCombatTarget() ?: return
        
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            if (npc.moveToAttackRange(it, target, distance = if (combatClass == CombatClass.MELEE) 1 else 7, projectile = true) && npc.isAttackDelayReady()) {
                when (combatClass) {
                    CombatClass.MELEE -> minionMeleeAttack(npc, target)
                    CombatClass.RANGED -> minionRangedAttack(npc, target)
                    CombatClass.MAGIC -> minionMagicAttack(npc, target)
                    else -> return
                }
                npc.postAttackLogic(target)
            }
            it.wait(1)
            target = npc.getCombatTarget() ?: break
        }
        
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
    
    /**
     * Melee minion attack - deals high melee damage
     */
    private fun minionMeleeAttack(npc: Npc, target: Pawn) {
        npc.prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        npc.animate(422)
        
        if (MeleeCombatFormula.getAccuracy(npc, target) >= npc.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(npc, target)
            // Minions hit much softer for easier fight
            val damage = npc.world.random(maxHit + 10)
            target.hit(damage, type = HitType.HIT, delay = 1)
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Ranged minion attack - deals high ranged damage
     */
    private fun minionRangedAttack(npc: Npc, target: Pawn) {
        val projectile = npc.createProjectile(
            target,
            gfx = 249, // Arrow projectile
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        
        npc.prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        npc.animate(426)
        npc.world.spawn(projectile)
        
        val hitDelay = RangedCombatStrategy.getHitDelay(npc.getFrontFacingTile(target), target.getCentreTile())
        
        npc.world.queue {
            wait(hitDelay - 1)
            
            // Use ranged accuracy formula for ranged attacks
            val accuracy = if (npc.combatDef.ranged > 0) {
                // Lower accuracy for easier fight
                npc.world.randomDouble() < 0.60 // 60% accuracy
            } else {
                MeleeCombatFormula.getAccuracy(npc, target) >= npc.world.randomDouble()
            }
            
            if (accuracy) {
                // Ranged minions hit much softer for easier fight
                val damage = npc.world.random(20)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 250, height = 0)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }
    
    /**
     * Magic minion attack - deals high magic damage
     */
    private fun minionMagicAttack(npc: Npc, target: Pawn) {
        val projectile = npc.createProjectile(
            target,
            gfx = 162, // Magic projectile
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        
        npc.prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.AGGRESSIVE)
        npc.animate(711)
        npc.world.spawn(projectile)
        
        val hitDelay = RangedCombatStrategy.getHitDelay(npc.getFrontFacingTile(target), target.getCentreTile())
        
        npc.world.queue {
            wait(hitDelay - 1)
            
            if (MagicCombatFormula.getAccuracy(npc, target) >= npc.world.randomDouble()) {
                // Magic minions hit much softer for easier fight
                val damage = npc.world.random(25)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 163, height = 0)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }



    /**
     * Spawns a minion near the boss based on type
     * Minions are temporary and will not respawn after death
     * Maximum of 3 total minions can exist at once
     */
    private fun Npc.spawnMinion(type: String) {
        // Prevent concurrent spawns
        if (attr.getOrDefault(SPAWNING_MINION_ATTR, false)) {
            return
        }
        
        // Visual effect at boss location
        this.graphic(id = 86, height = 100) // Purple spawn effect on boss

        val target = getCombatTarget() ?: return
        if (target !is Player) return

        // Get current minion list and clean up dead minions first
        val minions = attr[MINIONS_ATTR] ?: mutableListOf()
        // Remove dead or inactive minions from the list
        minions.removeAll { it.get() == null || !it.get()!!.isActive() || !it.get()!!.isSpawned() }
        attr[MINIONS_ATTR] = minions
        
        val activeMinions = minions.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        
        // Check if we've reached the max minion limit
        if (activeMinions.size >= MAX_MINIONS) {
            return
        }

        // Determine which NPC to spawn based on type
        val npcName = when (type) {
            "melee" -> "npc.gnome_driver"      // Melee fighter (Gnome Driver)
            "ranged" -> "npc.gnome_archer"     // Ranged attacker (Gnome Archer)
            "mage" -> "npc.gnome_mage"  // Magic user (Gnome Mage)
            else -> return
        }
        
        val minionId = getRSCM(npcName)
        
        // Check if a minion of this type already exists
        val existingMinion = activeMinions.firstOrNull { it.id == minionId }
        
        // If a minion of this type already exists, don't spawn another one
        if (existingMinion != null) {
            return
        }
        
        // Re-check active minions count after cleanup (in case some were removed)
        val currentActiveMinions = minions.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        if (currentActiveMinions.size >= MAX_MINIONS) {
            return
        }

        // Set flag to prevent concurrent spawns
        attr[SPAWNING_MINION_ATTR] = true

        try {
            // Find a spawn location near the boss (1-2 tiles away)
            val bossTile = this.tile
            val spawnOffsetX = world.random(-2..2) // -2 to 2
            val spawnOffsetZ = world.random(-2..2) // -2 to 2
            val spawnTile = bossTile.transform(spawnOffsetX, spawnOffsetZ)

            // Create and spawn the minion
            val minion = Npc(getRSCM(npcName), spawnTile, world)
            minion.respawns = false // Minions don't respawn - they're temporary
            minion.walkRadius = 5 // Allow minions to move to attack players
            minion.setActive(true)
            
            // Set combat class based on minion type
            when (type) {
                "melee" -> minion.combatClass = CombatClass.MELEE
                "ranged" -> minion.combatClass = CombatClass.RANGED
                "mage" -> minion.combatClass = CombatClass.MAGIC
            }

            // Spawn the minion in the world
            world.spawn(minion)
            
            // Add minion to boss's minion list
            minions.add(WeakReference(minion))
            attr[MINIONS_ATTR] = minions

            // Use a queue task to ensure minion is fully initialized before attacking
            minion.queue {
                wait(1) // Wait one tick for NPC to be fully initialized
                // Make the minion attack the player immediately
                minion.attack(target)
            }
            
            // Set timeout to remove minion after 5 minutes
            world.queue {
                wait(MINION_TIMEOUT_TICKS)
                if (minion.isSpawned() && minion.isActive()) {
                    world.remove(minion)
                    val updatedMinions = attr[MINIONS_ATTR] ?: mutableListOf()
                    updatedMinions.removeAll { it.get() == null || it.get() == minion }
                    attr[MINIONS_ATTR] = updatedMinions
                }
            }

            // Show effect to player
            target.graphic(id = 86, height = 0)
        } catch (e: Exception) {
            // If spawn fails, log the error but don't crash
            println("Error spawning minion: ${e.message}")
            e.printStackTrace()
        } finally {
            // Always clear the spawning flag
            attr[SPAWNING_MINION_ATTR] = false
        }
    }
    
    /**
     * Cleans up all minions spawned by the boss when the boss dies
     */
    private fun cleanupMinions(boss: Npc) {
        val minions = boss.attr[MINIONS_ATTR] ?: return
        minions.mapNotNull { it.get() }.forEach { minion ->
            if (minion.isSpawned() && minion.isActive()) {
                world.remove(minion)
            }
        }
        boss.attr.remove(MINIONS_ATTR)
    }
    
    /**
     * ========== CERBERUS COMBAT LOGIC ==========
     * Cerberus uses magic as his main attack, with a special melee attack
     * that shouts "Raaa" and hits 30-60 damage (fully negated by Protect from Melee).
     */
    
    /**
     * Main combat loop for Cerberus
     */
    private suspend fun Npc.cerberusCombat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                // Decide between magic attack (main) or special melee attack
                val useMeleeAttack = world.chance(MELEE_ATTACK_CHANCE_NUMERATOR, MELEE_ATTACK_CHANCE_DENOMINATOR)
                
                if (useMeleeAttack) {
                    cerberusSpecialMeleeAttack(target, it)
                } else {
                    cerberusMagicAttack(target)
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
     * Cerberus's main magic attack
     * Hits 5-25 if player is not praying, 0 if player is praying Protect from Magic
     */
    private fun Npc.cerberusMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        
        // Use a fire-based spell for Cerberus
        val spell = CombatSpell.FIRE_BLAST
        val projectile = createProjectile(
            target,
            gfx = spell.projectile,
            type = ProjectileType.MAGIC,
            endHeight = spell.projectilEndHeight,
        )
        
        animate(spell.castAnimation)
        world.spawn(projectile)
        
        val hitDelay = MagicCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        
        // Check for Protect from Magic prayer
        if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
            // Protect from Magic completely blocks Cerberus's magic damage
            target.hit(0, type = HitType.BLOCK, delay = hitDelay - 1, attackersIndex = index)
        } else {
            // Cerberus hits 5-25 damage when player is not praying
            val damage = world.random(5..25) // Random between 5 and 25 (inclusive)
            target.hit(damage, type = HitType.HIT, delay = hitDelay - 1, attackersIndex = index)
        }
        
        // Show impact graphic
        spell.impactGfx?.let { impact ->
            target.graphic(impact.id, impact.height, delay = hitDelay - 1)
        }
    }
    
    /**
     * Cerberus's special melee attack
     * Shouts "Raaa" above his head, waits 1 second, then hits 30-60 damage
     * Fully negated by Protect from Melee prayer
     */
    private suspend fun Npc.cerberusSpecialMeleeAttack(target: Pawn, it: QueueTask) {
        // Shout "Raaa" above his head
        forceChat("Raaa")
        
        // Play roar/growl sound effect when Cerberus does the "Raaa" attack
        if (target is Player) {
            world.spawn(
                org.alter.game.model.entity.AreaSound(
                    this.tile,
                    org.alter.api.cfg.Sound.SKELETAL_HELLHOUND_ATTACK,
                    radius = 10,
                    volume = 50
                )
            )
        }
        
        // Show blast wave graphic effect on Cerberus
        graphic(id = 157, height = 0) // Ground shockwave/blast wave graphic
        
        // Also show blast wave effect on the ground around Cerberus
        val cerberusTile = this.tile
        for (x in -1..1) {
            for (z in -1..1) {
                val tile = cerberusTile.transform(x, z)
                world.spawn(org.alter.game.model.TileGraphic(tile, id = 157, height = 0, delay = 0))
            }
        }
        
        // Wait 1 second (2 ticks = ~1.2 seconds)
        it.wait(2)
        
        // Check if target is still valid
        if (!isAlive() || target.isDead() || !canEngageCombat(target)) {
            return
        }
        
        // Prepare melee attack
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(4925) // Melee attack animation (can be adjusted)
        
        // Check for Protect from Melee prayer
        if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
            // Fully negate the attack
            target.hit(0, type = HitType.BLOCK, delay = 1)
            target.message("Your protection prayer fully negates Cerberus's melee attack!")
        } else {
            // Deal 30-60 damage
            val damage = world.random(MELEE_MIN_DAMAGE..MELEE_MAX_DAMAGE)
            target.hit(damage, type = HitType.HIT, delay = 1)
            
            if (target is Player) {
                target.message("Cerberus's powerful melee strike hits you!")
            }
        }
    }
}
