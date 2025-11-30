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

/**
 * @author Alycia <https://github.com/alycii>
 * Venenatis Combat Plugin - The Spider Wilderness Boss
 * 
 * Venenatis is a massive venomous spider with web-based attacks:
 * - Web Projectile: Entangles and damages players
 * - Spawn Spiderlings: Summons smaller spider minions
 * - Venom Spit: Poison damage over time
 * - Web Trap: Creates sticky traps that slow players
 * - Web Stick: Sticks player in place and removes overhead prayers
 * 
 * Combat Level: 464, Hitpoints: 850
 * Location: Silk Chasm (Multi-combat wilderness)
 * 
 * ============================================================================
 * 🎮 GUIDE FOR EDITING VENENATIS COMBAT 🎮
 * ============================================================================
 * 
 * This guide will help you customize Venenatis's combat behavior!
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
 * Look for a file like "VenenatisConfigsPlugin.kt" and find:
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
 *         prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.ACCURATE)
 *         
 *         // Make Venenatis say something
 *         forceChat("*Does something cool!*")
 *         
 *         // Play an animation (find animation IDs in the game)
 *         animate(5319) // Replace with your animation ID
 *         
 *         // Optional: Show a graphic effect
 *         graphic(172) // Replace with your graphic ID
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
 *     when (this.world.random(3)) {
 *         0 -> normalStabAttack(target)
 *         1 -> normalRangedAttack(target)
 *         2 -> myNewAttack(target)  // ← Add your new attack here!
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
 *         prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.ACCURATE)
 *         forceChat("*This attack ignores prayers!*")
 *         animate(5319)
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
 * There are two ways to set max hit:
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
 *         // When HP is below 25%, use enraged attacks
 *         getCurrentHp() <= getMaxHp() * 0.25 -> {
 *             // Change 0.25 to 0.5 for enraged at 50% HP instead
 *             // Change 0.25 to 0.1 for enraged at 10% HP instead
 *         }
 *         
 *         // Special attacks happen every 4 attacks, with 1/3 chance
 *         attackCount >= 4 && this.world.chance(1, 3) -> {
 *             // Change 4 to 3 for specials every 3 attacks
 *             // Change (1, 3) to (1, 2) for 50% chance instead of 33%
 *         }
 *     }
 * 
 * To change how often special attacks happen:
 * 
 *     attackCount >= 3 && this.world.chance(1, 2) -> {
 *         // This means: every 3 attacks, 50% chance for special
 *     }
 * 
 * To change which attacks are used:
 * 
 *     when (this.world.random(5)) {
 *         0 -> webProjectileAttack(target)    // 20% chance
 *         1 -> spawnSpiderlingsAttack(target) // 20% chance
 *         2 -> venomSpitAttack(target)        // 20% chance
 *         3 -> webTrapAttack(target)          // 20% chance
 *         4 -> webStickAttack(target)         // 20% chance
 *     }
 * 
 * To make one attack more common:
 * 
 *     when {
 *         this.world.chance(1, 2) -> webProjectileAttack(target)  // 50% chance
 *         this.world.chance(1, 2) -> venomSpitAttack(target)      // 25% chance (50% of remaining)
 *         else -> normalStabAttack(target)                         // 25% chance
 *     }
 * 
 * 
 * ✨ 6. ADDING SPECIAL EFFECTS ✨
 * ================================
 * 
 * POISON EFFECT:
 * 
 *     if (target is Player) {
 *         target.poison(initialDamage = 4) {  // Starts at 4 damage
 *             target.message("You are poisoned!")
 *         }
 *     }
 * 
 * FREEZE/STUN EFFECT:
 * 
 *     if (target is Player) {
 *         target.freeze(cycles = 5) {  // Freeze for 5 cycles (3 seconds)
 *             target.message("You break free!")
 *         }
 *         
 *         // Or use stun
 *         target.stun(3)  // Stun for 3 cycles
 *     }
 * 
 * KNOCKBACK EFFECT:
 * 
 *     val knockbackTile = Tile(
 *         target.tile.x + 2,  // Move 2 tiles east
 *         target.tile.z,      // Same Z coordinate
 *         target.tile.height
 *     )
 *     target.moveTo(knockbackTile)
 * 
 * DISABLE PRAYERS:
 * 
 *     if (target is Player) {
 *         Prayers.deactivateAll(target)  // Turn off all prayers
 *         Prayers.disableOverheads(target, cycles = 10)  // Disable overheads for 10 cycles
 *     }
 * 
 * DAMAGE OVER TIME:
 * 
 *     target.queue {
 *         repeat(5) {  // Repeat 5 times
 *             wait(2)  // Wait 2 cycles between each
 *             if (target.isAlive()) {
 *                 val damage = this.world.random(1..5)
 *                 target.hit(damage, type = HitType.POISON, delay = 0)
 *             }
 *         }
 *     }
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
 * 
 * ============================================================================
 */

class VenenatisCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.venenatis") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    /**
     * Main combat loop for Venenatis
     * 
     * HOW TO EDIT THIS FUNCTION:
     * 
     * 1. Change attack distance:
     *    - distance = 8  → Can attack from 8 tiles away (current)
     *    - distance = 1  → Must be next to target (melee only)
     *    - distance = 10 → Can attack from 10 tiles away
     * 
     * 2. Change when enraged phase starts:
     *    - getCurrentHp() <= getMaxHp() * 0.25  → Enraged at 25% HP (current)
     *    - getCurrentHp() <= getMaxHp() * 0.5   → Enraged at 50% HP
     *    - getCurrentHp() <= getMaxHp() * 0.1   → Enraged at 10% HP
     * 
     * 3. Change special attack frequency:
     *    - attackCount >= 4 && this.world.chance(1, 3)  → Every 4 attacks, 33% chance (current)
     *    - attackCount >= 3 && this.world.chance(1, 2)  → Every 3 attacks, 50% chance
     *    - attackCount >= 5 && this.world.chance(1, 4)  → Every 5 attacks, 25% chance
     * 
     * 4. Change normal attack selection:
     *    - Change the numbers in when (this.world.random(3)) to add/remove attacks
     *    - Add more -> when (this.world.random(4)) { 0, 1, 2, 3 -> ... }
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return  // Get the target Venenatis is fighting

        // Main combat loop - runs while Venenatis can fight the target
        while (canEngageCombat(target)) {
            facePawn(target)  // Face the target
            
            // Move to attack range and check if ready to attack
            // distance = 8 means Venenatis can attack from 8 tiles away
            // projectile = true means this attack can use projectiles (ranged/magic)
            // Check if target is in melee range (distance 2)
            val isInMeleeRange = this.tile.getDistance(target.tile) <= 2
            
            if (moveToAttackRange(it, target, distance = if (isInMeleeRange) 2 else 8, projectile = !isInMeleeRange) && isAttackDelayReady()) {
                // Use melee if in melee range, otherwise use magic
                if (isInMeleeRange) {
                    normalStabAttack(target)      // Melee stab attack
                } else {
                    normalMagicAttack(target)     // Magic venom attack
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
     * 
     * HOW TO EDIT:
     * 
     * 1. Change attack type:
     *    - CombatClass.MELEE → Use melee formula
     *    - CombatClass.RANGED → Use ranged formula
     *    - CombatClass.MAGIC → Use magic formula
     * 
     * 2. Change combat style:
     *    - CombatStyle.STAB → Stab attack (current)
     *    - CombatStyle.SLASH → Slash attack
     *    - CombatStyle.CRUSH → Crush attack
     * 
     * 3. Change attack style:
     *    - AttackStyle.ACCURATE → More accurate
     *    - AttackStyle.AGGRESSIVE → More damage
     *    - AttackStyle.DEFENSIVE → More defense
     * 
     * 4. Change animation:
     *    - animate(5319) → Change 5319 to a different animation ID
     * 
     * 5. Change poison chance:
     *    - this.world.chance(3, 10) → 30% chance (3 out of 10)
     *    - this.world.chance(1, 2) → 50% chance
     *    - this.world.chance(1, 5) → 20% chance
     * 
     * 6. Change poison damage:
     *    - initialDamage = 4 → Starts at 4 damage per tick
     *    - initialDamage = 6 → Starts at 6 damage per tick
     */
    private fun Npc.normalStabAttack(target: Pawn) {
        // Prepare the attack - tells the game what type of attack this is
        prepareAttack(CombatClass.MELEE, CombatStyle.STAB, AttackStyle.ACCURATE)
        
        // Make Venenatis say something (optional)
        forceChat("*Strikes with venomous fangs*")
        
        // Play the attack animation (5319 = spider stab animation)
        animate(5319)
        
        // Play melee attack sound
        if (target is Player) {
            this.world.spawn(
                AreaSound(this.tile, 3607, radius = 10, volume = 50) // Spider attack sound
            )
        }
        
        // Calculate max hit based on protection prayer
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
            0  // 0 max hit if praying melee
        } else {
            this.world.random(20..60)  // 20-60 max hit if not praying melee
        }
        
        // Deal damage to the target
        if (maxHit > 0) {
            val damage = this.world.random(maxHit + 1)
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

    private fun Npc.normalRangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        forceChat("*Hssssss*")
        animate(5320) // Spider ranged animation
        
        val projectile = createProjectile(
            target, 
            gfx = 1749, 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = delay
        ) { hit ->
            if (hit.landed() && this.world.chance(1, 4)) {
                if (target is Player) {
                    target.forceChat("I'm stuck!")
                }
            }
        }
    }

    private fun Npc.normalMagicAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*Casts venom magic*")
        animate(5321) // Spider magic animation
        graphic(172) // Venom aura
        
        // Play magic attack sound
        if (target is Player) {
            this.world.spawn(
                AreaSound(this.tile, 384, radius = 10, volume = 50) // Magic attack sound
            )
        }
        
        val projectile = createProjectile(
            target, 
            gfx = 1750, 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        // Calculate max hit based on protection prayer
        val maxHit = if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
            0  // 0 max hit if praying magic
        } else {
            this.world.random(20..50)  // 20-50 max hit if not praying magic
        }
        
        // Deal damage after projectile delay
        this.world.queue {
            wait(delay)
            
            if (target.isAlive()) {
                if (maxHit > 0) {
                    val damage = this@normalMagicAttack.world.random(maxHit + 1)
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

    private fun Npc.webProjectileAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*WEAVES A POWERFUL WEB*")
        animate(5320) // Web animation
        graphic(170) // Web casting graphic
        
        if (target is Player) {
            target.message("Venenatis weaves a powerful web attack!")
        }
        
        // Deal damage to main target first
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = 1
        )
        
        // Launch multiple web projectiles in a spread pattern
        world.players.forEach { player ->
            if (!player.tile.isWithinRadius(target.tile, 2) || !player.isAlive()) return@forEach
            if (player.tile.isWithinRadius(this.tile, 12)) {
                val projectile = createProjectile(
                    player, 
                    gfx = 1749, 
                    startHeight = 43, 
                    endHeight = 31, 
                    delay = 51, 
                    angle = 15, 
                    steepness = 127
                )
                
                world.spawn(projectile)
                
                val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(player), player.getCentreTile())
                
                // Apply web effect after delay
                this.world.queue {
                    wait(delay)
                    
                    if (player.isAlive()) {
                        val damage = this@webProjectileAttack.world.random(10..25) // 10-25 damage
                        player.hit(damage, type = HitType.HIT, delay = 0)
                        player.graphic(id = 171, height = 0, delay = 0) // Web entangle graphic
                        
                        // Entangle player
                        player.message("You are entangled in Venenatis's web!")
                        player.freeze(cycles = 5) {
                            player.message("You break free from the web!")
                        }
                        
                        // Reduce player's run energy (simplified)
                        if (player is Player) {
                            player.message("Your energy is drained by the web!")
                        }
                    }
                }
            }
        }
    }

    private fun Npc.spawnSpiderlingsAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*CALLS FORTH HER CHILDREN*")
        animate(5322) // Summoning animation
        graphic(174) // Spawning graphic
        
        // Deal damage to main target
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = 1
        )
        
        if (target is Player) {
            target.message("Spiderlings emerge from the shadows to attack you!")
        }
        
        // Create spawn effects around the area
        val spiderlingCount = this.world.random(3..5)
        
        repeat(spiderlingCount) {
            val spawnTile = Tile(
                this.tile.x + this.world.random(-4..4),
                this.tile.z + this.world.random(-4..4),
                this.tile.height
            )
            
            // Create spawn effect and damage nearby players
            world.spawn(TileGraphic(spawnTile, id = 86, height = 100, delay = 0))
            
            // Damage players near spawn locations
            this.world.queue {
                wait(2)
                
                world.players.forEach { nearbyPlayer ->
                    if (nearbyPlayer.tile.isWithinRadius(spawnTile, 1) && nearbyPlayer.isAlive()) {
                        val damage = this@spawnSpiderlingsAttack.world.random(5..15)
                        nearbyPlayer.hit(damage, type = HitType.HIT, delay = 0)
                        nearbyPlayer.message("A spiderling bites you!")
                    }
                }
            }
        }
    }

    private fun Npc.venomSpitAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*blaaaargh*")
        animate(5321) // Venom animation
        graphic(175) // Venom charge graphic
        
        // Deal damage to main target
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = 1
        )
        
        if (target is Player) {
            target.message("Venenatis spits deadly venom in random directions!")
        }
        
        // Create random 1-tile venom pools scattered randomly around the area
        val poolCount = this.world.random(15..25) // 15-25 random pools
        val venomTiles = mutableSetOf<Tile>()
        val poisonedPlayers = mutableSetOf<Player>() // Track players who are already poisoned to avoid re-poisoning
        
        // Queue the venom pool creation and monitoring in background (non-blocking)
        this.world.queue {
            wait(2)
            
            // Create all venom pools - each pool is exactly 1 tile, placed on completely random tiles
            repeat(poolCount) {
                // Random position in a large area around Venenatis (20x20 radius = 10 tiles in each direction)
                val centerX = this@venomSpitAttack.tile.x
                val centerZ = this@venomSpitAttack.tile.z
                val offsetX = this@venomSpitAttack.world.random(-10..10)
                val offsetZ = this@venomSpitAttack.world.random(-10..10)
                
                val venomTile = Tile(
                    centerX + offsetX,
                    centerZ + offsetZ,
                    this@venomSpitAttack.tile.height
                )
                
                // Each pool is exactly 1 tile - add it to the set
                venomTiles.add(venomTile)
                
                // Spawn visible venom pool graphic on this single tile (poison circle)
                world.spawn(TileGraphic(venomTile, id = 289, height = 0, delay = 0))
            }
            
            // Track players currently on pools and players who are poisoned (continue after stepping off)
            val playersOnPools = mutableSetOf<Player>()
            val poisonedPlayers = mutableSetOf<Player>() // Players who are poisoned (continues after stepping off)
            
            // Continuously check for players stepping on pools
            var poolLifetime = 40 // Pools last for 40 ticks
            while (poolLifetime > 0) {
                wait(1) // Check every cycle (1 tick)
                
                // Refresh graphics to keep pools visible
                if (poolLifetime % 10 == 0) {
                    venomTiles.forEach { tile ->
                        world.spawn(TileGraphic(tile, id = 289, height = 0, delay = 0))
                    }
                }
                
                // Check all players for stepping on venom pools
                world.players.forEach { player ->
                    if (player.isAlive()) {
                        val isOnPool = player.tile in venomTiles
                        
                        if (isOnPool) {
                            // Player is on a venom pool
                            if (player !in playersOnPools) {
                                // Just stepped on - add to tracking
                                playersOnPools.add(player)
                                player.message("You step in a pool of deadly venom!")
                            }
                            
                            // Deal 2 poison damage every 1 tick while on the pool
                            player.hit(2, type = HitType.POISON, delay = 0)
                        } else {
                            // Player stepped off the pool
                            if (player in playersOnPools) {
                                playersOnPools.remove(player)
                                
                                // Start poison effect that continues after stepping off
                                if (player !in poisonedPlayers) {
                                    poisonedPlayers.add(player)
                                    
                                    // Poison effect: 8 damage every 5 ticks for 80 ticks (16 damage instances)
                                    player.queue {
                                        var ticksRemaining = 80 // Poison lasts for 80 ticks (16 instances of 8 damage)
                                        while (ticksRemaining > 0 && player.isAlive()) {
                                            wait(5) // Every 5 ticks
                                            if (player.isAlive()) {
                                                player.hit(8, type = HitType.POISON, delay = 0)
                                            }
                                            ticksRemaining -= 5
                                        }
                                        // Remove from poisoned set after poison expires
                                        poisonedPlayers.remove(player)
                                    }
                                }
                            }
                        }
                    }
                }
                
                poolLifetime--
            }
        }
    }

    private fun Npc.webTrapAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*Ssssssss*")
        animate(5323) // Web trap animation
        
        // Deal damage to main target
        val hit = dealHit(
            target = target,
            formula = MagicCombatFormula,
            delay = 1
        )
        
        if (target is Player) {
            target.message("Venenatis lays deadly web traps around the area!")
        }
        
        // Create web traps in a 5x5 grid around the target
        val centerX = target.tile.x
        val centerZ = target.tile.z
        
        // Create 8-12 web traps randomly placed
        val trapCount = this.world.random(8..12)
        val trapTiles = mutableSetOf<Tile>()
        
        this.world.queue {
            wait(2)
            
            // Create all trap tiles
            repeat(trapCount) {
                val trapTile = Tile(
                    centerX + this@webTrapAttack.world.random(-3..3),
                    centerZ + this@webTrapAttack.world.random(-3..3),
                    target.tile.height
                )
                trapTiles.add(trapTile)
                
                // Warning phase - show warning graphic (web graphic)
                world.spawn(TileGraphic(trapTile, id = 177, height = 0, delay = 0)) // Warning web graphic
            }
            
            // After warning delay, activate traps and keep them visible
            wait(3)
            
            // Activate all traps and keep them visible
            trapTiles.forEach { trapTile ->
                // Spawn the actual trap graphic (web graphic)
                world.spawn(TileGraphic(trapTile, id = 178, height = 0, delay = 0)) // Web trap graphic
            }
            
            // Track players who are frozen or in grace period
            val frozenPlayers = mutableSetOf<Player>()
            val gracePeriodPlayers = mutableSetOf<Player>()
            
            // Keep traps visible and check for players stepping on them
            var trapLifetime = 100 // Traps last for 100 cycles (~1 minute)
            while (trapLifetime > 0) {
                wait(1) // Check every cycle
                
                // Refresh graphics more frequently to keep traps visible (every 5 cycles instead of 10)
                if (trapLifetime % 5 == 0) {
                    trapTiles.forEach { tile ->
                        world.spawn(TileGraphic(tile, id = 178, height = 0, delay = 0)) // Web trap graphic
                    }
                }
                
                // Check all players for stepping on traps
                world.players.forEach { player ->
                    if (player.isAlive() && player.tile in trapTiles) {
                        // Only trigger if player is not frozen and not in grace period
                        if (player !in frozenPlayers && player !in gracePeriodPlayers) {
                            player.message("You are caught in a sticky web trap!")
                            
                            // Freeze for 20 seconds (~33 cycles)
                            val freezeCycles = TimeConstants.secondsToCycles(20) ?: 33
                            frozenPlayers.add(player)
                            player.freeze(cycles = freezeCycles) {
                                player.message("You break free from the web trap!")
                                frozenPlayers.remove(player)
                                
                                // Give player 5 seconds grace period to step out
                                val graceCycles = TimeConstants.secondsToCycles(5) ?: 8
                                gracePeriodPlayers.add(player)
                                
                                // Remove from grace period after 5 seconds
                                player.world.queue {
                                    wait(graceCycles)
                                    gracePeriodPlayers.remove(player)
                                    
                                    // If still on trap after grace period, trigger trap again immediately
                                    if (player.isAlive() && player.tile in trapTiles) {
                                        player.message("You're still standing on a web trap!")
                                        // Trigger freeze again immediately
                                        val freezeCycles2 = TimeConstants.secondsToCycles(20) ?: 33
                                        frozenPlayers.add(player)
                                        player.freeze(cycles = freezeCycles2) {
                                            player.message("You break free from the web trap!")
                                            frozenPlayers.remove(player)
                                            
                                            // Give another 5 seconds grace period
                                            val graceCycles2 = TimeConstants.secondsToCycles(5) ?: 8
                                            gracePeriodPlayers.add(player)
                                            
                                            // Remove from grace period after 5 seconds (recursive check)
                                            player.world.queue {
                                                wait(graceCycles2)
                                                gracePeriodPlayers.remove(player)
                                                
                                                // If still on trap, trigger again
                                                if (player.isAlive() && player.tile in trapTiles) {
                                                    player.message("You're still standing on a web trap!")
                                                    // This will be handled by the main loop checking
                                                }
                                            }
                                        }
                                        player.graphic(id = 171, height = 0, delay = 0) // Web graphic on player
                                        
                                        // Damage over time while frozen
                                        player.queue {
                                            repeat(freezeCycles2) {
                                                wait(1)
                                                if (player.isAlive() && player in frozenPlayers) {
                                                    val trapDamage = this@webTrapAttack.world.random(1..3) // 1-3 damage per tick
                                                    player.hit(trapDamage, type = HitType.POISON, delay = 0)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            player.graphic(id = 171, height = 0, delay = 0) // Web graphic on player
                            
                            // Small damage over time while frozen
                            player.queue {
                                repeat(freezeCycles) {
                                    wait(1)
                                    if (player.isAlive() && player in frozenPlayers) {
                                        val trapDamage = this@webTrapAttack.world.random(1..3) // 1-3 damage per tick
                                        player.hit(trapDamage, type = HitType.POISON, delay = 0)
                                    }
                                }
                            }
                        }
                    } else {
                        // Player moved off trap - remove from grace period if they were in it
                        if (player in gracePeriodPlayers && player.tile !in trapTiles) {
                            gracePeriodPlayers.remove(player)
                            player.message("You safely moved away from the web trap!")
                        }
                    }
                }
                
                trapLifetime--
            }
        }
    }

    private fun Npc.webStickAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        forceChat("*SHOOTS STICKY WEB STRANDS*")
        animate(5320) // Web animation
        graphic(170) // Web casting graphic
        
        if (target is Player) {
            target.message("Venenatis shoots sticky web strands at you!")
        }
        
        // Create projectile
        val projectile = createProjectile(
            target, 
            gfx = 1749,  // Web projectile graphic
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile())
        
        // Apply web stick effect after delay
        this.world.queue {
            wait(delay)
            
            if (target is Player && target.isAlive()) {
                val damage = this@webStickAttack.world.random(15..30) // 15-30 damage
                target.hit(damage, type = HitType.HIT, delay = 0)
                target.graphic(id = 171, height = 0, delay = 0) // Web entangle graphic
                
                // Stick player in place (freeze for 2 seconds)
                // 2 seconds ≈ 3-4 cycles (using TimeConstants conversion)
                val freezeCycles = TimeConstants.secondsToCycles(2) ?: 4
                target.message("You are stuck in Venenatis's sticky web!")
                target.freeze(cycles = freezeCycles) {
                    target.message("You break free from the sticky web!")
                }
                
                // Remove overhead prayers and disable them for 2 seconds
                Prayers.deactivateAll(target)
                val disableCycles = TimeConstants.secondsToCycles(2) ?: 4
                Prayers.disableOverheads(target, disableCycles)
                target.message("Your overhead prayer has been knocked off!")
                target.message("You cannot use overhead prayers for a short time.")
            } else if (target.isAlive()) {
                // For non-player targets, just deal damage
                val damage = this@webStickAttack.world.random(15..30)
                target.hit(damage, type = HitType.HIT, delay = 0)
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
