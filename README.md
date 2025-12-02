# Alter RSPS

A modern Old School RuneScape Private Server (RSPS) built with Kotlin, featuring a comprehensive game engine, plugin system, and REST API for client connectivity.

[![Revision](https://img.shields.io/badge/Revision-228-blueviolet)](https://oldschool.runescape.wiki/w/Update:Leagues_V:_Raging_Echos_Rewards_Are_Here)
[![License](https://img.shields.io/badge/license-ISC-informational)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-17-blue)](https://adoptium.net/)
[![Latest Release](https://img.shields.io/badge/Release-v1.1.6-green)](https://github.com/hadefuwa/alter-rsps/releases)

## Features

- **Full OSRS Revision 228 Support** - Complete game content and mechanics
- **Plugin-Based Architecture** - Easy to extend with custom content
- **REST API** - HTTP endpoints for client configuration (`jav_config.ws`, `world_list.ws`, `bootstrap.json`)
- **LAN Support** - Server binds to all interfaces (0.0.0.0) for network access
- **RSProx Compatible** - Works seamlessly with RSProx client
- **Player Saving** - Persistent player data and positions
- **Comprehensive Content** - Wilderness monsters, NPCs, objects, items, and more
- **Boss Content** - Multiple bosses including Dagannoth Kings, Vardorvis, Warden, God Wars Dungeon, and more
- **Minigames** - Last Man Standing, Wintertodt, and more
- **Skills** - Agility, Cooking, Firemaking, Fishing, Herblore, Mining, Slayer, Smithing, Thieving, Woodcutting

## Requirements

- **Java Development Kit (JDK) 17** or higher
- **Gradle** (included via wrapper)
- **OSRS Cache** - Place in `data/cache/` directory
- **RSA Key** - Auto-generated on first run if missing

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/hadefuwa/alter-rsps.git
cd alter-rsps
```

### 2. Configure the Server

Edit `game.yml` to customize your server:

```yaml
name: "Alter"
game-port: 43594
revision: 228

# Server IP for LAN access
services:
  - class: org.alter.plugins.service.restapi.RestApiService
    port: 8080
    server-ip: "192.168.0.13"  # Your server's LAN IP
    server-hostname: "oldschool.runescape.com"  # Optional: for hostname-based clients
```

### 3. Configure World List

Edit `data/cfg/world.json`:

```json
[
  {
    "id": 1,
    "types": ["FREE"],
    "address": "192.168.0.13",
    "activity": "Skilling",
    "location": "UNITED_STATES",
    "players": 1000
  }
]
```

### 4. Run the Server

```bash
./gradlew run
```

The server will start on:
- **Game Server**: Port `43594`
- **REST API**: Port `8080`

## Connecting with RSProx

### 1. Install RSProx

Download RSProx from: https://github.com/blurite/rsprox/releases

### 2. Configure RSProx

Create `%USERPROFILE%\.rsprox\proxy-targets.yaml`:

```yaml
config:
  - id: 1
    name: Alter
    jav_config_url: http://192.168.0.13:8080/jav_config.ws
    varp_count: 15000
    revision: 228.2
    modulus: abf0cb79b0fdc8175ca45f5f6b78e9326c59e892806a503eafacebe2b8888f5c034ccf0440973d0947f12069cc3d572bf6ddaaed4eed2d381d525cee777b5192ce424c835ec994dfcdf26b159fc5d5c3b414fdedbddae228b60795b74de56c2cc1c7020f17331b4b2206efcb17dc8d3331817476e947655a55705a944250f9b9
    game_server_port: 43594
```

**Important**: 
- Replace `192.168.0.13` with your server's actual IP address
- Get the modulus from `modulus` file in the project root (generated on first server start)

### 3. Launch RSProx

1. Start your Alter server
2. Launch RSProx
3. Select "Alter" from the server list
4. Connect and play!

## Network Configuration

### LAN Access

The server is configured to accept connections from all network interfaces (0.0.0.0). To allow connections from other devices:

1. **Find your server's IP address:**
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```

2. **Update `game.yml`:**
   ```yaml
   server-ip: "YOUR_LAN_IP"  # e.g., "192.168.0.13"
   ```

3. **Update `data/cfg/world.json`:**
   ```json
   "address": "YOUR_LAN_IP"
   ```

4. **Configure Windows Firewall** (if needed):
   ```powershell
   # Allow port 43594 (game server)
   New-NetFirewallRule -DisplayName "Alter RSPS Game Server" -Direction Inbound -LocalPort 43594 -Protocol TCP -Action Allow
   
   # Allow port 8080 (REST API)
   New-NetFirewallRule -DisplayName "Alter RSPS REST API" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
   ```

## Project Structure

```
alter-rsps/
├── game-server/          # Core game engine
├── game-plugins/         # Game content plugins
│   ├── areas/            # Area-specific content (Varrock, Lumbridge, Wilderness, etc.)
│   ├── bosses/           # Boss implementations
│   ├── minigames/        # Minigame implementations
│   ├── npcs/             # NPC handlers and combat configs
│   ├── skills/           # Skill implementations
│   └── items/            # Item interactions
├── game-api/             # API definitions
├── plugins/              # Utility plugins (filestore, rscm, tools)
├── util/                 # Utility libraries
├── http-api/             # Web interface (Vue.js)
├── data/                 # Game data and configuration
│   ├── cache/            # OSRS cache files
│   ├── cfg/              # Configuration files
│   ├── rsa/              # RSA keys
│   └── saves/            # Player save data
├── game.yml              # Main server configuration
└── modulus               # RSA modulus (auto-generated)
```

## Configuration Files

- **`game.yml`** - Main server configuration (ports, services, privileges)
- **`data/cfg/world.json`** - World list configuration
- **`dev-settings.yml`** - Development settings (optional)
- **`proxy-targets.yaml`** - RSProx configuration template

## REST API Endpoints

The server provides the following HTTP endpoints:

- `http://YOUR_IP:8080/jav_config.ws` - Client configuration file
- `http://YOUR_IP:8080/world_list.ws` - World list (binary format)
- `http://YOUR_IP:8080/bootstrap.json` - RuneLite bootstrap configuration
- `http://YOUR_IP:8080/players` - Online players list (JSON)
- `http://YOUR_IP:8080/player/:name` - Player information (JSON)

## Building

```bash
# Build the project
./gradlew build

# Run the server
./gradlew run

# Clean build artifacts
./gradlew clean
```

## Development

### Plugin System Architecture

The server uses a plugin-based architecture where all game content is implemented as **KotlinPlugin** classes. Plugins are automatically discovered and loaded at server startup.

#### How Plugins Work

1. **Plugin Discovery**: The server scans the classpath for all classes extending `KotlinPlugin`
2. **Auto-Loading**: Plugins are instantiated automatically - no registration needed
3. **Event Binding**: Plugins bind to game events (NPC clicks, item usage, etc.) in their `init {}` block

### Creating Your First Plugin

#### Basic Plugin Structure

```kotlin
package org.alter.plugins.content.areas.myarea

import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class MyPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Bind events here
    }
}
```

### Common Plugin Patterns

#### 1. Creating NPCs

```kotlin
init {
    // Spawn an NPC at coordinates (x, z, height)
    spawnNpc("npc.shop_keeper", x = 3211, z = 3424, height = 0, walkRadius = 3)

    // Handle NPC interactions
    onNpcOption("npc.shop_keeper", "Talk-to") {
        player.queue {
            chatNpc(player, "Hello! How can I help you?")
        }
    }

    onNpcOption("npc.shop_keeper", "Trade") {
        player.openShop("My Shop")
    }
}
```

**RSCM Names**: NPCs, items, and objects use RSCM (RuneScape Config Manager) identifiers like `"npc.shop_keeper"` or `"item.bread"`. These are defined in the cache.

#### 2. Creating Shops

```kotlin
init {
    // Spawn the shopkeeper
    spawnNpc("npc.shop_keeper_2825", 3212, 3425, 0, 0, Direction.SOUTH)

    // Create the shop
    createShop(
        name = "Food Shop",
        currency = CoinCurrency(),
        stockSize = 50,
        purchasePolicy = PurchasePolicy.BUY_TRADEABLES
    ) {
        // Add items to shop
        items[0] = ShopItem(getRSCM("item.bread"), 100, 10, 50) // ID, stock, price, restock amount
        items[1] = ShopItem(getRSCM("item.cake"), 100, 50, 50)
    }

    // Bind NPC to shop
    onNpcOption("npc.shop_keeper_2825", "Trade") {
        player.openShop("Food Shop")
    }
}
```

#### 3. Handling Item Interactions

```kotlin
init {
    // Click item in inventory (option number based on right-click menu)
    onItemOption("item.bread", 2) {  // Option 2 = "Eat"
        player.message("You eat the bread.")
        player.heal(5, 0)
        player.inventory.remove(player.getInteractingItemSlot(), 1)
    }

    // Use item on another item
    onItemOnItem("item.chisel", "item.diamond") {
        player.message("You cut the diamond.")
    }

    // Use item on object
    onItemOnObj("obj.altar", "item.bones") {
        player.message("You offer the bones to the gods.")
    }
}
```

**Item Option Numbers**:
- Option 1: Usually first custom action
- Option 2: Common actions like "Eat", "Drink", "Wield"
- Option 3-4: Additional actions
- Option 5: Usually "Drop"

#### 4. Handling Object Interactions

```kotlin
init {
    // Click object (ladder, door, etc.)
    onObjOption("obj.ladder_17385", "Climb-up") {
        player.moveTo(x = 3210, z = 3424, height = 1)
    }

    // Spawn custom objects
    spawnObj("obj.ladder_17385", x = 3210, z = 3424, height = 0, type = 10, rot = 0)
}
```

#### 5. Creating Food Items

```kotlin
// Add to Food.kt enum
LOBSTER(item = "item.lobster", heal = 12)

// The EatingPlugin automatically binds all Food enum entries
// No additional code needed!
```

#### 6. Player Dialogs

```kotlin
onNpcOption("npc.shopkeeper", "Talk-to") {
    player.queue {
        chatNpc(player, "Welcome to my shop!")
        chatNpc(player, "Would you like to see what I have for sale?")

        when (options(player, "Yes please.", "No thanks.")) {
            1 -> player.openShop("My Shop")
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}
```

### Finding RSCM IDs

RSCM IDs are string identifiers for game objects. To find them:

1. **Check existing plugins** - Look at similar content in `game-plugins/`
2. **Use the cache** - The game cache contains all definitions
3. **Common patterns**:
   - NPCs: `"npc.name_id"` (e.g., `"npc.shop_keeper_2825"`)
   - Items: `"item.name"` (e.g., `"item.bread"`, `"item.lobster"`)
   - Objects: `"obj.name_id"` (e.g., `"obj.ladder_17385"`)

### Project Structure for Plugins

```
game-plugins/src/main/kotlin/org/alter/plugins/content/
├── areas/                    # Area-specific content
│   ├── lumbridge/
│   │   └── npcs/
│   │       └── stores/
│   │           └── LumbridgeShopPlugin.kt
│   └── varrock/
│       └── npcs/
│           └── stores/
│               └── VarrockFoodShopPlugin.kt
├── items/                    # Item interactions
│   └── consumables/
│       └── food/
│           ├── Food.kt      # Food definitions
│           ├── Foods.kt     # Food mechanics
│           └── EatingPlugin.kt
├── mechanics/                # Game mechanics
│   ├── prayer/
│   └── hpregeneration/
├── npcs/                     # Global NPC handlers
├── objects/                  # Object interactions
│   └── trapdoor/
└── skills/                   # Skill implementations
    └── agility/
```

### Configuration Files

#### Defining Custom Data

You can define custom content in JSON files under `data/cfg/`:

**Example: `data/cfg/trapdoors/trapdoors.json`**
```json
[
  {
    "objectId": 1579,
    "destinationX": 3209,
    "destinationY": 9616,
    "destinationZ": 0
  }
]
```

**Loading in Plugin:**
```kotlin
class TrapdoorService : Service {
    override fun init(server: Server, world: World, serviceManager: ServiceManager) {
        val file = File("./data/cfg/trapdoors/trapdoors.json")
        val trapdoors = gson.fromJson(file.readText(), Array<Trapdoor>::class.java)
        // Use trapdoors...
    }
}
```

### Best Practices

1. **Organization**: Place plugins in appropriate folders (`areas/`, `items/`, `skills/`, etc.)
2. **RSCM Usage**: Always use RSCM identifiers, never hardcode numeric IDs
3. **Error Handling**: Wrap RSCM lookups in try-catch to handle missing items gracefully
4. **Player Feedback**: Always message the player so they know something happened
5. **Testing**: Test with multiple scenarios (full inventory, missing items, etc.)

### Common Helper Functions

```kotlin
// Player extensions
player.message("Hello!")              // Send chat message
player.moveTo(x, z, height)          // Teleport player
player.heal(amount, overheal)        // Heal HP
player.animate(animId)               // Play animation
player.playSound(soundId)            // Play sound
player.openShop("Shop Name")         // Open shop interface
player.getInteractingItemSlot()      // Get clicked item slot

// Inventory
player.inventory.add(itemId, amount)
player.inventory.remove(slot, amount)
player.inventory.contains(itemId)

// Skills
player.getSkills().getCurrentLevel(Skills.HITPOINTS)
player.getSkills().getBaseLevel(Skills.ATTACK)

// Timers
player.timers[FOOD_DELAY] = 3       // Set timer (game ticks)
player.timers.has(FOOD_DELAY)        // Check if timer active

// RSCM lookups
val itemId = getRSCM("item.bread")   // Get item ID
val itemDef = getItem(itemId)        // Get item definition
```

### Debugging

Enable debug logging in `dev-settings.yml`:

```yaml
debug-examines: true
debug-objects: true
debug-packets: true
```

**Console Logging:**
```kotlin
// Use System.err for console output (shows in IntelliJ/terminal)
System.err.println("Debug: Plugin loaded")

// Or write to files for persistent debugging
java.io.File("debug.txt").appendText("Event triggered\n")
```

### Example: Complete Shop Plugin

See [VarrockFoodShopPlugin.kt](game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/npcs/stores/VarrockFoodShopPlugin.kt) for a complete example of:
- Spawning NPCs
- Creating shops with dynamic pricing
- Handling NPC dialogs
- Using the Food enum

## Troubleshooting

### Server won't start
- Check that port 43594 and 8080 are not in use
- Verify Java 17+ is installed: `java -version`
- Check `data/cache/` contains OSRS cache files

### Can't connect from other devices
- Verify server is bound to 0.0.0.0 (default)
- Check Windows Firewall allows ports 43594 and 8080
- Verify `server-ip` in `game.yml` matches your LAN IP
- Test connectivity: `ping YOUR_SERVER_IP`

### RSProx can't load server
- Verify `jav_config_url` is accessible: `http://YOUR_IP:8080/jav_config.ws`
- Check modulus in `proxy-targets.yaml` matches `modulus` file
- Ensure world address in `world.json` matches server IP

## License

This project is licensed under the ISC License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built on [RsMod2](https://github.com/rsmod/rsmod) framework
- Uses [OpenRune-FileStore](https://github.com/OpenRune/OpenRune-FileStore) for cache management
- Route finder from [RsMod RouteFinder](https://github.com/rsmod/rsmod/tree/main/engine/routefinder)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Recent Updates (v1.1.6)

- Added new boss content: Dagannoth Kings, Vardorvis, and Warden
- Added minigame support: Last Man Standing and Wintertodt
- Updated combat configurations for various NPCs
- Enhanced plugin system with improved NPC and object handling
- Various bug fixes and improvements

## Support

For issues, questions, or contributions, please open an issue on GitHub.

## Releases

Check out the [Releases](https://github.com/hadefuwa/alter-rsps/releases) page for the latest version and changelog.

---

**Note**: This is a private server for educational purposes. Ensure you comply with Jagex's terms of service when using this software.

