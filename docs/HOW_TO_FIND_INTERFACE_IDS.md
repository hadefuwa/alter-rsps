# 🔍 How to Find Interface IDs

This guide will help you find the correct interface IDs for your server.

## Method 1: Use Developer Commands (Easiest)

If you have developer privileges, you can use these commands to test interfaces:

### Basic Interface Command
```
::interface 270
```
This opens interface 270 on the main screen. Try different numbers until you find the right one!

### Advanced Interface Command
```
::openinterface 270 548 0 0 1
```
Format: `::openinterface <interfaceId> <parent> <child> <clickable> <modal>`

**Parameters:**
- `interfaceId`: The interface ID to open
- `parent`: Parent component (usually 548 for fixed, 161 for resizable)
- `child`: Child component (usually 0 for main screen)
- `clickable`: 0 or 1 (whether interface is clickable)
- `modal`: 0 or 1 (whether interface is modal)

### Common Interface IDs to Try

| Interface ID | Likely Purpose |
|--------------|----------------|
| 270 | **Smelting interface (standard OSRS)** |
| 311 | Combat Masteries (your server) |
| 446 | Jewelry crafting with moulds (rings, necklaces, amulets, bracelets) |
| 149 | Inventory |
| 320 | Skills |
| 387 | Equipment |
| 218 | Magic |
| 541 | Prayer |
| 593 | Attack/Combat |

## Method 2: Check Existing Plugins

Look at existing interface plugins to see what IDs are being used:

```
game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/
```

Example:
```kotlin
// In CombatMasteriesPlugin.kt
private val COMBAT_MASTERIES_INTERFACE_ID = 311

// In BankPlugin.kt - check what interface ID is used for bank
```

## Method 3: Use RuneLite Widget Inspector

If you're using RuneLite:
1. Enable "Widget Inspector" plugin
2. Click on the interface you want to inspect
3. The widget inspector will show you the interface ID and component IDs

## Method 4: Search the Codebase

Search for interface IDs in the codebase:

```bash
# Search for interface IDs
grep -r "interfaceId = " game-plugins/
grep -r "openInterface" game-plugins/
```

## Method 5: Check Interface Destination Enum

Common interface destinations are defined in:
```
game-api/src/main/kotlin/org/alter/api/InterfaceDestination.kt
```

This shows interface IDs for standard game tabs.

## Method 6: Test Common Smelting Interface IDs

For smelting specifically:

1. **270** - Standard OSRS smelting interface (confirmed correct)
2. **311** - Currently used by Combat Masteries (wrong for smelting)
3. **446** - Jewelry crafting with moulds (NOT for smelting)

### How to Test:

1. Use the `::interface` command to test:
   ```
   ::interface 270
   ```

2. Look for the interface that shows:
   - Bar icons (bronze, iron, steel, etc.)
   - Clickable buttons for each bar type
   - Usually has components 14-21 for different bars

3. Once you find the correct interface, update your code:
   ```kotlin
   private val SMELTING_INTERFACE_ID = 270  // Replace with correct ID
   ```

## Method 7: Check Client Cache

Interface definitions are stored in the client cache. You can:
- Use cache editing tools
- Check the cache structure
- Look for interface definitions

## Quick Reference: Finding the Smelting Interface

1. **Interface 270** is the correct smelting interface (confirmed)
2. Interface 446 is for jewelry crafting with moulds (NOT smelting)
3. Interface 311 is used by Combat Masteries (NOT smelting)

## Troubleshooting

### Interface Not Opening?
- Check if you have the right privileges for `::interface` command
- Try different parent/child combinations with `::openinterface`
- Make sure the interface ID exists in your client cache

### Wrong Interface Opens?
- The interface ID might be different for your server version
- Try the alternative IDs listed above
- Check if your server uses a custom interface

### Can't Find the Interface?
- Use RuneLite Widget Inspector if available
- Check decompiled client code for interface references
- Ask other developers or check server documentation

---

**Note:** Interface IDs can vary between server versions and custom implementations. Always test with the `::interface` command before committing to a specific ID!
