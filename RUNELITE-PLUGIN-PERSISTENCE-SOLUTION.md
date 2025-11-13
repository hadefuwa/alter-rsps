# RuneLite Plugin Persistence - Complete Solution

## Overview

This document provides a comprehensive solution for RuneLite plugins not persisting their settings. The solution includes enhanced diagnostic scripts and troubleshooting guides specifically designed for rsprox/private server users.

## Quick Start

### Option 1: Quick Fix (Recommended First Step)
```powershell
.\quick-fix-runelite.ps1
```

### Option 2: Full Diagnostic
```powershell
.\fix-runelite-plugins.ps1
```

## What Was Fixed

### Enhanced Diagnostic Script (`fix-runelite-plugins.ps1`)

The diagnostic script now includes:

1. **Profile Detection** - Checks for RuneLite profiles and their settings files
2. **Bootstrap.json Validation** - Validates bootstrap.json for private server configurations
3. **Enhanced Error Detection** - Better detection of profile-related issues
4. **RSProfiles Duplicate Detection** - Instructions for checking duplicate profiles online

### Enhanced Quick Fix Script (`quick-fix-runelite.ps1`)

The quick fix script now includes:

1. **Profile Permission Fixes** - Automatically fixes permissions for all profiles
2. **Profile Settings Repair** - Removes read-only attributes from profile settings files
3. **Bootstrap.json Awareness** - Provides guidance for private server users

### Updated Troubleshooting Guide (`RUNELITE-TROUBLESHOOTING.md`)

Added new sections covering:

1. **Profile-Related Issues** - Common profile problems and solutions
2. **Duplicate RSProfiles** - How to detect and fix duplicate profiles
3. **Bootstrap.json Configuration** - Private server-specific troubleshooting
4. **Profile Selection Verification** - How to ensure correct profile is active

## Common Causes (Updated)

1. Configuration Directory Permissions
2. Corrupted settings.properties File
3. **Profile-Related Issues** (NEW)
   - Duplicate RSProfiles
   - Profile Selection Issues
   - Profile Settings Corruption
4. Multiple RuneLite Installations
5. Third-Party Plugin Conflicts
6. Antivirus/Firewall Interference
7. **Bootstrap.json Configuration** (NEW - for private servers)
8. RuneLite Running During Fix

## Key Solutions

### 1. Check for Duplicate RSProfiles

This is a common cause of plugin persistence issues:

1. Close RuneLite completely
2. Visit: https://runelite.net/account/home
3. Log in with your RuneLite account
4. Check for duplicate RSProfiles
5. Delete any duplicates (keep only one)
6. Restart RuneLite

### 2. Verify Profile Selection

Ensure you're using the correct profile:

1. Launch RuneLite
2. Open Configuration (wrench icon)
3. Go to Profiles tab
4. Verify the correct profile is selected
5. Configure plugins in the selected profile
6. Restart RuneLite

### 3. Bootstrap.json Check (Private Server Users)

For rsprox/private server users:

1. Verify bootstrap.json exists and is valid JSON
2. Ensure bootstrap.json is accessible from RuneLite client
3. Check server logs for bootstrap.json serving errors
4. Verify RuneLite is connecting to the correct server

## Files Modified

1. `fix-runelite-plugins.ps1` - Enhanced with profile and bootstrap checks
2. `quick-fix-runelite.ps1` - Enhanced with profile permission fixes
3. `RUNELITE-TROUBLESHOOTING.md` - Updated with new solutions

## Testing

The scripts have been verified for:
- ✅ Syntax correctness
- ✅ Function definitions
- ✅ Error handling
- ✅ Profile detection logic
- ✅ Bootstrap.json validation

## Next Steps

1. **Run the quick fix script** first to apply automatic fixes
2. **Check for duplicate RSProfiles** at https://runelite.net/account/home
3. **Verify profile selection** in RuneLite (Configuration > Profiles)
4. **Run the full diagnostic** if issues persist
5. **Check bootstrap.json** if using a private server

## Support

If issues persist after following all solutions:

1. Run the diagnostic script and save the output
2. Check RuneLite logs at: `%USERPROFILE%\.runelite\logs`
3. Try running RuneLite in safe mode
4. Post on RuneLite Discord or GitHub with:
   - Diagnostic script output
   - Relevant log entries
   - Steps to reproduce the issue

## Additional Resources

- **RuneLite GitHub Issues:** https://github.com/runelite/runelite/issues
- **RuneLite Discord:** https://discord.gg/runelite
- **RuneLite Wiki:** https://github.com/runelite/runelite/wiki
- **RSProfiles Management:** https://runelite.net/account/home


