# Client Editing Guide - Getting Started with Small Edits

## Overview

This guide will help you get started with editing client files for the Alter RSPS project. The client is based on **RuneLite**, which is an open-source Old School RuneScape client. This guide focuses on making small, incremental edits to help you learn the process safely.

## Understanding Client Files

### What Are Client Files?

The client files in this project are:
- **`runelite.jar`** - The base RuneLite client
- **`custom-runelite-client.jar`** - A customized version of the RuneLite client

These are **compiled Java/Kotlin applications** packaged as JAR (Java Archive) files. They contain:
- Client-side game logic
- UI components and interfaces
- Rendering code
- Network communication code
- Client scripts and configurations

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

1. Create a backup of your client JAR files:
   ```bash
   # In PowerShell (Windows)
   Copy-Item runelite.jar runelite.jar.backup
   Copy-Item custom-runelite-client.jar custom-runelite-client.jar.backup
   ```

2. Create a new directory for client development:
   ```bash
   mkdir client-development
   cd client-development
   ```

### Step 2: Decompile the JAR File

You have several options for decompiling:

#### Option A: Using JD-GUI (Easiest for Beginners)

1. **Download JD-GUI**: https://java-decompiler.github.io/
2. **Open the JAR file**:
   - Launch JD-GUI
   - File → Open → Select `custom-runelite-client.jar`
   - Browse the decompiled code
3. **Export source code**:
   - File → Save All Sources
   - Save to a folder (e.g., `client-sources/`)

#### Option B: Using IntelliJ IDEA (Recommended)

1. **Open IntelliJ IDEA**
2. **Open the JAR as a library**:
   - File → Project Structure → Libraries
   - Click `+` → Java → Select `custom-runelite-client.jar`
3. **Decompile on-the-fly**:
   - IntelliJ can decompile classes when you navigate to them
   - Right-click on a class → Decompile

#### Option C: Using Fernflower (Command Line)

```bash
# Download Fernflower (included with IntelliJ IDEA)
# Or use online: https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine

java -jar fernflower.jar custom-runelite-client.jar client-sources/
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
   ```bash
   Copy-Item modified-client.jar custom-runelite-client.jar -Force
   ```

2. **Start the server**:
   ```bash
   ./gradlew run
   ```

3. **Launch the client** and test your changes

4. **If something breaks**, restore from backup:
   ```bash
   Copy-Item custom-runelite-client.jar.backup custom-runelite-client.jar -Force
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
```bash
# Before any edit
Copy-Item custom-runelite-client.jar custom-runelite-client.jar.backup
```

### 3. Use Version Control
```bash
git init
git add custom-runelite-client.jar.backup
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

### Problem: Client Won't Start After Edit
**Solution**: 
1. Restore from backup
2. Check for compilation errors
3. Verify all dependencies are included

### Problem: Can't Find the Code to Edit
**Solution**:
1. Use a decompiler with search functionality
2. Look for string literals that match what you see in-game
3. Check the RuneLite GitHub repository for source code reference

### Problem: Changes Don't Appear
**Solution**:
1. Ensure you recompiled correctly
2. Check that you're using the modified JAR
3. Clear client cache if applicable

### Problem: Missing Dependencies
**Solution**:
1. Extract dependencies from original JAR
2. Check `META-INF/MANIFEST.MF` for classpath
3. Include all required libraries when compiling

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
   - File → Open → `custom-runelite-client.jar`

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
   javac -cp "custom-runelite-client.jar" YourClass.java
   ```

6. **Update JAR**:
   ```bash
   jar uf custom-runelite-client.jar YourClass.class
   ```

7. **Test**:
   - Start server
   - Launch client
   - Verify the change appears

## Quick Reference: Common Commands

### Backup Client
```powershell
# Windows PowerShell
Copy-Item custom-runelite-client.jar custom-runelite-client.jar.backup
```

### Decompile with JD-GUI
1. Download JD-GUI
2. Open JAR file
3. File → Save All Sources

### Compile Single Class
```bash
javac -cp "custom-runelite-client.jar:other-deps/*" YourClass.java
```

### Update JAR with Modified Class
```bash
jar uf custom-runelite-client.jar YourClass.class
```

### Extract JAR Contents
```bash
jar xf custom-runelite-client.jar
```

### View JAR Contents
```bash
jar tf custom-runelite-client.jar
```

## Understanding Client Configuration

The `bootstrap.json` file in your project root configures how the RuneLite client is downloaded and launched. This file tells the RuneLite launcher:
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

