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
        // 1. SPAWN THE BOSS 📍
        // This makes the boss appear in the game
        spawnNpc("npc.crazy_archaeologist", x = 3200, z = 3200, walkRadius = 5)

        // 2. DEFINE STATS & DROPS 📊
        // This tells the game how strong the boss is and what it drops
        setCombatDef("npc.crazy_archaeologist") {
            configs {
                attackSpeed = 4      // How fast it attacks
                respawnDelay = 50     // How long before it comes back after dying
            }
            aggro {
                radius = 10          // How far it can see players
                searchDelay = 2       // How often it looks for players
                alwaysAggro()         // Attacks players even if they're high level
            }
            stats {
                hitpoints = 500      // How much health it has
                attack = 200         // How accurate it is
                strength = 200       // How hard it hits
                defence = 150         // How well it blocks
                magic = 300
                ranged = 1
            }
            bonuses {
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 100
                defenceRanged = 50
            }
            anims {
                attack = 3353        // Animation when it attacks
                block = 424          // Animation when it blocks
                death = 836          // Animation when it dies
            }
            
            // 🔊 SOUNDS: Make your boss sound epic! (Optional but recommended)
            sound {
                attackSound = Sound.ROCK_CRAB_ATTACK  // Sound when boss attacks
                attackArea = true                     // All nearby players hear it
                attackVolume = 50                     // Volume (0-100)
                attackRadius = 10                     // How far sound travels
                
                blockSound = Sound.ROCK_CRAB_HIT     // Sound when boss blocks
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.ROCK_CRAB_DEATH   // Sound when boss dies
                deathArea = true
                deathVolume = 60
                deathRadius = 12
            }
            drops {
                always { add("item.big_bones", 1) }
                main(128) {
                    add("item.coins", 5000, 10000, 20)
                    add("item.dragon_scimitar", 1, 5)  // Rare!
                }
            }
        }

        // 3. COMBAT LOGIC ⚔️
        // When the boss starts fighting, run the combat loop
        onNpcCombat("npc.crazy_archaeologist") { npc.queue { combatLoop() } }
    }

    // This is the brain of the boss! 🧠
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        // Keep fighting while boss is alive
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Move close and attack when ready
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && 
                npc.isAttackDelayReady()) {
                
                // Attack with melee! Change maxHit to make it hit harder or softer
                BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                npc.postAttackLogic(target)
            }
            
            wait(1)
            target = npc.getCombatTarget() as? Player ?: break
        }
        
        // Clean up when combat ends
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

#### E. What animations does it use? (Visuals)
Look for `anims`. See the **Animation Guide** section below for detailed information!

#### F. What sounds does it make? (Audio)
Look for `sound`. See the **Sound Guide** section below for detailed information!

---

## 🎬 Animation Guide: Making Your Boss Look Cool!

Animations control how your boss **looks** when it fights. This section will teach you everything about animations!

### What Are Animations?

Animations are visual effects that play when your boss does certain actions:
- **`attack`**: Plays when the boss attacks (swings weapon, casts spell, etc.)
- **`block`**: Plays when the boss blocks or defends against an attack
- **`death`**: Plays when the boss dies

### Can I Use Any Animation ID? (Like `attack = 9999`?)

**Short answer:** Technically yes, but it might not look right!

- ✅ **Valid IDs**: Animation IDs that exist in the game client will play. The game won't crash.
- ⚠️ **Invalid IDs**: If you use an ID that doesn't exist (like `9999`), the boss might:
  - Not animate at all (stand still)
  - Play a default/fallback animation
  - Look weird or glitchy

**Best Practice:** Use animation IDs that you know exist. See the examples below!

### How to Find Animation IDs

Here are several ways to find the perfect animation for your boss:

#### Method 1: Use the In-Game Command (Easiest!)
1. In-game, type: `::anim [ID]` (e.g., `::anim 3353`)
2. Your character will play that animation
3. Try different IDs until you find one you like!

#### Method 2: Look at Existing Bosses
Check other boss files in the codebase:
- `game-plugins/src/main/kotlin/org/alter/plugins/content/bosses/`
- See what animations they use and copy them!

#### Method 3: Use Online Resources
- **OSRS Entity Viewer**: Search for NPCs and see their animations
- **OSRS Wiki**: Look up NPC pages to find animation information
- **Rune-Server Forums**: Community resources with animation lists

