# NPC Model & Client-Server Interaction Guide

## Understanding the NPC System in OSRS Private Servers

### Overview

When working with NPCs in an OSRS private server, you need to understand that **NPC models and their properties are split between the client and server**. This is why you can't just create a brand new NPC with a custom name like "sewer_abomination" - the client wouldn't know what that is.

---

## How NPCs Work: Client vs Server

### Client-Side (RuneLite/OSRS Client)

The client contains:
- **NPC Models** - The 3D model/appearance of the NPC
- **NPC Animations** - Walk, attack, death animations
- **NPC Options** - Right-click menu options (Attack, Talk-to, Trade, etc.)
- **NPC Names** - Display names shown when you hover over them
- **NPC IDs** - Unique identifiers for each NPC variant

**Key Point:** The client has a **cache** file that contains all this data. This cache is essentially a database of all NPCs that exist in the game.

### Server-Side (Alter RSPS)

The server contains:
- **NPC Stats** - HP, attack, strength, defence, etc.
- **NPC Combat Logic** - How the NPC fights
- **NPC Drop Tables** - What items the NPC drops
- **NPC Spawn Locations** - Where NPCs appear in the world
- **NPC Behaviors** - Aggression, wandering, etc.

---

## The Problem You Encountered

When you tried to create `"npc.sewer_abomination"`:

```kotlin
spawnNpc("npc.sewer_abomination", x = 3237, z = 9866, height = 0, walkRadius = 6)
```

**Error:** `RSCM returned -1 for npc.sewer_abomination`

### Why This Failed

1. The server looked up "sewer_abomination" in its RSCM (RuneScape Cache Manager) file
2. The RSCM maps NPC names to NPC IDs from the client cache
3. "sewer_abomination" doesn't exist in the client cache
4. No ID could be found, so it returned -1 (invalid)
5. The server cannot spawn an NPC without a valid client-side ID

---

## How to Use Existing NPCs

### Step 1: Find Available NPCs

Your server has a file that lists all NPCs from the client cache:

**Location:** `Alter/data/cfg/rscm/npc.rscm`

**Format:**
```
npc_name:npc_id
```

**Examples:**
```
king_black_dragon:239
cerberus:5862
kalphite_queen_963:963
abyssal_sire_5889:5889
```

### Step 2: Check NPC Options

Not all NPCs have the same right-click options. The options are **hardcoded in the client cache**.

**Common Options:**
- **Attack** - Combat NPCs (monsters, bosses)
- **Talk-to** - Dialogue NPCs
- **Trade** - Shop NPCs
- **Pick-pocket** - Thieving NPCs
- **Examine** - All NPCs have this

**The Issue with Abyssal Sire:**

The Abyssal Sire (NPC 5889) is a special boss NPC that may not have standard "Attack" options because in the real game, it has unique mechanics and might only be attackable during certain phases or conditions.

### Step 3: Use a Compatible NPC

When creating a boss, choose an NPC that:
1. **Exists in the client cache** (in npc.rscm)
2. **Has "Attack" as an option**
3. **Looks appropriate** for your purpose

**Good Boss NPCs:**
```
cerberus:5862           - Three-headed hellhound boss
kalphite_queen:963      - Giant beetle boss
king_black_dragon:239   - Dragon boss
corporeal_beast:319     - Giant beast boss
giant_mole:5779         - Large mole boss
```

---

## The RSCM File Explained

### What is RSCM?

**RSCM** = RuneScape Cache Manager

It's a mapping file that connects:
- Server NPC names (strings) → Client NPC IDs (numbers)

### File Structure

**Location:** `Alter/data/cfg/rscm/npc.rscm`

**Example entries:**
```
cow:2790
cow_2791:2791
zombie:26
cerberus:5862
king_black_dragon:239
```

### How to Use It

**Search for an NPC:**
```bash
grep -i "cerberus" Alter/data/cfg/rscm/npc.rscm
```

**Output:**
```
cerberus:5862
cerberus_5863:5863
cerberus_5866:5866
```

**Use in code:**
```kotlin
spawnNpc("npc.cerberus", x = 3237, z = 9866, height = 0, walkRadius = 6)
```

Note: Add `"npc."` prefix when using in code!

---

## Adding Custom NPC Models (Advanced)

If you want to add completely new NPC models or modify existing ones, you need to:

### 1. Modify the Client Cache

**Tools Required:**
- **RuneLite Cache Tool** or **OpenRS2 Cache Library**
- **NPC Model Editor** (like OSRS Model Editor)

**Process:**
1. Extract the OSRS cache from your client
2. Add/modify NPC definitions
3. Import 3D models (if custom)
4. Configure NPC options, animations, name
5. Repack the cache
6. Distribute new cache to all players

### 2. Update Server RSCM

After adding to client cache, update `npc.rscm`:

```
my_custom_boss:99999
```

### 3. Update Server Code

Now you can use it:
```kotlin
spawnNpc("npc.my_custom_boss", x = 3237, z = 9866, height = 0, walkRadius = 6)
```

---

## Workaround: Reskinning Existing NPCs

Instead of creating new NPCs, **reskin existing ones**:

### Example: Sewer Boss

**Want:** A unique sewer abomination boss

