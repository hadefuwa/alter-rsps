# Client Editing Guide - Getting Started with Small Edits

## Overview

This guide will help you get started with editing client files for the Alter RSPS project. The client is based on **RuneLite**, which is an open-source Old School RuneScape client. This guide focuses on making small, incremental edits to help you learn the process safely.

## Quick Start - Which File to Edit?

**TL;DR**: Always edit **`libs/custom-runelite-client.jar`** - this is your working file.

- ✅ **Edit this**: `libs/custom-runelite-client.jar` - Your editable client file
- 📦 **Keep as backup**: `libs/runelite.jar` - Reference/backup copy (don't edit this!)

### ⚠️ CRITICAL: The Workflow

**Editing `libs/custom-runelite-client.jar` alone won't change your running client!**

RSProx uses its own client files from `C:\Users\Hamed\.rsprox\runelite\`. After editing, you **must copy your modified JAR back to RSProx**:

**Manual Steps:**
1. Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
2. Copy `custom-runelite-client.jar`
3. Go to: `C:\Users\Hamed\.rsprox\runelite\`
4. Find `latest-runelite-43602.jar` (or latest version) and delete it
5. Paste `custom-runelite-client.jar` into the folder
6. Rename the pasted file from `custom-runelite-client.jar` to `latest-runelite-43602.jar` (match the exact filename RSProx uses)

**Why use `libs/` folder?** It keeps your project organized, makes backups easier, and lets you track changes with git. But remember: **copy to RSProx to actually use your changes!**

If the `libs/` directory doesn't exist or is empty, see the "Getting the Client Files" section below to set it up.

## 🧪 Simple Test: Change Window Title (5-Minute Proof)

Want to verify you're editing the right file? Here's the **simplest possible change** - modify the client window title:

### Step 1: Backup
```powershell
Copy-Item libs\custom-runelite-client.jar libs\custom-runelite-client.jar.backup
```

### Step 2: Open in JD-GUI
1. Download JD-GUI: https://java-decompiler.github.io/ (or use IntelliJ IDEA)
2. File → Open → Select `libs/custom-runelite-client.jar`
3. Wait for it to load (may take a minute)

### Step 3: Find the Window Title
1. Press **Ctrl+F** to search
2. Search for: **`"RuneLite"`** (with quotes)
3. Look for results in classes like:
   - `net.runelite.client.ui.ClientUI`
   - `net.runelite.client.RuneLite`
   - Or any class with "title" or "window" in the name

### Step 4: Export and Edit
1. Right-click the class containing "RuneLite" → **Save Source**
2. Save to a folder (e.g., `client-sources/`)
3. Open the `.java` file in a text editor
4. Find the line with `"RuneLite"` and change it to something obvious:
   ```java
   // Before
   frame.setTitle("RuneLite");
   
   // After (make it OBVIOUS so you can see it worked!)
   frame.setTitle("ALTER RSPS TEST - EDITED!");
   ```

### Step 5: Compile
```powershell
# Navigate to where you saved the source
cd client-sources

# Compile (adjust classpath if needed)
javac -cp "..\libs\custom-runelite-client.jar" YourClassName.java
```

### Step 6: Update the JAR
```powershell
# Update the JAR with your compiled class
jar uf ..\libs\custom-runelite-client.jar YourClassName.class
```

### Step 7: Copy to RSProx and Test!
1. **Copy your edited JAR to RSProx** (replace the existing file):
   - Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
   - Copy `custom-runelite-client.jar`
   - Go to: `C:\Users\Hamed\.rsprox\runelite\`
   - Find `latest-runelite-43602.jar` (or whatever the latest version is) and delete it
   - Paste `custom-runelite-client.jar` into the folder
   - Rename the pasted file from `custom-runelite-client.jar` to `latest-runelite-43602.jar` (must match the exact filename)
   
   **Important**: RSProx looks for a specific filename like `latest-runelite-43602.jar`. You must rename your file to match exactly.

2. **Launch your client through RSProx**
3. **Look at the window title bar** - it should now say "ALTER RSPS TEST - EDITED!" instead of "RuneLite"
4. ✅ **If you see your change, you're editing the right file!**

**If it doesn't work:**
- Make sure you edited `custom-runelite-client.jar`, not `runelite.jar`
- Check that you compiled and updated the JAR correctly
- Try searching for other strings like `"RuneLite"` or `setTitle` in different classes

### Alternative: Quick Hex Edit (Even Simpler, But Less "Proper")

For the absolute fastest test without decompiling:

1. **Download HxD** (free hex editor): https://mh-nexus.com/en/hxd/
2. **Open** `libs/custom-runelite-client.jar` in HxD
3. **Search for the window title** (Ctrl+F):
   - **Important**: Don't search for just "RuneLite" - that appears thousands of times as package paths!
   - Instead, search for: **`"RuneLite"`** (with quotes) - this finds the actual title string
   - Or search for: **`setTitle`** to find where the title is set
   - Or search for: **`RuneLite`** but look for results that are NOT part of `net/runelite/client/` paths
4. **Find the right match**: Look for a standalone "RuneLite" string (not in a file path)
5. **Replace** it with: `ALTER TEST` (must be same length or shorter - "RuneLite" is 8 chars, "ALTER TEST" is 10, so use `ALTER TE` or pad with spaces)
6. **Save** the file
6. **⚠️ Copy to RSProx** (replace the existing file):
   - Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
   - Copy `custom-runelite-client.jar`
   - Go to: `C:\Users\Hamed\.rsprox\runelite\`
   - Find `latest-runelite-43602.jar` (or latest version) and delete it
   - Paste `custom-runelite-client.jar` into the folder
   - Rename the pasted file from `custom-runelite-client.jar` to `latest-runelite-43602.jar` (must match exactly)
7. **Launch client through RSProx** - window title should show your change!

**Note**: Hex editing is quick but fragile. For proper, repeatable changes, see "Proper Workflow for Multiple Changes" below.

## Proper Workflow for Multiple Changes

For making many changes over time with your existing RSProx client, here are your options:

### Option 1: String Replacement Script (Simplest - For String Changes)

**Best for:** Simple text/string changes (window titles, messages, etc.)

Use the provided Python script to replace strings programmatically (no hex editor needed):

```powershell
# Make sure you have Python installed, then:
python scripts/modify_jar_string.py libs/custom-runelite-client.jar "Alter" "PANDA SERVER"
```

**Requirements:** 
- Python 3 installed
- Strings must be the same length (script will tell you if they're not)

**What it does:**
- Creates a backup automatically
- Searches through the JAR file
- Replaces the string in all text files and class files
- Updates the JAR file

**Pros:** Programmatic, repeatable, no manual editing, creates backups automatically  
**Cons:** Limited to string replacements (same length), can't change code logic

### Option 2: Decompile → Edit → Recompile (For Code Changes)

**Best for:** Logic changes, method modifications

**The Problem:** As we saw, decompiled code has syntax errors and missing dependencies.

**The Solution:** Fix the decompilation issues systematically:

1. **Export all sources** from JD-GUI (you already did this to `decompiled-client/`)
2. **Fix syntax errors** in the decompiled code (like we started doing)
3. **Set up proper classpath** with all dependencies from the JAR
4. **Compile with all dependencies**:
   ```powershell
   # Extract all dependencies from JAR first
   jar xf libs\custom-runelite-client.jar
   
   # Compile with full classpath
   javac -cp "libs\custom-runelite-client.jar;extracted-deps/*" YourClass.java
   ```
5. **Update JAR** with compiled class

**This is the workflow you're already using** - we just need to fix the compilation issues properly.

### Option 3: Build from RuneLite Source (If You Want Full Control)

**Only if:** You want to make extensive changes and have full source control

This requires cloning RuneLite and building from scratch. You don't need this if you're just editing the existing client.

### Recommendation for Your Use Case

Since you're using an existing RSProx client and want to make edits:

1. **For simple changes (strings):** Use hex editor - it's fast and works
2. **For code changes:** Continue with decompile/edit/recompile, but:
   - Fix syntax errors systematically
   - Extract all dependencies from the JAR
   - Compile with proper classpath
   - This is what we were working on - we just hit compilation issues that need fixing

**You don't need to download RuneLite source** - you can work with the JAR you already have!

## Understanding Client Files

### What Are Client Files?

The client files in this project are located in the `libs/` directory:
- **`libs/runelite.jar`** - The base RuneLite client (backup/reference copy)
- **`libs/custom-runelite-client.jar`** - **The file you edit** - A customized version of the RuneLite client

**Important**: Always edit `libs/custom-runelite-client.jar`, not `runelite.jar`. The `runelite.jar` file should be kept as a backup/reference.

These are **compiled Java/Kotlin applications** packaged as JAR (Java Archive) files. They contain:
- Client-side game logic
- UI components and interfaces
- Rendering code
- Network communication code
- Client scripts and configurations

### Getting the Client Files

If you're using **RSProx** (RuneScape Proxy), the client files are located in:
- `C:\Users\Hamed\.rsprox\runelite\` (Windows)

### ⚠️ IMPORTANT: Understanding the Workflow

**The `libs/` folder is just a workspace for editing - RSProx doesn't automatically use it!**

Here's how it works:

1. **RSProx uses its own client files** from `C:\Users\Hamed\.rsprox\runelite\latest-runelite-43602.jar` (or whatever the latest version is)
2. **You edit files in `libs/`** to keep your project organized
3. **After editing, you must copy the modified JAR back to RSProx** to actually use your changes

### Setup Steps (Manual)

1. **Create the `libs` folder** in your project:
   - Navigate to: `C:\Users\Hamed\Documents\alter-rsps\`
   - Create a new folder called `libs`

2. **Copy the latest JAR from RSProx**:
   - Go to: `C:\Users\Hamed\.rsprox\runelite\`
   - Find the latest file (e.g., `latest-runelite-43602.jar`)
   - Copy it
   - Paste it into `C:\Users\Hamed\Documents\alter-rsps\libs\`
   - Rename it to `runelite.jar` (this is your backup)
   - Copy it again in the same folder
   - Rename the copy to `custom-runelite-client.jar` (this is what you'll edit)

**You should now have:**
- `libs\runelite.jar` (backup/reference)
- `libs\custom-runelite-client.jar` (your editable file)

### Client vs Server

**Important distinction:**
- **Server files** (in `game-server/`, `game-plugins/`) - Handle game logic, NPCs, items, combat, etc.
- **Client files** (JAR files) - Handle what the player sees, UI rendering, input handling, etc.

## Prerequisites

Before you start editing client files, you'll need:

1. **Java Development Kit (JDK) 17+** - Already required for the server
2. **Java Decompiler** - To view/edit the source code from JAR files
3. **Java IDE** - IntelliJ IDEA (recommended) or Eclipse
4. **Build Tools** - Gradle (already included in project)
5. **Git** - For version control (recommended)

## Approach 1: Decompile and Edit JAR Files (Recommended for Small Edits)

This approach is best for making small, targeted changes to the existing client.

### Step 1: Set Up Your Workspace

1. **Ensure the libs directory exists** (see "Getting the Client Files" section above if needed)

2. **Create a backup of your client JAR file** (always backup before editing!):
   ```powershell
   # In PowerShell (Windows)
   # Backup the file you'll be editing
   Copy-Item libs\custom-runelite-client.jar libs\custom-runelite-client.jar.backup
   
   # Optional: Also backup the base reference file
   Copy-Item libs\runelite.jar libs\runelite.jar.backup
   ```

3. **Create a new directory for client development** (optional, for organizing decompiled sources):
   ```powershell
   # PowerShell (Windows)
   New-Item -ItemType Directory -Path "client-development" -Force
   cd client-development
   ```

### Step 2: Decompile the JAR File

You have several options for decompiling:

#### Option A: Using JD-GUI (Easiest for Beginners)

1. **Download JD-GUI**: https://java-decompiler.github.io/
2. **Open the JAR file**:
   - Launch JD-GUI
   - File → Open → Select `libs/custom-runelite-client.jar`
   - Wait for it to load (may take a minute for large JARs)
   - Browse the decompiled code
3. **Export ALL source code** (recommended - makes searching much easier!):
   - File → **Save All Sources** (or press Ctrl+Alt+S)
   - Choose a folder to save to (e.g., `C:\Users\Hamed\Documents\alter-rsps\client-sources\`)
   - Click "Save"
   - Wait for export to complete (this may take a few minutes - the JAR has thousands of classes)
   - You'll now have all the Java source files in folders matching the package structure
   
   **After exporting, you can:**
   - Search through all files using your text editor (Ctrl+Shift+F in most editors)
   - Easily find `ClientUI.java` in `client-sources\net\runelite\client\ui\ClientUI.java`
   - Edit files directly in your IDE

#### Option B: Using IntelliJ IDEA (Recommended)

1. **Open IntelliJ IDEA**
2. **Open the JAR as a library**:
   - File → Project Structure → Libraries
   - Click `+` → Java → Select `libs/custom-runelite-client.jar`
3. **Decompile on-the-fly**:
   - IntelliJ can decompile classes when you navigate to them
   - Right-click on a class → Decompile

#### Option C: Using Fernflower (Command Line)

```bash
# Download Fernflower (included with IntelliJ IDEA)
# Or use online: https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine

java -jar fernflower.jar libs/custom-runelite-client.jar client-sources/
```

### Step 3: Make Small Edits

Start with simple, non-critical changes:

#### Example 1: Change a Text String

1. **Find a simple text message** in the decompiled code
2. **Edit the string**:
   ```java
   // Before
   String message = "Welcome to the game!";
   
   // After
   String message = "Welcome to Alter RSPS!";
   ```
3. **Save the file**

#### Example 2: Change a Color Value

1. **Find UI color definitions**:
   ```java
   // Look for color constants
   Color backgroundColor = new Color(0, 0, 0); // Black
   ```
2. **Modify the color**:
   ```java
   Color backgroundColor = new Color(50, 50, 50); // Dark gray
   ```

#### Example 3: Modify a Simple Configuration

1. **Find configuration values**:
   ```java
   private static final int MAX_FPS = 50;
   ```
2. **Change the value**:
   ```java
   private static final int MAX_FPS = 60;
   ```

### Step 4: Recompile the Modified Code

1. **Set up a Java project**:
   ```bash
   mkdir client-project
   cd client-project
   # Copy your edited source files here
   ```

2. **Create a build file** (`build.gradle` or use Maven):
   ```gradle
   plugins {
       id 'java'
   }
   
   sourceCompatibility = '17'
   targetCompatibility = '17'
   
   repositories {
       mavenCentral()
   }
   
   dependencies {
       // Add dependencies from the original JAR
       // You may need to extract these from the JAR's META-INF
   }
   ```

3. **Compile your changes**:
   ```bash
   ./gradlew build
   # Or
   javac -cp "original-jar.jar:other-dependencies/*" YourEditedClass.java
   ```

4. **Package into JAR**:
   ```bash
   jar cvf modified-client.jar -C compiled-classes/ .
   ```

### Step 5: Test Your Changes

1. **Replace the original JAR** (keep backup!):
   ```powershell
   Copy-Item modified-client.jar libs/custom-runelite-client.jar -Force
   ```

2. **⚠️ IMPORTANT: Copy to RSProx to actually use your changes!**
   
   **Manual Steps:**
   - Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
   - Copy `custom-runelite-client.jar`
   - Go to: `C:\Users\Hamed\.rsprox\runelite\`
   - Find `latest-runelite-43602.jar` (or latest version) and delete it
   - Paste `custom-runelite-client.jar` into the folder
   - Rename the pasted file from `custom-runelite-client.jar` to `latest-runelite-43602.jar` (must match the exact filename RSProx uses)
   
   **What this does**: Replaces `latest-runelite-43602.jar` with your edited version. RSProx will use it because it has the same filename.
   
   **What this does**: Takes your edited `custom-runelite-client.jar` and **replaces** `latest-runelite-43602.jar` with it. RSProx will use the replaced file because it has the same filename it's looking for.

3. **Start the server**:
   ```powershell
   ./gradlew run
   ```

4. **Launch the client through RSProx** and test your changes

5. **If something breaks**, restore from backup:
   ```powershell
   # Restore libs folder
   Copy-Item libs\custom-runelite-client.jar.backup libs\custom-runelite-client.jar -Force
   
   # Restore RSProx (copy from backup or re-download)
   $rsproxJar = Get-ChildItem "$env:USERPROFILE\.rsprox\runelite\latest-runelite-*.jar" | Sort-Object Name -Descending | Select-Object -First 1
   Copy-Item libs\runelite.jar $rsproxJar.FullName -Force
   ```

## Approach 2: Build from RuneLite Source (Advanced)

For more extensive changes, you might want to work with the RuneLite source code directly.

### Step 1: Clone RuneLite Repository

```bash
git clone https://github.com/runelite/runelite.git
cd runelite
```

### Step 2: Build the Client

```bash
./gradlew build
```

### Step 3: Make Your Customizations

1. **Create a custom plugin** or modify existing code
2. **Build your custom version**:
   ```bash
   ./gradlew :client:build
   ```

### Step 4: Integrate with Your Server

1. **Configure the client** to connect to your server
2. **Update bootstrap.json** if needed
3. **Test the connection**

## Recommended Tools

### Decompilers
- **JD-GUI** - Simple GUI decompiler (https://java-decompiler.github.io/)
- **IntelliJ IDEA** - Built-in decompiler (recommended)
- **Fernflower** - Command-line decompiler
- **CFR** - Another good decompiler option

### Hex Editors (for binary edits)
- **HxD** (Windows) - Free hex editor
- **Hex Fiend** (Mac) - Free hex editor
- **010 Editor** - Advanced hex editor (paid)

### Build Tools
- **Gradle** - Already in your project
- **Maven** - Alternative build tool
- **javac** - Java compiler (command line)

## Best Practices for Small Edits

### 1. Start Small
- Begin with cosmetic changes (text, colors, UI elements)
- Avoid modifying core game logic initially
- Test each change before moving to the next

### 2. Always Backup
```powershell
# Before any edit - ALWAYS backup the file you're editing
Copy-Item libs\custom-runelite-client.jar libs\custom-runelite-client.jar.backup

# Tip: Create timestamped backups for multiple edit sessions
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
Copy-Item libs\custom-runelite-client.jar "libs\custom-runelite-client.jar.backup-$timestamp"
```

### 3. Use Version Control
```bash
git init
git add libs/custom-runelite-client.jar.backup
git commit -m "Backup before editing"
```

### 4. Test Incrementally
- Make one small change at a time
- Test immediately after each change
- Document what you changed

### 5. Keep Notes
Create a file `CLIENT_CHANGES.md`:
```markdown
## Client Edit Log

### 2024-01-XX - Changed Welcome Message
- File: `WelcomeScreen.java`
- Change: Modified welcome text
- Result: ✅ Working
```

## Common Small Edits You Can Try

### 1. Change Client Title
- **Location**: Main window title
- **Difficulty**: Easy
- **Impact**: Cosmetic only

### 2. Modify UI Colors
- **Location**: Theme/color constants
- **Difficulty**: Easy
- **Impact**: Visual only

### 3. Change Font Sizes
- **Location**: UI rendering code
- **Difficulty**: Easy-Medium
- **Impact**: Visual only

### 4. Adjust Window Sizes
- **Location**: Window initialization
- **Difficulty**: Easy
- **Impact**: Visual only

### 5. Modify Startup Messages
- **Location**: Login/startup screens
- **Difficulty**: Easy
- **Impact**: Cosmetic only

### 6. Change Icon/Logo
- **Location**: Resource files
- **Difficulty**: Medium
- **Impact**: Visual only

## Troubleshooting

### Problem: libs Directory or JAR Files Don't Exist
**Solution**: 
1. Follow the "Getting the Client Files" section above
2. Copy the latest JAR from your RSProx directory (`C:\Users\<YourUsername>\.rsprox\runelite\`)
3. Or download from the RuneLite repository URL in `config/bootstrap.json`

### Problem: Which File Should I Edit?
**Solution**:
- **Always edit**: `libs/custom-runelite-client.jar`
- **Keep as backup/reference**: `libs/runelite.jar`
- Never edit `runelite.jar` directly - it's your safety net!

### Problem: Client Won't Start After Edit
**Solution**: 
1. Restore from backup: `Copy-Item libs\custom-runelite-client.jar.backup libs\custom-runelite-client.jar -Force`
2. Check for compilation errors
3. Verify all dependencies are included
4. Make sure you edited `custom-runelite-client.jar`, not `runelite.jar`

### Problem: Can't Find the Code to Edit
**Solution**:
1. Use a decompiler with search functionality (JD-GUI or IntelliJ)
2. Look for string literals that match what you see in-game
3. Check the RuneLite GitHub repository for source code reference
4. Make sure you're opening `custom-runelite-client.jar`, not `runelite.jar`

### Problem: Changes Don't Appear
**Solution**:
1. Ensure you recompiled correctly
2. Check that you're using the modified `custom-runelite-client.jar`
3. Clear client cache if applicable (RSProx cache: `C:\Users\<YourUsername>\.rsprox\caches\`)
4. Verify you replaced the correct JAR file

### Problem: Missing Dependencies
**Solution**:
1. Extract dependencies from original JAR
2. Check `META-INF/MANIFEST.MF` for classpath
3. Include all required libraries when compiling
4. RSProx dependencies are in `C:\Users\<YourUsername>\.rsprox\launcher\repository\`

## Safety Tips

1. **Never edit the client while the server is running**
2. **Always test in a development environment first**
3. **Keep multiple backups** at different stages
4. **Document all changes** you make
5. **Start with cosmetic changes** before touching game logic
6. **Test thoroughly** before deploying to production

## Next Steps

Once you're comfortable with small edits:

1. **Learn Java/Kotlin** - Understanding the language helps significantly
2. **Study RuneLite Architecture** - Read the RuneLite documentation
3. **Join Communities** - RuneLite Discord, RSPS forums
4. **Experiment Gradually** - Move from cosmetic to functional changes
5. **Contribute** - Share your improvements with the community

## Resources

- **RuneLite GitHub**: https://github.com/runelite/runelite
- **RuneLite Wiki**: https://github.com/runelite/runelite/wiki
- **Java Decompiler Tools**: https://java-decompiler.github.io/
- **JD-GUI Download**: https://github.com/java-decompiler/jd-gui/releases
- **IntelliJ IDEA**: https://www.jetbrains.com/idea/

## Example: Your First Edit

Let's walk through changing a simple text message:

1. **Open the JAR in JD-GUI**:
   - Launch JD-GUI
   - File → Open → `libs/custom-runelite-client.jar`

2. **Search for text**:
   - Use the search function (Ctrl+F)
   - Search for a visible string like "Loading..."

3. **Export the source**:
   - Right-click the class → Save Source
   - Save to `client-sources/`

4. **Edit the file**:
   ```java
   // Find this line
   label.setText("Loading...");
   
   // Change to
   label.setText("Loading Alter RSPS...");
   ```

5. **Compile**:
   ```bash
   javac -cp "libs/custom-runelite-client.jar" YourClass.java
   ```

6. **Update JAR**:
   ```bash
   jar uf libs/custom-runelite-client.jar YourClass.class
   ```

7. **Test**:
   - Start server
   - Launch client
   - Verify the change appears

## Quick Reference: Common Commands

### Setup libs Directory (First Time)
1. Create folder: `C:\Users\Hamed\Documents\alter-rsps\libs\`
2. Go to: `C:\Users\Hamed\.rsprox\runelite\`
3. Copy the latest `latest-runelite-XXXXX.jar` file
4. Paste into `libs\` folder
5. Rename to `runelite.jar` (backup)
6. Copy it again in the same folder
7. Rename the copy to `custom-runelite-client.jar` (for editing)

### Backup Client
**Always backup before editing!**
- Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
- Copy `custom-runelite-client.jar`
- Paste it in the same folder
- Rename the copy to `custom-runelite-client.jar.backup`

### Decompile with JD-GUI
1. Download JD-GUI
2. Open JAR file
3. File → Save All Sources

### Compile Single Class
```bash
javac -cp "libs/custom-runelite-client.jar:other-deps/*" YourClass.java
```

### Update JAR with Modified Class
```powershell
jar uf libs/custom-runelite-client.jar YourClass.class
```

### Copy Edited JAR to RSProx (After Editing)
**This is REQUIRED - RSProx won't see your changes until you replace its JAR file!**

1. Go to: `C:\Users\Hamed\Documents\alter-rsps\libs\`
2. Copy `custom-runelite-client.jar`
3. Go to: `C:\Users\Hamed\.rsprox\runelite\`
4. Find `latest-runelite-43602.jar` (or whatever the latest version number is) and delete it
5. Paste `custom-runelite-client.jar` into the folder
6. Rename the pasted file from `custom-runelite-client.jar` to `latest-runelite-43602.jar` (must match the exact filename)

**Key Point**: RSProx looks for a specific filename like `latest-runelite-43602.jar`. You must rename your file to match exactly, or RSProx won't use it.

### Extract JAR Contents
```bash
jar xf libs/custom-runelite-client.jar
```

### View JAR Contents
```bash
jar tf libs/custom-runelite-client.jar
```

## Understanding Client Configuration

The `config/bootstrap.json` file configures how the RuneLite client is downloaded and launched. This file tells the RuneLite launcher:
- Which client version to download
- JVM arguments to use
- Client artifacts location

**Note**: You typically don't need to edit `bootstrap.json` for small client edits, but it's useful to understand if you're customizing the client build process.

## Conclusion

Editing client files can be challenging at first, but starting with small, cosmetic changes is a great way to learn. Always backup your files, test thoroughly, and gradually work your way up to more complex modifications.

Remember: **Small changes, frequent testing, and good backups** are the keys to successful client editing!

## Getting Help

If you run into issues:

1. **Check the logs** - Client errors may appear in console output
2. **Verify Java version** - Ensure you're using JDK 17+
3. **Test with original JAR** - Make sure the unmodified client works first
4. **Check dependencies** - Ensure all required libraries are present
5. **Search RuneLite issues** - Many problems have been encountered before

Good luck with your client editing journey! 🚀

