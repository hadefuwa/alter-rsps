# 🎮 How to Create Your Own Boss! 🐉

Hello! Today we are going to learn how to make a **BRAND NEW BOSS** for your game!

Follow these 3 simple steps to get started:

### 1️⃣ Create a New File
Go to your plugins folder at:
`game-plugins/src/main/kotlin/org/alter/plugins/content/bosses`

Create a NEW folder for your boss (e.g. `myboss`) and then create a NEW file inside it called:
`MyBossPlugin.kt`

(You can name it whatever you want, like `SuperDragon.kt` or `MegaGiant.kt`)

### 2️⃣ Find Your Boss ID
We need to know the code name for the monster you want to use.

1.  Open the file: `data/cfg/rscm/npc.rscm`
2.  Press **Ctrl+F** and search for the monster you want (e.g. "rock" or "dragon").
3.  Copy the full name, like `npc.rock_925` or `npc.king_black_dragon`.
4.  Use this name in your code!

### 3️⃣ Copy This Template
Copy and paste this **ENTIRE** code block into your new file. This is your starting point!

```kotlin
package org.alter.plugins.content.bosses.example

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.model.combat.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.rscm.RSCM.getRSCM

// 👉 CHANGE "MyBossPlugin" to your boss name!
class MyBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // ==========================================
        // 1. SPAWN THE BOSS 📍
        // ==========================================
        // 👉 CHANGE "npc.crazy_archaeologist" to your NPC ID
        // 👉 CHANGE x and z to where you want it to spawn
        spawnNpc("npc.crazy_archaeologist", x = 3200, z = 3200, walkRadius = 5)
        
        // Bonus: Spawn a decoration item nearby
        spawnItem("item.spade", 1, 3205, 3205)

        // ==========================================
        // 2. DEFINE STATS & DROPS 📊
        // ==========================================
        // 👉 CHANGE "npc.crazy_archaeologist" to your NPC ID
        setCombatDef("npc.crazy_archaeologist") {
            configs {
                attackSpeed = 4 // How fast it attacks (4 = 2.4s)
                respawnDelay = 50 // How long to wait before coming back
            }
            // ⚔️ AGGRESSION: Make it attack players!
            aggro {
                radius = 10         // How far can it see you?
                searchDelay = 2     // How often to look for players (in cycles)
                alwaysAggro()       // Attacks even high-level players
            }
            stats {
                hitpoints = 500
                attack = 200
                strength = 200
                defence = 150
                magic = 300
                ranged = 1
            }
            // 🛡️ BONUSES: Defense against different attacks
            bonuses {
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 100
                defenceRanged = 50
            }
            anims {
                attack = 3353 // Attack Animation ID
                block = 424   // Block Animation ID
                death = 836   // Death Animation ID
            }
            drops {
                always { add("item.big_bones", 1) }
                main(128) {
                    add("item.coins", 5000, 10000, 20)
                    add("item.dragon_scimitar", 1, 5) // Rare!
                }
            }
        }

        // ==========================================
        // 3. COMBAT LOGIC ⚔️
        // ==========================================
        // 👉 CHANGE "npc.crazy_archaeologist" to your NPC ID
        onNpcCombat("npc.crazy_archaeologist") {
            npc.queue {
                combatLoop()
            }
        }
    }

    // This is the brain of the boss! 🧠
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        // Loop continuously while in combat
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Move to attack range if needed and check if attack is ready
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && npc.isAttackDelayReady()) {
                
                // 🌡️ PHASE CHECK: Is the boss angry? (Low HP)
                val isAngry = npc.getCurrentHp() < (npc.getMaxHp() / 2) // Less than 50% HP

                if (isAngry) {
                    npc.forceChat("I AM ANGRY NOW!") // Talk more when angry
                }

                // 🎲 DECISION TIME: What should I do?
                val dice = npc.world.random(100)

                // 1. SPECIAL ATTACKS (The cool stuff)
                // If angry, use specials MORE often (40% chance)! If calm, only 20%.
                val specialChance = if (isAngry) 40 else 20

                if (dice < specialChance) {
                    // Pick a RANDOM SPECIAL
                    val specialType = npc.world.random(1..3)
                    
                    if (specialType == 1) { 
                        // 💥 Special 1: Fire Rain (Area Attack)
                        npc.forceChat("Burn!")
                        BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 45, projectile = 100)

                    } else if (specialType == 2) {
                        // ❄️ Special 2: Freeze Spell
                        npc.forceChat("Freeze!")
                        BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979)
                        target.freeze(5) // Freeze for 5 ticks (3 seconds)

                    } else {
                        // 💀 Special 3: Super Heavy Hit (Unblockable)
                        npc.forceChat("CRUSH!")
                        BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
                    }

                } else {
                    // 2. NORMAL ATTACKS (The basic stuff)
                    // Pick a basic attack
                    val normalType = npc.world.random(1..2)

                    if (normalType == 1) {
                        // 🗡️ Basic Melee
                        BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                    } else {
                        // 🏹 Basic Ranged (Throws rock)
                        BossAttacks.ranged(npc, target, projectile = 11, maxHit = 20, anim = 426)
                    }
                }
                
                // Tell the game we finished an attack
                npc.postAttackLogic(target)
            }
            
            // Wait 1 tick (0.6s) before checking again
            wait(1)
            
            // Update target (in case they died or ran away)
            target = npc.getCombatTarget() as? Player ?: break
        }
        
        // Cleanup when combat ends
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
```

