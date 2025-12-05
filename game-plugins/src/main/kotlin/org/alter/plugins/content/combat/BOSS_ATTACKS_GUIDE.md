# BossAttacks Guide

The `BossAttacks` object is a universal utility designed to simplify the creation of boss combat mechanics in the Alter RSPS. It abstracts away the repetitive code for animations, projectiles, damage calculations, and hit delays, allowing you to focus on the unique behavior of your bosses.

## Location
`org.alter.plugins.content.combat.BossAttacks`

## Core Functions

### 1. `melee`
Performs a standard melee attack.
*   **Use case:** Basic boss melee swings.
*   **Features:** Handles animation, accuracy/max hit formulas, and standard 1-tick delay.
*   **Example:**
    ```kotlin
    BossAttacks.melee(
        npc = this,
        target = target,
        anim = 7000, // Optional: override animation
        maxHit = 45, // Optional: fixed max hit
        onHit = { hit ->
            if (hit.landed) {
                target.graphic(100) // Play graphic on hit
            }
        }
    )
    ```

### 2. `ranged`
Performs a standard ranged attack with a projectile.
*   **Use case:** Boss shooting arrows, rocks, or other projectiles.
*   **Features:** Spawns projectile, calculates travel time, and applies damage on impact.
*   **Example:**
    ```kotlin
    BossAttacks.ranged(
        npc = this,
        target = target,
        projectile = 100, // Projectile graphic ID
        anim = 7001,
        damageMultiplier = 1.5 // 50% more damage than normal
    )
    ```

### 3. `magic`
Performs a standard magic attack with a projectile.
*   **Use case:** Boss casting spells.
*   **Features:** Similar to ranged, but uses magic accuracy/defense formulas.
*   **Example:**
    ```kotlin
    BossAttacks.magic(
        npc = this,
        target = target,
        projectile = 101,
        onHit = { hit ->
            if (hit.landed) {
                target.freeze(5) // Freeze player on hit
            }
        }
    )
    ```

### 4. `unblockable`
Performs an attack that ignores accuracy checks and always hits.
*   **Use case:** "Ultimate" attacks, environmental damage, or mechanics that must deal damage (e.g., standing in fire).
*   **Features:** Guaranteed damage, optional projectile.
*   **Example:**
    ```kotlin
    BossAttacks.unblockable(
        npc = this,
        target = target,
        damage = 99, // Deals exactly 99 damage
        anim = 7005
    )
    ```

### 5. `aoe`
Performs an Area of Effect attack.
*   **Use case:** Explosions, ground slams, or multi-target spells.
*   **Features:** Hits all players within a radius. Can be centered on the NPC, a player, or a specific tile.
*   **Example:**
    ```kotlin
    BossAttacks.aoe(
        npc = this,
        center = this.tile, // Center on the boss
        radius = 3, // 3-tile radius
        combatClass = CombatClass.MAGIC,
        onHit = { player, hit ->
            player.message("You are burned by the fire!")
        }
    )
    ```

## Utility Functions

### `knockback`
Knocks the target back and deals damage.
*   **Use case:** "Get off me" mechanics (e.g., Callisto).
*   **Example:** `BossAttacks.knockback(this, target)`

### `stun`
Stuns the target for a duration.
*   **Use case:** Disabling players temporarily.
*   **Example:** `BossAttacks.stun(target, cycles = 5)`

### `teleportTargetToNpc`
Teleports the target next to the NPC.
*   **Use case:** Pulling players in (e.g., Crazy Archaeologist).
*   **Example:** `BossAttacks.teleportTargetToNpc(this, target)`

## Best Practices
1.  **Use `onHit` callbacks:** This is the best place to add special effects like graphics, sounds, or status ailments (poison, freeze) that should only happen if the attack successfully lands.
2.  **Multipliers:** Use `accuracyMultiplier` and `damageMultiplier` to tune difficulty without hardcoding values.
3.  **Combine functions:** You can chain these in your combat loop. For example, have a 10% chance to use `unblockable` and a 90% chance to use `melee`.