#### Method 4: Check the Animation Constants
The codebase has many animation IDs defined in:
- `game-api/src/main/kotlin/org/alter/api/cfg/Animation.kt`
- Look for constants like `CRAZY_ARCHAEOLOGIST_BOOK = 3353`

### Common Animation IDs by NPC Type

Here are some popular animation IDs you can use:

#### 🐉 Dragon-Type Animations
```kotlin
anims {
    attack = 81   // Dragonfire attack
    block = 89    // Dragon block/hit
    death = 92    // Dragon death
}
```

#### 👹 Demon-Type Animations
```kotlin
anims {
    attack = 64   // Demon attack
    block = 65    // Demon block/hit
    death = 67    // Demon death
}
```

#### 🧙 Magic User Animations
```kotlin
anims {
    attack = 1979 // Magic spell cast (ancient)
    block = 424   // Standard block
    death = 836   // Standard death
}
```

#### ⚔️ Melee Warrior Animations
```kotlin
anims {
    attack = 422  // Punch/melee attack
    block = 424   // Block with shield
    death = 836   // Death animation
}
```

#### 🏹 Ranged Animations
```kotlin
anims {
    attack = 426  // Bow attack
    block = 424   // Block
    death = 836   // Death
}
```

#### 🦎 Lizard/Reptile Animations
```kotlin
anims {
    attack = 2776 // Lizard attack
    block = 2777  // Lizard hit
    death = 2778  // Lizard death
}
```

#### 🕷️ Spider Animations
```kotlin
anims {
    attack = 5327 // Giant spider attack
    block = 5328  // Spider defend
    death = 5329  // Spider death
}
```

#### 👻 Ghost/Undead Animations
```kotlin
anims {
    attack = 5532 // Ghost attack
    block = 5533  // Ghost hit
    death = 5534  // Ghost death
}
```

### More Animation Examples

Here are additional animation IDs you can experiment with:

```kotlin
// Boss-Specific Animations
anims {
    attack = 3353  // Crazy Archaeologist book attack
    block = 424    // Standard block
    death = 836    // Standard death
}

// Alternative Attack Animations
attack = 451      // Different melee swing
attack = 7060     // Heavy/unblockable attack
attack = 1978     // Single-target magic cast
attack = 1979     // Multi-target magic cast
attack = 2652     // Ranged attack (TzTok-Jad style)
attack = 2656     // Magic attack (TzTok-Jad style)

// Alternative Block Animations
block = 1683      // Different block animation
block = 424       // Standard block (most common)

// Alternative Death Animations
death = 836       // Standard death (most common)
death = 1684       // Alternative death
death = 92         // Dragon death
```

### Using Different Animations for Different Attacks

You can override the default `attack` animation in your combat loop! This lets you use different animations for different attack types:

```kotlin
// In your combatLoop():
if (dice < 30) {
    // Special attack with custom animation
    BossAttacks.melee(npc, target, maxHit = 50, anim = 7060) // Heavy attack!
} else {
    // Normal attack with different animation
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422) // Quick punch!
}
```

### Animation Tips & Tricks

1. **Match the NPC Type**: If your boss is a dragon, use dragon animations. If it's a mage, use magic animations.

2. **Test in Game**: Always test your animations in-game using `::anim [ID]` to see how they look!

3. **Mix and Match**: You can use a dragon attack animation with a demon death animation - be creative!

4. **Special Attacks**: Use unique animations for special attacks to make them stand out:
   ```kotlin
   // Normal attack
   BossAttacks.melee(npc, target, anim = 422)
   
   // Special attack with dramatic animation
   BossAttacks.melee(npc, target, maxHit = 99, anim = 7060)
   npc.forceChat("ULTIMATE STRIKE!")
   ```

5. **Death Animations**: You can even have multiple death animations! (See advanced section below)

### Advanced: Multiple Death Animations

You can make your boss randomly choose from multiple death animations:

```kotlin
anims {
    attack = 3353
    block = 424
    death {  // Use a block instead of single value
        add(836)   // Death animation 1
        add(1684)  // Death animation 2
        add(92)    // Death animation 3
    }
}
```

The game will randomly pick one when the boss dies!

### Quick Reference: Animation ID Cheat Sheet

