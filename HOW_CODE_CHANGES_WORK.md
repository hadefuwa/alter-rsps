# How Code Changes Work in Alter RSPS

## Overview

This document explains how code changes work in the Alter RSPS codebase, based on the plugin system architecture and recent fixes (settings close button, home teleport button, teleport menu).

---

## Plugin System Architecture

### Core Concept

The server uses a **plugin-based architecture** where game logic is organized into plugins. Each plugin is a Kotlin class that extends `KotlinPlugin` and registers event handlers.

### Plugin Structure

```kotlin
class MyPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Register event handlers here
    }
}
```

**Key Components:**
- **PluginRepository (`r`)**: Manages all plugins and their registration
- **World (`world`)**: Contains game state, players, NPCs, objects
- **Server (`server`)**: Server instance

---

## Event Handlers

### How Events Work

Plugins register **event handlers** that respond to specific game events. When an event occurs, the server calls the appropriate handler.

### Common Event Handlers

#### 1. Button Clicks (`onButton`)

**Purpose**: Handle clicks on interface buttons

**Example - Settings Close Button:**
```kotlin
onButton(interfaceId = 134, component = 4) {
    player.closeComponent(parent = 161, child = 18)
}
```

**How it works:**
- When player clicks button on interface 134, component 4
- Server finds registered handler
- Executes the code block
- Player's settings window closes

**Example - Home Teleport Button:**
```kotlin
onButton(interfaceId = 218, component = 7) {
    val option = player.getInteractingOption()
    
    if (option == 10) {
        // Right-click: Open teleport menu
        // ... menu code ...
    } else {
        // Left-click: Instant teleport home
        val home = world.gameContext.home
        player.prepareForTeleport()
        player.moveTo(home)
    }
}
```

**Key Points:**
- `interfaceId`: The interface ID (e.g., 218 = spellbook)
- `component`: The component ID within the interface (e.g., 7 = home teleport button)
- `player`: The player who clicked the button
- `getInteractingOption()`: Gets which option was clicked (1 = left-click, 10 = right-click option)

#### 2. Login Events (`onLogin`)

**Purpose**: Execute code when a player logs in

**Example - Enable Right-Click Option:**
```kotlin
onLogin {
    player.setInterfaceEvents(
        interfaceId = 218, 
        component = 7, 
        range = 0..0, 
        setting = InterfaceEvent.ClickOp2
    )
}
```

**How it works:**
- When player logs in
- Server calls all `onLogin` handlers
- Code executes for that player
- Enables right-click option on home teleport button

#### 3. Commands (`onCommand`)

**Purpose**: Handle player commands (chat commands)

**Example:**
```kotlin
onCommand("home", description = "Teleports you home") {
    val home = world.gameContext.home
    player.moveTo(home)
}
```

**How it works:**
- Player types `::home` in chat
- Server finds command handler
- Executes teleport code

---

## Making Code Changes

### Step-by-Step Process

#### 1. Locate the Relevant Plugin

**Example**: To fix the home teleport button, we found:
- File: `game-plugins/src/main/kotlin/org/alter/plugins/content/commands/commands/all/TeleportsPlugin.kt`
- This plugin handles teleport-related functionality

**How to find plugins:**
- Look in `game-plugins/src/main/kotlin/org/alter/plugins/content/`
- Organized by feature (commands, interfaces, npcs, etc.)
- Search for keywords related to what you want to change

#### 2. Understand the Current Code

**Example - Before Fix:**
```kotlin
onButton(interfaceId = 218, component = 7) {
    // No handler = button does nothing
}
```

**Problem**: No handler registered, so clicking the button does nothing.

#### 3. Add or Modify Event Handler

**Example - After Fix:**
```kotlin
onButton(interfaceId = 218, component = 7) {
    val option = player.getInteractingOption()
    
    if (option == 10) {
        // Handle right-click (teleport menu)
    } else {
        // Handle left-click (instant teleport)
        val home = world.gameContext.home
        player.prepareForTeleport()
        player.moveTo(home)
    }
}
```

