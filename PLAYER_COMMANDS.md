# Player Commands Reference

Complete list of all available player commands in the game, organized by privilege level.

---

## **All Players** (No Privilege Required)

These commands are available to all players:

- `::home` - Teleports you home
- `::thieving` - Teleports you to the test thieving area
- `::yell <message>` - Yell to everyone (shows your rank)
- `::empty` - Empty your inventory
- `::slayer` - Check your slayer task progress
- `::resettask` - Reset your slayer task
- `::tabreset` - Reset all bank tabs (dumps items back to main tab)
- `::randbank` - Fill bank with random items (700 items)
- `::prayerbook` - (Currently empty implementation)
- `::getdist <x> <z>` - Get distance to coordinates
- `::qutest` - Check memory usage (test command)
- `::gc` - Garbage Collector - free up unused memory
- `::heap` - Creates heap dump
- `::getvarp <varp_id>` - Get varp state
- `::resetface` - Reset face pawn and interactions
- `::col_grid <player_name>` - Show collision grid around player

---

## **Admin Commands** (Requires `ADMIN_POWER`)

These commands require administrator privileges:

### Teleportation
- `::tele <x> <y> [height]` - Teleport to coordinates
- `::edge` - Teleport to Edgeville
- `::varrock` - Teleport to Varrock
- `::falador` - Teleport to Falador
- `::lumbridge` - Teleport to Lumbridge
- `::yanille` - Teleport to Yanille
- `::gnome` - Teleport to Gnome Stronghold

### Item & NPC Management
- `::item <item_id> [amount]` - Spawn items
- `::npc <npc_id>` - Spawn NPC
- `::spawn` - Spawn items with UI
- `::spawn2` - Spawn untradable items with UI
- `::food` - Fill inventory with manta rays

### Utility
- `::mypos` / `::coords` / `::pos` - Show your position coordinates
- `::openshop` / `::shop` / `::store <shop_id>` - Open shop
- `::broadcast <message>` - Broadcast message to everyone
- `::setrunenergy <amount>` - Set run energy
- `::transmog <npc_id>` - Transmog yourself into an NPC
- `::obank` - Open bank
- `::tournament` - Open tournament supplies interface
- `::img <image_id>` - Show chat images by id
- `::cmds` - Display commands list

---

## **Developer Commands** (Requires `DEV_POWER`)

These commands require developer privileges:

### Animation & Graphics
- `::anim <animation_id>` - Play animation
- `::gfx <graphic_id> [height]` - Play graphic
- `::chatanim <key> <npc_id>` - Chat dialogue test
- `::emotes` - Unlock all emotes

### Audio
- `::sound <sound_id>` - Play sound
- `::song <song_id>` - Play song
- `::jingle <jingle_id>` - Play jingle

### Inventory & Bank
- `::openbank` - Open bank
- `::emptybank` - Empty your bank
- `::inv <inventory_id> <item_ids...>` - Add items to inventory
- `::sets` - Open itemsets interface
- `::setamount <amount>` - Set amount of all items in bank

### Items & Entities
- `::getitems <keyword>` - Get items matching keyword to bank
- `::getitemstype <equip_slot>` - Get items by equipment slot type
- `::getitemlist` - (Empty implementation)
- `::find` / `::search <entity_type> <keyword>` - Search for items/NPCs/objects
  - Entity types: `item`/`i`, `npc`/`n`, `object`/`obj`/`o`

### NPCs & Objects
- `::obj <object_id> [type] [rotation]` - Spawn object
- `::removenpc` - Remove NPC under player
- `::removeobj` - Remove object under player
- `::aboutobj` - Show object information at your tile

### Skills & Stats
- `::master` - Set all skills to level 99
- `::reset` - Reset all skills to lowest level
- `::setlvl <skill> <level>` - Set skill level
  - Skill can be name or ID (supports shortcuts: `con`, `hp`, `craft`, `hunt`, `slay`, `pray`, `mage`, `fish`, `herb`, `rc`, `fm`)
- `::max` - Show max hit and accuracy for all combat styles

### Combat & Abilities
- `::infhp` - Toggle infinite HP
- `::infpray` - Toggle infinite prayer points
- `::infrunes` - Toggle infinite runes
- `::infrun` - Toggle infinite run energy
- `::hitme <hit_type> [damage]` - Test hitsplash
- `::spellbook <book_id>` - Switch spellbook (0-3)

### Movement & Collision
- `::noclip` - Toggle noclip (walk through walls)
- `::clip` - Show tile flags and collision info
- `::teler <region_id>` - Teleport to region
- `::invisible` - Toggle invisibility

### Interfaces & UI
- `::interface <interface_id>` - Open interface by id
- `::openinterface <component> <parent> <child> [clickable] [modal]` - Open interface with full parameters
- `::openurl <url>` - Open URL in browser

### Varbits & Varps
- `::varbit <varbit_id> <state>` - Set varbit state
- `::getvarbit <varbit_id>` - Get varbit state
- `::getvarbits <varp_id>` - Get all varbits for a varp
- `::logchanges` - Toggle logging of varbit/varp changes
- `::varp <varp_id> <state>` - Set varp state

### Server Management
- `::reboot <cycles>` - Restart server after cycles
- `::shutdown <cycles>` - Shutdown server after cycles
- `::reloaditems` - Reload all item definitions

### Scripting & Testing
- `::script <script_id> [args...]` - Run client script
- `::resetface` - Reset face pawn and interactions

---

## **Command Aliases**

Some commands have multiple aliases that perform the same function:

- `::mypos`, `::coords`, `::pos` - All show position
- `::openshop`, `::shop`, `::store` - All open shop
- `::find`, `::search` - Both search for entities

---

## **Usage Notes**

- **Case Insensitive**: All commands are case-insensitive (automatically converted to lowercase)
- **Privilege Levels**: 
  - No privilege required for basic player commands
  - `ADMIN_POWER` required for admin commands
  - `DEV_POWER` required for developer commands
- **Arguments**: 
  - Required arguments are shown in angle brackets: `<arg>`
  - Optional arguments are shown in square brackets: `[arg]`
- **Command Format**: All commands start with `::` (double colon)

---

## **Command Count Summary**

- **All Players**: 16 commands
- **Admin Commands**: 18 commands
- **Developer Commands**: 50+ commands
- **Total**: 100+ commands available

---

*Last updated: Generated from codebase analysis*