| Animation Type | Common IDs | Description |
|---------------|------------|-------------|
| **Attack - Melee** | `422`, `423`, `451`, `64`, `81` | Punches, swings, claws |
| **Attack - Magic** | `1978`, `1979`, `2656`, `3353` | Spell casting |
| **Attack - Ranged** | `426`, `2652`, `7552` | Bow, crossbow, thrown |
| **Attack - Special** | `7060`, `5069` | Heavy/unblockable attacks |
| **Block** | `424`, `1683` | Defending/blocking |
| **Death** | `836`, `1684`, `92`, `67` | Death animations |

### Testing Your Animations

**Before you finalize your boss, test the animations!**

1. **In-Game Testing:**
   ```
   ::anim 3353  // Test attack animation
   ::anim 424   // Test block animation
   ::anim 836   // Test death animation
   ```

2. **Spawn Your Boss:**
   - Spawn the boss in-game
   - Attack it to see the block animation
   - Watch it attack to see the attack animation
   - Kill it to see the death animation

3. **Quick Iteration:**
   - Change animation IDs in your code
   - Restart the server (or use hot-reload if available)
   - Test again until it looks perfect!

### Common Mistakes to Avoid

❌ **Don't:** Use random high numbers like `9999` without testing
✅ **Do:** Test animations with `::anim` command first

❌ **Don't:** Use player animations for NPCs (they might look weird)
✅ **Do:** Use NPC-specific animations when possible

❌ **Don't:** Forget to set all three animations (attack, block, death)
✅ **Do:** Set all three for a complete boss experience

❌ **Don't:** Use the same animation for everything
✅ **Do:** Mix different animations for variety (especially in combat loop)

---

## 🔊 Sound Guide: Making Your Boss Sound Epic!

Sounds add atmosphere and make your boss feel more alive! This section will teach you everything about boss sounds.

### What Are Sounds?

Sounds are audio effects that play when your boss performs actions:
- **`attackSound`**: Plays when the boss attacks
- **`blockSound`**: Plays when the boss blocks or gets hit
- **`deathSound`**: Plays when the boss dies

### Basic Sound Configuration

Here's the basic structure for adding sounds to your boss:

```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK  // Sound ID when attacking
    attackArea = true                      // Can nearby players hear it?
    attackVolume = 50                     // How loud (0-100)
    attackRadius = 10                     // How far sound travels (in tiles)
    
    blockSound = Sound.ROCK_CRAB_HIT      // Sound when blocking/hit
    blockArea = true
    blockVolume = 40
    blockRadius = 8
    
    deathSound = Sound.ROCK_CRAB_DEATH    // Sound when dying
    deathArea = true
    deathVolume = 60
    deathRadius = 12
}
```

### Sound Parameters Explained

#### `attackSound`, `blockSound`, `deathSound`
- **What it does:** The sound ID to play
- **How to find:** Use constants from `Sound` object (see examples below)
- **Example:** `Sound.ROCK_CRAB_ATTACK`, `Sound.GIANT_ATTACK`

#### `attackArea`, `blockArea`, `deathArea`
- **What it does:** If `true`, all players nearby can hear the sound
- **If `false`:** Only the player being attacked hears it
- **Best for bosses:** Usually set to `true` for epic atmosphere!

#### `attackVolume`, `blockVolume`, `deathVolume`
- **What it does:** How loud the sound is (0 = silent, 100 = max volume)
- **Recommended:** 
  - Attack: 40-60 (loud enough to be heard)
  - Block: 30-50 (softer, less intrusive)
  - Death: 50-70 (dramatic!)

#### `attackRadius`, `blockRadius`, `deathRadius`
- **What it does:** How many tiles away players can hear the sound
- **Only works if:** `attackArea = true` (or `blockArea`/`deathArea`)
- **Recommended:**
  - Attack: 8-12 tiles
  - Block: 6-10 tiles
  - Death: 10-15 tiles (everyone should hear the boss die!)

### Common Sound IDs by NPC Type

Here are popular sound constants you can use:

#### 🪨 Rock/Stone Bosses
```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK    // 718
    blockSound = Sound.ROCK_CRAB_HIT        // 720
    deathSound = Sound.ROCK_CRAB_DEATH     // 719
}
```

**Alternative Rock Sounds:**
```kotlin
attackSound = Sound.ROCKSLUG_ATTACK       // 729
blockSound = Sound.ROCKSLUG_HIT           // 731
deathSound = Sound.ROCKSLUG_DEATH         // 730
```

