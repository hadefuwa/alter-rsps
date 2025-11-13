# RSProx RuneLite Plugin Persistence - Profile-Specific Solution

## Important: Profile-Specific Scripts

I've created **RSProx-specific scripts** that ONLY affect your RSProx/Alter profile, leaving your other RuneLite installations completely untouched.

## RSProx-Specific Scripts (Use These!)

### Option 1: Quick Fix for RSProx Profile Only
```powershell
.\quick-fix-rsprox-profile.ps1
```
- ✅ Only fixes the RSProx/Alter profile
- ✅ Does NOT affect other RuneLite installations
- ✅ Does NOT affect other profiles
- ✅ Safe to use if you have multiple RuneLite setups

### Option 2: Full Diagnostic for RSProx Profile Only
```powershell
.\fix-rsprox-profile.ps1
```
- ✅ Only checks the RSProx/Alter profile
- ✅ Does NOT affect other RuneLite installations
- ✅ Provides detailed diagnostics for RSProx profile only

## General Scripts (Affects ALL Profiles)

The following scripts affect **ALL** RuneLite profiles and installations:

- `fix-runelite-plugins.ps1` - Affects ALL profiles
- `quick-fix-runelite.ps1` - Affects ALL profiles

**Only use these if you want to fix ALL RuneLite installations at once.**

## How RSProx Profile Detection Works

The RSProx-specific scripts look for profiles named:
- `Alter`
- `rsprox`
- `RSProx`
- `Private Server`

If your RSProx profile has a different name, you can edit the scripts and add your profile name to the `$rsproxProfileNames` array.

## Setting Up RSProx Profile

If you don't have an RSProx profile yet:

1. **Launch RuneLite**
2. **Go to Configuration** (wrench icon)
3. **Click on Profiles tab**
4. **Create a new profile** named `Alter` or `rsprox`
5. **Switch to that profile** when using RSProx
6. **Configure your plugins** in that profile

## Why Use Profile-Specific Scripts?

RuneLite uses profiles to separate configurations. By using a dedicated profile for RSProx:

- ✅ Your regular RuneLite settings stay untouched
- ✅ RSProx settings are isolated
- ✅ You can switch between profiles easily
- ✅ No conflicts between different RuneLite setups

## Quick Start for RSProx Users

1. **Create RSProx profile** in RuneLite (if you haven't already)
2. **Run the RSProx-specific quick fix:**
   ```powershell
   .\quick-fix-rsprox-profile.ps1
   ```
3. **Launch RuneLite** and switch to your RSProx profile
4. **Configure plugins** in that profile
5. **Restart RuneLite** and verify settings persist

## Files Created

- `quick-fix-rsprox-profile.ps1` - RSProx profile quick fix (profile-specific)
- `fix-rsprox-profile.ps1` - RSProx profile diagnostic (profile-specific)

## Files Modified (Now Have Warnings)

- `fix-runelite-plugins.ps1` - Now warns it affects ALL profiles
- `quick-fix-runelite.ps1` - Now warns it affects ALL profiles

## Summary

✅ **Use RSProx-specific scripts** (`quick-fix-rsprox-profile.ps1` or `fix-rsprox-profile.ps1`) to only affect your RSProx client

❌ **Avoid general scripts** (`fix-runelite-plugins.ps1` or `quick-fix-runelite.ps1`) unless you want to fix ALL RuneLite installations

Your other RuneLite installations are now safe! 🎉


