# Ghost NPC Investigation

## Issue Description
Players are reporting being attacked by "ghost" NPCs that are invisible or dead. This usually happens after killing an NPC. The NPC seems to continue attacking or re-engages combat while it should be dead/respawning.

## Initial Findings & Fixes
1.  **NpcDeathAction.kt**:
    *   **Problem**: The `reset()` function was called *before* the respawn delay. `reset()` unlocks the NPC (`lock = LockState.NONE`).
    *   **Consequence**: The NPC would be "dead" (waiting to respawn) but technically "alive" and "unlocked" on the server. It could acquire targets and attack, but would be invisible to the client because `NpcInfo` set it to invisible.
    *   **Fix Applied**: Moved `npc.unlock()` to *after* the `wait(respawnDelay)`. Also added `lock()` and `stopMovement()` to the `reset()` function to ensure it stays inert during the wait.

## Current Status
The user reports the issue is **still occurring**.

## Root Cause Analysis
1.  **Custom Combat Scripts**: Many NPCs (like Vet'ion, Revenants) use custom combat scripts that run their own loops (e.g., `while (canEngageCombat(target))`).
2.  **`Combat.canEngage` Bypass**: The `Combat.canEngage` function checked for death (`isDead()`) but **did NOT check if the pawn was locked (`lock.canAttack()`)**.
3.  **Respawn State**: When an NPC dies and enters the respawn wait period:
    *   Its HP is reset to full (so `isDead()` is false).
    *   It is locked (`LockState.FULL`) via `NpcDeathAction`.
    *   It is invisible (`setAllOpsInvisible`).
4.  **The Glitch**: Because `Combat.canEngage` returned `true` (since the NPC was not "dead"), the custom combat loops continued to run or were restarted (e.g., by `NpcAggroPlugin` or auto-retaliate logic). The NPC would then attack the player while invisible and locked.

## Resolution
1.  **Modified `Combat.kt`**: Added a check in `canEngage` to explicitly return `false` if `pawn.lock.canAttack()` is false.
    *   This ensures that any NPC currently in a locked state (like during respawn) cannot engage in combat, regardless of whether it uses the default `CombatPlugin` or a custom script.

## Verification
*   **NpcDeathAction**: Locks the NPC during respawn.
*   **Combat.canEngage**: Now respects the lock.
*   **Result**: Custom combat loops will terminate immediately when the NPC is locked, preventing ghost attacks.