#### 👹 Giant Bosses
```kotlin
sound {
    attackSound = Sound.GIANT_ATTACK       // 448
    blockSound = Sound.GIANT_HIT            // 451
    deathSound = Sound.GIANT_DEATH         // 450
}
```

**Giant Variants:**
```kotlin
attackSound = Sound.FIRE_GIANT_ATTACK     // 447
attackSound = Sound.EARTH_GIANT_ATTACK    // 446
attackSound = Sound.MOSS_GIANT_ATTACK     // 449
```

#### 🤖 Golem Bosses
```kotlin
sound {
    attackSound = Sound.GOLEM_ATTACK        // 474
    blockSound = Sound.GOLEM_HIT             // 476
    deathSound = Sound.GOLEM_DEATH          // 475
}
```

#### 🐉 Dragon Bosses
```kotlin
sound {
    attackSound = Sound.BABYDRAGON_ATTACK   // Dragon attack sound
    blockSound = Sound.BABYDRAGON_HIT       // Dragon hit sound
    deathSound = Sound.BABYDRAGON_DEATH     // Dragon death sound
}
```

#### 👻 Undead/Skeleton Bosses
```kotlin
sound {
    attackSound = Sound.SKELETAL_HELLHOUND_ATTACK  // Powerful skeletal sound
    blockSound = Sound.SKELETAL_HELLHOUND_HIT
    deathSound = Sound.SKELETAL_HELLHOUND_DEATH
}
```

#### 🕷️ Spider Bosses
```kotlin
sound {
    attackSound = Sound.GIANT_SPIDER_ATTACK
    blockSound = Sound.GIANT_SPIDER_HIT
    deathSound = Sound.GIANT_SPIDER_DEATH
}
```

#### 🦎 Lizard/Reptile Bosses
```kotlin
sound {
    attackSound = Sound.LIZARD_ATTACK
    blockSound = Sound.LIZARD_HIT
    deathSound = Sound.LIZARD_DEATH
}
```

### How to Find Sound IDs

#### Method 1: Check the Sound Constants
Look in: `game-api/src/main/kotlin/org/alter/api/cfg/Sound.kt`

Search for sounds related to your boss type:
- Rock boss? Search for "ROCK" or "STONE"
- Dragon boss? Search for "DRAGON"
- Giant boss? Search for "GIANT"

#### Method 2: Look at Existing Bosses
Check other boss files to see what sounds they use:
- `game-plugins/src/main/kotlin/org/alter/plugins/content/bosses/`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/`

#### Method 3: Use Generic Combat Sounds
If you can't find a specific sound, use generic combat sounds:
```kotlin
// Generic melee sounds
attackSound = Sound.SWORDCLASH4        // 21
blockSound = Sound.STEEL_MAIL         // 14

// Impact sounds
blockSound = Sound.ROCK_IMPACT         // 851
blockSound = Sound.STONE_IMPACT        // 860
```

### Sound Tips & Tricks

1. **Match the Theme:** If your boss is a rock, use rock sounds. If it's a dragon, use dragon sounds!

2. **Volume Balance:**
   - Death sounds should be louder (50-70) - it's dramatic!
   - Attack sounds medium (40-60) - noticeable but not annoying
   - Block sounds softer (30-50) - happens frequently

3. **Area Sounds for Bosses:**
   - Set `attackArea = true` for epic boss fights
   - Makes the boss feel more powerful and immersive
   - Players nearby will hear the boss even if they're not fighting it

4. **Radius Matters:**
   - Small radius (5-8): Intimate, close-range sounds
   - Medium radius (8-12): Standard boss sounds
   - Large radius (12-20): Epic, world-shaking boss sounds

5. **Mix and Match:**
   - You can use different sound types for variety
   - Example: Rock attack sound + Giant death sound = Unique boss!

### Optional: No Sounds

If you don't want sounds, you can simply **omit the `sound { }` block entirely**. The boss will work fine without sounds, just silently.

### Quick Reference: Sound Cheat Sheet

| Sound Type | Common Constants | Description |
|------------|------------------|-------------|
| **Rock** | `ROCK_CRAB_ATTACK`, `ROCKSLUG_ATTACK` | Rock/stone themed |
| **Giant** | `GIANT_ATTACK`, `FIRE_GIANT_ATTACK` | Large creature sounds |
| **Golem** | `GOLEM_ATTACK`, `GOLEM_HIT` | Mechanical/stone golem |
| **Dragon** | `BABYDRAGON_ATTACK` | Dragon-themed |
| **Undead** | `SKELETAL_HELLHOUND_ATTACK` | Skeleton/undead |
| **Spider** | `GIANT_SPIDER_ATTACK` | Spider-themed |
| **Generic** | `SWORDCLASH4`, `STEEL_MAIL` | Generic combat sounds |

### Testing Your Sounds

1. **Spawn your boss in-game**
2. **Attack it** - Listen for the attack sound
3. **Let it hit you** - Listen for the block sound
4. **Kill it** - Listen for the death sound
5. **Adjust volume/radius** if needed!

### Common Sound Mistakes to Avoid

❌ **Don't:** Set volume to 100 (too loud, annoying!)
✅ **Do:** Use 40-70 range for most sounds

❌ **Don't:** Forget to set `attackArea = true` if you want area sounds
✅ **Do:** Set area to `true` for epic boss atmosphere

❌ **Don't:** Use player sounds for NPCs (might sound weird)
✅ **Do:** Use NPC-specific sounds when possible

❌ **Don't:** Make all sounds the same volume
✅ **Do:** Death should be loudest, block should be softest

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

**Basic Melee:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 20, anim = 422)  // Quick punch
```