**What changed:**
- Added handler for interface 218, component 7
- Check which option was clicked
- Execute appropriate action

#### 4. Use Available APIs

**Common Player Methods:**
- `player.moveTo(tile)` - Teleport player
- `player.closeComponent(parent, child)` - Close interface component
- `player.openInterface(...)` - Open interface
- `player.setInterfaceEvents(...)` - Enable interface interactions
- `player.getInteractingOption()` - Get clicked option number
- `player.queue { ... }` - Queue actions (for delays, dialogs, etc.)
- `player.prepareForTeleport()` - Prepare player for teleport

**Common World Access:**
- `world.gameContext.home` - Get home location
- `world.players` - Access all players
- `world.npcs` - Access all NPCs

---

## How Changes Take Effect

### Server Restart Required

**Important**: Code changes require a **full server restart** to take effect.

**Why:**
- Plugins are loaded at server startup
- Event handlers are registered during initialization
- Changes to plugin code are not hot-reloaded

**Process:**
1. Make code changes
2. Compile code (builds Kotlin to bytecode)
3. Stop server
4. Start server (reloads all plugins)
5. Changes are active

### Compilation

**Before server restart:**
- Kotlin code must compile successfully
- Any errors prevent server from starting
- Fix compilation errors first

**Example Error:**
```
Unresolved reference 'TaskPriority'
```
**Fix**: Add missing import:
```kotlin
import org.alter.game.model.queue.TaskPriority
```

---

## Real Examples from Our Fixes

### Example 1: Settings Close Button Fix

**Problem**: Settings window couldn't be closed

**Solution**:
```kotlin
// File: OptionsTabFirstPlugin.kt
onButton(interfaceId = 134, component = 4) {
    player.closeComponent(parent = 161, child = 18)
}
```

**What happened:**
1. Identified interface 134 (settings window)
2. Found component 4 (close button)
3. Added handler that closes the component
4. Server restart → button now works

### Example 2: Home Teleport Button Fix

**Problem**: Home teleport button did nothing

**Solution**:
```kotlin
// File: TeleportsPlugin.kt
onButton(interfaceId = 218, component = 7) {
    val option = player.getInteractingOption()
    
    if (option == 10) {
        // Right-click: Show teleport menu
        // ... pagination code ...
    } else {
        // Left-click: Instant teleport
        val home = world.gameContext.home
        player.prepareForTeleport()
        player.moveTo(home)
    }
}
```

**What happened:**
1. Identified interface 218, component 7 (home teleport button)
2. Added handler for button clicks
3. Checked option number (1 = left-click, 10 = right-click)
4. Implemented different behavior for each option
5. Server restart → button now works

### Example 3: Enabling Right-Click Option

**Problem**: Right-click didn't show "Teleport Menu" option

**Solution**:
```kotlin
// File: TeleportsPlugin.kt
onLogin {
    player.setInterfaceEvents(
        interfaceId = 218, 
        component = 7, 
        range = 0..0, 
        setting = InterfaceEvent.ClickOp2
    )
}
```

**What happened:**
1. Enabled `ClickOp2` event on the button
2. This allows right-click to trigger option 10
3. Server restart → right-click now works

**Note**: The option text ("Animation") comes from the client cache and can't be changed dynamically. We use option 10 to trigger our menu.

---

## Key Concepts

### 1. Interface IDs and Components

**Interface ID**: The main interface (e.g., 218 = spellbook, 134 = settings)

**Component ID**: A specific element within the interface (e.g., component 7 = home teleport button)

**Finding IDs:**
- Check existing code for similar interfaces
- Use developer commands (if available)
- Check OSRS Wiki for interface information
- Trial and error (enable debug logging)

### 2. Option Numbers

**Left-click**: Usually option 1

**Right-click options**: 
- Option 2 = first right-click option
- Option 10 = second right-click option (in some cases)
- Varies by interface/component

