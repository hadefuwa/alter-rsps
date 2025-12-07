# 🖥️ Interfaces Guide

Welcome to the **Interfaces Guide**! This guide will teach you everything you need to know about working with game interfaces in your plugins.

## 📋 Table of Contents

1. [Introduction](#introduction)
2. [Opening Interfaces](#opening-interfaces)
3. [Interface Destinations](#interface-destinations)
4. [Handling Button Clicks](#handling-button-clicks)
5. [Setting Component Properties](#setting-component-properties)
6. [Interface Events](#interface-events)
7. [Interface Open/Close Handlers](#interface-openclose-handlers)
8. [Common Patterns](#common-patterns)
9. [Complete Examples](#complete-examples)
10. [Best Practices](#best-practices)

---

## Introduction

Interfaces are the windows and menus that players interact with in the game. They can be:
- **Full-screen overlays** (like the bank or shop interface)
- **Modal dialogs** (like confirmation windows)
- **Tab interfaces** (like the inventory or skills tab)
- **Chat window menus** (like option selections)

---

## Opening Interfaces

### Basic Syntax

The most common way to open an interface is using `player.openInterface()`:

```kotlin
// Open interface 311 on the main screen
player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
```

### Inside a Queue Block

When opening interfaces from event handlers (like button clicks or object interactions), you should use `player.queue`:

```kotlin
onObjOption(obj = "object.furnace", option = "Use") {
    player.queue {
        player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
    }
}
```

### Closing Interfaces

To close an interface:

```kotlin
// Close by interface ID
player.closeInterface(interfaceId = 311)

// Close by destination
player.closeInterface(dest = InterfaceDestination.MAIN_SCREEN)
```

---

## Interface Destinations

`InterfaceDestination` tells the game **where** to display your interface. Here are the most common ones:

### Main Screen
Opens the interface as a full-screen modal overlay:
```kotlin
player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
```
**Use for:** Bank, shops, custom interfaces, etc.

### Chat Box
Opens the interface in the chat window area:
```kotlin
player.openInterface(parent = 162, child = 566, interfaceId = 219)
```
**Use for:** Option menus, dialogs, etc.

### Tab Interfaces
Opens/reopens a tab interface:
```kotlin
player.openInterface(dest = InterfaceDestination.INVENTORY)
player.openInterface(dest = InterfaceDestination.SKILLS)
player.openInterface(dest = InterfaceDestination.EQUIPMENT)
```

### Available Destinations

| Destination | Description | Interface ID |
|------------|-------------|--------------|
| `MAIN_SCREEN` | Full-screen modal overlay | - |
| `CHAT_BOX` | Chat window area | 162 |
| `INVENTORY` | Inventory tab | 149 |
| `SKILLS` | Skills tab | 320 |
| `EQUIPMENT` | Equipment tab | 387 |
| `PRAYER` | Prayer tab | 541 |
| `MAGIC` | Magic tab | 218 |
| `ATTACK` | Attack/Combat tab | 593 |
| `QUEST_ROOT` | Quest tab | 629 |
| `SETTINGS` | Settings tab | 116 |
| `EMOTES` | Emotes tab | 216 |
| `ACCOUNT_MANAGEMENT` | Account tab | 109 |
| `SOCIAL` | Friends/Ignore tab | 429 |
| `CLAN_CHAT` | Clan chat tab | 707 |
| `LOG_OUT` | Logout tab | 182 |
| `XP_COUNTER` | XP counter overlay | 122 |

---

## Handling Button Clicks

### Basic Button Handler

Use `onButton` to handle clicks on interface buttons:

```kotlin
init {
    onButton(interfaceId = 311, component = 14) {
        player.message("Button 14 was clicked!")
    }
}
```

### Multiple Components

Handle multiple buttons with a loop:

```kotlin
init {
    // Handle buttons 14-21
    for (component in 14..21) {
        onButton(interfaceId = 311, component = component) {
            player.message("Button $component was clicked!")
        }
    }
}
```

### Getting Interaction Information

You can get information about the interaction:

```kotlin
onButton(interfaceId = 311, component = 14) {
    val slot = player.getInteractingSlot()  // Get the slot that was clicked
    val option = player.getInteractingOption()  // Get the option number (1, 2, 3, etc.)
    
    when (option) {
        1 -> player.message("First option clicked")
        2 -> player.message("Second option clicked")
        3 -> player.message("Third option clicked")
    }
}
```

---

## Setting Component Properties

### Setting Text

Update the text displayed on a component:

```kotlin
player.setComponentText(
    interfaceId = 311,
    component = 1,
    text = "Hello, ${player.username}!"
)
```

### Setting Items

Display an item on a component:

```kotlin
val itemId = getRSCM("item.bronze_bar")
player.setComponentItem(
    interfaceId = 311,
    component = 1,
    item = itemId,
    amountOrZoom = 1  // Amount for stacks, or zoom level for display
)
```

### Setting NPC Head

Display an NPC's head:

```kotlin
val npcId = getRSCM("npc.guard")
player.setComponentNpcHead(
    interfaceId = 311,
    component = 1,
    npc = npcId
)
```

### Setting Player Head

Display the player's head:

```kotlin
player.setComponentPlayerHead(
    interfaceId = 311,
    component = 1
)
```

### Setting Animation

Display an animation on a component:

```kotlin
player.setComponentAnim(
    interfaceId = 311,
    component = 1,
    anim = Animation.SMITHING_SMELT.id
)
```

### Hiding/Showing Components

Hide or show components:

```kotlin
// Hide a component
player.setComponentHidden(
    interfaceId = 311,
    component = 1,
    hidden = true
)

// Show a component
player.setComponentHidden(
    interfaceId = 311,
    component = 1,
    hidden = false
)
```

---

## Interface Events

Interface events control what interactions are allowed on components. You need to set events before components can be clicked.

### Setting Events

```kotlin
// Enable button clicks on component 1, options 1-5
player.setInterfaceEvents(
    interfaceId = 311,
    component = 1,
    from = 1,
    to = 5,
    setting = 1  // 1 = enabled
)
```

### Using InterfaceEvent Enum

For more control, use the `InterfaceEvent` enum:

```kotlin
import org.alter.api.ext.InterfaceEvent

// Enable click operations
player.setInterfaceEvents(
    interfaceId = 311,
    component = 1,
    range = 1..5,
    InterfaceEvent.ClickOp1,
    InterfaceEvent.ClickOp2,
    InterfaceEvent.ClickOp3
)

// Enable drag and drop
player.setInterfaceEvents(
    interfaceId = 311,
    component = 1,
    range = 0..27,
    InterfaceEvent.DragTargetable,
    InterfaceEvent.UseOnInventory
)
```

### Common Interface Events

| Event | Description |
|-------|-------------|
| `ClickOp1` - `ClickOp10` | Enable click options 1-10 |
| `UseOnInventory` | Allow items to be used on this component |
| `UseOnGroundItem` | Allow ground items to be used on this component |
| `UseOnNpc` | Allow NPCs to be used on this component |
| `UseOnObject` | Allow objects to be used on this component |
| `UseOnPlayer` | Allow players to be used on this component |
| `UseOnComponent` | Allow components to be used on this component |
| `DragTargetable` | Component can be dragged to |
| `ComponentTargetable` | Component can be targeted by other components |

---

## Interface Open/Close Handlers

### onInterfaceOpen

Execute code when an interface is opened:

```kotlin
init {
    onInterfaceOpen(interfaceId = 311) {
        // Initialize interface components
        player.setComponentText(interfaceId = 311, component = 1, text = "Welcome!")
        
        // Set up interface events
        player.setInterfaceEvents(
            interfaceId = 311,
            component = 14,
            from = 1,
            to = 8,
            setting = 1
        )
    }
}
```

### onInterfaceClose

Execute code when an interface is closed:

```kotlin
init {
    onInterfaceClose(interfaceId = 311) {
        // Clean up any state
        player.attr.remove(INTERACTING_INTERFACE_ATTR)
    }
}
```

---

## Common Patterns

### Pattern 1: Menu Selection Interface

Create a menu where players select from options:

```kotlin
init {
    onButton(interfaceId = 311, component = 14) {
        player.queue {
            val options = arrayOf("Option 1", "Option 2", "Option 3")
            val selected = options(player, *options, title = "Select an option")
            
            when (selected) {
                1 -> player.message("You selected Option 1")
                2 -> player.message("You selected Option 2")
                3 -> player.message("You selected Option 3")
            }
        }
    }
}
```

### Pattern 2: Dynamic Component Updates

Update interface components based on player state:

```kotlin
private fun updateInterface(player: Player) {
    val level = player.getSkills().getCurrentLevel(Skills.SMITHING)
    val xp = player.getSkills().getXp(Skills.SMITHING)
    
    player.setComponentText(
        interfaceId = 311,
        component = 1,
        text = "Smithing Level: $level"
    )
    
    player.setComponentText(
        interfaceId = 311,
        component = 2,
        text = "Experience: $xp"
    )
}

init {
    onInterfaceOpen(interfaceId = 311) {
        updateInterface(player)
    }
}
```

### Pattern 3: Item Display with Interaction

Display items that can be clicked:

```kotlin
init {
    onInterfaceOpen(interfaceId = 311) {
        // Display items
        val items = listOf(
            getRSCM("item.bronze_bar"),
            getRSCM("item.iron_bar"),
            getRSCM("item.steel_bar")
        )
        
        items.forEachIndexed { index, itemId ->
            player.setComponentItem(
                interfaceId = 311,
                component = 14 + index,
                item = itemId,
                amountOrZoom = 1
            )
        }
        
        // Enable clicks on item components
        player.setInterfaceEvents(
            interfaceId = 311,
            component = 14,
            from = 1,
            to = items.size,
            setting = 1
        )
    }
    
    onButton(interfaceId = 311, component = 14) {
        val slot = player.getInteractingSlot()
        player.message("You clicked item at slot $slot")
    }
}
```

### Pattern 4: Close Button

Handle interface close buttons:

```kotlin
init {
    // Component 0 is often the close button
    onButton(interfaceId = 311, component = 0) {
        player.closeInterface(interfaceId = 311)
    }
}
```

---

## Complete Examples

### Example 1: Simple Custom Interface

```kotlin
package org.alter.plugins.content.interfaces.example

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class ExampleInterfacePlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val EXAMPLE_INTERFACE_ID = 311

    init {
        // Handle interface open
        onInterfaceOpen(EXAMPLE_INTERFACE_ID) {
            // Set welcome text
            player.setComponentText(
                interfaceId = EXAMPLE_INTERFACE_ID,
                component = 1,
                text = "Welcome, ${player.username}!"
            )
            
            // Enable button clicks
            player.setInterfaceEvents(
                interfaceId = EXAMPLE_INTERFACE_ID,
                component = 14,
                from = 1,
                to = 3,
                setting = 1
            )
        }

        // Handle close button
        onButton(interfaceId = EXAMPLE_INTERFACE_ID, component = 0) {
            player.closeInterface(interfaceId = EXAMPLE_INTERFACE_ID)
        }

        // Handle action buttons
        onButton(interfaceId = EXAMPLE_INTERFACE_ID, component = 14) {
            val option = player.getInteractingOption()
            when (option) {
                1 -> player.message("You clicked option 1!")
                2 -> player.message("You clicked option 2!")
                3 -> player.message("You clicked option 3!")
            }
        }
    }
}
```

### Example 2: Interface with Item Selection

```kotlin
package org.alter.plugins.content.interfaces.example

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class ItemSelectionInterfacePlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val ITEM_SELECTION_INTERFACE_ID = 311
    
    private val selectableItems = listOf(
        "item.bronze_bar",
        "item.iron_bar",
        "item.steel_bar",
        "item.mithril_bar"
    )

    init {
        onInterfaceOpen(ITEM_SELECTION_INTERFACE_ID) {
            // Display items
            selectableItems.forEachIndexed { index, itemName ->
                val itemId = getRSCM(itemName)
                player.setComponentItem(
                    interfaceId = ITEM_SELECTION_INTERFACE_ID,
                    component = 14 + index,
                    item = itemId,
                    amountOrZoom = 1
                )
            }
            
            // Enable clicks
            player.setInterfaceEvents(
                interfaceId = ITEM_SELECTION_INTERFACE_ID,
                component = 14,
                from = 1,
                to = selectableItems.size,
                setting = 1
            )
        }

        // Handle item clicks
        onButton(interfaceId = ITEM_SELECTION_INTERFACE_ID, component = 14) {
            val slot = player.getInteractingSlot()
            if (slot in selectableItems.indices) {
                val itemName = selectableItems[slot]
                player.message("You selected: $itemName")
                player.closeInterface(interfaceId = ITEM_SELECTION_INTERFACE_ID)
            }
        }
    }
}
```

### Example 3: Chat Window Options Menu

```kotlin
package org.alter.plugins.content.interfaces.example

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class OptionsMenuPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    init {
        onObjOption(obj = "object.furnace", option = "Use") {
            player.queue {
                val options = arrayOf(
                    "Smelt Bronze bar",
                    "Smelt Iron bar",
                    "Smelt Steel bar",
                    "Cancel"
                )
                
                val selected = options(player, *options, title = "What would you like to smelt?")
                
                when (selected) {
                    1 -> {
                        player.message("Smelting bronze bar...")
                        // Add smelting logic here
                    }
                    2 -> {
                        player.message("Smelting iron bar...")
                        // Add smelting logic here
                    }
                    3 -> {
                        player.message("Smelting steel bar...")
                        // Add smelting logic here
                    }
                    4 -> {
                        player.message("Cancelled.")
                    }
                }
            }
        }
    }
}
```

---

## Best Practices

### ✅ DO

1. **Always use `player.queue` when opening interfaces from event handlers**
   ```kotlin
   onButton(interfaceId = 100, component = 1) {
       player.queue {
           player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
       }
   }
   ```

2. **Set interface events when opening interfaces**
   ```kotlin
   onInterfaceOpen(interfaceId = 311) {
       player.setInterfaceEvents(
           interfaceId = 311,
           component = 14,
           from = 1,
           to = 5,
           setting = 1
       )
   }
   ```

3. **Use constants for interface IDs**
   ```kotlin
   private val MY_INTERFACE_ID = 311
   ```

4. **Clean up state when interfaces close**
   ```kotlin
   onInterfaceClose(interfaceId = 311) {
       // Remove temporary attributes, reset state, etc.
   }
   ```

5. **Update interface components when player state changes**
   ```kotlin
   private fun refreshInterface(player: Player) {
       // Update all dynamic components
   }
   ```

### ❌ DON'T

1. **Don't open interfaces directly from event handlers without `player.queue`**
   ```kotlin
   // ❌ BAD
   onButton(interfaceId = 100, component = 1) {
       player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
   }
   
   // ✅ GOOD
   onButton(interfaceId = 100, component = 1) {
       player.queue {
           player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
       }
   }
   ```

2. **Don't forget to set interface events**
   ```kotlin
   // ❌ BAD - buttons won't work
   onInterfaceOpen(interfaceId = 311) {
       // Missing setInterfaceEvents!
   }
   ```

3. **Don't hardcode interface IDs everywhere**
   ```kotlin
   // ❌ BAD
   player.openInterface(interfaceId = 311, ...)
   player.closeInterface(interfaceId = 311)
   onButton(interfaceId = 311, ...)
   
   // ✅ GOOD
   private val MY_INTERFACE_ID = 311
   player.openInterface(interfaceId = MY_INTERFACE_ID, ...)
   ```

4. **Don't leave interfaces open when players log out or disconnect**
   ```kotlin
   // Handle player logout/disconnect to close interfaces
   ```

---

## Finding Interface IDs

### Method 1: Use the Developer Command

If you have developer privileges, you can use the interface command:
```
::interface 311
```

### Method 2: Check Existing Plugins

Look at existing interface plugins in:
```
game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/
```

### Method 3: Use RuneLite Developer Tools

If you're using RuneLite, you can use the developer tools to inspect interfaces.

---

## Troubleshooting

### Buttons Not Working?

1. **Check if interface events are set:**
   ```kotlin
   player.setInterfaceEvents(interfaceId = 311, component = 14, from = 1, to = 5, setting = 1)
   ```

2. **Verify the component ID is correct**

3. **Check if the interface is actually open**

### Interface Not Opening?

1. **Make sure you're using `player.queue` in event handlers**
2. **Verify the interface ID is correct**
3. **Check if another interface is blocking it (close it first)**

### Components Not Updating?

1. **Make sure the interface is open when you update components**
2. **Verify component IDs are correct**
3. **Check if you're updating the right interface ID**

---

## Additional Resources

- **Interface API**: `game-api/src/main/kotlin/org/alter/api/ext/PlayerExt.kt`
- **Interface Events**: `game-api/src/main/kotlin/org/alter/api/ext/InterfaceEvents.kt`
- **Interface Destinations**: `game-api/src/main/kotlin/org/alter/api/InterfaceDestination.kt`
- **Example Plugins**: `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/`

---

## Quick Reference

### Opening Interfaces
```kotlin
player.openInterface(interfaceId = 311, dest = InterfaceDestination.MAIN_SCREEN)
```

### Closing Interfaces
```kotlin
player.closeInterface(interfaceId = 311)
```

### Button Handlers
```kotlin
onButton(interfaceId = 311, component = 14) { }
```

### Setting Text
```kotlin
player.setComponentText(interfaceId = 311, component = 1, text = "Hello")
```

### Setting Items
```kotlin
player.setComponentItem(interfaceId = 311, component = 1, item = itemId, amountOrZoom = 1)
```

### Setting Events
```kotlin
player.setInterfaceEvents(interfaceId = 311, component = 14, from = 1, to = 5, setting = 1)
```

---

Happy coding! 🎮✨
