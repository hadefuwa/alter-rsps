package org.alter.plugins.content.npcs.generalgraardor

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Graphic
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
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * General Graardor Combat Plugin
 * 
 * General Graardor is a slow, high-damage boss with special mechanics:
 * - Melee Attack: Heavy punches, very high max hit
 * - Ranged Attack: Big ranged attacks that alternate randomly with melee
 * - Range Stomp: If player stands under him (same tile), deals small damage
 * - Magic Shockwave Special: Random chance, typeless damage, cannot be prayed
 * 
 * Minions:
 * - Sergeant Strongstack (Melee)
 * - Sergeant Steelwill (Magic)
 * - Sergeant Grimspike (Range)
 */
class GeneralGraardorCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute key to track minions spawned by this boss
        private val MINIONS_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("general_graardor_minions")
        private val SPAWNING_MINION_ATTR = AttributeKey<Boolean>("spawning_minion") // Prevent concurrent spawns
        private const val MAX_MINIONS = 3 // Maximum total minions allowed
        private const val MINION_CLEANUP_DISTANCE = 20 // Remove minions more than 20 tiles from boss
        private const val MINION_TIMEOUT_TICKS = 300 // Remove minions after 5 minutes (300 ticks)
        
        /**
         * Animation IDs for Graardor's attacks
         */
        private const val MELEE_ATTACK_ANIM = Animation.GENERAL_GRAARDOR_MELEE_ATTACK
        private const val RANGED_ATTACK_ANIM = Animation.GENERAL_GRAARDOR_RANGED_ATTACK
        
        /**
         * Graphics for Graardor's attacks
         */
        private const val STOMP_GFX = 157 // Ground shake graphic
        private const val SHOCKWAVE_GFX = 1203 // Magic shockwave graphic
        private const val RANGED_PROJECTILE_GFX = 249 // Ranged projectile graphic
        
        /**
         * Damage ranges
         */
        private const val MELEE_MAX_HIT = 100 // Very high max hit
        private const val RANGED_MAX_HIT = 80 // High ranged max hit
        private const val STOMP_DAMAGE_MIN = 5
        private const val STOMP_DAMAGE_MAX = 15
        private const val SHOCKWAVE_DAMAGE_MIN = 20
        private const val SHOCKWAVE_DAMAGE_MAX = 50
        
        /**
         * Special attack chances
         */
        private const val SHOCKWAVE_CHANCE = 15 // 15% chance for magic shockwave
    }
    
    init {
        /**
         * Handle Graardor's combat
         */
        onNpcCombat("npc.general_graardor") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Spawn minions when Graardor spawns
        onNpcSpawn("npc.general_graardor") {
            val npc = ctx as Npc
            // Initialize minion tracking
            if (npc.attr[MINIONS_ATTR] == null) {
                npc.attr[MINIONS_ATTR] = mutableListOf()
            }
            // Spawn all three sergeants
            spawnSergeants(npc)
        }
        
        // Clean up minions when boss dies
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.general_graardor")) {
                cleanupMinions(npc)
            }
        }
        
        // Clean up minions when minions die
        onAnyNpcDeath {
            val npc = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.sergeant_strongstack"),
                getRSCM("npc.sergeant_steelwill"),
                getRSCM("npc.sergeant_grimspike")
            )
            if (npc.id in minionIds && !npc.respawns) {
                // Remove this minion from its boss's minion list
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.general_graardor")) {
                        val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                        minions.removeAll { it.get() == null || it.get() == npc }
                        boss.attr[MINIONS_ATTR] = minions
                    }
                }
            }
        }
        
        // Configure sergeants to be aggressive to ALL players
        onNpcSpawn(npc = "npc.sergeant_strongstack") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MELEE
            }
        }
        
        onNpcSpawn(npc = "npc.sergeant_steelwill") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MAGIC
            }
        }
        
        onNpcSpawn(npc = "npc.sergeant_grimspike") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.RANGED
            }
        }
    }
    
    /**
     * Main combat loop for General Graardor
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        // Initialize minion tracking if not already set
        if (attr[MINIONS_ATTR] == null) {
            attr[MINIONS_ATTR] = mutableListOf()
        }
        
        // Update minions
        updateMinions()
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Check for range stomp (player standing under Graardor)
            if (target is Player && target.tile.sameAs(this.tile)) {
                rangeStomp(target)
                it.wait(1)
                target = getCombatTarget() ?: break
                continue
            }
            
            // Check if we should use ranged attack (can attack from distance)
            val useRanged = this.world.random(100) < 50 // 50% chance for ranged attack
            
            if (useRanged) {
                // Move to ranged attack range (distance 10)
                if (moveToAttackRange(it, target, distance = 10, projectile = true) && isAttackDelayReady()) {
                    // Randomly choose between magic shockwave special or ranged attack
                    val useShockwave = this.world.random(100) < SHOCKWAVE_CHANCE
                    
                    if (useShockwave) {
                        magicShockwave(target)
                    } else {
                        rangedAttack(target)
                    }
                    
                    postAttackLogic(target)
                }
            } else {
                // Move to melee range
                if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                    // Randomly choose between magic shockwave special or melee attack
                    val useShockwave = this.world.random(100) < SHOCKWAVE_CHANCE
                    
                    if (useShockwave) {
                        magicShockwave(target)
                    } else {
                        meleeAttack(target)
                    }
                    
                    postAttackLogic(target)
                }
            }
            
            it.wait(1)
            target = getCombatTarget() ?: break
        }
        
        resetFacePawn()
        removeCombatTarget()
    }
    
    /**
     * Melee attack - slow, very high damage
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            // Cap at MELEE_MAX_HIT (100) - very high damage
            val damage = minOf(this.world.random(maxHit + 1), MELEE_MAX_HIT)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Ranged attack - big ranged attacks with projectile
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
            
            if (RangedCombatFormula.getAccuracy(this@rangedAttack, target) >= this@rangedAttack.world.randomDouble()) {
                val maxHit = RangedCombatFormula.getMaxHit(this@rangedAttack, target)
                // Cap at RANGED_MAX_HIT (80) - high ranged damage
                val damage = minOf(this@rangedAttack.world.random(maxHit + 1), RANGED_MAX_HIT)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }
    
    /**
     * Range stomp - deals small damage if player stands under Graardor
     */
    private fun Npc.rangeStomp(target: Player) {
        // Show stomp animation and graphic
        graphic(id = STOMP_GFX, height = 0)
        
        // Deal small typeless damage (cannot be prayed)
        val damage = this.world.random(STOMP_DAMAGE_MIN..STOMP_DAMAGE_MAX)
        target.hit(damage, type = HitType.HIT, delay = 0)
        target.graphic(id = STOMP_GFX, height = 0, delay = 0)
        
        if (damage > 0) {
            target.message("General Graardor stomps on you!")
        }
    }
    
    /**
     * Magic shockwave special - typeless damage, cannot be prayed
     */
    private fun Npc.magicShockwave(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.GENERAL_GRAARDOR_RANGED_ATTACK) // Use ranged animation for shockwave
        
        // Show shockwave graphic on Graardor
        graphic(id = SHOCKWAVE_GFX, height = 0)
        
        // Create shockwave projectile
        val projectile = createProjectile(
            target,
            gfx = SHOCKWAVE_GFX,
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay
        val hitDelay = MagicCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal typeless damage (cannot be prayed against)
        this.world.queue {
            wait(hitDelay - 1)
            
            // Typeless damage - bypasses all prayers and protection
            val damage = this@magicShockwave.world.random(SHOCKWAVE_DAMAGE_MIN..SHOCKWAVE_DAMAGE_MAX)
            
            // Always hits (typeless damage ignores accuracy)
            target.hit(damage, type = HitType.HIT)
            target.graphic(id = SHOCKWAVE_GFX, height = 0) // Impact graphic
            
            if (target is Player && damage > 0) {
                target.message("General Graardor's magic shockwave cannot be blocked!")
            }
        }
    }
    
    /**
     * Spawn all three sergeants as minions
     */
    private fun spawnSergeants(boss: Npc) {
        val bossTile = boss.tile
        
        // Spawn positions around Graardor
        val spawnPositions = listOf(
            bossTile.transform(1, 0),   // East
            bossTile.transform(-1, 0),  // West
            bossTile.transform(0, 1)     // North
        )
        
        val sergeantTypes = listOf(
            "npc.sergeant_strongstack" to CombatClass.MELEE,
            "npc.sergeant_steelwill" to CombatClass.MAGIC,
            "npc.sergeant_grimspike" to CombatClass.RANGED
        )
        
        sergeantTypes.forEachIndexed { index, (npcName, combatClass) ->
            val spawnTile = spawnPositions.getOrNull(index) ?: bossTile.transform(0, -1)
            
            try {
                val minion = Npc(getRSCM(npcName), spawnTile, world)
                minion.respawns = false
                minion.walkRadius = 5
                minion.setActive(true)
                minion.combatClass = combatClass
                
                world.spawn(minion)
                
                // Add to minion list
                val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                minions.add(WeakReference(minion))
                boss.attr[MINIONS_ATTR] = minions
            } catch (e: Exception) {
                println("Error spawning sergeant: ${e.message}")
            }
        }
    }
    
    /**
     * Update and clean up minions
     */
    private fun Npc.updateMinions() {
        val minions = attr[MINIONS_ATTR] ?: return
        val activeMinions = minions.mapNotNull { it.get() }.filter { it.isActive() && it.isSpawned() }
        
        // Clean up dead/invalid minions
        minions.removeAll { it.get() == null || !it.get()!!.isActive() || !it.get()!!.isSpawned() }
        
        // Clean up minions that are too far from boss
        activeMinions.forEach { minion ->
            val distance = tile.getDistance(minion.tile)
            if (distance > MINION_CLEANUP_DISTANCE) {
                world.remove(minion)
                minions.removeAll { it.get() == minion }
            }
        }
        
        attr[MINIONS_ATTR] = minions
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
}
