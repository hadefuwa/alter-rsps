package org.alter.plugins.content.npcs.kriltsutsaroth

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
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * K'ril Tsutsaroth Combat Plugin
 * 
 * K'ril Tsutsaroth is a hard-hitting melee boss with special mechanics:
 * - Default Attack: Melee, very high damage
 * - Special Attack: Typeless damage that ignores prayer and drains prayer points (around 12)
 * - Magic Splash Attack: Rare magic attack
 * 
 * Minions:
 * - Balfrug Kreeyath (Magic)
 * - Tstanon Karlak (Melee)
 * - Zakl'n Gritch (Range)
 */
class KrilTsutsarothCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute key to track minions spawned by this boss
        private val MINIONS_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("kril_tsutsaroth_minions")
        
        /**
         * Animation IDs for K'ril's attacks
         */
        private const val MELEE_ATTACK_ANIM = Animation.KRIL_TSUTSAROTH_ATTACK
        private const val MAGIC_ATTACK_ANIM = Animation.KRIL_TSUTSAROTH_MAGIC_ATTACK
        
        /**
         * Graphics for K'ril's attacks
         */
        private const val MAGIC_PROJECTILE_GFX = Graphic.KRIL_TSUTSAROTH_MAGIC_PROJECTILE
        private const val SPECIAL_ATTACK_GFX = Graphic.KRIL_TSUTSAROTH_MAGIC_ATTACK
        
        /**
         * Damage ranges
         */
        private const val MELEE_MAX_HIT = 60 // Very high damage
        private const val SPECIAL_DAMAGE_MIN = 20
        private const val SPECIAL_DAMAGE_MAX = 40
        private const val MAGIC_SPLASH_DAMAGE_MIN = 10
        private const val MAGIC_SPLASH_DAMAGE_MAX = 25
        
        /**
         * Prayer drain amount
         */
        private const val PRAYER_DRAIN_AMOUNT = 12
        
        /**
         * Attack chances
         */
        private const val SPECIAL_ATTACK_CHANCE = 25 // 25% chance for special attack
        private const val MAGIC_SPLASH_CHANCE = 10 // 10% chance for magic splash (rare)
    }
    
    init {
        /**
         * Handle K'ril's combat
         */
        onNpcCombat("npc.kril_tsutsaroth") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Spawn minions when K'ril spawns
        onNpcSpawn("npc.kril_tsutsaroth") {
            val npc = ctx as Npc
            // Initialize minion tracking
            if (npc.attr[MINIONS_ATTR] == null) {
                npc.attr[MINIONS_ATTR] = mutableListOf()
            }
            // Spawn all three minions
            spawnMinions(npc)
        }
        
        // Clean up minions when boss dies
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.kril_tsutsaroth")) {
                cleanupMinions(npc)
            }
        }
        
        // Clean up minions when minions die
        onAnyNpcDeath {
            val npc = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.balfrug_kreeyath"),
                getRSCM("npc.tstanon_karlak"),
                getRSCM("npc.zakln_gritch")
            )
            if (npc.id in minionIds && !npc.respawns) {
                // Remove this minion from its boss's minion list
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.kril_tsutsaroth")) {
                        val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                        minions.removeAll { it.get() == null || it.get() == npc }
                        boss.attr[MINIONS_ATTR] = minions
                    }
                }
            }
        }
        
        // Configure minions to be aggressive to ALL players
        onNpcSpawn(npc = "npc.balfrug_kreeyath") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MAGIC
            }
        }
        
        onNpcSpawn(npc = "npc.tstanon_karlak") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MELEE
            }
        }
        
        onNpcSpawn(npc = "npc.zakln_gritch") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.RANGED
            }
        }
    }
    
    /**
     * Main combat loop for K'ril Tsutsaroth
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
            
            // Move to melee range
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                // Randomly choose attack type
                val roll = this.world.random(100)
                
                when {
                    roll < MAGIC_SPLASH_CHANCE -> {
                        // Rare magic splash attack
                        magicSplashAttack(target)
                    }
                    roll < (MAGIC_SPLASH_CHANCE + SPECIAL_ATTACK_CHANCE) -> {
                        // Special attack: typeless damage + prayer drain
                        specialAttack(target)
                    }
                    else -> {
                        // Default melee attack (very high damage)
                        meleeAttack(target)
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
     * Melee attack - very high damage
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            // Cap at MELEE_MAX_HIT (60) - very high damage
            val damage = minOf(this.world.random(maxHit + 1), MELEE_MAX_HIT)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Special attack - typeless damage that ignores prayer and drains prayer points
     */
    private fun Npc.specialAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        // Show special attack graphic
        graphic(id = SPECIAL_ATTACK_GFX, height = 0)
        
        // Deal typeless damage (ignores prayer, always hits)
        val damage = this.world.random(SPECIAL_DAMAGE_MIN..SPECIAL_DAMAGE_MAX)
        target.hit(damage, type = HitType.HIT, delay = 1)
        target.graphic(id = SPECIAL_ATTACK_GFX, height = 0, delay = 1)
        
        // Drain prayer points (around 12)
        if (target is Player) {
            val currentPrayer = target.getSkills().getCurrentLevel(Skills.PRAYER)
            val drainAmount = minOf(PRAYER_DRAIN_AMOUNT, currentPrayer)
            if (drainAmount > 0) {
                target.getSkills().alterCurrentLevel(skill = Skills.PRAYER, value = -drainAmount, capValue = 0)
                target.message("K'ril Tsutsaroth drains your Prayer points!")
            }
            
            if (damage > 0) {
                target.message("K'ril Tsutsaroth's special attack cannot be blocked!")
            }
        }
    }
    
    /**
     * Magic splash attack - rare magic attack
     */
    private fun Npc.magicSplashAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_BLAST
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
        val hitDelay = MagicCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal magic damage
        this.world.queue {
            wait(hitDelay - 1)
            
            if (MagicCombatFormula.getAccuracy(this@magicSplashAttack, target) >= this@magicSplashAttack.world.randomDouble()) {
                val damage = this@magicSplashAttack.world.random(MAGIC_SPLASH_DAMAGE_MIN..MAGIC_SPLASH_DAMAGE_MAX)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = MAGIC_PROJECTILE_GFX, height = 0) // Impact graphic
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
        
        // Clear spell attribute after attack
        attr.remove(Combat.CASTING_SPELL)
    }
    
    /**
     * Spawn all three minions
     */
    private fun spawnMinions(boss: Npc) {
        val bossTile = boss.tile
        
        // Spawn positions around K'ril
        val spawnPositions = listOf(
            bossTile.transform(1, 0),   // East
            bossTile.transform(-1, 0),  // West
            bossTile.transform(0, 1)     // North
        )
        
        val minionTypes = listOf(
            "npc.balfrug_kreeyath" to CombatClass.MAGIC,
            "npc.tstanon_karlak" to CombatClass.MELEE,
            "npc.zakln_gritch" to CombatClass.RANGED
        )
        
        minionTypes.forEachIndexed { index, (npcName, combatClass) ->
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
                println("Error spawning minion: ${e.message}")
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
            if (distance > 20) {
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


