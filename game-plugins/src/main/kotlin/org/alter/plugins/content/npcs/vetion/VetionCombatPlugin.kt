package org.alter.plugins.content.npcs.vetion

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.game.model.move.moveTo

/**
 * Vetion Combat Plugin - The Skeletal Wilderness Boss
 * 
 * Vetion is an undead skeletal warrior with necromantic powers:
 * - Skeletal Claw: Melee slash attack
 * - Bone Throw: Ranged bone projectile attack
 * - Necromantic Strike: Magic death energy attack
 * - Skeletal Bite: Melee crush attack
 * - Summon Hellhounds: Summons skeletal hounds to attack nearby players
 * - Bone Barrage: Launches multiple bone projectiles in sequence
 * - Earth Shake: Ground-based area damage attack
 * 
 * Combat Level: 454, Hitpoints: 800
 * Location: Vet'ion's Lair (Multi-combat wilderness)
 * 
 * ============================================================================
 * 🎮 GUIDE FOR EDITING VETION COMBAT 🎮
 * ============================================================================
 * 
 * This guide will help you customize Vetion's combat behavior!
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
 * Look for a file like "VetionConfigsPlugin.kt" and find:
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
 *         prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
 *         
 *         // Make Vetion say something
 *         forceChat("*Raises skeletal arms!*")
 *         
 *         // Play an animation (find animation IDs in the game)
 *         animate(5485) // Replace with your animation ID
 *         
 *         // Optional: Show a graphic effect
 *         graphic(100) // Replace with your graphic ID
 *         
 *         // Deal damage to the target
 *         dealHit(
 *             target = target,
 *             maxHit = 30,  // Maximum damage this attack can deal
 *             landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
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
 *     when (this.world.random(4)) {
 *         0 -> skeletalClawAttack(target)
 *         1 -> boneThrowAttack(target)
 *         2 -> necromanticStrikeAttack(target)
 *         3 -> myNewAttack(target)  // ← Add your new attack here!
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
 *         prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
 *         forceChat("*This attack ignores prayers!*")
 *         animate(5485)
 *         
 *         // Calculate max hit manually (ignoring prayer reduction)
 *         val maxHit = 35
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
 * Vetion uses custom max hits in his attacks. Here's how to change them:
 * 
 * METHOD 1: Set a fixed max hit (current method)
 * 
 *     dealHit(
 *         target = target,
 *         maxHit = 32,  // Always maxes at 32 damage
 *         landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
 *         delay = 1
 *     )
 * 
 * METHOD 2: Use formula-based max hit
 * 
 *     val maxHit = MeleeCombatFormula.getMaxHit(this, target)
 *     dealHit(
 *         target = target,
 *         maxHit = maxHit,  // Uses calculated max hit
 *         landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
 *         delay = 1
 *     )
 * 
 * METHOD 3: Add bonus damage to formula
 * 
 *     val baseMaxHit = MeleeCombatFormula.getMaxHit(this, target)
 *     val customMaxHit = baseMaxHit + 10  // Add 10 extra damage
 *     dealHit(
 *         target = target,
 *         maxHit = customMaxHit,
 *         landHit = MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
 *         delay = 1
 *     )
 * 
 * METHOD 4: Deal damage directly (bypasses formulas)
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
 *     // Special attacks happen at different attack counts:
 *     if (attackCount >= 4 && this.world.chance(1, 3)) {
 *         // Every 4 attacks, 33% chance (1 in 3)
 *         // Change 4 to 3 for every 3 attacks
 *         // Change (1, 3) to (1, 2) for 50% chance
 *     } else if (attackCount >= 6 && this.world.chance(1, 4)) {
 *         // Every 6 attacks, 25% chance (1 in 4)
 *     } else if (attackCount >= 8 && this.world.chance(1, 5)) {
 *         // Every 8 attacks, 20% chance (1 in 5)
 *     }
 * 
 * To change how often special attacks happen:
 * 
 *     if (attackCount >= 3 && this.world.chance(1, 2)) {
 *         // This means: every 3 attacks, 50% chance for special
 *     }
 * 
 * To change which attacks are used:
 * 
 *     when (this.world.random(4)) {
 *         0 -> skeletalClawAttack(target)      // 25% chance
 *         1 -> boneThrowAttack(target)         // 25% chance
 *         2 -> necromanticStrikeAttack(target)  // 25% chance
 *         3 -> skeletalBiteAttack(target)      // 25% chance
 *     }
 * 
 * To make one attack more common:
 * 
 *     when {
 *         this.world.chance(1, 2) -> skeletalClawAttack(target)  // 50% chance
 *         this.world.chance(1, 2) -> boneThrowAttack(target)     // 25% chance (50% of remaining)
 *         else -> necromanticStrikeAttack(target)                // 25% chance
 *     }
 * 
 * 
 * ✨ 6. ADDING SPECIAL EFFECTS ✨
 * ================================
 * 
 * PROJECTILE EFFECTS:
 * 
 *     val projectile = createProjectile(
 *         target, 
 *         gfx = 9,           // Graphic ID (bone = 9, dark magic = 100)
 *         startHeight = 43,   // Starting height
 *         endHeight = 31,     // Ending height
 *         delay = 51,         // Projectile delay
 *         angle = 15,         // Launch angle
 *         steepness = 127     // Arc steepness
 *     )
 *     world.spawn(projectile)
 * 
 * AREA OF EFFECT DAMAGE:
 * 
 *     world.players.forEach { player ->
 *         if (player.tile.getDistance(this.tile) <= 3 && player.isAlive()) {
 *             val damage = this.world.random(15..25)
 *             player.hit(damage, type = HitType.HIT, delay = 1)
 *         }
 *     }
 * 
 * TILE GRAPHICS (GROUND EFFECTS):
 * 
 *     world.spawn(TileGraphic(
 *         id = 86,              // Graphic ID
 *         tile = target.tile,   // Where to show it
 *         height = 0,           // Height offset
 *         delay = 20            // Delay before showing
 *     ))
 * 
 * STAGGERED ATTACKS (MULTIPLE PROJECTILES):
 * 
 *     repeat(4) { i ->  // Launch 4 projectiles
 *         world.queue {
 *             wait(i + 1)  // Stagger timing (1, 2, 3, 4 cycles apart)
 *             // Create and launch projectile here
 *         }
 *     }
 * 
 * EXPANDING AREA EFFECTS:
 * 
 *     for (radius in 1..3) {  // Expand from radius 1 to 3
 *         world.queue {
 *             wait(radius)  // Wait before each ring
 *             // Create graphics in expanding circle
 *         }
 *     }
 * 
 * CUSTOM MESSAGES:
 * 
 *     if (target is Player) {
 *         target.message("Vet'ion: Your bones will join my collection!")
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
 * Accuracy Check: formula.getAccuracy(this, target) >= world.randomDouble()
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
 * - Vetion uses mixed combat styles (melee, ranged, magic), so choose the right formula
 * - Use world.queue { wait(X) } for delayed effects
 * - Use repeat(X) { } for multiple projectiles or effects
 * 
 * ============================================================================
 */

class VetionCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Handle combat for Phase 1 (Purple) - 6611
        onNpcCombat("npc.vetion") {
            npc.queue {
                npc.combat(this)
            }
        }
        
        // Handle combat for Phase 2 (Orange/Reborn) - 6612
        onNpcCombat("npc.vetion_6612") {
            npc.queue {
                npc.combat(this)
            }
        }
    }

    /**
     * Main combat loop for Vetion
     * 
     * HOW TO EDIT THIS FUNCTION:
     * 
     * 1. Change attack distance:
     *    - distance = 1  → Must be next to target (melee only, current)
     *    - distance = 3  → Can attack from 3 tiles away
     *    - distance = 5  → Can attack from 5 tiles away
     * 
     * 2. Change special attack timing:
     *    - attackCount >= 4 && this.world.chance(1, 3)  → Every 4 attacks, 33% chance (current)
     *    - attackCount >= 3 && this.world.chance(1, 2)  → Every 3 attacks, 50% chance
     *    - attackCount >= 6 && this.world.chance(1, 4)  → Every 6 attacks, 25% chance
     * 
     * 3. Change taunt frequency:
     *    - attackCount >= 3 && this.world.chance(1, 5)  → Every 3 attacks, 20% chance (current)
     *    - attackCount >= 2 && this.world.chance(1, 3)  → Every 2 attacks, 33% chance
     * 
     * 4. Change normal attack selection:
     *    - Change the numbers in when (this.world.random(4)) to add/remove attacks
     *    - Add more -> when (this.world.random(5)) { 0, 1, 2, 3, 4 -> ... }
     */
    private suspend fun Npc.combat(it: QueueTask) {
        var target = getCombatTarget() ?: return  // Get the target Vetion is fighting
        var attackCount = 0  // Counts how many attacks have been made (resets on specials)

        // Opening taunt - message sent when combat starts
        if (target is Player) {
            target.message("Vet'ion: You dare disturb the eternal rest of the dead!")
        }

        // Main combat loop - runs while Vetion can fight the target
        while (canEngageCombat(target)) {
            facePawn(target)  // Face the target
            
            // Move to attack range and check if ready to attack
            // distance = 1 means Vetion must be next to target (melee range)
            // projectile = false means this is a melee attack (no projectiles)
            if (moveToAttackRange(it, target, distance = 1, projectile = false) && isAttackDelayReady()) {
                attackCount++  // Increment attack counter
                
                // ============================================================
                // ATTACK SELECTION LOGIC
                // ============================================================
                // Special attacks have priority and happen at specific attack counts
                
                // SPECIAL ATTACK 1: Summon Hellhounds (COMMENTED OUT - Not working)
                // Every 4 attacks, 33% chance (1 in 3)
                // Change 4 to 3 for every 3 attacks, or (1, 3) to (1, 2) for 50% chance
                // if (attackCount >= 4 && this.world.chance(1, 3)) {
                //     summonHellhoundsAttack(target)  // Summon skeletal hounds
                //     attackCount = 0  // Reset counter after special
                // } 
                // SPECIAL ATTACK 2: Bone Barrage (COMMENTED OUT - Not working)
                // Every 6 attacks, 25% chance (1 in 4)
                // Change 6 to 5 for every 5 attacks, or (1, 4) to (1, 3) for 33% chance
                // else if (attackCount >= 6 && this.world.chance(1, 4)) {
                //     boneBarrageAttack(target)  // Launch multiple bone projectiles
                //     attackCount = 0  // Reset counter after special
                // } 
                // SPECIAL ATTACK: Earth Shake (AOE)
                // Every 5 attacks, 30% chance (1 in 3)
                if (attackCount >= 5 && this.world.chance(1, 3)) {
                    earthShakeAttack(target)  // Ground-based area damage
                    attackCount = 0  // Reset counter after special
                } 
                // NORMAL ATTACK: Standard melee attack
                else {
                    skeletalClawAttack(target)  // Standard melee slash attack
                }
                
                // Random skeletal taunts and mocking speech during combat
                // Every 2 attacks, 30% chance (1 in 3) to say a taunt or mock
                // Change 2 to 1 for every attack, or (1, 3) to (1, 2) for 50% chance
                if (attackCount >= 2 && this.world.chance(1, 3) && target is Player) {
                    // Randomly picks one of many mocking messages
                    when (this.world.random(20)) {
                        0 -> {
                            forceChat("*You're Useless*")
                            target.message("Vet'ion: You're Useless!")
                        }
                        1 -> {
                            forceChat("*Pathetic Mortal*")
                            target.message("Vet'ion: Pathetic Mortal!")
                        }
                        2 -> {
                            forceChat("*You Can't Even Hit Me*")
                            target.message("Vet'ion: You Can't Even Hit Me!")
                        }
                        3 -> {
                            forceChat("*Weakling*")
                            target.message("Vet'ion: Weakling!")
                        }
                        4 -> {
                            forceChat("*Is That All You've Got?*")
                            target.message("Vet'ion: Is That All You've Got?")
                        }
                        5 -> {
                            forceChat("*You're So Weak*")
                            target.message("Vet'ion: You're So Weak!")
                        }
                        6 -> {
                            forceChat("*Laughs Mockingly*")
                            target.message("Vet'ion: *Laughs Mockingly*")
                        }
                        7 -> {
                            forceChat("*You're Nothing*")
                            target.message("Vet'ion: You're Nothing!")
                        }
                        8 -> {
                            forceChat("*Too Easy*")
                            target.message("Vet'ion: Too Easy!")
                        }
                        9 -> {
                            forceChat("*You're A Joke*")
                            target.message("Vet'ion: You're A Joke!")
                        }
                        10 -> {
                            forceChat("*Can't Even Touch Me*")
                            target.message("Vet'ion: Can't Even Touch Me!")
                        }
                        11 -> {
                            forceChat("*You're Wasting My Time*")
                            target.message("Vet'ion: You're Wasting My Time!")
                        }
                        12 -> {
                            forceChat("*Death comes for all mortals!*")
                            target.message("Vet'ion: Death comes for all mortals!")
                        }
                        13 -> {
                            forceChat("*Your bones will join my collection!*")
                            target.message("Vet'ion: Your bones will join my collection!")
                        }
                        14 -> {
                            forceChat("*The skeletal army awakens!*")
                            target.message("Vet'ion: The skeletal army awakens!")
                        }
                        15 -> {
                            forceChat("*Feel the power of undeath!*")
                            target.message("Vet'ion: Feel the power of undeath!")
                        }
                        16 -> {
                            forceChat("*Your flesh will rot from your bones!*")
                            target.message("Vet'ion: Your flesh will rot from your bones!")
                        }
                        17 -> {
                            forceChat("*The bone yard claims another victim!*")
                            target.message("Vet'ion: The bone yard claims another victim!")
                        }
                        18 -> {
                            forceChat("*You're Not Even A Challenge*")
                            target.message("Vet'ion: You're Not Even A Challenge!")
                        }
                        19 -> {
                            forceChat("*This Is Too Easy*")
                            target.message("Vet'ion: This Is Too Easy!")
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
     * Skeletal claw melee attack
     * 
     * HOW TO EDIT:
     * 
     * 1. Change attack type:
     *    - CombatClass.MELEE → Use melee formula (current)
     *    - CombatClass.RANGED → Use ranged formula
     *    - CombatClass.MAGIC → Use magic formula
     * 
     * 2. Change combat style:
     *    - CombatStyle.SLASH → Slash attack (current)
     *    - CombatStyle.STAB → Stab attack
     *    - CombatStyle.CRUSH → Crush attack
     * 
     * 3. Change attack style:
     *    - AttackStyle.AGGRESSIVE → More damage (current)
     *    - AttackStyle.ACCURATE → More accurate
     *    - AttackStyle.DEFENSIVE → More defense
     * 
     * 4. Change max hit:
     *    - maxHit = 32 → Max damage is 32 (current)
     *    - maxHit = 40 → Max damage is 40
     *    - maxHit = 25 → Max damage is 25
     * 
     * 5. Change animation:
     *    - animate(5485) → Change 5485 to a different animation ID
     * 
     * 6. Change graphics:
     *    - id = 80 → Slash graphic on hit (current)
     *    - id = 85 → Miss graphic (current)
     */
    private fun Npc.skeletalClawAttack(target: Pawn) {
        // Prepare the attack - tells the game what type of attack this is
        prepareAttack(CombatClass.MELEE, CombatStyle.SLASH, AttackStyle.AGGRESSIVE)
        
        // Play attack sound
        world.spawn(AreaSound(tile, id = 2564, radius = 10, volume = 5))
        
        // Play the attack animation from combat definition (works for both Phase 1 and Phase 2)
        animate(combatDef.attackAnimation)
        
        // Deal damage to the target
        dealHit(
            target = target,                    // Who to hit
            maxHit = 32,                        // Maximum damage this attack can deal
            // Accuracy check: uses magic formula accuracy (even for melee attack)
            // Change MagicCombatFormula to MeleeCombatFormula for proper melee accuracy
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1                           // Delay before hit lands (1 = immediate)
        ) { hit ->
            // This code runs when the hit lands or misses
            if (hit.landed()) {
                // Show slash graphic on successful hit
                target.graphic(id = 80, height = 0, delay = 1)
                if (target is Player) {
                    target.message("Vet'ion's skeletal claws tear into you!")
                    // Random mocking when hitting the player
                    if (this.world.chance(1, 3)) {
                        when (this.world.random(5)) {
                            0 -> forceChat("*Too Easy*")
                            1 -> forceChat("*You're So Weak*")
                            2 -> forceChat("*Can't Even Dodge That*")
                            3 -> forceChat("*Pathetic*")
                            4 -> forceChat("*Laughs*")
                        }
                    }
                }
            } else {
                // Show miss graphic when attack misses
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
                // Mock the player when they dodge
                if (target is Player && this.world.chance(1, 4)) {
                    when (this.world.random(4)) {
                        0 -> forceChat("*You Got Lucky*")
                        1 -> forceChat("*Next Time You Won't*")
                        2 -> forceChat("*Can't Dodge Forever*")
                        3 -> forceChat("*Close One*")
                    }
                }
            }
        }
    }

    private fun Npc.boneThrowAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        
        // Play bone throw sound
        world.spawn(AreaSound(tile, id = 2708, radius = 10, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        val projectile = createProjectile(
            target, 
            gfx = 9, // Bone projectile 
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        dealHit(
            target = target,
            maxHit = 28,
            landHit = RangedCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 10, height = 0, delay = 1) // Bone hit graphic
                if (target is Player) {
                    target.message("A skeletal bone strikes you!")
                    // Random mocking when hitting the player
                    if (this.world.chance(1, 3)) {
                        when (this.world.random(4)) {
                            0 -> forceChat("*Direct Hit*")
                            1 -> forceChat("*You're Useless*")
                            2 -> forceChat("*Too Slow*")
                            3 -> forceChat("*Can't Dodge Bones*")
                        }
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
                // Mock the player when they dodge
                if (target is Player && this.world.chance(1, 4)) {
                    when (this.world.random(3)) {
                        0 -> forceChat("*Lucky Dodge*")
                        1 -> forceChat("*Next One Will Hit*")
                        2 -> forceChat("*Running Scared?*")
                    }
                }
            }
        }
    }

    private fun Npc.necromanticStrikeAttack(target: Pawn) {
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        
        // Play dark magic sound
        world.spawn(AreaSound(tile, id = 177, radius = 10, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        val projectile = createProjectile(
            target, 
            gfx = 100, // Dark magic projectile
            startHeight = 43, 
            endHeight = 31, 
            delay = 51, 
            angle = 15, 
            steepness = 127
        )
        
        world.spawn(projectile)
        
        val delay = RangedCombatStrategy.getHitDelay(getFrontFacingTile(target), target.getCentreTile()) - 1
        
        dealHit(
            target = target,
            maxHit = 30,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = delay
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 101, height = 0, delay = 1) // Dark magic hit graphic
                if (target is Player) {
                    target.message("Necromantic energy courses through you!")
                    // Random mocking when hitting the player
                    if (this.world.chance(1, 3)) {
                        when (this.world.random(4)) {
                            0 -> forceChat("*Feel My Power*")
                            1 -> forceChat("*You're Useless*")
                            2 -> forceChat("*Darkness Consumes You*")
                            3 -> forceChat("*Can't Resist Magic*")
                        }
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
                // Mock the player when they dodge
                if (target is Player && this.world.chance(1, 4)) {
                    when (this.world.random(3)) {
                        0 -> forceChat("*Magic Resisted?*")
                        1 -> forceChat("*Lucky Block*")
                        2 -> forceChat("*Next Spell Will Hit*")
                    }
                }
            }
        }
    }

    private fun Npc.skeletalBiteAttack(target: Pawn) {
        prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
        
        // Play bite sound
        world.spawn(AreaSound(tile, id = 2564, radius = 10, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        dealHit(
            target = target,
            maxHit = 29,
            landHit = MagicCombatFormula.getAccuracy(this, target) >= world.randomDouble(),
            delay = 1
        ) { hit ->
            if (hit.landed()) {
                target.graphic(id = 80, height = 0, delay = 1) // Bite graphic
                if (target is Player) {
                    target.message("Vet'ion's skeletal jaws snap at you!")
                    // Random mocking when hitting the player
                    if (this.world.chance(1, 3)) {
                        when (this.world.random(4)) {
                            0 -> forceChat("*Crunch!*")
                            1 -> forceChat("*You're Useless*")
                            2 -> forceChat("*Bones Breaking*")
                            3 -> forceChat("*Can't Escape My Jaws*")
                        }
                    }
                }
            } else {
                target.graphic(id = 85, height = 124, delay = hit.getClientHitDelay())
                // Mock the player when they dodge
                if (target is Player && this.world.chance(1, 4)) {
                    when (this.world.random(3)) {
                        0 -> forceChat("*Missed The Bite*")
                        1 -> forceChat("*Too Quick For You*")
                        2 -> forceChat("*Next Bite Will Get You*")
                    }
                }
            }
        }
    }
    
    private suspend fun Npc.summonHellhoundsAttack(target: Pawn) {
        // Summon skeletal hellhounds attack (simplified for this implementation)
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        
        // Play summoning sound
        world.spawn(AreaSound(tile, id = 224, radius = 12, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        if (target is Player) {
            target.message("Vet'ion: Rise, my skeletal hounds!")
            // Mocking when summoning
            if (this.world.chance(1, 2)) {
                when (this.world.random(3)) {
                    0 -> forceChat("*You're Surrounded Now*")
                    1 -> forceChat("*No Escape*")
                    2 -> forceChat("*My Hounds Will Get You*")
                }
            }
        }
        
        // Create summoning effects around Vet'ion
        val vetionTile = this.tile
        for (x in -2..2) {
            for (z in -2..2) {
                if (kotlin.math.abs(x) + kotlin.math.abs(z) == 2) { // Diamond pattern
                    val tile = vetionTile.transform(x, z)
                    world.spawn(TileGraphic(id = 86, tile = tile, height = 0, delay = 20))
                }
            }
        }
        
        // After delay, deal area damage representing hellhound attacks
        world.queue {
            wait(3)
            
            // Damage players near Vet'ion representing hellhound attacks
            // Collect players first to avoid concurrent modification
            val playersToDamage = mutableListOf<Player>()
            world.players.forEach { player ->
                playersToDamage.add(player)
            }
            playersToDamage.forEach { player ->
                if (player.tile.getDistance(vetionTile) <= 4 && player.getCurrentHp() > 0) {
                    player.hit(this@summonHellhoundsAttack.world.random(18) + 12, type = HitType.HIT, delay = 0)
                    player.message("Skeletal hellhounds emerge and attack you!")
                }
            }
        }
    }
    
    private suspend fun Npc.boneBarrageAttack(target: Pawn) {
        // Bone barrage attack - multiple bone projectiles
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        
        // Play barrage sound (repeated with projectiles)
        world.spawn(AreaSound(tile, id = 2708, radius = 12, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        if (target is Player) {
            target.message("Vet'ion unleashes a barrage of ancient bones!")
            // Mocking when using bone barrage
            if (this.world.chance(1, 2)) {
                when (this.world.random(3)) {
                    0 -> forceChat("*Can't Dodge All Of These*")
                    1 -> forceChat("*Bone Storm!*")
                    2 -> forceChat("*You're Useless*")
                }
            }
        }
        
        // Launch 4 bone projectiles in sequence
        repeat(4) { i ->
            world.queue {
                wait(i + 1) // Staggered timing
                
                val projectile = createProjectile(
                    target, 
                    gfx = 9, // Bone projectile
                    startHeight = 43, 
                    endHeight = 31, 
                    delay = 30, 
                    angle = 15, 
                    steepness = 127
                )
                
                world.spawn(projectile)
                
                // Each bone has chance to hit
                wait(2)
                if (target.getCurrentHp() > 0 && this@boneBarrageAttack.world.chance(3, 4)) {
                    target.hit(this@boneBarrageAttack.world.random(15) + 10, type = HitType.HIT, delay = 0)
                    target.graphic(id = 10, height = 0, delay = 0)
                    
                    if (i == 0 && target is Player) {
                        target.message("Ancient bones pummel you!")
                    }
                }
            }
        }
    }

    private suspend fun Npc.earthShakeAttack(target: Pawn) {
        // Earth shake attack - ground-based area damage
        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        
        // Play earthquake sound
        world.spawn(AreaSound(tile, id = 159, radius = 15, volume = 5))
        
        animate(combatDef.attackAnimation) // Use combat definition animation
        
        val shakeCenter = this.tile
        
        // Increased AOE size - now affects a 5-tile radius (was 3)
        val aoeRadius = 5
        
        // Warn all nearby players with speech and messages
        val playersToWarn = mutableListOf<Player>()
        world.players.forEach { player ->
            if (player.tile.getDistance(shakeCenter) <= 12) {
                playersToWarn.add(player)
            }
        }
        
        // Also add the main target if it's a player and not already in the list
        if (target is Player && target !in playersToWarn && target.tile.getDistance(shakeCenter) <= 12) {
            playersToWarn.add(target)
        }
        
        // Vetion speaks to announce the attack
        val warningMessages = listOf(
            "The very earth trembles before me!",
            "Feel the power of the ground beneath you!",
            "The earth itself rejects your presence!",
            "Tremble as the ground shakes!",
            "The ground will swallow you whole!"
        )
        val warningMessage = warningMessages[this.world.random(warningMessages.size) % warningMessages.size]
        forceChat(warningMessage)
        
        // Send warning message to all nearby players
        playersToWarn.forEach { player ->
            player.message("Vet'ion: $warningMessage")
            player.message("The ground begins to shake! Get away from Vet'ion!")
        }
        
        // Create earthquake graphics in expanding circles with warning
        // First show warning graphics (increased radius area)
        world.queue {
            wait(1)
            // Show warning graphics in the entire area that will be affected (5-tile radius)
            for (x in -aoeRadius..aoeRadius) {
                for (z in -aoeRadius..aoeRadius) {
                    if (kotlin.math.abs(x) + kotlin.math.abs(z) <= aoeRadius) {
                        val tile = shakeCenter.transform(x, z)
                        world.spawn(TileGraphic(id = 99, tile = tile, height = 0, delay = 0))
                    }
                }
            }
        }
        
        // Create expanding earthquake graphics (expanding to 5-tile radius)
        for (radius in 1..aoeRadius) {
            world.queue {
                wait(radius + 1) // Start after initial warning
                
                for (x in -radius..radius) {
                    for (z in -radius..radius) {
                        if (kotlin.math.abs(x) + kotlin.math.abs(z) == radius) {
                            val tile = shakeCenter.transform(x, z)
                            world.spawn(TileGraphic(id = 99, tile = tile, height = 0, delay = 0))
                        }
                    }
                }
            }
        }
        
        // Deal damage to all players in area after earthquake buildup
        // Give players time to move away (5 ticks total from start)
        world.queue {
            wait(5) // Increased delay to give players time to react
            
            // Collect players first to avoid concurrent modification
            val playersToDamage = mutableListOf<Player>()
            world.players.forEach { player ->
                playersToDamage.add(player)
            }
            playersToDamage.forEach { player ->
                // Only damage players still within 5 tiles of the center (increased from 3)
                if (player.tile.getDistance(shakeCenter) <= aoeRadius && player.getCurrentHp() > 0) {
                    player.hit(this@earthShakeAttack.world.random(22) + 15, type = HitType.HIT, delay = 0)
                    player.message("The ground shakes violently beneath your feet!")
                } else if (player.tile.getDistance(shakeCenter) <= 12 && player.getCurrentHp() > 0) {
                    // Players who moved away get a message
                    player.message("You managed to escape the earthquake!")
                }
            }
        }
    }
}