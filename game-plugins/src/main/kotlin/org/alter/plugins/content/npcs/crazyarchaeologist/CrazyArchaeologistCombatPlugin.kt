package org.alter.plugins.content.npcs.crazyarchaeologist

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Graphic
import org.alter.api.ext.*
import org.alter.api.PrayerIcon
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.api.EquipmentType
import org.alter.game.action.EquipAction
import org.alter.game.model.entity.*
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.*
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy

class CrazyArchaeologistCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val TILE_BLOCK_CHECK_TIMER = TimerKey()

    companion object {
        /**
         * Special attack configuration constants.
         * These control when and how often special attacks occur.
         */
        private const val TELEPORT_ATTACK_MIN_COUNT = 1  // Minimum attacks before teleport can trigger (reduced to 1 for more frequent teleports)
        private const val TELEPORT_ATTACK_CHANCE_NUMERATOR = 2  // 2 in TELEPORT_ATTACK_CHANCE_DENOMINATOR
        private const val TELEPORT_ATTACK_CHANCE_DENOMINATOR = 3  // 67% chance when conditions are met (increased for more frequent teleports)

        private const val BOOK_RAIN_ATTACK_MIN_COUNT = 2  // Minimum attacks before book rain can trigger (reduced from 3)
        private const val BOOK_RAIN_ATTACK_CHANCE_NUMERATOR = 2  // 2 in BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR
        private const val BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR = 3  // 67% chance when conditions are met (increased from 50%)

        private const val UNEQUIP_ATTACK_MIN_COUNT = 3  // Minimum attacks before unequip can trigger (reduced from 4)
        private const val UNEQUIP_ATTACK_CHANCE_NUMERATOR = 1  // 1 in UNEQUIP_ATTACK_CHANCE_DENOMINATOR
        private const val UNEQUIP_ATTACK_CHANCE_DENOMINATOR = 2  // 50% chance when conditions are met (increased from 33%)
        
        /**
         * Special effect chance constants.
         */
        private const val FREEZE_EFFECT_CHANCE_NUMERATOR = 1
        private const val FREEZE_EFFECT_CHANCE_DENOMINATOR = 4  // 25% chance to freeze on hit
    }

    init {
        onNpcCombat("npc.crazy_archaeologist") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Prevent players from standing on the same tile as the Crazy Archaeologist
        onGlobalNpcSpawn {
            if (npc.id == getRSCM("npc.crazy_archaeologist")) {
                npc.timers[TILE_BLOCK_CHECK_TIMER] = 1 // Check every cycle
            }
        }
        
        onTimer(TILE_BLOCK_CHECK_TIMER) {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.crazy_archaeologist") && npc.isAlive()) {
                val npcTile = npc.tile
                val attackRange = 10 // Same range as combat detection
                
                // Check all players and push them off if they're on the same tile
                npc.world.players.forEach { player ->
                    if (player.initiated && !player.isDead()) {
                        // Check if player is on the same tile as NPC
                        if (player.tile.sameAs(npcTile)) {
                            // Find an adjacent tile to push the player to
                            val adjacentTiles = listOf(
                                npcTile.transform(1, 0),   // East
                                npcTile.transform(-1, 0),  // West
                                npcTile.transform(0, 1),   // North
                                npcTile.transform(0, -1),  // South
                                npcTile.transform(1, 1),   // Northeast
                                npcTile.transform(-1, 1),  // Northwest
                                npcTile.transform(1, -1),  // Southeast
                                npcTile.transform(-1, -1)  // Southwest
                            )
                            
                            // Find the first walkable adjacent tile
                            val targetTile = adjacentTiles.firstOrNull { tile ->
                                val chunk = npc.world.chunks.get(tile, createIfNeeded = false)
                                if (chunk == null) return@firstOrNull false
                                
                                npc.world.reachStrategy.reached(
                                    flags = npc.world.collision,
                                    level = tile.height,
                                    srcX = npcTile.x,
                                    srcZ = npcTile.z,
                                    destX = tile.x,
                                    destZ = tile.z,
                                    destWidth = 1,
                                    destLength = 1,
                                    srcSize = 1,
                                    locShape = -2
                                )
                            }
                            
                            if (targetTile != null) {
                                player.moveTo(targetTile)
                                player.message("You cannot stand on the same tile as the Crazy Archaeologist!")
                            }
                        }
                        
                        // Check if player is locked and in combat with Crazy Archaeologist
                        // If they're locked and can't move, knock them back and unlock them
                        if (player.isLocked() && !player.lock.canMove() && 
                            player.tile.isWithinRadius(npcTile, attackRange) &&
                            npc.damageMap.getDamageFrom(player) > 0) {
                            
                            // Player is locked and in combat - find a safe tile to knock them back to
                            val playerTile = player.tile
                            val knockbackTiles = listOf(
                                playerTile.transform(1, 0),   // East
                                playerTile.transform(-1, 0),  // West
                                playerTile.transform(0, 1),   // North
                                playerTile.transform(0, -1),  // South
                                playerTile.transform(1, 1),   // Northeast
                                playerTile.transform(-1, 1),  // Northwest
                                playerTile.transform(1, -1),  // Southeast
                                playerTile.transform(-1, -1)  // Southwest
                            )
                            
                            // Find a walkable tile away from the NPC
                            val safeTile = knockbackTiles.firstOrNull { tile ->
                                // Make sure it's not the NPC's tile
                                if (tile.sameAs(npcTile)) return@firstOrNull false
                                
                                val chunk = npc.world.chunks.get(tile, createIfNeeded = false)
                                if (chunk == null) return@firstOrNull false
                                
                                npc.world.reachStrategy.reached(
                                    flags = npc.world.collision,
                                    level = tile.height,
                                    srcX = playerTile.x,
                                    srcZ = playerTile.z,
                                    destX = tile.x,
                                    destZ = tile.z,
                                    destWidth = 1,
                                    destLength = 1,
                                    srcSize = 1,
                                    locShape = -2
                                )
                            }
                            
                            if (safeTile != null) {
                                // Reset interactions and unlock the player
                                player.resetInteractions()
                                player.unlock()
                                
                                // Move player to safe tile
                                player.moveTo(safeTile)
                                player.message("The Crazy Archaeologist's magic knocks you back!")
                            } else {
                                // If we can't find a safe tile, at least unlock the player
                                player.resetInteractions()
                                player.unlock()
                                player.message("You break free from the lock!")
                            }
                        }
                    }
                }
                
                // Reset timer to check again next cycle
                npc.timers[TILE_BLOCK_CHECK_TIMER] = 1
            }
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var attackCount = 0  // For special attacks (teleport/book rain)
        var regularAttackCount = 0  // For tracking regular attacks to trigger unblockable attack
        var hasTaunted = false

        while (isAlive()) {
            // Find all nearby players within attack range
            val nearbyPlayers = mutableListOf<Player>()
            val npcTile = this.tile
            val attackRange = 10 // Magic attack range
            
            this.world.players.forEach { player ->
                if (player.initiated && !player.isDead() && 
                    player.tile.isWithinRadius(npcTile, attackRange) &&
                    canEngageCombat(player)) {
                    nearbyPlayers.add(player)
                }
            }
            
            // If no players nearby, check if we have a combat target to continue
            if (nearbyPlayers.isEmpty()) {
                val target = getCombatTarget()
                if (target == null || !canEngageCombat(target)) {
                    break
                }
                it.wait(1)
                continue
            }
            
            // Opening taunt when combat starts (only once)
            // Display taunt above NPC's head instead of in chatbox
            if (!hasTaunted && nearbyPlayers.isNotEmpty()) {
                when (this.world.random(5)) {
                    0 -> this.forceChat("The ancient texts speak of your demise!")
                    1 -> this.forceChat("Knowledge is power, and I have both!")
                    2 -> this.forceChat("You disturb the sacred ruins!")
                    3 -> this.forceChat("The books shall be your undoing!")
                    4 -> this.forceChat("Prepare to face the wisdom of ages!")
                }
                hasTaunted = true
            }
            
            // Randomly select a target to face (changes target more frequently)
            if (nearbyPlayers.isNotEmpty()) {
                val targetPlayer = nearbyPlayers.random()
                facePawn(targetPlayer)
            }
            
            // Check if NPC is still alive and has HP before attacking
            if (isAttackDelayReady() && isAlive() && getCurrentHp() > 0) {
                attackCount++
                regularAttackCount++

                /**
                 * Special Attack Logic:
                 * 
                 * The attack count increments with each successful attack. Special attacks have
                 * minimum attack count requirements and probability chances:
                 * 
                 * 1. Unblockable Magic Attack (Priority 0 - highest):
                 *    - Triggers randomly every 2-10 regular attacks
                 *    - Cannot be nullified by any prayer
                 *    - Hits 1-30 damage
                 * 
                 * 2. Teleport Attack (Priority 1):
                 *    - Requires: attackCount >= TELEPORT_ATTACK_MIN_COUNT (4)
                 *    - Chance: TELEPORT_ATTACK_CHANCE_NUMERATOR / TELEPORT_ATTACK_CHANCE_DENOMINATOR (1/3 = 33%)
                 *    - Effect: Teleports random player next to archaeologist and deals damage
                 *    - Resets attack count to 0 on success
                 * 
                 * 3. Book Rain Attack (Priority 2, only if teleport didn't trigger):
                 *    - Requires: attackCount >= BOOK_RAIN_ATTACK_MIN_COUNT (3)
                 *    - Chance: BOOK_RAIN_ATTACK_CHANCE_NUMERATOR / BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR (1/2 = 50%)
                 *    - Effect: Area-of-effect attack in 7x7 area around center of players (50-90 damage)
                 *    - Resets attack count to 0 on success
                 * 
                 * 4. Unequip Attack (Priority 3, only if teleport/book rain didn't trigger):
                 *    - Requires: attackCount >= UNEQUIP_ATTACK_MIN_COUNT (5)
                 *    - Chance: UNEQUIP_ATTACK_CHANCE_NUMERATOR / UNEQUIP_ATTACK_CHANCE_DENOMINATOR (1/4 = 25%)
                 *    - Effect: Removes a random equipped item from a player and puts it in their inventory (if space available)
                 *    - Resets attack count to 0 on success
                 * 
                 * 5. Regular Book Attack (Default):
                 *    - Used when special attacks don't trigger
                 *    - Attacks ALL nearby players simultaneously
                 *    - Nullified by Protect from Missiles prayer (0 damage)
                 *    - Randomly selects from 3 book types (NORMAL, EXPLOSIVE, FREEZE)
                 *    - Attack count continues incrementing, making specials more likely over time
                 */
                
                // Check if it's time for unblockable attack (randomly every 2-5 regular attacks)
                // Probability increases as count increases: at 2 attacks = 1/4 chance, at 5 attacks = guaranteed
                val shouldUseUnblockable = if (regularAttackCount >= 2 && regularAttackCount <= 5) {
                    if (regularAttackCount == 5) {
                        true // Guaranteed at 5 attacks
                    } else {
                        // Increasing probability: 1/4 at count 2, 1/3 at count 3, 1/2 at count 4
                        val denominator = 6 - regularAttackCount
                        this.world.chance(1, denominator)
                    }
                } else {
                    false
                }
                
                // Check if NPC is still alive before executing attacks
                if (!isAlive()) {
                    break
                }
                
                if (shouldUseUnblockable) {
                    // Unblockable magic attack - cannot be nullified
                    nearbyPlayers.forEach { player ->
                        if (isAlive()) {  // Check before each attack
                            unblockableMagicAttack(player)
                        }
                    }
                    regularAttackCount = 0  // Reset counter after unblockable attack
                    
                    // Post attack logic for first player (for timing)
                    if (nearbyPlayers.isNotEmpty() && isAlive()) {
                        postAttackLogicWithEnrage(nearbyPlayers.first())
                    }
                } else {
                    // When multiple players are present, prioritize book rain attack (AOE is more effective)
                    val hasMultiplePlayers = nearbyPlayers.size > 1
                    
                    // Check book rain first if multiple players are present, otherwise check teleport first
                    // With multiple players, book rain has higher chance (guaranteed if conditions met)
                    if (hasMultiplePlayers && attackCount >= BOOK_RAIN_ATTACK_MIN_COUNT) {
                        // With multiple players, book rain triggers more often - check with higher probability
                        val bookRainChance = if (attackCount >= 5) {
                            // After 5 attacks, 75% chance (3 in 4)
                            this.world.chance(3, 4)
                        } else {
                            // After 3-4 attacks, 50% chance (1 in 2)
                            this.world.chance(BOOK_RAIN_ATTACK_CHANCE_NUMERATOR, BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR)
                        }
                        
                        if (bookRainChance) {
                            // Book rain on center of all players (multiple players present)
                            if (isAlive()) {  // Check before executing special attack
                                // Calculate center tile of all players
                                val avgX = nearbyPlayers.sumOf { it.tile.x } / nearbyPlayers.size
                                val avgZ = nearbyPlayers.sumOf { it.tile.z } / nearbyPlayers.size
                                val centerTile = Tile(avgX, avgZ, this.tile.height)
                                bookRainAttackAtTile(centerTile)
                                attackCount = 0
                                // Post attack logic for timing
                                postAttackLogicWithEnrage(nearbyPlayers.first())
                            }
                        } else if (attackCount >= TELEPORT_ATTACK_MIN_COUNT &&
                            this.world.chance(TELEPORT_ATTACK_CHANCE_NUMERATOR, TELEPORT_ATTACK_CHANCE_DENOMINATOR)) {
                            // Teleport multiple players (only if book rain didn't trigger)
                            if (isAlive()) {  // Check before executing special attack
                                teleportMultiplePlayers(nearbyPlayers)
                                attackCount = 0
                                // Post attack logic for timing (use first player for enrage)
                                if (nearbyPlayers.isNotEmpty()) {
                                    postAttackLogicWithEnrage(nearbyPlayers.first())
                                }
                            }
                        } else if (attackCount >= UNEQUIP_ATTACK_MIN_COUNT &&
                          this.world.chance(UNEQUIP_ATTACK_CHANCE_NUMERATOR, UNEQUIP_ATTACK_CHANCE_DENOMINATOR)) {
                            // Unequip attack on a random player (multiple players scenario)
                            if (isAlive()) {  // Check before executing special attack
                                val randomPlayer = nearbyPlayers.random()
                                unequipAttack(randomPlayer)
                                attackCount = 0
                                // Post attack logic for timing
                                postAttackLogicWithEnrage(randomPlayer)
                            }
                        }
                    } else if (attackCount >= TELEPORT_ATTACK_MIN_COUNT &&
                        this.world.chance(TELEPORT_ATTACK_CHANCE_NUMERATOR, TELEPORT_ATTACK_CHANCE_DENOMINATOR)) {
                        // Teleport multiple players (single or multiple player scenario)
                        if (isAlive()) {  // Check before executing special attack
                            teleportMultiplePlayers(nearbyPlayers)
                            attackCount = 0
                            // Post attack logic for timing
                            if (nearbyPlayers.isNotEmpty()) {
                                postAttackLogic(nearbyPlayers.first())
                            }
                        }
                    } else if (attackCount >= BOOK_RAIN_ATTACK_MIN_COUNT &&
                              this.world.chance(BOOK_RAIN_ATTACK_CHANCE_NUMERATOR, BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR)) {
                        // Book rain on center of all players (single player or fallback)
                        if (isAlive()) {  // Check before executing special attack
                            val centerTile = if (nearbyPlayers.size == 1) {
                                nearbyPlayers.first().tile
                            } else {
                                // Calculate center tile of all players
                                val avgX = nearbyPlayers.sumOf { it.tile.x } / nearbyPlayers.size
                                val avgZ = nearbyPlayers.sumOf { it.tile.z } / nearbyPlayers.size
                                Tile(avgX, avgZ, this.tile.height)
                            }
                            bookRainAttackAtTile(centerTile)
                            attackCount = 0
                            // Post attack logic for timing
                            postAttackLogic(nearbyPlayers.first())
                        }
                    } else if (attackCount >= UNEQUIP_ATTACK_MIN_COUNT &&
                          this.world.chance(UNEQUIP_ATTACK_CHANCE_NUMERATOR, UNEQUIP_ATTACK_CHANCE_DENOMINATOR)) {
                        // Unequip attack on a random player (single player scenario)
                        if (isAlive()) {  // Check before executing special attack
                            val randomPlayer = nearbyPlayers.random()
                            unequipAttack(randomPlayer)
                            attackCount = 0
                            // Post attack logic for timing
                            postAttackLogic(randomPlayer)
                        }
                    } else {
                        // Regular magic attacks - attack ALL nearby players simultaneously
                        // These are nullified by Protect from Missiles prayer
                        if (isAlive()) {  // Check before executing regular attacks
                            val bookType = when (this.world.random(3)) {
                                0 -> BookType.NORMAL
                                1 -> BookType.EXPLOSIVE
                                2 -> BookType.FREEZE
                                else -> BookType.NORMAL
                            }
                            
                            // Attack all players at once
                            nearbyPlayers.forEach { player ->
                                if (isAlive()) {  // Check before each attack
                                    bookAttack(player, bookType)
                                }
                            }
                            
                            // Post attack logic for first player (for timing)
                            if (nearbyPlayers.isNotEmpty() && isAlive()) {
                                postAttackLogic(nearbyPlayers.first())
                            }
                        }
                    }
                }
            }

            // Always wait at least 1 cycle to prevent infinite loops
            it.wait(1)
        }

        resetFacePawn()
        removeCombatTarget()
    }

    /**
     * Standard Book Attack - Regular magic attacks that can be nullified by Protect from Missiles prayer.
     * 
     * This attack:
     * 1. Hits hard when prayer is NOT active (base max hit + 25 bonus damage)
     * 2. Is completely nullified (0 damage) when Protect from Missiles prayer is active
     * 3. Applies special effects based on book type (NORMAL, EXPLOSIVE, FREEZE)
     */
    private fun Npc.bookAttack(target: Pawn, bookType: BookType) {
        // Don't attack if NPC is dead
        if (!isAlive()) {
            return
        }
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book throwing animation
        
        // Check if target has Protect from Missiles prayer - if so, completely nullify the attack
        // Standard attacks are blocked by range prayer, but special attacks bypass it
        if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
            // Attack is completely nullified - show blocked graphic and return (0 damage)
            val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
            target.graphic(id = 85, height = 124, delay = delay)
            return
        }
        
        val projectileGfx = when (bookType) {
            BookType.NORMAL -> Graphic.CRAZY_ARCHAEOLOGIST_BOOK     // Regular book projectile
            BookType.EXPLOSIVE -> Graphic.RAIN_OF_KNOWLEDGE_BOOK   // Explosive book (red)
            BookType.FREEZE -> Graphic.WATER_RUNE                   // Freeze book (blue) - using water rune graphic
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
        
        // Calculate custom max hit for higher damage when prayer is NOT active
        // This ensures the NPC hits hard, but only when the player doesn't have range prayer on
        val baseMaxHit = MagicCombatFormula.getMaxHit(this, target)
        val customMaxHit = baseMaxHit + 25  // Add 25 to base max hit for significantly higher damage
        
        val hit = dealHit(
            target = target,
            maxHit = customMaxHit,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
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
    
    /**
     * Unblockable Magic Attack - Cannot be nullified by any prayer.
     * 
     * This attack:
     * 1. Fires a special magic projectile
     * 2. Always hits for 1-30 damage (cannot be blocked by prayer)
     * 3. Triggers randomly every 2-10 regular attacks
     */
    private fun Npc.unblockableMagicAttack(target: Pawn) {
        // Don't attack if NPC is dead
        if (!isAlive()) {
            return
        }
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book animation
        
        // Special unblockable projectile (using a different graphic to distinguish it)
        val projectile = createProjectile(
            target, 
            gfx = 1576, // Special unblockable magic projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        // Deal unblockable damage (1-30, cannot be nullified)
        val damage = this.world.random(1..30) // Random damage between 1-30
        
        val hit = target.hit(
            damage = damage,
            type = HitType.HIT,
            delay = delay,
            attackersIndex = this.index
        )
        
        // Cancel the hit if the NPC dies or reaches 0 HP before it lands
        hit.setCancelIf { !this@unblockableMagicAttack.isAlive() || this@unblockableMagicAttack.getCurrentHp() <= 0 }
        
        if (target is Player) {
            target.message("The Crazy Archaeologist's unblockable magic strikes through your protection!")
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
            BookType.FREEZE -> {
                if (this.world.chance(FREEZE_EFFECT_CHANCE_NUMERATOR, FREEZE_EFFECT_CHANCE_DENOMINATOR)) {
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
    
    /**
     * Teleport Multiple Players - Special ability that teleports multiple nearby players next to the archaeologist.
     * 
     * This attack:
     * 1. Fires teleport projectiles at all nearby players
     * 2. After projectile delay, teleports each player to a random adjacent tile
     * 3. Deals damage from the teleport itself
     */
    private suspend fun Npc.teleportMultiplePlayers(players: List<Player>) {
        if (players.isEmpty()) return
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book animation
        
        // Fire projectiles at all players
        val hitDelays = mutableMapOf<Player, Int>()
        players.forEach { player ->
            // Special teleport projectile (using custom graphic ID as no constant exists)
            val projectile = createProjectile(
                player, 
                gfx = 1576, // Teleport book projectile - custom graphic for teleport effect
                startHeight = 43, 
                endHeight = 31, 
                delay = 51, 
                angle = 15, 
                steepness = 127
            )
            
            world.spawn(projectile)
            hitDelays[player] = RangedCombatStrategy.getHitDelay(getFrontFacingTile(player), player.getCentreTile())
        }

        // Teleport all players after their respective delays
        this.world.queue {
            // Wait for the longest hit delay to ensure all projectiles have time to travel
            val maxDelay = hitDelays.values.maxOrNull() ?: 0
            wait(maxDelay - 1)

            // Check if NPC is still alive before executing teleports
            if (!this@teleportMultiplePlayers.isAlive() || this@teleportMultiplePlayers.getCurrentHp() <= 0) {
                return@queue
            }

            // Find tiles next to the archaeologist to teleport players to
            val archaeologistTile = this@teleportMultiplePlayers.tile
            val surroundingTiles = mutableListOf<Tile>()

            // Get tiles in a 5x5 area around the archaeologist (more tiles for multiple players)
            for (x in -2..2) {
                for (z in -2..2) {
                    if (x == 0 && z == 0) continue // Skip the archaeologist's tile
                    val tile = archaeologistTile.transform(x, z)
                    surroundingTiles.add(tile)
                }
            }

            // Shuffle tiles to randomize teleport locations
            val shuffledTiles = surroundingTiles.shuffled(this@teleportMultiplePlayers.world.random)

            // Teleport each player to a different tile
            players.forEachIndexed { index, player ->
                // Check if player is still alive and in range
                if (!player.isAlive() || !player.tile.isWithinRadius(archaeologistTile, 10)) {
                    return@forEachIndexed
                }

                // Get a tile for this player (cycle through available tiles if needed)
                val teleportTile = shuffledTiles[index % shuffledTiles.size]
                
                player.graphic(id = 1577, height = 0, delay = 0) // Teleport out graphic
                player.moveTo(teleportTile)
                player.graphic(id = 1578, height = 0, delay = 1) // Teleport in graphic
                
                player.message("The Crazy Archaeologist teleports you to him!")

                // Deal some damage from the teleport
                val hit = player.hit(this@teleportMultiplePlayers.world.random(8), type = HitType.HIT, delay = 1)
                // Cancel the hit if the NPC dies or reaches 0 HP before it lands
                hit.setCancelIf { !this@teleportMultiplePlayers.isAlive() || this@teleportMultiplePlayers.getCurrentHp() <= 0 }
            }
        }
    }
    
    private suspend fun Npc.teleportAttack(target: Pawn) {
        /**
         * Teleport Attack - Special ability that teleports the target next to the archaeologist.
         * 
         * This attack:
         * 1. Fires a special teleport projectile at the target
         * 2. After projectile delay, teleports target to a random adjacent tile
         * 3. Deals damage from the teleport itself
         * 4. Works on both players and NPCs
         */
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book animation
        
        // Special teleport projectile (using custom graphic ID as no constant exists)
        val projectile = createProjectile(
            target, 
            gfx = 1576, // Teleport book projectile - custom graphic for teleport effect
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)

        // Teleport the target after a delay
        val hitDelay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        this.world.queue {
            wait(hitDelay - 1)

            // Check if both NPC and target are still alive and NPC has HP before executing teleport
            if (!this@teleportAttack.isAlive() || this@teleportAttack.getCurrentHp() <= 0 || !target.isAlive()) {
                return@queue
            }

            // Find a tile next to the archaeologist to teleport the target
            val archaeologistTile = this@teleportAttack.tile
            val surroundingTiles = mutableListOf<Tile>()

            // Get tiles in a 3x3 area around the archaeologist
            for (x in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && z == 0) continue // Skip the archaeologist's tile
                    val tile = archaeologistTile.transform(x, z)
                    surroundingTiles.add(tile)
                }
            }

            if (surroundingTiles.isNotEmpty()) {
                val teleportTile = surroundingTiles.random()
                target.graphic(id = 1577, height = 0, delay = 0) // Teleport out graphic
                target.moveTo(teleportTile)
                target.graphic(id = 1578, height = 0, delay = 1) // Teleport in graphic
                
                if (target is Player) {
                    target.message("The Crazy Archaeologist teleports you to him!")
                }

                // Deal some damage from the teleport
                val hit = target.hit(this@teleportAttack.world.random(8), type = HitType.HIT, delay = 1)
                // Cancel the hit if the NPC dies or reaches 0 HP before it lands
                hit.setCancelIf { !this@teleportAttack.isAlive() || this@teleportAttack.getCurrentHp() <= 0 }
            }
        }
    }
    
    /**
     * Unequip Attack - Special ability that removes a random equipped item from the target.
     * 
     * This attack:
     * 1. Fires a special unequip projectile at the target
     * 2. After projectile delay, attempts to unequip a random item from the player
     * 3. Only works if the player has inventory space
     * 4. Works only on players
     */
    private suspend fun Npc.unequipAttack(target: Pawn) {
        // Only works on players
        if (target !is Player) {
            return
        }
        
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book animation
        
        // Special unequip projectile
        val projectile = createProjectile(
            target, 
            gfx = 1576, // Special unequip projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        this.world.queue {
            wait(delay - 1)
            
            // Check if both NPC and target are still alive and NPC has HP
            if (!this@unequipAttack.isAlive() || this@unequipAttack.getCurrentHp() <= 0 || !target.isAlive()) {
                return@queue
            }
            
            // Show unequip graphic
            target.graphic(id = 131, height = 0, delay = 0) // Chaos/disruption graphic
            
            // Find all equipment slots that have items
            val equippedSlots = mutableListOf<Int>()
            EquipmentType.values.forEach { equipmentType ->
                val item = target.getEquipment(equipmentType)
                if (item != null) {
                    equippedSlots.add(equipmentType.id)
                }
            }
            
            // If player has equipped items and inventory space, unequip one
            if (equippedSlots.isNotEmpty() && target.inventory.freeSlotCount > 0) {
                // Pick a random equipped slot
                val randomSlot = equippedSlots.random()
                
                // Attempt to unequip the item
                val result = EquipAction.unequip(target, randomSlot)
                
                if (result == EquipAction.Result.SUCCESS) {
                    target.message("The Crazy Archaeologist's magic disrupts your equipment!")
                    this@unequipAttack.forceChat("Your equipment is mine!")
                } else if (result == EquipAction.Result.NO_FREE_SPACE) {
                    target.message("The Crazy Archaeologist tries to disrupt your equipment, but your inventory is full!")
                }
            } else if (equippedSlots.isEmpty()) {
                target.message("The Crazy Archaeologist's magic has nothing to disrupt!")
            } else {
                target.message("The Crazy Archaeologist tries to disrupt your equipment, but your inventory is full!")
            }
        }
    }
    
    private suspend fun Npc.bookRainAttack(target: Pawn) {
        bookRainAttackAtTile(target.tile)
    }
    
    private suspend fun Npc.bookRainAttackAtTile(targetTile: Tile) {
    /**
     * Book Rain Attack - Area-of-effect special ability.
     * 
     * This attack:
     * 1. Shows warning graphics on a 7x7 area around the target tile
     * 2. After a delay, explodes all tiles in the area
     * 3. Damages all pawns (players and NPCs) standing on affected tiles (50-90 damage)
     * 4. Does not damage the archaeologist itself
     */
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(Animation.CRAZY_ARCHAEOLOGIST_BOOK) // Book animation

        // Notify all nearby players
        this.world.players.forEach { player ->
            if (player.tile.isWithinRadius(targetTile, 7) && player.initiated) {
                player.message("The Crazy Archaeologist summons a rain of explosive books!")
            }
        }

        // Create multiple books around the target area
        val affectedTiles = mutableListOf<Tile>()

        // Get tiles in a 7x7 area around the target (49 tiles total)
        for (x in -3..3) {
            for (z in -3..3) {
                val tile = targetTile.transform(x, z)
                affectedTiles.add(tile)
            }
        }

        // Show warning graphics on tiles (books falling from sky)
        affectedTiles.forEach { tile ->
            world.spawn(TileGraphic(tile, id = Graphic.RAIN_OF_KNOWLEDGE_BOOK, height = 100, delay = 0))
        }

        // After delay, damage anyone in the affected area
        this.world.queue {
            wait(4)

            // Check if NPC is still alive and has HP before dealing damage
            if (!this@bookRainAttackAtTile.isAlive() || this@bookRainAttackAtTile.getCurrentHp() <= 0) {
                return@queue
            }

            affectedTiles.forEach { tile ->
                // Show explosion graphic on each affected tile
                world.spawn(TileGraphic(tile, id = 157, height = 0, delay = 0)) // Explosion graphic

                // Damage any pawns (players or NPCs) on this tile
                // Max damage: 90 (50-90 random) - high damage AOE attack
                world.players.forEach { player ->
                    if (player.tile == tile && player.isAlive() && this@bookRainAttackAtTile.isAlive() && this@bookRainAttackAtTile.getCurrentHp() > 0) {
                        val damage = this@bookRainAttackAtTile.world.random(50..90)
                        val hit = player.hit(damage, type = HitType.HIT, delay = 0)
                        // Cancel the hit if the NPC dies or reaches 0 HP before it lands
                        hit.setCancelIf { !this@bookRainAttackAtTile.isAlive() || this@bookRainAttackAtTile.getCurrentHp() <= 0 }
                    }
                }
                world.npcs.forEach { npc ->
                    if (npc.tile == tile && npc.isAlive() && npc != this@bookRainAttackAtTile && this@bookRainAttackAtTile.isAlive() && this@bookRainAttackAtTile.getCurrentHp() > 0) {
                        val damage = this@bookRainAttackAtTile.world.random(50..90)
                        val hit = npc.hit(damage, type = HitType.HIT, delay = 0)
                        // Cancel the hit if the NPC dies or reaches 0 HP before it lands
                        hit.setCancelIf { !this@bookRainAttackAtTile.isAlive() || this@bookRainAttackAtTile.getCurrentHp() <= 0 }
                    }
                }
            }
        }
    }
    
    /**
     * Post-attack logic with enrage mechanic.
     * When NPC is below 40% HP, attack speed increases significantly (faster attacks).
     */
    private fun Npc.postAttackLogicWithEnrage(target: Pawn) {
        // Call the regular post attack logic
        postAttackLogic(target)
    }

    /**
     * Check if NPC is enraged (below 40% HP).
     * When enraged, the wait time in combat loop is reduced for faster attacks.
     */
    private fun Npc.isEnraged(): Boolean {
        val maxHp = getMaxHp()
        val currentHp = getCurrentHp()
        val hpPercentage = (currentHp.toDouble() / maxHp.toDouble()) * 100
        return hpPercentage < 40.0
    }

    private enum class BookType {
        NORMAL,
        EXPLOSIVE,
        FREEZE
    }
}