# Obor the Hill Giant Boss

This implementation adds Obor, the Hill Giant boss, to the game with a complete boss encounter system.

## Features

### 🏰 Boss Location
- **Location**: Obor's Lair in the Edgeville Dungeon
- **Coordinates**: (3093, 9842, height = 0)
- **Access**: Requires Giant Key (dropped by Hill Giants)

### 🗝️ Giant Key System
- **Drop Rate**: 1 in 128 chance from all Hill Giants
- **Usage**: Consumed upon entering Obor's lair
- **Item ID**: 20754 (from OSRS data)

### ⚔️ Combat Features
- **Combat Level**: 106
- **Enhanced Damage**: 50% more damage than regular Hill Giants
- **Special Abilities**:
  - **Stomp Attack**: 15% chance to hit all players within 3 tiles (15-25 damage + knockback)
  - **Regeneration**: Heals when below 50% HP during combat
  - **Combat Messages**: Dynamic battle text for immersion

### 💎 Loot System
- **Guaranteed Drops**:
  - 50,000-100,000 coins
  - 2-4 big bones

- **Rare Equipment** (high drop rates):
  - Rune weapons: scimitar (1/8), battleaxe (1/10), mace (1/12), sword (1/15)
  - Rune armor: chainbody (1/20), med helm (1/25), platelegs (1/30)
  - Ultra rare: rune platebody (1/50), rune kiteshield (1/40)

- **Resources**:
  - Coal (10-25), Iron ore (15-30), Adamant ore (5-10)
  - Runite ore (1/128 chance for 1-3)
  - Nature runes (20-50), Law runes (10-25), Death runes (5-15)
  - Sharks (5-12)

### 🌐 Teleport Access
- **Portal Nexus**: "Obor's Lair" option added to all Nexus Portals
- **Quick Access**: Direct teleport to Obor's chamber (bypasses key requirement for teleport)

### 🔄 Respawn System
- **Respawn Time**: 5 minutes after defeat
- **Notification**: All nearby players notified of defeat and respawn time

## File Structure

```
game-plugins/src/main/kotlin/org/alter/plugins/content/bosses/obor/
├── OborBossPlugin.kt      # Main boss mechanics and combat
├── OborKeyPlugin.kt       # Giant Key drops and access system  
├── OborLootPlugin.kt      # Enhanced loot drops and rewards
└── README.md             # This documentation
```

## How to Fight Obor

1. **Obtain Giant Key**: Kill Hill Giants in Edgeville Dungeon until you get a Giant Key (1/128 chance)
2. **Enter Lair**: Walk to coordinates (3092, 9842) with the key, or use Nexus Portal → "Obor's Lair"
3. **Fight Obor**: Engage the enhanced Hill Giant boss with special mechanics
4. **Collect Loot**: Obor drops valuable rewards worth the challenge
5. **Repeat**: Obor respawns after 5 minutes for the next challenger

## Integration Notes

- Fully integrated with existing combat systems
- Works with Boss Island enhancement plugins (if Obor is spawned there)
- Compatible with shared loot and damage tracking systems
- Uses proper RSCM item and NPC references

## Balancing

Obor is designed as a mid-level boss encounter:
- **Recommended Combat Level**: 70+
- **Difficulty**: Moderate (requires food and decent gear)
- **Risk vs Reward**: Key cost vs valuable guaranteed loot
- **Accessibility**: Easy access via teleport system