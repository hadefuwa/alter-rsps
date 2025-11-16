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
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.rscm.RSCM.getRSCM
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

    /**
     * Timer key for minion aggro checks
     */
    private val MINION_AGGRO_TIMER = TimerKey()

    /**
     * Cached list of valid item IDs from the entire game item table.
     * This is built once when the plugin initializes to avoid rebuilding it on every minion death.
     */
    private val validItemIds: List<Int> by lazy {
        buildValidItemList()
    }

    init {
        // Using Cerberus NPC model (5862) as the Sewer Abomination
        onNpcCombat("npc.cerberus") {
            npc.queue {
                npc.combat(this)
            }
        }

        // Custom combat handlers for minions to ensure they hit hard
        // Only apply to minions spawned by the boss (they don't respawn)
        onNpcCombat("npc.zombie") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.MELEE)
                }
            }
        }
        
        onNpcCombat("npc.goblin") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.RANGED)
                }
            }
        }
        
        onNpcCombat("npc.dark_wizard") {
            val npc = ctx as Npc
            if (!npc.respawns) {
                npc.queue {
                    minionCombat(npc, this, CombatClass.MAGIC)
                }
            }
        }

        // Aggro timer for minions
        onTimer(MINION_AGGRO_TIMER) {
            val npc = ctx as Npc
            if (!npc.respawns && npc.isActive() && npc.lock.canAttack() && !npc.isAttacking()) {
                // Manually check for players in radius and attack
                checkMinionAggro(npc)
            }
            // Reset timer
            if (npc.isActive()) {
                npc.timers[MINION_AGGRO_TIMER] = 1
            }
        }

        // Override unwanted NPC options for minions (they should only be attackable)
        // Only register handlers for options that actually exist on these NPCs
        // Goblins should only have "Attack" option, so no need to override anything
        
        // Check if dark_wizard has "talk-to" option and override it if it exists
        if (npcHasOption("npc.dark_wizard", "talk-to")) {
            onNpcOption("npc.dark_wizard", option = "talk-to") {
                val npc = ctx as Npc
                // If this is a minion (doesn't respawn), redirect to attack instead
                if (!npc.respawns) {
                    // Redirect to attack instead
                    player.attack(npc)
                }
            }
        }

        // Guaranteed random item drop for minions when they die
        // Use onAnyNpcDeath as a catch-all to ensure we catch all minion deaths
        onAnyNpcDeath {
            val npc = ctx as Npc
            val zombieId = getRSCM("npc.zombie")
            val goblinId = getRSCM("npc.goblin")
            val wizardId = getRSCM("npc.dark_wizard")
            
            // Check if this is a minion spawned by the boss (doesn't respawn)
            // and is one of the minion types
            if (!npc.respawns && (npc.id == zombieId || npc.id == goblinId || npc.id == wizardId)) {
                println("DEBUG: Minion death detected - NPC ${npc.id}, respawns: ${npc.respawns}, zombieId=$zombieId, goblinId=$goblinId, wizardId=$wizardId")
                dropGuaranteedRandomItem(npc)
            }
        }
    }

    /**
     * Builds a list of valid item IDs from the entire game item table.
     * Filters out placeholders, null names, and empty names.
     */
    private fun buildValidItemList(): List<Int> {
        val validItems = mutableListOf<Int>()
        
        for (itemId in 0 until itemSize()) {
            try {
                val def = getItem(itemId)
                // Filter out invalid items: placeholders, null names, and empty names
                if (!def.isPlaceholder && def.name.isNotBlank() && def.name.lowercase() != "null") {
                    validItems.add(itemId)
                }
            } catch (e: Exception) {
                // Skip items that can't be loaded
                continue
            }
        }
        
        return validItems.toList()
    }

    /**
     * Converts a clue scroll item ID to its corresponding clue casket item ID.
     * Returns the original item ID if it's not a clue scroll.
     */
    private fun convertClueScrollToCasket(itemId: Int): Int {
        try {
            val itemDef = getItem(itemId)
            val itemName = itemDef.name.lowercase()
            
            // Check if this is a clue scroll item
            if (itemName.contains("clue") && itemName.contains("scroll") && (
                itemName.contains("easy") || 
                itemName.contains("medium") || 
                itemName.contains("hard") || 
                itemName.contains("elite") || 
                itemName.contains("master") ||
                itemName.contains("beginner")
            )) {
                // Try to find the corresponding clue casket
                val casketName = when {
                    itemName.contains("beginner") -> "item.casket_easy"
                    itemName.contains("easy") -> "item.casket_easy"
                    itemName.contains("medium") -> "item.casket_medium"
                    itemName.contains("hard") -> "item.casket_hard"
                    itemName.contains("elite") -> "item.casket_elite"
                    itemName.contains("master") -> "item.casket_master"
                    else -> null
                }
                
                if (casketName != null) {
                    try {
                        return getRSCM(casketName)
                    } catch (e: Exception) {
                        // Fall through to return original item ID
                    }
                }
            }
        } catch (e: Exception) {
            // If we can't get item definition, just return original ID
        }
        
        // Not a clue scroll or conversion failed, return original item ID
        return itemId
    }

    /**
     * Drops a guaranteed random item from the entire game item table for minions.
     * This uses the same system as the server's random drop feature but is always guaranteed.
     * Also drops bones.
     */
    private fun dropGuaranteedRandomItem(npc: Npc) {
        try {
            // Get the killer (player who dealt the most damage)
            // Try to get killer from attribute first
            var killer = npc.attr[KILLER_ATTR]?.get() as? Player
            
            // If no killer from attribute, try to get from damage map
            if (killer == null) {
                val maxHp = npc.getMaxHp()
                val mostDamage = npc.damageMap.getMostDamagePercentage(maxHp)
                killer = mostDamage as? Player
            }
            
            if (killer == null) {
                println("DEBUG: No killer found for minion ${npc.id} - dropping item without owner")
                // Continue anyway - drop item without owner (public immediately)
            }

            // Drop bones first
            try {
                val bonesId = getRSCM("item.bones")
                val bonesGroundItem = GroundItem(
                    item = bonesId,
                    amount = 1,
                    tile = npc.tile,
                    owner = killer
                )
                bonesGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                bonesGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                bonesGroundItem.ownerShipType = 1
                npc.world.spawn(bonesGroundItem)
            } catch (e: Exception) {
                println("DEBUG: Error dropping bones for minion: ${e.message}")
            }

            // Use the cached valid item list
            if (validItemIds.isEmpty()) {
                println("DEBUG: No valid items in cache for minion drop")
                return
            }

            // Randomly select one item from the valid items
            val randomItemId = validItemIds[Random.nextInt(validItemIds.size)]
            
            // Convert clue scrolls to clue caskets before dropping
            val itemIdToDrop = convertClueScrollToCasket(randomItemId)
            val finalItemDef = getItem(itemIdToDrop)

            // Determine amount (1 for most items, random 1-100 for stackable items)
            val amount = if (finalItemDef.stackable) {
                Random.nextInt(1, 101) // 1-100 for stackable items
            } else {
                1 // Single item for non-stackable
            }

            // Create and spawn the random item
            val randomGroundItem = GroundItem(
                item = itemIdToDrop,
                amount = amount,
                tile = npc.tile,
                owner = killer // Can be null - item will be public immediately
            )

            // Set timers based on whether we have a killer
            if (killer != null) {
                // Killer sees for 1 minute, then everyone for 3 minutes
                randomGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
                randomGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
                randomGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
            } else {
                // No owner - item is public immediately
                randomGroundItem.timeUntilPublic = 0
                randomGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                randomGroundItem.ownerShipType = 0 // Public
            }

            npc.world.spawn(randomGroundItem)
            println("DEBUG: Spawned random item ${amount}x ${finalItemDef.name} (ID: $itemIdToDrop) at ${npc.tile}")

            // Notify the player about the random drop if we have a killer
            killer?.message("Minion drops: ${amount}x ${finalItemDef.name}")

        } catch (e: Exception) {
            println("Error dropping guaranteed random item for minion ${npc.id}: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return

        // Track minion spawns to avoid duplicates
        var meleeMinionSpawned = false
        var rangedMinionSpawned = false
        var mageMinionSpawned = false

        while (canEngageCombat(target)) {
            facePawn(target)

            // Check HP thresholds and spawn minions
            val hpPercent = (getCurrentHp().toDouble() / getMaxHp().toDouble()) * 100

            // Spawn melee minion at 70% HP
            if (!meleeMinionSpawned && hpPercent <= 70) {
                spawnMinion("melee")
                meleeMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted melee fighter!")
                }
            }

            // Spawn ranged minion at 35% HP
            if (!rangedMinionSpawned && hpPercent <= 35) {
                spawnMinion("ranged")
                rangedMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted archer!")
                }
            }

            // Spawn mage minion at 10% HP
            if (!mageMinionSpawned && hpPercent <= 10) {
                spawnMinion("mage")
                mageMinionSpawned = true
                if (target is Player) {
                    target.message("The abomination summons a corrupted sorcerer!")
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
            val damage = this.world.random(maxHit + 30)  // Increased bonus from +15 to +30
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
                target.hit(this@toxicSpitAttack.world.random(50), type = HitType.HIT)  // Increased from 30 to 50
                target.graphic(id = 289, height = 0) // Poison splash

                // 50% chance to poison
                if (this@toxicSpitAttack.world.chance(1, 2)) {
                    target.poison(initialDamage = 6) {
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
                target.hit(this@sewerGasAttack.world.random(40), type = HitType.HIT)  // Increased from 25 to 40

                // 75% chance to poison
                if (this@sewerGasAttack.world.chance(3, 4)) {
                    target.poison(initialDamage = 4) {
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
            // Increased damage bonus from +10 to +25
            val damage = maxOf(1, maxHit + 25)
            target.hit(this.world.random(damage), type = HitType.HIT, delay = 1)

            // Always poison on successful hit
            target.poison(initialDamage = 7) {
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
                // First hit - increased from 20 to 35
                val hit1 = this@acidWaveAttack.world.random(35)
                target.hit(hit1, type = HitType.HIT)
                target.graphic(id = 167, height = 0) // Acid splash

                if (target is Player) {
                    target.message("Acid burns you!")
                }

                wait(2)

                // Second hit (50% chance) - increased from 18 to 30
                if (this@acidWaveAttack.world.chance(1, 2)) {
                    val hit2 = this@acidWaveAttack.world.random(30)
                    target.hit(hit2, type = HitType.HIT)
                    target.graphic(id = 167, height = 0)

                    wait(2)

                    // Third hit (25% chance) - increased from 15 to 25
                    if (this@acidWaveAttack.world.chance(1, 4)) {
                        val hit3 = this@acidWaveAttack.world.random(25)
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
                // Deal moderate damage - increased from 28 to 45
                val damage = this@plagueBreathAttack.world.random(45)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 145, height = 100) // Dark cloud splash

                // Drain stats for players
                if (target is Player) {
                    target.message("The plague breath weakens you!")

                    // Drain attack by 1-3 levels
                    val attackDrain = this@plagueBreathAttack.world.random(3) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.ATTACK, value = 0 - attackDrain, capValue = 0)

                    // Drain strength by 1-3 levels
                    val strengthDrain = this@plagueBreathAttack.world.random(3) + 1
                    target.getSkills().alterCurrentLevel(skill = Skills.STRENGTH, value = 0 - strengthDrain, capValue = 0)

                    // Drain defence by 1-3 levels
                    val defenceDrain = this@plagueBreathAttack.world.random(3) + 1
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
            // Minions hit hard - add significant bonus damage
            val damage = npc.world.random(maxHit + 20)
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
                // Use a high accuracy for minions
                npc.world.randomDouble() < 0.85 // 85% accuracy
            } else {
                MeleeCombatFormula.getAccuracy(npc, target) >= npc.world.randomDouble()
            }
            
            if (accuracy) {
                // Ranged minions hit hard
                val damage = npc.world.random(35)
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
                // Magic minions hit hard
                val damage = npc.world.random(40)
                target.hit(damage, type = HitType.HIT)
                target.graphic(id = 163, height = 0)
            } else {
                target.hit(damage = 0, type = HitType.BLOCK)
            }
        }
    }

    /**
     * Checks for players in radius and makes minion attack them
     */
    private fun checkMinionAggro(npc: Npc) {
        val radius = 10 // Aggro radius for minions
        
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val tile = npc.tile.transform(x, z)
                val chunk = npc.world.chunks.get(tile, createIfNeeded = false) ?: continue
                
                val players = chunk.getEntities<Player>(tile, EntityType.PLAYER, EntityType.CLIENT)
                if (players.isEmpty()) {
                    continue
                }
                
                // Filter players that can be attacked (online, visible, and aggro check passes)
                val targets = players.filter { player ->
                    if (!player.isOnline || player.invisible) {
                        false
                    } else {
                        // Check if aggro check allows attacking this player
                        npc.aggroCheck == null || npc.aggroCheck?.invoke(npc, player) == true
                    }
                }
                
                if (targets.isEmpty()) {
                    continue
                }
                
                val target = targets.random()
                if (npc.getCombatTarget() != target) {
                    npc.attack(target)
                }
                return
            }
        }
    }

    /**
     * Spawns a minion near the boss based on type
     * Minions are temporary and will not respawn after death
     */
    private fun Npc.spawnMinion(type: String) {
        // Visual effect at boss location
        this.graphic(id = 86, height = 100) // Purple spawn effect on boss

        val target = getCombatTarget() ?: return
        if (target !is Player) return

        // Determine which NPC to spawn based on type
        val npcName = when (type) {
            "melee" -> "npc.zombie"      // Melee fighter
            "ranged" -> "npc.goblin"     // Ranged attacker (goblin with attack option)
            "mage" -> "npc.dark_wizard"  // Magic user
            else -> return
        }

        // Find a spawn location near the boss (1-2 tiles away)
        val bossTile = this.tile
        val spawnOffsetX = world.random(-2..2) // -2 to 2
        val spawnOffsetZ = world.random(-2..2) // -2 to 2
        val spawnTile = bossTile.transform(spawnOffsetX, spawnOffsetZ)

        // Create and spawn the minion
        val minion = Npc(getRSCM(npcName), spawnTile, world)
        minion.respawns = false // Minions don't respawn - they're temporary
        minion.walkRadius = 0 // Minions stay in place
        minion.setActive(true)

        // Set up aggro to always attack players
        minion.aggroCheck = { _, _ -> true }
        
        // Set combat class based on minion type
        when (type) {
            "melee" -> minion.combatClass = CombatClass.MELEE
            "ranged" -> minion.combatClass = CombatClass.RANGED
            "mage" -> minion.combatClass = CombatClass.MAGIC
        }

        // Spawn the minion in the world
        world.spawn(minion)

        // Set up aggro timer for minion to continuously check for players
        minion.timers[MINION_AGGRO_TIMER] = 1

        // Use a queue task to ensure minion is fully initialized before attacking
        minion.queue {
            wait(1) // Wait one tick for NPC to be fully initialized
            // Make the minion attack the player immediately
            minion.attack(target)
        }

        // Show effect to player
        target.graphic(id = 86, height = 0)
    }
}