**Heavy Melee:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 50, anim = 7060)  // Powerful strike
```

**Alternative Melee Animations:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 30, anim = 451)   // Sword swing
BossAttacks.melee(npc, target, maxHit = 30, anim = 423)   // Aggressive punch
BossAttacks.melee(npc, target, maxHit = 30, anim = 64)    // Demon claw
BossAttacks.melee(npc, target, maxHit = 30, anim = 81)    // Dragon claw
```

### ✨ Magic Attack
*The boss casts a spell with a projectile.*

**Basic Magic:**
```kotlin
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)
```

**Alternative Magic Animations:**
```kotlin
// Single-target spell
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1978)

// Multi-target spell (ancient)
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)

// Different magic style
BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 2656)
```

**Magic with Freeze:**
```kotlin
BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979)
target.freeze(5)  // Freeze player for 5 ticks
```

### 🏹 Ranged Attack
*The boss shoots an arrow or throws a rock.*

**Basic Ranged:**
```kotlin
BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 426)
```

**Alternative Ranged Animations:**
```kotlin
// Bow attack
BossAttacks.ranged(npc, target, projectile = 11, maxHit = 25, anim = 426)

// Crossbow attack
BossAttacks.ranged(npc, target, projectile = 27, maxHit = 25, anim = 7552)

// Thrown weapon
BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 2652)
```

### 💥 Super Area Attack (AoE)
*Hits EVERYONE nearby! Great for ultimate moves.*

**Basic AoE:**
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

**AoE Variations:**
```kotlin
// Small explosion (1x1)
BossAttacks.aoe(npc, target.tile, radius = 1, maxHit = 30, projectile = 100)

// Medium explosion (3x3)
BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)

// Huge explosion (5x5)
BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)

// Explode on boss location instead of player
BossAttacks.aoe(npc, npc.tile, radius = 3, maxHit = 50, projectile = 100)
```

### 💀 Unblockable Attack
*Always hits, ignores accuracy checks!*

```kotlin
BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
```

**When to use:** Ultimate attacks, environmental damage, mechanics that must hit.

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

---

## 🎁 Adding Special Functionality to Your Boss

Want to make your boss unique? You can add special rewards, messages, or mechanics that trigger when the boss dies or during combat!

### 🪨 Example: Mining XP Reward

The Mining Boss gives Mining experience when killed, making it perfect for players who want to train Mining while fighting!

**Add this code to your boss plugin:**

```kotlin
// ==========================================
// 4. SPECIAL FUNCTIONALITY 🎁
// ==========================================
// 🪨 MINING XP REWARD: Players get Mining XP when they kill the boss!
onNpcDeath("npc.rock_925") {  // 👉 CHANGE to your NPC ID
    val npc = this.npc
    // Find the player who dealt the most damage (the killer)
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Award mining XP based on boss HP (scaled reward)
    val miningXp = npc.combatDef.hitpoints * 10.0 // 10 XP per HP (100 HP = 1000 XP)
    killer.addXp(Skills.MINING, miningXp)
    killer.message("<col=00ff00>You gain ${miningXp.toInt()} Mining experience for defeating the Rock!</col>")
}
```

