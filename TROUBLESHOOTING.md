# Troubleshooting Guide

## Common Issues and Solutions

### Issue: Server fails to start with exit code 1

**Possible Causes:**

1. **Java Version Mismatch**
   - **Problem**: Project requires Java 17, but you have Java 25
   - **Solution**: 
     - In IntelliJ: File → Project Structure → Project → SDK → Select Java 17
     - Or download Java 17 from: https://adoptium.net/temurin/releases/?version=17
     - Make sure IntelliJ is using Java 17 for the project

2. **Missing RSA Keys**
   - **Problem**: RSA keys not generated
   - **Solution**: Run the `install` task first:
     - Gradle → Alter → game-server → Tasks → other → install

3. **Port Already in Use**
   - **Problem**: Port 43594 is already in use
   - **Solution**: 
     - Change port in `game.yml`: `game-port: 43595`
     - Or stop the application using port 43594

4. **Missing Configuration Files**
   - **Problem**: game.yml or dev-settings.yml missing
   - **Solution**: These should already be created, but if missing:
     - Copy from `game.example.yml` to `game.yml`
     - Copy from `dev-settings.example.yml` to `dev-settings.yml`

5. **Cache Files Missing**
   - **Problem**: Cache files not downloaded
   - **Solution**: Run `.\download-requirements.ps1` to download cache files

6. **Working Directory Issue**
   - **Problem**: Relative paths not resolving correctly
   - **Solution**: The build.gradle.kts has been updated to set working directory to root project

### How to Get Detailed Error Information

1. **In IntelliJ:**
   - Look at the Run console output
   - Scroll up to see the full stack trace
   - Look for lines starting with "Exception" or "Error"

2. **Run with Stacktrace:**
   - In IntelliJ: Right-click on the `run` task → Modify Run Configuration
   - Add to VM options: `-Dorg.gradle.debug=true`
   - Or add to Program arguments: `--stacktrace`

3. **Check Logs:**
   - Server logs should appear in the IntelliJ Run console
   - Look for error messages before the process exits

### Java Version Configuration in IntelliJ

1. **Set Project SDK:**
   - File → Project Structure → Project
   - Set SDK to Java 17
   - Set Language level to 17

2. **Set Gradle JVM:**
   - File → Settings → Build, Execution, Deployment → Build Tools → Gradle
   - Set Gradle JVM to Java 17

3. **Set Run Configuration:**
   - Run → Edit Configurations
   - Select your run configuration
   - Set JRE to Java 17

### Verifying Setup

Run the diagnostic script:
```powershell
.\check-server-setup.ps1
```

This will check:
- Required files exist
- RSA keys exist
- Java version
- Port availability

### Still Having Issues?

Please provide:
1. Full error message from IntelliJ Run console
2. Java version being used (check with `java -version`)
3. Output from `.\check-server-setup.ps1`
4. Any stack traces or exception messages

