# RuneLite Plugin Persistence Issues - Troubleshooting Guide

## Problem
RuneLite plugins don't persist their settings when you restart the client. Your plugin configurations are lost after closing and reopening RuneLite.

## Common Causes

1. **Configuration Directory Permissions**
   - RuneLite stores settings in `%USERPROFILE%\.runelite\`
   - If this directory doesn't have write permissions, settings won't save

2. **Corrupted settings.properties File**
   - The `settings.properties` file may be corrupted or locked
   - This prevents RuneLite from saving plugin configurations

3. **Profile-Related Issues**
   - **Duplicate RSProfiles**: Having duplicate RuneScape profiles can cause settings to not persist
   - **Profile Selection**: Using the wrong profile or switching profiles can cause settings to appear lost
   - **Profile Settings Corruption**: Individual profile settings files may be corrupted
   - Check for duplicates at: https://runelite.net/account/home

4. **Multiple RuneLite Installations**
   - Having multiple installations can cause conflicts
   - Different versions may use different configuration locations

5. **Third-Party Plugin Conflicts**
   - Plugins from the Plugin Hub may interfere with settings persistence
   - Some plugins may have bugs that prevent proper saving

6. **Antivirus/Firewall Interference**
   - Security software may block RuneLite from writing to its config directory
   - Windows Defender or other antivirus may quarantine config files

7. **Bootstrap.json Configuration (Private Servers)**
   - For rsprox/private server users, bootstrap.json configuration issues can affect plugin persistence
   - Invalid or corrupted bootstrap.json may cause RuneLite to use incorrect configuration paths

8. **RuneLite Running During Fix**
   - If RuneLite is running while trying to fix issues, files may be locked
   - Always close RuneLite before applying fixes

## Solutions

### Solution 1: Run the Diagnostic Script (Recommended)

1. **Close RuneLite completely** (check Task Manager)

2. **Run the full diagnostic script:**
   ```powershell
   .\fix-runelite-plugins.ps1
   ```

3. **Follow the recommendations** provided by the script

4. **Check for duplicate RSProfiles** at https://runelite.net/account/home and delete any duplicates

5. **Restart RuneLite** and verify plugins persist

### Solution 2: Quick Fix Script

1. **Right-click PowerShell** and select "Run as Administrator"

2. **Navigate to this directory** and run:
   ```powershell
   .\quick-fix-runelite.ps1
   ```

3. **Launch RuneLite** and test if settings persist

### Solution 3: Manual Fix

#### Step 1: Close RuneLite
- Make sure RuneLite is completely closed (check Task Manager)

#### Step 2: Fix Configuration Directory
1. Open File Explorer
2. Navigate to: `C:\Users\[YourUsername]\.runelite`
3. Right-click the `.runelite` folder → Properties → Security tab
4. Click "Edit" → Select your user → Check "Full control" → Apply

#### Step 3: Fix settings.properties
1. Navigate to: `C:\Users\[YourUsername]\.runelite\settings.properties`
2. Right-click → Properties → Uncheck "Read-only" if checked
3. If the file appears corrupted, rename it to `settings.properties.backup`
4. RuneLite will create a new file when you launch it

#### Step 4: Check for Multiple Installations
1. Search for "RuneLite" in Start Menu
2. Uninstall all but one installation
3. Keep only the latest version

#### Step 5: Add Windows Defender Exclusion
1. Open Windows Security → Virus & threat protection
2. Click "Manage settings" under Virus & threat protection settings
3. Scroll to "Exclusions" → Add or remove exclusions
4. Add folder: `C:\Users\[YourUsername]\.runelite`

### Solution 4: Check for Duplicate RSProfiles

Duplicate RuneScape profiles can cause plugin settings to not persist:

1. **Close RuneLite completely**
2. **Visit:** https://runelite.net/account/home
3. **Log in** with your RuneLite account
4. **Check for duplicate RSProfiles** - look for multiple profiles with the same name
5. **Delete any duplicate profiles** (keep only one)
6. **Restart RuneLite** and verify settings persist

### Solution 5: Verify Profile Selection

RuneLite uses profiles to store settings. Ensure you're using the correct profile:

1. **Launch RuneLite**
2. **Open Configuration** (wrench icon)
3. **Go to Profiles tab**
4. **Verify the correct profile is selected** (highlighted)
5. **If needed, create a new profile** and switch to it
6. **Configure your plugins** in the selected profile
7. **Restart RuneLite** to verify settings persist

### Solution 6: Safe Mode Test

To identify if a third-party plugin is causing the issue:

1. **Windows:** Use the "RuneLite (safe mode)" shortcut from Start Menu
2. **Or run:** `RuneLite.exe --safe-mode`
3. Configure plugins in safe mode
4. Restart and check if settings persist
5. If they do, enable plugins one by one to find the culprit

### Solution 7: Bootstrap.json Check (Private Server Users)

For rsprox/private server users, bootstrap.json issues can affect plugin persistence:

1. **Verify bootstrap.json exists** in your server directory
2. **Check bootstrap.json is valid JSON** (use a JSON validator)
3. **Ensure bootstrap.json is accessible** from RuneLite client
4. **Check server logs** for bootstrap.json serving errors
5. **Verify RuneLite is connecting to the correct server**

### Solution 8: Reset RuneLite Configuration

**WARNING:** This will delete all your RuneLite settings!

1. Close RuneLite completely
2. Navigate to: `C:\Users\[YourUsername]\.runelite`
3. **Backup the folder** (copy it somewhere safe)
4. Delete the entire `.runelite` folder
5. Launch RuneLite (it will create a new config)
6. Reconfigure your plugins
7. **Check for duplicate RSProfiles** after reset

## Verification

After applying fixes:

1. **Launch RuneLite**
2. **Enable/configure a plugin** (e.g., change a setting in Ground Items plugin)
3. **Close RuneLite completely**
4. **Reopen RuneLite**
5. **Check if the plugin setting persisted**

## Additional Resources

- **RuneLite GitHub Issues:** https://github.com/runelite/runelite/issues
- **RuneLite Discord:** https://discord.gg/runelite
- **RuneLite Wiki:** https://github.com/runelite/runelite/wiki

## Log Files

If issues persist, check RuneLite logs:
- Location: `C:\Users\[YourUsername]\.runelite\logs\`
- Look for errors related to "config", "settings", or "plugin"

## Still Having Issues?

1. Run the diagnostic script and save the output
2. Check RuneLite logs for errors
3. Try running RuneLite in safe mode
4. Post on RuneLite Discord or GitHub with:
   - Diagnostic script output
   - Relevant log entries
   - Steps to reproduce the issue

