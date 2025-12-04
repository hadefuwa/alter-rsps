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
        
        // Combat phase attributes
        private val RAGE_MODE_ATTR = AttributeKey<Boolean>("rage_mode")
        private val ENRAGE_MODE_ATTR = AttributeKey<Boolean>("enrage_mode")
        private val WAR_CRY_ACTIVE_ATTR = AttributeKey<Boolean>("war_cry_active")
        private val WAR_CRY_END_ATTR = AttributeKey<Int>("war_cry_end_tick")
        private val LAST_WAR_CRY_ATTR = AttributeKey<Int>("last_war_cry_tick")
        private val DEFENSIVE_STANCE_ATTR = AttributeKey<Boolean>("defensive_stance")
        private val DEFENSIVE_STANCE_END_ATTR = AttributeKey<Int>("defensive_stance_end_tick")
        
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
        private const val MELEE_MAX_HIT = 62 // Very high max hit
        private const val RAGE_MELEE_MAX_HIT = 75 // Enraged melee
        private const val RANGED_MAX_HIT = 45 // High ranged max hit
        private const val STOMP_DAMAGE_MIN = 8
        private const val STOMP_DAMAGE_MAX = 20
        private const val SHOCKWAVE_DAMAGE_MIN = 25
        private const val SHOCKWAVE_DAMAGE_MAX = 50
        private const val GROUND_SLAM_DAMAGE_MIN = 15
        private const val GROUND_SLAM_DAMAGE_MAX = 35
        
        /**
         * Rage mechanics
         */
        private const val RAGE_THRESHOLD_HP = 100 // Enter rage mode below this HP
        private const val ENRAGE_THRESHOLD_HP = 50 // Enter enrage mode below this HP
        private const val RAGE_DAMAGE_MULTIPLIER = 1.25 // 25% more damage when enraged
        
        /**
         * War cry mechanics
         */
        private const val WAR_CRY_CHANCE = 20 // 20% chance for war cry
        private const val WAR_CRY_COOLDOWN = 15 // 15 ticks between war cries
        private const val WAR_CRY_BUFF_DURATION = 10 // 10 ticks of increased damage
        
        /**
         * Defensive stance
         */
        private const val DEFENSIVE_STANCE_CHANCE = 15 // 15% chance when low HP
        private const val DEFENSIVE_STANCE_DURATION = 8 // 8 ticks of reduced damage
        
        /**
         * Special attack chances
         */
        private const val SHOCKWAVE_CHANCE = 15 // 15% chance for magic shockwave
        private const val GROUND_SLAM_CHANCE = 20 // 20% chance for ground slam
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
            // Initialize combat attributes
            npc.attr[RAGE_MODE_ATTR] = false
            npc.attr[ENRAGE_MODE_ATTR] = false
            npc.attr[WAR_CRY_ACTIVE_ATTR] = false
            npc.attr[WAR_CRY_END_ATTR] = 0
            npc.attr[LAST_WAR_CRY_ATTR] = 0
            npc.attr[DEFENSIVE_STANCE_ATTR] = false
            npc.attr[DEFENSIVE_STANCE_END_ATTR] = 0
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
        
        // Initialize all attributes if not set
        if (attr[RAGE_MODE_ATTR] == null) attr[RAGE_MODE_ATTR] = false
        if (attr[ENRAGE_MODE_ATTR] == null) attr[ENRAGE_MODE_ATTR] = false
        if (attr[WAR_CRY_ACTIVE_ATTR] == null) attr[WAR_CRY_ACTIVE_ATTR] = false
        if (attr[WAR_CRY_END_ATTR] == null) attr[WAR_CRY_END_ATTR] = 0
        if (attr[LAST_WAR_CRY_ATTR] == null) attr[LAST_WAR_CRY_ATTR] = 0
        if (attr[DEFENSIVE_STANCE_ATTR] == null) attr[DEFENSIVE_STANCE_ATTR] = false
        if (attr[DEFENSIVE_STANCE_END_ATTR] == null) attr[DEFENSIVE_STANCE_END_ATTR] = 0
        
        // Update minions
        updateMinions()
        
        while (canEngageCombat(target)) {
            facePawn(target)
            
            val currentTick = this.world.currentCycle
            val currentHp = getCurrentHp()
            
            // Update rage/enrage phases based on HP
            updateCombatPhase(currentHp)
            
            // Update war cry and defensive stance status
            updateWarCry(currentTick)
            updateDefensiveStance(currentTick)
            
            // Check for war cry (buff minions and self)
            val lastWarCry = attr[LAST_WAR_CRY_ATTR] ?: 0
            val ticksSinceWarCry = currentTick - lastWarCry
            if (ticksSinceWarCry >= WAR_CRY_COOLDOWN && this.world.random(100) < WAR_CRY_CHANCE) {
                warCry(currentTick)
            }
            
            // Check for defensive stance (low HP defensive mode)
            if (currentHp < RAGE_THRESHOLD_HP && !attr[DEFENSIVE_STANCE_ATTR]!! && this.world.random(100) < DEFENSIVE_STANCE_CHANCE) {
                activateDefensiveStance(currentTick)
            }
            
            // Check for range stomp (player standing under Graardor)
            if (target is Player && target.tile.sameAs(this.tile)) {
                rangeStomp(target)
                it.wait(1)
                target = getCombatTarget() ?: break
                continue
            }
            
            // Choose attack based on phase and random chance
            val attackRoll = this.world.random(100)
            val isEnraged = attr[ENRAGE_MODE_ATTR] ?: false
            
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                when {
                    attackRoll < GROUND_SLAM_CHANCE && isEnraged -> {
                        // Ground slam AoE (only when enraged)
                        groundSlam(target)
                    }
                    attackRoll < (GROUND_SLAM_CHANCE + SHOCKWAVE_CHANCE) -> {
                        // Magic shockwave
                        magicShockwave(target)
                    }
                    attackRoll < 60 -> {
                        // Melee attack (most common)
                        meleeAttack(target)
                    }
                    else -> {
                        // Ranged attack
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
     * Melee attack - slow, very high damage, enhanced when enraged
     */
    private fun Npc.meleeAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        animate(MELEE_ATTACK_ANIM)
        
        if (MeleeCombatFormula.getAccuracy(this, target) >= this.world.randomDouble()) {
            val maxHit = MeleeCombatFormula.getMaxHit(this, target)
            val isEnraged = attr[ENRAGE_MODE_ATTR] ?: false
            val isWarCry = attr[WAR_CRY_ACTIVE_ATTR] ?: false
            
            // Calculate base damage
            val baseCap = if (isEnraged) RAGE_MELEE_MAX_HIT else MELEE_MAX_HIT
            var damage = minOf(this.world.random(maxHit + 1), baseCap)
            
            // Apply war cry bonus
            if (isWarCry) {
                damage = (damage * 1.15).toInt() // 15% boost from war cry
            }
            
            target.hit(damage, type = HitType.HIT, delay = 1)
            target.graphic(id = 254, height = 100, delay = 0)
            
            if (isEnraged && target is Player) {
                target.message("General Graardor strikes with enraged fury!")
            }
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
     * Ground slam AoE attack - damages all nearby players
     */
    private fun Npc.groundSlam(target: Pawn) {
        animate(MELEE_ATTACK_ANIM)
        graphic(id = STOMP_GFX, height = 0)
        
        // Find all players within 3 tiles
        val nearbyPlayers = mutableListOf<Player>()
        if (target is Player) {
            nearbyPlayers.add(target)
        }
        
        // Get other players in range
        this.world.players.forEach { player ->
            if (player != target && player.tile.isWithinRadius(this.tile, 3)) {
                nearbyPlayers.add(player)
            }
        }
        
        // Damage all nearby players
        nearbyPlayers.forEach { player ->
            val damage = this.world.random(GROUND_SLAM_DAMAGE_MIN..GROUND_SLAM_DAMAGE_MAX)
            player.hit(damage, type = HitType.HIT, delay = 1)
            player.graphic(id = STOMP_GFX, height = 0, delay = 1)
            player.message("General Graardor slams the ground with tremendous force!")
        }
    }
    
    /**
     * War cry - buffs Graardor and nearby minions
     */
    private fun Npc.warCry(currentTick: Int) {
        attr[WAR_CRY_ACTIVE_ATTR] = true
        attr[WAR_CRY_END_ATTR] = currentTick + WAR_CRY_BUFF_DURATION
        attr[LAST_WAR_CRY_ATTR] = currentTick
        
        // Show war cry animation/graphic
        graphic(id = SHOCKWAVE_GFX, height = 100)
        
        // Buff nearby minions
        val minions = attr[MINIONS_ATTR] ?: mutableListOf()
        minions.mapNotNull { it.get() }.forEach { minion ->
            if (minion.isActive() && minion.isSpawned()) {
                // Visual effect on minions
                minion.graphic(id = SHOCKWAVE_GFX, height = 100)
            }
        }
        
        // Notify nearby players
        this.world.players.forEach { player ->
            if (player.tile.isWithinRadius(this.tile, 15)) {
                player.message("General Graardor roars with fury, empowering his forces!")
            }
        }
    }
    
    /**
     * Activate defensive stance - reduces damage taken
     */
    private fun Npc.activateDefensiveStance(currentTick: Int) {
        attr[DEFENSIVE_STANCE_ATTR] = true
        attr[DEFENSIVE_STANCE_END_ATTR] = currentTick + DEFENSIVE_STANCE_DURATION
        
        graphic(id = 1203, height = 100) // Defensive glow
        
        // Notify nearby players
        this.world.players.forEach { player ->
            if (player.tile.isWithinRadius(this.tile, 15)) {
                player.message("General Graardor takes a defensive stance!")
            }
        }
    }
    
    /**
     * Update combat phase based on HP
     */
    private fun Npc.updateCombatPhase(currentHp: Int) {
        val wasEnraged = attr[ENRAGE_MODE_ATTR] ?: false
        val wasRaged = attr[RAGE_MODE_ATTR] ?: false
        
        // Enter enrage mode (below 50 HP)
        if (currentHp <= ENRAGE_THRESHOLD_HP && !wasEnraged) {
            attr[ENRAGE_MODE_ATTR] = true
            graphic(id = SHOCKWAVE_GFX, height = 100)
            
            this.world.players.forEach { player ->
                if (player.tile.isWithinRadius(this.tile, 15)) {
                    player.message("General Graardor enters a state of pure rage!")
                }
            }
        }
        // Enter rage mode (below 100 HP)
        else if (currentHp <= RAGE_THRESHOLD_HP && !wasRaged) {
            attr[RAGE_MODE_ATTR] = true
            graphic(id = 157, height = 0)
            
            this.world.players.forEach { player ->
                if (player.tile.isWithinRadius(this.tile, 15)) {
                    player.message("General Graardor becomes enraged!")
                }
            }
        }
    }
    
    /**
     * Update war cry status
     */
    private fun Npc.updateWarCry(currentTick: Int) {
        val isActive = attr[WAR_CRY_ACTIVE_ATTR] ?: false
        if (isActive) {
            val endTick = attr[WAR_CRY_END_ATTR] ?: 0
            if (currentTick >= endTick) {
                attr[WAR_CRY_ACTIVE_ATTR] = false
            }
        }
    }
    
    /**
     * Update defensive stance status
     */
    private fun Npc.updateDefensiveStance(currentTick: Int) {
        val isActive = attr[DEFENSIVE_STANCE_ATTR] ?: false
        if (isActive) {
            val endTick = attr[DEFENSIVE_STANCE_END_ATTR] ?: 0
            if (currentTick >= endTick) {
                attr[DEFENSIVE_STANCE_ATTR] = false
                
                this.world.players.forEach { player ->
                    if (player.tile.isWithinRadius(this.tile, 15)) {
                        player.message("General Graardor's defensive stance ends.")
                    }
                }
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