### 4️⃣ Adjust This! (Customize Your Boss)

Now that you have the file created, look for the parts with `👉 CHANGE` or `👉 ADJUST`.

Here is what you can change:

#### A. Where does it live? (Spawning)
Look for `spawnNpc`. Change the **X** and **Z** numbers to move your boss.
*   Type `::coords` in-game to find your spot!

#### B. How strong is it? (Stats)
Look for `stats`.
*   Want a super tank? Increase `hitpoints` and `defence`.
*   Want a glass cannon? Increase `strength` but lower `hitpoints`.

#### C. What does it drop? (Loot)
Look for `drops`.
*   You can change "item.coins" to any item name you want, like "item.partyhat"!
*   The last number is the rarity. Lower number = rarer!

#### D. How does it fight? (Combat)
Look for `combatLoop`.
*   You can change the `dice` numbers to make special attacks happen more often.
*   You can change `BossAttacks.melee` to `BossAttacks.ranged` if you want it to shoot arrows!

### 💥 Damage: Fixed or Stat-Based?
You have two choices for how hard your boss hits:

1.  **Fixed Max Hit (Easiest)**:
    *   Example: `BossAttacks.melee(npc, target, maxHit = 50)`
    *   The boss will hit between 0 and 50. It ignores its strength level!
    *   *Good for:* Predictable bosses.

2.  **Stat-Based Damage (Advanced)**:
    *   Example: `BossAttacks.melee(npc, target)` (Delete `maxHit`)
    *   The game calculates damage using the boss's `strength` stat!
    *   High Strength + Low Attack = Inaccurate but deadly (Glass Cannon).
    *   Low Strength + High Attack = Hits often but weak (Chip Damage).
    *   *Good for:* Complex bosses where stats matter.

---

## 📚 More Combat Examples

If you want to try different attacks, here are some options you can swap into your `combatLoop`:

### 🗡️ Melee Attack
*The boss swings its weapon or punches.*
```kotlin
BossAttacks.melee(npc, target, maxHit = 20, anim = 451)
```

### ✨ Magic Attack
*The boss casts a spell with a projectile.*
```kotlin
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)
```

### 🏹 Ranged Attack
*The boss shoots an arrow or throws a rock.*
```kotlin
BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 426)
```

### 💥 Super Area Attack (AoE)
*Hits EVERYONE nearby! Great for ultimate moves.*
```kotlin
BossAttacks.aoe(
    npc = npc,
    center = target.tile, // Explode on the player!
    radius = 3,           // 3x3 Explosion
    combatClass = CombatClass.MAGIC,
    maxHit = 50,
    projectile = 100      // Fireball graphic
)
```

---

## 🧠 Advanced Logic (If Statements)

You can use `if` statements to make your boss smarter!

### ❤️ Last Stand (Low HP Enrage)
*Checking if HP is under 10%*
```kotlin
if (npc.getCurrentHp() < (npc.getMaxHp() * 0.10)) {
    npc.forceChat("THIS ISN'T OVER!")
    // Use a super strong attack!
    BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)
}
```

### 📏 Distance Check (Too Far?)
*Checking how far away the player is*
```kotlin
if (npc.tile.getDistance(target.tile) > 5) {
    npc.forceChat("Get over here!")
    // Teleport player to boss!
    target.moveTo(npc.tile) 
}
```

### 🛡️ Equipment Check (Holding a Shield?)
*Checking if player has a specific item*
```kotlin
if (target.equipment[Equipment.SHIELD_SLOT].hasAny()) {
    npc.forceChat("Your shield won't save you!")
    // Use magic (goes through armor!)
    BossAttacks.magic(npc, target, projectile = 100, maxHit = 40)
}

---

## 🔮 Bonus: Prayer Smasher!

Make your boss smart! If the player tries to protect themselves, punish them!

### 1. Add these imports:
```kotlin
import org.alter.game.model.combat.Prayer
import org.alter.game.model.combat.Prayers
```

### 2. Add this code inside your `combatLoop`:
*Checking what prayers the player is using.*

```kotlin
            // 🚫 PRAYER CHECK: Is player using Protect from Melee?
            if (Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)) {
                npc.forceChat("Your prayers are useless!")
                
                // Switch to MAGIC attack to hit through their prayer!
                BossAttacks.magic(npc, target, projectile = 100, maxHit = 35, anim = 1979)
            }
```

Have fun creating your monster! 🧟‍♂️🤖👹
