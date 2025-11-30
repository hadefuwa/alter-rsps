package org.alter.plugins.content.npcs.venenatis

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
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.game.model.timer.TimeConstants
import org.alter.rscm.RSCM.getRSCM
import org.alter.game.model.attr.AttributeKey

/**
 * @author Alycia <https://github.com/alycii>
 * Venenatis Combat Plugin - The Spider Wilderness Boss
 * 
 * Venenatis is a massive venomous spider with web-based attacks.
 * 
 * Combat Level: 464, Hitpoints: 850
 * Location: Silk Chasm (Multi-combat wilderness)
 */

class VenenatisCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        // Attribute to mark duplicates
        private val IS_DUPLICATE_ATTR = AttributeKey<Boolean>("venenatis_duplicate")
    }

    init {
        // Cache the Venenatis NPC ID to avoid repeated lookups
        val venenatisId = getRSCM("npc.venenatis")
        
        onNpcCombat("npc.venenatis") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Prevent duplicates from dropping loot
        onAnyNpcDeath {
            val npc = ctx as Npc
            // If this is a duplicate, prevent loot drops
            if (npc.attr.has(IS_DUPLICATE_ATTR) && npc.attr[IS_DUPLICATE_ATTR] == true) {
                // Clear loot tables to prevent drops
                npc.combatDef = npc.combatDef.copy(LootTables = null)
            }
        }
        
        // Prevent flip-over animation by clearing block animations immediately after they're set
        // Hook into hit processing to clear block animations for Venenatis
        world.queue {
            while (true) {
                wait(1)
                world.npcs.forEach { npc ->
                    if (npc.id == venenatisId && npc.isSpawned()) {
                        // If block animation is -1 (disabled) but an animation was set,
                        // clear it immediately to prevent flip-over
                        // Only clear if the NPC is not currently in an attack animation
                        val blockAnim = npc.combatDef.blockAnimation
                        if (blockAnim == -1 && npc.previouslySetAnim != -1) {
                            // Check if this is likely a block animation (not an attack)
                            // Attack animations are 5319 (melee) and 5321 (magic)
                            val currentAnim = npc.previouslySetAnim
                            if (currentAnim != 5319 && currentAnim != 5321 && currentAnim != 5320) {
                                // This is likely a block animation, clear it
                                npc.animate(-1)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Main combat loop for Venenatis
     * Alternates between melee and magic attacks every 5 attacks
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return  // Get the target Venenatis is fighting
        var currentAttackStyle = "melee"  // Current attack style: "melee" or "magic" (NO RANGED)
        var styleAttackCount = 0  // Counts attacks with current style
        var attackCount = 0  // Total attack counter for special attacks

        // Main combat loop - runs while Venenatis can fight the target
        while (canEngageCombat(target)) {
            facePawn(target)  // Face the target
            
            // Determine attack range based on current style
            // Melee requires distance 1, magic can attack from distance 8
            val attackDistance = if (currentAttackStyle == "melee") 1 else 8
            val useProjectile = currentAttackStyle != "melee"
            
            if (moveToAttackRange(it, target, distance = attackDistance, projectile = useProjectile) && isAttackDelayReady()) {
                attackCount++  // Increment total attack counter
                styleAttackCount++  // Increment style-specific attack counter
                
                // Special attack: Duplicate every 10 attacks
                if (attackCount >= 10 && this.world.chance(1, 2)) {
                    duplicateAttack(target)
                    attackCount = 0  // Reset counter after special attack
                } else {
                    // Normal attack - use current attack style
                    // Change attack style every 5 attacks
                    if (styleAttackCount >= 5) {
                        currentAttackStyle = if (currentAttackStyle == "melee") "magic" else "melee"
                        styleAttackCount = 0  // Reset style counter
                    }
                    
                    if (currentAttackStyle == "melee") {
                        normalStabAttack(target)      // Melee stab attack
                    } else {
                        normalMagicAttack(target)     // Magic venom attack
                    }
                }
                
                postAttackLogic(target)  // Handle post-attack effects (sets attack delay timer)
            }
            
            it.wait(1)  // Wait 1 cycle before checking again
            target = getCombatTarget() ?: break  // Update target (might have changed)
        }

        // Clean up when combat ends
        resetFacePawn()      // Stop facing the target
        removeCombatTarget() // Clear the combat target
    }

    /**
     * Normal melee stab attack
     */
    private fun Npc.normalStabAttack(target: Pawn) {
        // Prepare the attack - tells the game what type of attack this is
        prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.ACCURATE)
        
        // Make Venenatis say something (optional)
        forceChat("*Strikes with venomous fangs*")
        
        // Play the attack animation (5319 = spider stab animation)
        animate(5319)
        
        // Check if this is a duplicate (hits through prayer)
        val isDuplicate = this.attr.has(IS_DUPLICATE_ATTR) && this.attr[IS_DUPLICATE_ATTR] == true
        
        // Calculate max hit based on protection prayer
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
            if (isDuplicate) {
                5  // Duplicate hits through prayer with at least 5 damage
            } else {
                0  // 0 max hit if praying melee
            }
        } else {
            this.world.random(20..60)  // 20-60 max hit if not praying melee
        }
        
        // Deal damage to the target
        if (maxHit > 0) {
            val damage = if (isDuplicate && target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
                // Duplicate always deals at least 5 damage through prayer
                5
            } else {
                this.world.random(maxHit + 1)
            }
            target.hit(damage, type = HitType.HIT, delay = 1)
            
            // 30% chance to poison
            if (damage > 0 && this.world.chance(3, 10)) {
                if (target is Player) {
                    target.message("You have been poisoned!")
                    target.poison(initialDamage = 4) {
                        target.message("The venom courses through your veins.")
                    }
                }
            }
        } else {
            // Still apply a 0 damage hit to show the attack animation
            target.hit(0, type = HitType.HIT, delay = 1)
        }
    }

    /**
     * Special attack: Creates a duplicate of Venenatis that disappears after 10 seconds
     */
    private fun Npc.duplicateAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*CREATES A MIRROR IMAGE OF HERSELF*")
        animate(5322) // Summoning animation
        graphic(174) // Spawning graphic
        
        if (target is Player) {
            target.message("Venenatis creates a duplicate of herself!")
        }
        
        // Find a nearby spawn location (2-4 tiles away)
        val spawnOffsetX = this.world.random(-4..4)
        val spawnOffsetZ = this.world.random(-4..4)
        val spawnTile = Tile(
            this.tile.x + spawnOffsetX,
            this.tile.z + spawnOffsetZ,
            this.tile.height
        )
        
        // Create the duplicate
        val duplicate = Npc(getRSCM("npc.venenatis"), spawnTile, this.world)
        duplicate.respawns = false  // Don't respawn when killed
        duplicate.walkRadius = 3  // Same walk radius as original
        duplicate.setActive(true)
        
        // Set duplicate to have 30 hitpoints instead of full HP
        // Create a modified combat definition with 30 HP and no loot tables
        val originalCombatDef = duplicate.combatDef
        duplicate.combatDef = originalCombatDef.copy(
            hitpoints = 30,
            LootTables = null  // No loot drops for duplicates
        )
        duplicate.setCurrentHp(30)  // Set current HP to 30 as well
        
        // Mark as duplicate so it can hit through prayer
        duplicate.attr[IS_DUPLICATE_ATTR] = true
        
        // Spawn the duplicate
        val world = this.world
        world.spawn(duplicate)
        
        // Make duplicate face the target
        duplicate.facePawn(target)
        
        // Make duplicate attack the target
        duplicate.attack(target)
        
        // Remove duplicate after 10 seconds
        world.queue {
            val secondsToCycles = TimeConstants.secondsToCycles(10) ?: 17  // 10 seconds = ~17 cycles
            wait(secondsToCycles)
            
            // Remove the duplicate if it still exists
            if (duplicate.isSpawned()) {
                if (target is Player) {
                    target.message("Venenatis's duplicate fades away!")
                }
                // Stop combat before removing
                Combat.reset(duplicate)
                duplicate.resetFacePawn()
                world.remove(duplicate)
            }
        }
    }

    private fun Npc.normalMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*Casts venom magic*")
        animate(5321) // Spider magic animation
        graphic(172) // Venom aura
        
        val projectile = createProjectile(
            target, 
            gfx = 1750, 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        this.world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        // Check if this is a duplicate (hits through prayer)
        val isDuplicate = this.attr.has(IS_DUPLICATE_ATTR) && this.attr[IS_DUPLICATE_ATTR] == true
        
        // Calculate max hit based on protection prayer
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
            if (isDuplicate) {
                5  // Duplicate hits through prayer with at least 5 damage
            } else {
                0  // 0 max hit if praying magic
            }
        } else {
            this.world.random(20..50)  // 20-50 max hit if not praying magic
        }
        
        // Deal damage after projectile delay
        this.world.queue {
            wait(delay)
            
            if (target.isAlive()) {
                if (maxHit > 0) {
                    val damage = if (isDuplicate && target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                        // Duplicate always deals at least 5 damage through prayer
                        5
                    } else {
                        this@normalMagicAttack.world.random(maxHit + 1)
                    }
                    target.hit(damage, type = HitType.HIT, delay = 0)
                    target.graphic(id = 173, height = 0, delay = 0) // Venom splash graphic
                } else {
                    // Still apply a 0 damage hit to show the attack animation
                    target.hit(0, type = HitType.HIT, delay = 0)
                    target.graphic(id = 173, height = 0, delay = 0) // Venom splash graphic
                }
            }
        }
    }

    // Helper function to get direction from one tile to another
    fun Tile.getDirection(target: Tile): Int {
        val deltaX = target.x - this.x
        val deltaZ = target.z - this.z
        
        return when {
            deltaZ > 0 && deltaX == 0 -> 0 // North
            deltaZ > 0 && deltaX > 0 -> 1 // Northeast
            deltaZ == 0 && deltaX > 0 -> 2 // East
            deltaZ < 0 && deltaX > 0 -> 3 // Southeast
            deltaZ < 0 && deltaX == 0 -> 4 // South
            deltaZ < 0 && deltaX < 0 -> 5 // Southwest
            deltaZ == 0 && deltaX < 0 -> 6 // West
            deltaZ > 0 && deltaX < 0 -> 7 // Northwest
            else -> 2 // Default to East
        }
    }
}
