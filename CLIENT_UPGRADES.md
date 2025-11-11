# Client Upgrades Guide

## Overview

This document explains how to modify the client cache to change interface component option text and other client-side elements. This is a permanent solution that requires modifying the cache files directly.

## Table of Contents

1. [Understanding Cache Modification](#understanding-cache-modification)
2. [How the Cache System Works](#how-the-cache-system-works)
3. [Prerequisites](#prerequisites)
4. [Cache Structure](#cache-structure)
5. [Modifying Interface Component Options](#modifying-interface-component-options)
6. [Step-by-Step Guide: Changing Home Teleport Button Option](#step-by-step-guide-changing-home-teleport-button-option)
7. [Repacking the Cache](#repacking-the-cache)
8. [Testing Your Changes](#testing-your-changes)
9. [Troubleshooting](#troubleshooting)
10. [Important Notes](#important-notes)

---

## Understanding Cache Modification

### What is Cache Modification?

The RuneScape client stores game data (items, NPCs, objects, interfaces, etc.) in a compressed archive format called the "cache". Interface component option text (like "Cast", "Animation", "Teleport Menu") is stored in these cache files and cannot be changed dynamically at runtime.

To change these option names, you must:
1. Extract the relevant data from the cache
2. Modify the option text
3. Repack the modified data back into the cache
4. Distribute the updated cache to players

### Why Modify the Cache?

- **Permanent Changes**: Changes persist across server restarts
- **Client-Side Customization**: Allows you to customize option names, tooltips, and other client-side text
- **Professional Appearance**: Makes your server look more polished and customized

### Limitations

- **Requires Cache Tools**: You need specialized tools to extract and repack cache data
- **Client Distribution**: Players must download the updated cache
- **Version Control**: Cache modifications can conflict with client updates
- **Complexity**: More complex than server-side code changes

---

## How the Cache System Works

### Cache Architecture

The cache is organized into **indices** (categories) and **archives** (groups of related files):

```
Cache
├── Index 0: Reference files
├── Index 1: Maps
├── Index 2: Configs (Items, NPCs, Objects, Interfaces, etc.)
├── Index 3: Models
├── Index 4: Animations
├── Index 5: Sounds
├── Index 6: Maps (2D)
├── Index 7: Music
└── Index 8: Client scripts (CS2)
```

### Definition System

In this codebase, cache definitions are handled through:

- **Decoders**: Read data from cache files (`plugins/filestore/src/main/kotlin/dev/openrune/cache/filestore/definition/decoder/`)
- **Encoders**: Write data back to cache files (`plugins/filestore/src/main/kotlin/dev/openrune/cache/filestore/definition/encoder/`)
- **Data Types**: Kotlin data classes representing cache structures (`plugins/filestore/src/main/kotlin/dev/openrune/cache/filestore/definition/data/`)

### Interface Definitions

Interface definitions are stored in **Index 2** (Configs). Each interface has:
- An interface ID (e.g., 218 for the spellbook)
- Components (sub-elements within the interface)
- Component options (right-click menu options)

**Note**: Interface definitions may be stored as:
- CS2 scripts (client scripts) in Index 8
- Binary interface definitions in Index 2
- Or a combination of both

---

## Prerequisites

### Required Tools

1. **Cache Editor/Viewer**
   - RS Cache Suite
   - Tom's Cache Suite
   - Or custom tools built with the filestore plugin

2. **Java/Kotlin Development Environment**
   - For building custom cache tools using the filestore plugin

3. **Backup of Original Cache**
   - Always backup your cache before making modifications

### Knowledge Required

- Basic understanding of cache structure
- Familiarity with binary data formats
- Understanding of interface IDs and component IDs
- Basic programming knowledge (for custom tools)

---

## Cache Structure

### Locating Cache Files

The cache is typically located in:
- **Windows**: `%USERPROFILE%\.runescape\cache\` or server data directory
- **Linux/Mac**: `~/.runescape/cache/` or server data directory

### Cache File Format

Cache files are stored as:
- **main_file_cache.dat**: Main cache data
- **main_file_cache.idx0-8**: Index files for each cache index

### Interface Storage

Interfaces are typically stored in:
- **Index 2**: Interface definitions (binary format)
- **Index 8**: CS2 scripts that control interface behavior

---

## Modifying Interface Component Options

### Understanding Interface Component Options

Each interface component can have up to 5 options:
- **Option 1**: Left-click (primary action)
- **Option 2**: Right-click option 1
- **Option 3**: Right-click option 2
- **Option 4**: Right-click option 3
- **Option 5**: Right-click option 4

### Option Text Storage

Option text is stored in the interface definition file. The format varies by client revision but typically includes:
- Component ID
- Option index (1-5)
- Option text string

### Finding Interface Definitions

To find interface 218 (spellbook) component 7 (home teleport button):

1. **Using Cache Tools**:
   - Open cache in RS Cache Suite or similar tool
   - Navigate to Index 2 → Interface definitions
   - Search for interface ID 218
   - Find component 7
   - Locate option 2 text ("Animation")

2. **Using Code**:
   - The filestore plugin can read cache data
   - You can write a tool to extract interface definitions
   - Search for interface ID 218, component 7, option 2

---

## Step-by-Step Guide: Changing Home Teleport Button Option

### Goal

Change the right-click option text for interface 218, component 7 from "Animation" to "Teleport Menu".

### Step 1: Backup Your Cache

```bash
# Create a backup directory
mkdir cache_backup
cp main_file_cache.dat cache_backup/
cp main_file_cache.idx* cache_backup/
```

### Step 2: Extract Interface Definition

**Option A: Using Cache Tools**

1. Open RS Cache Suite or similar tool
2. Load your cache file
3. Navigate to Index 2 → Interfaces
4. Find interface ID 218
5. Export the interface definition to a file

**Option B: Using Custom Tool**

Create a Kotlin tool using the filestore plugin:

```kotlin
import dev.openrune.cache.CacheManager
import java.nio.file.Paths

fun extractInterface() {
    val cachePath = Paths.get("path/to/cache")
    CacheManager.init(cachePath, 317) // Your cache revision
    
    // Interface definitions may be in Index 2 or Index 8
    // You'll need to find the correct archive/file for interface 218
    val interfaceData = CacheManager.cache.data(2, archiveId, fileId)
    
    // Parse and modify the interface definition
    // This requires understanding the binary format
}
```

### Step 3: Locate Component 7, Option 2

In the interface definition, find:
- **Component ID**: 7
- **Option Index**: 2 (second right-click option)
- **Current Text**: "Animation"

The exact format depends on your client revision. Common formats:

**Format 1 (Binary)**:
```
Component ID (2 bytes)
Option Count (1 byte)
For each option:
  Option Index (1 byte)
  Text Length (1 byte)
  Text (variable length)
```

**Format 2 (CS2 Script)**:
Interfaces may be defined in CS2 scripts. Look for scripts that set component options.

### Step 4: Modify the Option Text

Change "Animation" to "Teleport Menu":

1. Locate the text string "Animation" in the interface definition
2. Replace it with "Teleport Menu"
3. Update the string length if necessary (if length is stored separately)

**Important**: 
- Ensure the new text length matches the format requirements
- Some formats use null-terminated strings
- Some formats store length as a byte before the string

### Step 5: Update String Length

If the format stores string length separately:

```kotlin
val oldText = "Animation" // Length: 9
val newText = "Teleport Menu" // Length: 14

// Update the length byte/word before the string
// Format: [Length Byte][Text Bytes]
```

### Step 6: Repack the Cache

See [Repacking the Cache](#repacking-the-cache) section below.

---

## Repacking the Cache

### Using Cache Tools

1. **RS Cache Suite**:
   - After modifying the interface definition
   - Use "Repack" or "Save" function
   - This updates the cache files

2. **Tom's Cache Suite**:
   - Similar process
   - Save changes to update cache

### Using Custom Tool

Create a tool to write modified data back:

```kotlin
import dev.openrune.cache.CacheManager
import java.nio.file.Paths

fun repackCache() {
    val cachePath = Paths.get("path/to/cache")
    CacheManager.init(cachePath, 317)
    
    // Write modified interface data back
    val modifiedData = modifyInterfaceData(originalData)
    CacheManager.cache.write(2, archiveId, fileId, modifiedData)
    
    // Update cache version table
    CacheManager.cache.update()
    CacheManager.cache.close()
}
```

### Cache Update Process

1. Write modified data to cache using `cache.write()`
2. Call `cache.update()` to update version table
3. Close cache with `cache.close()`
4. Test the modified cache

---

## Testing Your Changes

### Step 1: Load Modified Cache

1. Place modified cache files in server data directory
2. Start server with modified cache
3. Connect with client

### Step 2: Verify Changes

1. Open spellbook (interface 218)
2. Right-click home teleport button (component 7)
3. Verify option 2 now says "Teleport Menu" instead of "Animation"
4. Test that clicking the option opens the teleport menu

### Step 3: Test Functionality

Ensure the server-side code still works:
- Left-click should teleport home instantly
- Right-click → "Teleport Menu" should open destination menu
- All teleport destinations should work correctly

---

## Troubleshooting

### Issue: Changes Not Appearing

**Possible Causes**:
- Cache not properly repacked
- Wrong interface/component ID
- Client using cached data (clear client cache)
- Wrong cache revision

**Solutions**:
- Verify cache was saved correctly
- Double-check interface and component IDs
- Clear client cache: Delete `%USERPROFILE%\.runescape\cache\`
- Ensure server and client use same cache revision

### Issue: Option Text Cut Off

**Cause**: String length not updated correctly

**Solution**: 
- Ensure length byte/word matches new text length
- Check if format uses null-terminated strings

### Issue: Cache Corruption

**Cause**: Incorrect modification or repacking

**Solution**:
- Restore from backup
- Verify modification format matches cache structure
- Check byte alignment and data types

### Issue: Client Crashes

**Cause**: Invalid data format or corrupted cache

**Solution**:
- Restore original cache
- Verify all modifications are correct
- Check for buffer overflows or underflows
- Ensure proper data alignment

---

## Important Notes

### Backup Strategy

- **Always backup** before modifying cache
- Keep multiple backup versions
- Test changes on a development server first

### Version Compatibility

- Cache modifications are revision-specific
- Changes may break with client updates
- Document all modifications for future updates

### Distribution

- Modified cache must be distributed to all players
- Consider using a cache downloader/updater
- Version check cache to ensure compatibility

### Legal Considerations

- Modifying cache files may violate terms of service for official clients
- For private servers, ensure you have rights to modify client files
- Document all modifications for compliance

### Best Practices

1. **Documentation**: Document all cache modifications
2. **Version Control**: Track cache versions
3. **Testing**: Test thoroughly before deploying
4. **Rollback Plan**: Have a plan to revert changes if needed
5. **Incremental Changes**: Make small, testable changes

### Alternative Approaches

If cache modification is too complex, consider:

1. **Server-Side Workaround**: Use existing option names (current approach)
2. **Client Scripts**: Some interfaces allow dynamic option text via CS2 scripts
3. **Custom Client**: Build a custom client with modified interface definitions
4. **Interface Replacement**: Replace the entire interface with a custom one

---

## Example: Complete Modification Workflow

### Scenario

Change home teleport button option 2 from "Animation" to "Teleport Menu".

### Workflow

```bash
# 1. Backup cache
cp cache/* cache_backup/

# 2. Extract interface 218 definition
# (Using cache tool or custom script)

# 3. Modify option text
# Change "Animation" → "Teleport Menu"

# 4. Repack cache
# (Using cache tool or custom script)

# 5. Test
# Start server, connect client, verify changes

# 6. Deploy
# Distribute modified cache to players
```

### Code Example: Custom Extraction Tool

```kotlin
package dev.openrune.cache.tools

import dev.openrune.cache.CacheManager
import java.nio.file.Paths

object InterfaceModifier {
    fun modifyHomeTeleportOption() {
        val cachePath = Paths.get("data/cache")
        CacheManager.init(cachePath, 317)
        
        // Find interface 218 archive/file
        // This requires reverse engineering the cache structure
        val interfaceId = 218
        val componentId = 7
        val optionIndex = 2
        
        // Extract interface definition
        // Modify option text
        // Repack cache
        
        println("Interface modification complete")
    }
}
```

---

## Additional Resources

### Cache Tools

- **RS Cache Suite**: Popular cache editor/viewer
- **Tom's Cache Suite**: Alternative cache tool
- **Custom Tools**: Build using filestore plugin

### Documentation

- Cache structure documentation (if available)
- Client revision-specific format docs
- Interface definition format specifications

### Community

- Private server development forums
- Cache modification tutorials
- Reverse engineering resources

---

## Conclusion

Cache modification allows permanent client-side customization but requires:
- Specialized tools and knowledge
- Careful testing and validation
- Proper backup and version management

For simpler solutions, consider server-side workarounds or client scripts. For permanent, professional customization, cache modification is the way to go.

**Remember**: Always backup, test thoroughly, and document your changes!