**What this does:**
- When the boss dies, it finds the player who killed it
- Awards Mining XP based on the boss's HP (bigger boss = more XP!)
- Shows a colored message to the player

**You'll need this import:**
```kotlin
import org.alter.api.Skills
```

### 🎯 More Special Functionality Ideas

Here are other unique features you can add to your boss:

#### 💰 Bonus Coins on Kill
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Give bonus coins
    val bonusCoins = 50000
    killer.inventory.add("item.coins_995", bonusCoins)
    killer.message("You receive $bonusCoins coins as a bonus reward!")
}
```

#### 🏆 Kill Count Tracking
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Track how many times this player has killed the boss
    val currentKills = killer.attr.getOrDefault("your_boss_kills", 0)
    killer.attr["your_boss_kills"] = currentKills + 1
    killer.message("You have killed this boss ${currentKills + 1} times!")
}
```

#### ⚡ Special Item Drop (Guaranteed)
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Always drop a special item
    val specialItem = GroundItem(
        item = getRSCM("item.your_special_item"),
        amount = 1,
        tile = npc.tile,
        owner = killer
    )
    npc.world.spawn(specialItem)
    killer.message("The boss dropped a special item!")
}
```

#### 🎓 Skill XP in Multiple Skills
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Award XP in multiple skills
    killer.addXp(Skills.ATTACK, 500.0)
    killer.addXp(Skills.STRENGTH, 500.0)
    killer.addXp(Skills.DEFENCE, 500.0)
    killer.addXp(Skills.MINING, 250.0)  // Bonus mining XP!
    killer.message("You gain experience in multiple skills!")
}
```

#### 📢 Broadcast Message (Everyone Sees It!)
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Broadcast to all players in the world
    npc.world.players.forEach { player ->
        player.message("<col=ff0000>${killer.username} has defeated the ${npc.def.name}!</col>")
    }
}
```

#### 🎲 Random Chance for Special Reward
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // 10% chance for a super rare drop
    if (npc.world.random(10) == 0) {  // 1 in 10 chance
        val rareItem = GroundItem(
            item = getRSCM("item.ultra_rare_item"),
            amount = 1,
            tile = npc.tile,
            owner = killer
        )
        npc.world.spawn(rareItem)
        
        // Broadcast to everyone!
        npc.world.players.forEach { player ->
            player.message("<col=ff00ff>${killer.username} received an ULTRA RARE drop from ${npc.def.name}!</col>")
        }
    }
}
```

### 💡 Tips for Adding Special Features

1. **Place it in the `init` block**: Add your `onNpcDeath` handler alongside your other boss setup code.

2. **Use the killer check**: Always check if `killer` is a `Player` before giving rewards, or your code might crash!

3. **Scale rewards**: Make rewards scale with boss difficulty (HP, combat level, etc.) so harder bosses give better rewards.

4. **Test thoroughly**: Make sure your special functionality works correctly and doesn't break the game!

5. **Be creative**: Think about what makes your boss unique. A fire boss could give Firemaking XP, a fishing boss could give Fishing XP, etc.!

### 📝 Quick Reference: Available Skills

You can award XP in any of these skills:
- `Skills.ATTACK`, `Skills.STRENGTH`, `Skills.DEFENCE`, `Skills.HITPOINTS`
- `Skills.MAGIC`, `Skills.RANGED`, `Skills.PRAYER`
- `Skills.MINING`, `Skills.WOODCUTTING`, `Skills.FISHING`
- `Skills.COOKING`, `Skills.SMITHING`, `Skills.HERBLORE`
- `Skills.CRAFTING`, `Skills.FLETCHING`, `Skills.CONSTRUCTION`
- `Skills.AGILITY`, `Skills.THIEVING`, `Skills.SLAYER`
- `Skills.FARMING`, `Skills.RUNECRAFT`, `Skills.HUNTER`

**Example:**
```kotlin
killer.addXp(Skills.MINING, 1000.0)  // 1000 Mining XP
killer.addXp(Skills.SMITHING, 500.0)  // 500 Smithing XP
```

Have fun creating your monster! 🧟‍♂️🤖👹