**How to find:**
- Use `player.getInteractingOption()` to see what option was clicked
- Test different options
- Check client cache for option names

### 3. Event Registration

**Events are registered in `init` block:**
```kotlin
init {
    onButton(...) { ... }
    onLogin { ... }
    onCommand(...) { ... }
}
```

**Order matters**: Handlers are checked in registration order (first match wins)

### 4. Player Context

**In event handlers, `player` refers to:**
- The player who triggered the event
- The player who clicked the button
- The player who typed the command
- The player who logged in

**You can access:**
- `player.inventory` - Player's inventory
- `player.equipment` - Player's equipment
- `player.tile` - Player's current location
- `player.skills` - Player's skills
- And many more...

---

## Common Patterns

### Pattern 1: Simple Button Handler

```kotlin
onButton(interfaceId = X, component = Y) {
    // Do something when button is clicked
    player.message("Button clicked!")
}
```

### Pattern 2: Conditional Logic Based on Option

```kotlin
onButton(interfaceId = X, component = Y) {
    val option = player.getInteractingOption()
    
    when (option) {
        1 -> {
            // Left-click action
        }
        10 -> {
            // Right-click action
        }
    }
}
```

### Pattern 3: Queued Actions (for delays, dialogs)

```kotlin
onButton(interfaceId = X, component = Y) {
    player.queue(TaskPriority.STRONG) {
        // Actions that need delays or await player input
        val selected = options(player, "Option 1", "Option 2", title = "Choose")
        // ... handle selection ...
    }
}
```

### Pattern 4: Teleportation

```kotlin
onButton(interfaceId = X, component = Y) {
    if (!player.lock.canTeleport()) {
        return@onButton  // Can't teleport (in combat, etc.)
    }
    
    val destination = Tile(x = 3211, z = 3424, height = 0)
    player.prepareForTeleport()
    player.moveTo(destination)
}
```

---

## Debugging Tips

### 1. Check Compilation Errors

**Before restarting server:**
- Fix all compilation errors
- Check imports are correct
- Verify method names and signatures

### 2. Enable Debug Logging

**If available:**
- Enable button click logging
- See what interface/component/option was clicked
- Verify your handler is being called

### 3. Test Incrementally

**Don't change everything at once:**
- Make small changes
- Test each change
- Build up functionality gradually

### 4. Use Existing Code as Reference

**Look at similar implementations:**
- How other buttons are handled
- How other plugins are structured
- Copy patterns that work

---

## Summary

**How code changes work:**

1. **Find the plugin** that handles what you want to change
2. **Add or modify event handlers** (`onButton`, `onLogin`, `onCommand`, etc.)
3. **Use available APIs** (`player.moveTo()`, `player.closeComponent()`, etc.)
4. **Compile code** (fix any errors)
5. **Restart server** (changes take effect)
6. **Test** the changes

**Key takeaway**: The plugin system uses event handlers that respond to game events. You register handlers in plugin `init` blocks, and they execute when the corresponding event occurs. All changes require a server restart to take effect.

---

## Quick Reference

**File Locations:**
- Plugins: `game-plugins/src/main/kotlin/org/alter/plugins/content/`
- Server code: `game-server/src/main/kotlin/org/alter/game/`
- API: `game-api/src/main/kotlin/org/alter/api/`

**Common Imports:**
```kotlin
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.model.Tile
import org.alter.game.model.queue.TaskPriority
```

**Common Event Handlers:**
- `onButton(interfaceId, component) { ... }`
- `onLogin { ... }`
- `onCommand("command") { ... }`
- `onNpcClick(npcId) { ... }`
- `onObjectClick(objectId) { ... }`

**Common Player Methods:**
- `player.moveTo(tile)` - Teleport
- `player.closeComponent(parent, child)` - Close interface
- `player.message(text)` - Send message
- `player.getInteractingOption()` - Get clicked option
- `player.queue { ... }` - Queue actions

