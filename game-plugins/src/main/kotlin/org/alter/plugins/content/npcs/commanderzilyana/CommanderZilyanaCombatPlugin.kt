package org.alter.plugins.content.npcs.commanderzilyana

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
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * Commander Zilyana Combat Plugin
 * 
 * Commander Zilyana is a fast melee boss with special mechanics:
 * - Very Fast Melee Attacks: Attacks every 2 ticks
 * - Long Movement Range: Chases player quickly
 * - Special Dash Attack: Every few ticks, unavoidable light damage
 * - No Magic/Ranged: Only uses melee attacks
 * 
 * Minions:
 * - Starlight (Melee)
 * - Growler (Magic)
 * - Bree (Range)
 */
class CommanderZilyanaCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute key to track minions spawned by this boss
        private val MINIONS_ATTR = AttributeKey<MutableList<WeakReference<Npc>>>("commander_zilyana_minions")
        private val LAST_DASH_ATTACK_ATTR = AttributeKey<Int>("last_dash_attack_tick")
        
        /**
         * Animation IDs for Zilyana's attacks
         */
        private const val MELEE_ATTACK_ANIM = Animation.COMMANDER_ZILYANA_MELEE_ATTACK
        
        /**
         * Graphics for Zilyana's attacks
         */
        private const val DASH_ATTACK_GFX = Graphic.COMMANDER_ZILYANA_MAGIC_HIT // Lightning/light graphic
        
        /**
         * Damage ranges
         */
        private const val MELEE_MAX_HIT = 27 // OSRS max hit
        private const val DASH_DAMAGE_MIN = 5
        private const val DASH_DAMAGE_MAX = 15 // Light damage
        
        /**
         * Dash attack timing
         */
        private const val DASH_ATTACK_INTERVAL = 8 // Every 8 ticks (approximately every few attacks)
    }
    
    init {
        /**
         * Handle Zilyana's combat
         */
        onNpcCombat("npc.commander_zilyana") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Spawn minions when Zilyana spawns
        onNpcSpawn("npc.commander_zilyana") {
            val npc = ctx as Npc
            // Initialize minion tracking
            if (npc.attr[MINIONS_ATTR] == null) {
                npc.attr[MINIONS_ATTR] = mutableListOf()
            }
            // Initialize dash attack timer
            npc.attr[LAST_DASH_ATTACK_ATTR] = 0
            // Spawn all three minions
            spawnMinions(npc)
        }
        
        // Clean up minions when boss dies
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.commander_zilyana")) {
                cleanupMinions(npc)
            }
        }
        
        // Clean up minions when minions die
        onAnyNpcDeath {
            val npc = ctx as Npc
            val minionIds = listOf(
                getRSCM("npc.starlight"),
                getRSCM("npc.growler"),
                getRSCM("npc.bree")
            )
            if (npc.id in minionIds && !npc.respawns) {
                // Remove this minion from its boss's minion list
                world.npcs.forEach { boss ->
                    if (boss.id == getRSCM("npc.commander_zilyana")) {
                        val minions = boss.attr[MINIONS_ATTR] ?: mutableListOf()
                        minions.removeAll { it.get() == null || it.get() == npc }
                        boss.attr[MINIONS_ATTR] = minions
                    }
                }
            }
        }
        
        // Configure minions to be aggressive to ALL players
        onNpcSpawn(npc = "npc.starlight") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MELEE
            }
        }
        
        onNpcSpawn(npc = "npc.growler") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.MAGIC
            }
        }
        
        onNpcSpawn(npc = "npc.bree") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.aggroCheck = { _, _ -> true }
                npc.combatClass = CombatClass.RANGED
            }
        }
    }
    
    /**
     * Main combat loop for Commander Zilyana
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return
        
        // Initialize minion tracking if not already set
        if (attr[MINIONS_ATTR] == null) {
            attr[MINIONS_ATTR] = mutableListOf()
        }
        
        // Initialize dash attack timer if not set
        if (attr[LAST_DASH_ATTACK_ATTR] == null) {
            attr[LAST_DASH_ATTACK_ATTR] = 0
        }
        
        // Update minions
        updateMinions()
        
        var attackCount = 0
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            // Long movement range - chase player quickly
            // Use larger distance tolerance for faster chasing
            if (moveToAttackRange(it, target, distance = 1, projectile = false)) {
                // Check if it's time for dash attack
                val currentTick = this.world.currentCycle
                val lastDashTick = attr[LAST_DASH_ATTACK_ATTR] ?: 0
                val ticksSinceLastDash = currentTick - lastDashTick
                
                if (isAttackDelayReady()) {
                    attackCount++
                    
                    // Dash attack every few ticks
                    if (ticksSinceLastDash >= DASH_ATTACK_INTERVAL) {
                        dashAttack(target)
                        attr[LAST_DASH_ATTACK_ATTR] = currentTick
                    } else {
                        // Regular fast melee attack
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
     * Melee attack - very fast, standard melee damage
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            // Cap at MELEE_MAX_HIT (27)
            val damage = minOf(this.world.random(maxHit + 1), MELEE_MAX_HIT)
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0) // Impact graphic
        } else {
            target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
        }
    }
    
    /**
     * Dash attack - unavoidable light damage
     * This is Zilyana's special attack that cannot be blocked
     */
    private fun Npc.dashAttack(target: Pawn) {
        // Show dash animation and graphic
        animate(MELEE_ATTACK_ANIM) // Use melee animation for dash
        graphic(id = DASH_ATTACK_GFX, height = 0)
        
        // Deal unavoidable typeless damage (light damage)
        val damage = this.world.random(DASH_DAMAGE_MIN..DASH_DAMAGE_MAX)
        
        // Always hits - unavoidable
        target.hit(damage, type = HitType.HIT, delay = 1)
        target.graphic(id = DASH_ATTACK_GFX, height = 0, delay = 1)
        
        if (target is Player && damage > 0) {
            target.message("Commander Zilyana dashes at you with unavoidable damage!")
        }
    }
    
    /**
     * Spawn all three minions
     */
    private fun spawnMinions(boss: Npc) {
        val bossTile = boss.tile
        
        // Spawn positions around Zilyana
        val spawnPositions = listOf(
            bossTile.transform(1, 0),   // East
            bossTile.transform(-1, 0),  // West
            bossTile.transform(0, 1)     // North
        )
        
        val minionTypes = listOf(
            "npc.starlight" to CombatClass.MELEE,
            "npc.growler" to CombatClass.MAGIC,
            "npc.bree" to CombatClass.RANGED
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

