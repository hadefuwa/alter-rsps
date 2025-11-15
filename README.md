# Alter RSPS

A modern Old School RuneScape Private Server (RSPS) built with Kotlin, featuring a comprehensive game engine, plugin system, and REST API for client connectivity.

[![Revision](https://img.shields.io/badge/Revision-228-blueviolet)](https://oldschool.runescape.wiki/w/Update:Leagues_V:_Raging_Echos_Rewards_Are_Here)
[![License](https://img.shields.io/badge/license-ISC-informational)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-17-blue)](https://adoptium.net/)

## Features

- **Full OSRS Revision 228 Support** - Complete game content and mechanics
- **Plugin-Based Architecture** - Easy to extend with custom content
- **REST API** - HTTP endpoints for client configuration (`jav_config.ws`, `world_list.ws`, `bootstrap.json`)
- **LAN Support** - Server binds to all interfaces (0.0.0.0) for network access
- **RSProx Compatible** - Works seamlessly with RSProx client
- **Player Saving** - Persistent player data and positions
- **Comprehensive Content** - Wilderness monsters, NPCs, objects, items, and more

## Requirements

- **Java Development Kit (JDK) 17** or higher
- **Gradle** (included via wrapper)
- **OSRS Cache** - Place in `data/cache/` directory
- **RSA Key** - Auto-generated on first run if missing

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/hadefuwa/alter-rsps.git
cd alter-rsps/Alter
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
Alter/
├── game-server/          # Core game engine
├── game-plugins/         # Game content plugins
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

### Adding Custom Content

Content is added via plugins in `game-plugins/src/main/kotlin/org/alter/plugins/content/`. See existing plugins for examples.

### Debugging

Enable debug logging in `dev-settings.yml`:

```yaml
debug-examines: true
debug-objects: true
debug-packets: true
```

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

## Support

For issues, questions, or contributions, please open an issue on GitHub.

---

**Note**: This is a private server for educational purposes. Ensure you comply with Jagex's terms of service when using this software.