**Solution:** Use an existing boss NPC that fits thematically

**Options:**
1. **Cerberus (5862)** - Hellhound, fits sewer theme
2. **Abyssal Demon** - Dark, corrupted creature
3. **Giant Mole (5779)** - Underground creature
4. **Corporeal Beast (319)** - Large, intimidating

**Implementation:**
```kotlin
// Spawn Cerberus as your "Sewer Abomination"
spawnNpc("npc.cerberus", x = 3237, z = 9866, height = 0, walkRadius = 6)

// Players see Cerberus visually
// But it has YOUR custom stats, drops, and mechanics
setCombatDef("npc.cerberus") {
    stats {
        hitpoints = 500  // Your custom stats
        attack = 200
        // etc...
    }
    drops {
        // Your custom drops
    }
}
```

---

## Client Cache Files Explained

### What's in the Cache?

**NPC Definitions:**
- NPC ID
- NPC Name (display name)
- Model IDs (what it looks like)
- Animation IDs (how it moves)
- Size (1x1, 2x2, 3x3 tiles, etc.)
- Combat Level
- Right-click Options
- Colors/Textures
- Examine Text

### Where is the Cache?

**Client Cache Location (RuneLite):**
```
C:\Users\[YourName]\.runelite\cache\
```

**Server Cache Reference:**
```
Alter/data/cfg/rscm/
├── npc.rscm      (NPC mappings)
├── item.rscm     (Item mappings)
└── object.rscm   (Object mappings)
```

---

## NPC Options Deep Dive

### How Options Work

Each NPC has up to 5 right-click options defined in the client cache.

**Example - Cerberus:**
- Option 1: "Attack"
- Option 2: "Examine"
- Options 3-5: Empty

**Example - Shop Keeper:**
- Option 1: "Talk-to"
- Option 2: "Trade"
- Option 3: "Examine"

### Why Some NPCs Can't Be Attacked

If an NPC doesn't have "Attack" in its options array in the cache, you **cannot** add it server-side with `onNpcOption`.

**Wrong Approach:**
```kotlin
// This won't work if the NPC doesn't have "Attack" in client cache
onNpcOption("npc.shop_keeper", option = "attack") {
    player.attack(npc)
}
```

The option won't appear in the right-click menu because the client doesn't know the NPC should have an Attack option.

---

## Best Practices

### For Custom Bosses

1. **Choose a boss NPC from RSCM** that has Attack options
2. **Configure server-side stats** to match your vision
3. **Add custom combat mechanics** in your plugin
4. **Create unique drop tables**
5. **Players see the base model, but everything else is custom**

### Checking if an NPC Works

Before coding, test if the NPC can be attacked:

1. Spawn it in-game using a command (if available)
2. Right-click to see available options
3. If "Attack" appears → Good to use!
4. If no "Attack" → Find a different NPC

### Quick Reference: Finding Boss NPCs

**Search RSCM for boss-type NPCs:**
```bash
grep -iE "boss|demon|dragon|giant|queen|king" Alter/data/cfg/rscm/npc.rscm
```

**Test multiple variants:**
```bash
grep "cerberus" Alter/data/cfg/rscm/npc.rscm
```

Output:
```
cerberus:5862
cerberus_5863:5863
cerberus_5866:5866
```

Try each variant - they might have different sizes or options!

---

## Summary

### The Core Limitation

**You cannot create NPCs that don't exist in the client cache.**

### The Solution

**Reuse existing NPCs with custom server-side behavior.**

### The Process

1. Find an NPC in `npc.rscm` that fits your theme
2. Verify it has "Attack" option (test in-game)
3. Use it in your plugin with `spawnNpc("npc.name", ...)`
4. Customize stats, drops, and combat via `setCombatDef`
5. Players fight a familiar-looking NPC with unique mechanics

### Going Further

To add truly custom NPCs:
1. Learn cache editing
2. Modify client cache
3. Update server RSCM
4. Distribute new client to players

---

## Additional Resources

### Useful Files in Your Server

- `Alter/data/cfg/rscm/npc.rscm` - NPC name to ID mappings
- `Alter/data/cfg/npcs.csv` - NPC descriptions
- `Alter/game-plugins/src/.../npcs/` - Example NPC plugins

### Community Tools

- **OpenRS2** - Cache library and tools
- **RuneLite** - Has cache inspection tools
- **OSRS Wiki** - NPC IDs and information

### Example Code Reference

Check these files in your server for examples:
- `KbdCombatPlugin.kt` - King Black Dragon implementation
- `DragonCombatPlugin.kt` - Multiple dragon variants
- `CowPlugin.kt` - Simple NPC with drops

---

## Troubleshooting

### Error: "RSCM returned -1"

**Cause:** NPC name doesn't exist in npc.rscm

**Fix:** Use an existing NPC name from the RSCM file

### Error: "No Attack option appears"

**Cause:** NPC doesn't have Attack in client cache options

**Fix:** Choose a different NPC model that has Attack

### Boss doesn't spawn

**Check:**
1. Correct NPC name with "npc." prefix
2. Valid coordinates
3. Plugin loaded successfully (check server logs)

---

**Created for:** Alter RSPS Development
**Last Updated:** 2025-01-16
