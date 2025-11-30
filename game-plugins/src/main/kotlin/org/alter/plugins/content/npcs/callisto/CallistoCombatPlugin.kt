package org.alter.plugins.content.npcs.callisto

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
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.MeleeCombatStrategy
import org.alter.plugins.content.mechanics.prayer.Prayer
import org.alter.plugins.content.mechanics.prayer.Prayers

/**
 * @author Alycia <https://github.com/alycii>
 * Callisto Combat Plugin - The Bear Wilderness Boss
 * 
 * Callisto is a massive bear with powerful area-of-effect attacks:
 * - Shockwave Attack: Damages all nearby players
 * - Bear Swipe: High damage melee attack
 * - Ground Slam: Creates traps that stun players
 * - Roar: Fear effect that can force players to move
 * - Knockback Attack: Sends players flying with a powerful blow
 * 
 * Combat Level: 470, Hitpoints: 255
 * Location: Callisto's Den (Multi-combat wilderness)
 * 
 * ============================================================================
 * 🎮 GUIDE FOR EDITING CALLISTO COMBAT 🎮
 * ============================================================================
 * 
 * This guide will help you customize Callisto's combat behavior!
 * 
 * 📋 TABLE OF CONTENTS:
 * 1. Changing Attack Speed
 * 2. Adding/Modifying Attacks
 * 3. Checking Protection Prayers
 * 4. Setting Max Hit Damage
 * 5. Changing Attack Patterns
 * 6. Adding Special Effects
 * 
 * 
 * ⚡ 1. CHANGING ATTACK SPEED ⚡
 * ==============================
 * 
 * Attack speed is controlled in the NPC's config file (not this file).
 * Look for a file like "CallistoConfigsPlugin.kt" and find:
 * 
 *     attackSpeed = 4  // This is in game cycles (ticks)
 * 
 * Lower number = faster attacks:
 * - attackSpeed = 2  → Very fast (attacks every 2 cycles = 1.2 seconds)
 * - attackSpeed = 3  → Fast (attacks every 3 cycles = 1.8 seconds)
 * - attackSpeed = 4  → Normal (attacks every 4 cycles = 2.4 seconds) ← DEFAULT
 * - attackSpeed = 5  → Slow (attacks every 5 cycles = 3.0 seconds)
 * - attackSpeed = 7  → Very slow (attacks every 7 cycles = 4.2 seconds)
 * 
 * Note: 1 game cycle = 0.6 seconds
 * 
 * 
 * 🎯 2. ADDING/MODIFYING ATTACKS 🎯
 * ==================================
 * 
 * To add a new attack, create a new function like this:
 * 
 *     private fun Npc.myNewAttack(target: Pawn) {
 *         // Set the attack type (MELEE, RANGED, or MAGIC)
 *         prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
 *         
 *         // Make Callisto say something
 *         forceChat("*Roars ferociously!*")
 *         
 *         // Play an animation (find animation IDs in the game)
 *         animate(4925) // Replace with your animation ID
 *         
 *         // Optional: Show a graphic effect
 *         graphic(157) // Replace with your graphic ID
 *         
 *         // Deal damage to the target
 *         val hit = dealHit(
 *             target = target,
 *             formula = MeleeCombatFormula,  // Use MeleeCombatFormula, RangedCombatFormula, or MagicCombatFormula
 *             delay = 1  // Delay before hit lands (1 = immediate)
 *         ) { hit ->
 *             // This code runs when the hit lands
 *             if (hit.landed() && target is Player) {
 *                 target.message("You got hit!")
 *             }
 *         }
 *     }
 * 
 * Then add it to the attack selection in the combat() function:
 * 
 *     when (this.world.random(5)) {
 *         0 -> shockwaveAttack(target)
 *         1 -> groundSlamAttack(target)
 *         2 -> bearRoarAttack(target)
 *         3 -> bearSwipeAttack(target)
 *         4 -> myNewAttack(target)  // ← Add your new attack here!
 *     }
 * 
 * 
 * 🛡️ 3. CHECKING PROTECTION PRAYERS 🛡️
 * =====================================
 * 
 * You can check if a player has a protection prayer active:
 * 
 *     if (target is Player) {
 *         // Check for Protect from Melee
 *         if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
 *             target.message("Your Protect from Melee blocks some damage!")
 *             // Reduce damage by 40% (protection prayers reduce damage by 60%, so 40% gets through)
 *         }
 *         
 *         // Check for Protect from Magic
 *         if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
 *             target.message("Your Protect from Magic blocks the attack!")
 *             // Magic protection completely blocks magic damage
 *         }
 *         
 *         // Check for Protect from Missiles (Ranged)
 *         if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
 *             target.message("Your Protect from Missiles blocks some damage!")
 *             // Ranged protection reduces damage by 40%
 *         }
 *     }
 * 
 * Example: Make an attack that ignores protection prayers:
 * 
 *     private fun Npc.ignoresPrayerAttack(target: Pawn) {
 *         prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
 *         forceChat("*This attack ignores prayers!*")
 *         animate(4925)
 *         
 *         // Calculate max hit manually (ignoring prayer reduction)
 *         val maxHit = MeleeCombatFormula.getMaxHit(this, target)
 *         
 *         // Deal damage directly without using dealHit (which respects prayers)
 *         val damage = this.world.random(maxHit + 1)
 *         target.hit(damage, type = HitType.HIT, delay = 1)
 *         target.message("This attack cannot be blocked by prayers!")
 *     }
 * 
 * 
 * 💥 4. SETTING MAX HIT DAMAGE 💥
 * ================================
 * 
 * There are three ways to set max hit:
 * 
 * METHOD 1: Use the formula (recommended - respects prayers and bonuses)
 * 
 *     val hit = dealHit(
 *         target = target,
 *         formula = MeleeCombatFormula,  // Automatically calculates max hit
 *         delay = 1
 *     )
 * 
 * METHOD 2: Set a custom max hit manually
 * 
 *     // Get the base max hit from the formula
 *     val baseMaxHit = MeleeCombatFormula.getMaxHit(this, target)
 *     
 *     // Add bonus damage (e.g., +10 extra damage)
 *     val customMaxHit = baseMaxHit + 10
 *     
 *     // Or set a fixed max hit
 *     val fixedMaxHit = 50  // Always maxes at 50 damage
 *     
 *     // Deal the hit with custom max
 *     val hit = dealHit(
 *         target = target,
 *         formula = MeleeCombatFormula,
 *         delay = 1,
 *         maxHit = customMaxHit  // Use your custom max hit
 *     )
 * 
 * METHOD 3: Deal damage directly (bypasses formulas)
 * 
 *     // Deal a fixed amount of damage
 *     val damage = this.world.random(20..40)  // Random damage between 20-40
 *     target.hit(damage, type = HitType.HIT, delay = 1)
 *     
 *     // Or deal a specific amount
 *     target.hit(30, type = HitType.HIT, delay = 1)  // Always deals 30 damage
 * 
 * 
 * 🎲 5. CHANGING ATTACK PATTERNS 🎲
 * ==================================
 * 
 * The attack pattern is controlled in the combat() function:
 * 
 *     when {
 *         // When HP is below 30%, use enraged attacks
 *         getCurrentHp() <= getMaxHp() * 0.3 -> {
 *             // Change 0.3 to 0.5 for enraged at 50% HP instead
 *             // Change 0.3 to 0.1 for enraged at 10% HP instead
 *         }
 *         
 *         // Special attacks happen with 25% chance (1 in 4)
 *         this.world.chance(1, 4) -> {
 *             // Change (1, 4) to (1, 2) for 50% chance instead of 25%
 *             // Change (1, 4) to (1, 3) for 33% chance
 *         }
 *     }
 * 
 * To change how often special attacks happen:
 * 
 *     this.world.chance(1, 2) -> {
 *         // This means: 50% chance for special attack every turn
 *     }
 * 
 * To change which attacks are used:
 * 
 *     when (this.world.random(5)) {
 *         0 -> shockwaveAttack(target)    // 20% chance
 *         1 -> groundSlamAttack(target)   // 20% chance
 *         2 -> bearRoarAttack(target)     // 20% chance
 *         3 -> bearSwipeAttack(target)    // 20% chance
 *         4 -> knockbackAttack(target)    // 20% chance
 *     }
 * 
 * To make one attack more common:
 * 
 *     when {
 *         this.world.chance(1, 2) -> shockwaveAttack(target)  // 50% chance
 *         this.world.chance(1, 2) -> bearRoarAttack(target)   // 25% chance (50% of remaining)
 *         else -> normalBearAttack(target)                      // 25% chance
 *     }
 * 
 * 
 * ✨ 6. ADDING SPECIAL EFFECTS ✨
 * ================================
 * 
 * STUN EFFECT:
 * 
 *     if (target is Player) {
 *         target.stun(3)  // Stun for 3 cycles (1.8 seconds)
 *         target.graphic(80)  // Show stun graphic
 *     }
 * 
 * KNOCKBACK EFFECT:
 * 
 *     val knockbackTile = Tile(
 *         target.tile.x + 3,  // Move 3 tiles east
 *         target.tile.z,      // Same Z coordinate
 *         target.tile.height
 *     )
 *     target.moveTo(knockbackTile)
 * 
 * AREA OF EFFECT DAMAGE:
 * 
 *     world.players.forEach { player ->
 *         if (player.tile.getDistance(this.tile) <= 3 && player.isAlive()) {
 *             val damage = this.world.random(10..20)
 *             player.hit(damage, type = HitType.HIT, delay = 1)
 *         }
 *     }
 * 
 * DAMAGE OVER TIME (BLEEDING):
 * 
 *     target.queue {
 *         repeat(5) {  // Repeat 5 times
 *             wait(2)  // Wait 2 cycles between each
 *             if (target.isAlive()) {
 *                 val damage = this.world.random(1..3)
 *                 target.hit(damage, type = HitType.POISON, delay = 0)
 *             }
 *         }
 *     }
 * 
 * STAT REDUCTION:
 * 
 *     if (target is Player) {
 *         val reduction = this.world.random(3..7)
 *         player.getSkills().alterCurrentLevel(0, -reduction) // Reduce Attack
 *         player.getSkills().alterCurrentLevel(1, -reduction) // Reduce Strength
 *         player.getSkills().alterCurrentLevel(2, -reduction) // Reduce Defence
 *     }
 * 
 * FORCE MOVEMENT:
 * 
 *     val escapeDirection = Tile(
 *         player.tile.x + 3,  // 3 tiles east
 *         player.tile.z,      // Same Z
 *         player.tile.height
 *     )
 *     player.moveTo(escapeDirection)
 * 
 * 
 * 📝 QUICK REFERENCE 📝
 * =====================
 * 
 * Combat Classes: CombatClass.MELEE, CombatClass.RANGED, CombatClass.MAGIC
 * Combat Styles: CombatStyle.STAB, CombatStyle.SLASH, CombatStyle.CRUSH, CombatStyle.RANGED, CombatStyle.MAGIC
 * Attack Styles: AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.DEFENSIVE, AttackStyle.CONTROLLED
 * 
 * Formulas: MeleeCombatFormula, RangedCombatFormula, MagicCombatFormula
 * 
 * Hit Types: HitType.HIT, HitType.POISON, HitType.DISEASE
 * 
 * Prayer Icons: PrayerIcon.PROTECT_FROM_MELEE, PrayerIcon.PROTECT_FROM_MAGIC, PrayerIcon.PROTECT_FROM_MISSILES
 * 
 * 
 * 💡 TIPS 💡
 * ===========
 * 
 * - Always test your changes in-game!
 * - Start with small changes and test each one
 * - Use target.message() to send messages to players for debugging
 * - Check if target is Player before using player-specific functions
 * - Use this.world.random() for random numbers
 * - Use this.world.chance(numerator, denominator) for percentages
 * - Callisto is melee-focused, so most attacks use MeleeCombatFormula
 * 
 * ============================================================================
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

    /**
     * Main combat loop for Callisto
     * 
     * HOW TO EDIT THIS FUNCTION:
     * 
     * 1. Change attack distance:
     *    - distance = 1  → Must be next to target (melee only, current)
     *    - distance = 3  → Can attack from 3 tiles away
     *    - distance = 5  → Can attack from 5 tiles away
     * 
     * 2. Change when enraged phase starts:
     *    - getCurrentHp() <= getMaxHp() * 0.3  → Enraged at 30% HP (current)
     *    - getCurrentHp() <= getMaxHp() * 0.5  → Enraged at 50% HP
     *    - getCurrentHp() <= getMaxHp() * 0.1  → Enraged at 10% HP
     * 
     * 3. Change special attack chance:
     *    - this.world.chance(1, 4) → 25% chance (1 in 4, current)
     *    - this.world.chance(1, 2) → 50% chance (1 in 2)
     *    - this.world.chance(1, 3) → 33% chance (1 in 3)
     * 
     * 4. Change which attacks are used:
     *    - Change the numbers in when (this.world.random(4)) or when (this.world.random(5))
     *    - Add more -> when (this.world.random(6)) { 0, 1, 2, 3, 4, 5 -> ... }
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return  // Get the target Callisto is fighting
        var attackCount = 0  // Counts how many attacks have been made

        // Main combat loop - runs while Callisto can fight the target
        while (canEngageCombat(target)) {
            facePawn(target)  // Face the target
            
            // Move to attack range and check if ready to attack
            // distance = 1 means Callisto must be next to target (melee range)
            // projectile = false means this is a melee attack (no projectiles)
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                attackCount++  // Increment attack counter
                
                // ============================================================
                // ATTACK SELECTION LOGIC
                // ============================================================
                // This decides which attack to use based on HP and chance
                
                val attackType = when {
                    // ENRAGED PHASE: When HP is below 30%
                    // Change 0.3 to 0.5 for enraged at 50% HP, or 0.1 for 10% HP
                    getCurrentHp() <= getMaxHp() * 0.3 -> "enraged"
                    
                    // SPECIAL ATTACKS: 25% chance (1 in 4) - Only knockback
                    // Change (1, 4) to (1, 2) for 50% chance, or (1, 3) for 33% chance
                    this.world.chance(1, 4) -> "special"
                    
                    // NORMAL ATTACKS: Default behavior - 80% melee, 20% magic
                    else -> "normal"
                }
                
                // Execute the selected attack type
                when (attackType) {
                    "enraged" -> {
                        // Enraged phase - more frequent special attacks
                        // 50% chance knockback, 50% chance normal attacks
                        if (this.world.chance(1, 2)) {
                            knockbackAttack(target)   // Knockback attack
                        } else {
                            // 80% melee, 20% magic
                            if (this.world.chance(4, 5)) {
                                normalBearAttack(target)  // Simple melee (80%)
                            } else {
                                whiteBlastAttack(target)  // Simple magic (20%)
                            }
                        }
                    }
                    "special" -> {
                        // Special attack - only knockback
                        knockbackAttack(target)   // Knockback attack
                    }
                    else -> {
                        // Normal attacks - 80% simple melee, 20% simple magic
                        if (this.world.chance(4, 5)) {
                            normalBearAttack(target)  // Simple melee attack (80%)
                        } else {
                            whiteBlastAttack(target)  // Simple magic attack (20%)
                        }
                    }
                }
                
                postAttackLogic(target)  // Handle post-attack effects
            }
            
            it.wait(1)  // Wait 1 cycle before checking again
            target = getCombatTarget() ?: break  // Update target (might have changed)
        }

        // Clean up when combat ends
        resetFacePawn()      // Stop facing the target
        removeCombatTarget() // Clear the combat target
    }

    /**
     * Normal melee bear attack
     * 
     * HOW TO EDIT:
     * 
     * 1. Change attack type:
     *    - CombatClass.MELEE → Use melee formula (current)
     *    - CombatClass.RANGED → Use ranged formula
     *    - CombatClass.MAGIC → Use magic formula
     * 
     * 2. Change combat style:
     *    - CombatStyle.CRUSH → Crush attack (current)
     *    - CombatStyle.STAB → Stab attack
     *    - CombatStyle.SLASH → Slash attack
     * 
     * 3. Change attack style:
     *    - AttackStyle.ACCURATE → More accurate (current)
     *    - AttackStyle.AGGRESSIVE → More damage
     *    - AttackStyle.DEFENSIVE → More defense
     * 
     * 4. Change animation:
     *    - animate(4925) → Change 4925 to a different animation ID
     * 
     * 5. Change damage threshold for reaction:
     *    - hit.hit.hitmarks.sumOf { it.damage } > 25 → Reacts if damage > 25 (current)
     *    - hit.hit.hitmarks.sumOf { it.damage } > 30 → Reacts if damage > 30
     *    - hit.hit.hitmarks.sumOf { it.damage } > 20 → Reacts if damage > 20
     */
    private fun Npc.normalBearAttack(target: Pawn) {
        // Prepare the attack - tells the game what type of attack this is
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        
        // Make Callisto say something (optional)
        forceChat("*Growls menacingly*")
        
        // Play the attack animation (4925 = bear attack animation)
        // To find other animation IDs, look in the game's animation files
        animate(4925)
        
        // Play sound effect (bear growl/attack sound)
        if (target is Player) {
            target.playSound(239) // Bear growl/attack sound
        }
        
        // Check if player has Protect from Melee - if not, deal 10-40 damage
        if (target is Player && !target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
            // Player doesn't have Protect from Melee - deal 10-40 damage
            val damage = this.world.random(10..40)
            target.hit(damage, type = HitType.HIT, delay = 1)
            if (damage > 25) {
                target.forceChat("Oof!")  // Make player say something
            }
        } else {
            // Player has Protect from Melee or is not a player - use normal formula
            val hit = dealHit(
                target = target,                    // Who to hit
                formula = MeleeCombatFormula,       // Use melee damage formula
                delay = 1                           // Delay before hit lands (1 = immediate)
            )
            
            // Check if the hit dealt significant damage and make player react
            // hit.hit.hitmarks.sumOf { it.damage } gets the total damage dealt
            // > 25 means only react if damage was more than 25
            if (hit.hit.hitmarks.sumOf { it.damage } > 25 && target is Player) {
                target.forceChat("Oof!")  // Make player say something
            }
        }
    }

    private suspend fun Npc.shockwaveAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*REARS UP FOR MASSIVE ATTACK*")
        animate(4927) // Shockwave animation
        graphic(157) // Ground shockwave graphic
        
        // Play sound effect (powerful roar/ground shake)
        world.players.forEach { player ->
            if (player.tile.getDistance(this.tile) <= 5) {
                player.playSound(240) // Powerful roar/ground shake sound
            }
        }
        
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
                player.playSound(241) // Ground shake impact sound
                
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
        
        // Play sound effect (ground slam)
        world.players.forEach { player ->
            if (player.tile.getDistance(this.tile) <= 5) {
                player.playSound(242) // Ground slam sound
            }
        }
        
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
                        player.playSound(243) // Trap activation sound
                        
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
        
        // Play sound effect (claw swipe)
        if (target is Player) {
            target.playSound(244) // Claw swipe sound
        }
        
        // Check if player has Protect from Melee - if not, deal 10-40 damage
        if (target is Player && !target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
            // Player doesn't have Protect from Melee - deal 10-40 damage
            val damage = this.world.random(10..40)
            target.hit(damage, type = HitType.HIT, delay = 1)
            
            if (damage > 20) {
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
        } else {
            // Player has Protect from Melee or is not a player - use normal formula
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
    }

    private suspend fun Npc.bearRoarAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*LETS OUT A TERRIFYING ROAR*")
        animate(4928) // Roar animation
        graphic(158) // Fear aura graphic
        
        // Play sound effect (terrifying roar)
        world.players.forEach { player ->
            if (player.tile.getDistance(this.tile) <= 5) {
                player.playSound(245) // Terrifying roar sound
            }
        }
        
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

    private suspend fun Npc.knockbackAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.ACCURATE)
        forceChat("*CHARGES WITH DEVASTATING FORCE*")
        animate(4925) // Bear charge/swipe animation
        graphic(245) // Powerful impact graphic
        
        // Play sound effect (charge/impact)
        if (target is Player) {
            target.playSound(246) // Charge/impact sound
        }
        
        if (target is Player) {
            // Check if player has Protect from Melee - if not, deal 10-40 damage
            if (!target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
                // Player doesn't have Protect from Melee - deal 10-40 damage
                val damage = this.world.random(10..40)
                target.hit(damage, type = HitType.HIT, delay = 1)
            } else {
                // Player has Protect from Melee - use normal formula
                dealHit(
                    target = target,
                    formula = MeleeCombatFormula,
                    delay = 1
                )
            }
            
            target.message("Callisto's massive blow sends you flying!")
            
            // Disable all protection prayers
            Prayers.deactivate(target, Prayer.PROTECT_FROM_MELEE)
            Prayers.deactivate(target, Prayer.PROTECT_FROM_MAGIC)
            Prayers.deactivate(target, Prayer.PROTECT_FROM_MISSILES)
            target.message("The powerful blow disrupts your prayers!")
            
            // Calculate knockback direction (away from Callisto)
            val npcTile = this.tile
            val playerTile = target.tile
            
            // Calculate direction from NPC to player
            val direction = Direction.between(npcTile, playerTile)
            val directionAngle = direction.angle
            
            // Calculate knockback distance (3-5 tiles away)
            val knockbackDistance = this.world.random(3..5)
            val endTile = when (direction) {
                Direction.NORTH -> Tile(playerTile.x, playerTile.z + knockbackDistance, playerTile.height)
                Direction.SOUTH -> Tile(playerTile.x, playerTile.z - knockbackDistance, playerTile.height)
                Direction.EAST -> Tile(playerTile.x + knockbackDistance, playerTile.z, playerTile.height)
                Direction.WEST -> Tile(playerTile.x - knockbackDistance, playerTile.z, playerTile.height)
                Direction.NORTH_EAST -> Tile(playerTile.x + knockbackDistance, playerTile.z + knockbackDistance, playerTile.height)
                Direction.NORTH_WEST -> Tile(playerTile.x - knockbackDistance, playerTile.z + knockbackDistance, playerTile.height)
                Direction.SOUTH_EAST -> Tile(playerTile.x + knockbackDistance, playerTile.z - knockbackDistance, playerTile.height)
                Direction.SOUTH_WEST -> Tile(playerTile.x - knockbackDistance, playerTile.z - knockbackDistance, playerTile.height)
                else -> Tile(playerTile.x, playerTile.z + knockbackDistance, playerTile.height) // Default to north
            }
            
            // Execute the knockback - use moveTo instead of forceMove to prevent player lock
            target.graphic(157) // Flying/knockback graphic
            target.moveTo(endTile) // Move player instantly without locking
            target.message("Callisto's massive blow sends you flying!")
            target.playSound(247) // Crash/landing sound
            
            // Additional damage on landing (fall damage) - delayed slightly
            target.queue {
                wait(1) // Small delay for landing effect
                target.message("You crash to the ground!")
                val fallDamage = this@knockbackAttack.world.random(5..15)
                target.hit(fallDamage, type = HitType.HIT, delay = 0)
                target.message("The impact hurts!")
            }
        } else {
            // For non-player targets, just deal damage
            dealHit(
                target = target,
                formula = MeleeCombatFormula,
                delay = 1
            )
        }
    }

    private suspend fun Npc.magicBlastAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*CHANNELS POWERFUL MAGIC ENERGY*")
        animate(4928) // Magic casting animation
        graphic(157) // Magic energy graphic
        
        // Play sound effect (magic charging)
        world.players.forEach { player ->
            if (player.tile.getDistance(this.tile) <= 5) {
                player.playSound(248) // Magic charging sound
            }
        }
        
        world.queue {
            wait(2)
            
            // Show magic projectile traveling to target
            if (target is Player) {
                // Spawn magic graphic at Callisto
                world.spawn(TileGraphic(this@magicBlastAttack.tile, id = 157, height = 100, delay = 0))
                
                wait(1)
                
                // Spawn magic impact graphic at player
                world.spawn(TileGraphic(target.tile, id = 157, height = 100, delay = 0))
                
                // Check if player has Protect from Magic - if not, deal 5-25 damage
                if (!target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                    // Player doesn't have Protect from Magic - deal 5-25 damage
                    val damage = this@magicBlastAttack.world.random(5..25)
                    target.hit(damage, type = HitType.HIT, delay = 0)
                    target.message("Callisto's magical blast strikes you!")
                    target.graphic(157) // Impact graphic on player
                    
                    if (damage > 30) {
                        target.forceChat("Argh!")  // React to high damage
                    }
                } else {
                    // Player has Protect from Magic - blocked or reduced
                    target.message("Your Protect from Magic partially blocks the magical blast!")
                    // Still deal some damage through protection (reduced)
                    val damage = this@magicBlastAttack.world.random(2..12)
                    target.hit(damage, type = HitType.HIT, delay = 0)
                }
            } else {
                // For non-player targets, use magic formula
                dealHit(
                    target = target,
                    formula = MagicCombatFormula,
                    delay = 1
                )
            }
        }
    }

    private suspend fun Npc.magicAreaAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*CHANNELS DESTRUCTIVE MAGIC IN ALL DIRECTIONS*")
        animate(4928) // Magic casting animation
        graphic(157) // Magic energy graphic
        
        // Play sound effect (magic charging)
        world.players.forEach { player ->
            if (player.tile.getDistance(this.tile) <= 5) {
                player.playSound(248) // Magic charging sound
            }
        }
        
        world.queue {
            wait(2)
            
            // Find all players within 4 tiles (area of effect)
            val nearbyPlayers = mutableListOf<Player>()
            world.players.forEach { player ->
                if (player.tile.getDistance(this@magicAreaAttack.tile) <= 4 && player.isAlive()) {
                    nearbyPlayers.add(player)
                }
            }
            
            nearbyPlayers.forEach { player: Player ->
                val distance = this@magicAreaAttack.tile.getDistance(player.tile)
                
                // Spawn magic graphic at player location
                world.spawn(TileGraphic(player.tile, id = 157, height = 100, delay = 0))
                
                // Damage based on distance (closer = more damage)
                val damage = if (!player.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                    when {
                        distance <= 1 -> this@magicAreaAttack.world.random(15..30) // Point blank: 15-30 damage
                        distance == 2 -> this@magicAreaAttack.world.random(10..20) // Close: 10-20 damage
                        distance == 3 -> this@magicAreaAttack.world.random(5..15) // Medium: 5-15 damage
                        else -> this@magicAreaAttack.world.random(3..10) // Far: 3-10 damage
                    }
                } else {
                    // Player has Protect from Magic - reduced damage
                    when {
                        distance <= 1 -> this@magicAreaAttack.world.random(5..15) // Point blank: 5-15 damage
                        distance == 2 -> this@magicAreaAttack.world.random(3..10) // Close: 3-10 damage
                        distance == 3 -> this@magicAreaAttack.world.random(2..8) // Medium: 2-8 damage
                        else -> this@magicAreaAttack.world.random(1..5) // Far: 1-5 damage
                    }
                }
                
                player.hit(damage, type = HitType.HIT, delay = 1)
                player.message("Callisto's area magic spell strikes you!")
                player.graphic(157) // Impact graphic on player
                
                if (damage > 20) {
                    player.forceChat("Argh!")  // React to high damage
                }
            }
        }
    }

    private suspend fun Npc.whiteBlastAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*BLASTS WITH WHITE MAGIC*")
        animate(4928) // Magic casting animation
        graphic(157) // White magic graphic
        
        world.queue {
            wait(1)
            
            if (target is Player) {
                // Spawn white magic graphic at Callisto
                world.spawn(TileGraphic(this@whiteBlastAttack.tile, id = 157, height = 100, delay = 0))
                
                wait(1)
                
                // Spawn white magic impact graphic at player
                world.spawn(TileGraphic(target.tile, id = 157, height = 100, delay = 0))
                
                // Check if player has Protect from Magic - if not, deal 8-25 damage
                if (!target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                    // Player doesn't have Protect from Magic - deal 8-25 damage
                    val damage = this@whiteBlastAttack.world.random(8..25)
                    target.hit(damage, type = HitType.HIT, delay = 0)
                    target.message("Callisto's white magic blast hits you!")
                    target.graphic(157) // White impact graphic on player
                } else {
                    // Player has Protect from Magic - reduced damage
                    target.message("Your Protect from Magic partially blocks the white magic blast!")
                    val damage = this@whiteBlastAttack.world.random(3..12)
                    target.hit(damage, type = HitType.HIT, delay = 0)
                }
            } else {
                // For non-player targets, use magic formula
                dealHit(
                    target = target,
                    formula = MagicCombatFormula,
                    delay = 1
                )
            }
        }
    }
}
