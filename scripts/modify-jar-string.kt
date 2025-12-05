import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.JarEntry

/**
 * Simple script to replace strings in a JAR file
 * 
 * Usage: kotlin modify-jar-string.kt <jar-file> <old-string> <new-string>
 */
fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: modify-jar-string <jar-file> <old-string> <new-string>")
        println("Example: modify-jar-string libs/custom-runelite-client.jar \"Alter\" \"PANDA SERVER\"")
        return
    }
    
    val jarPath = args[0]
    val oldString = args[1]
    val newString = args[2]
    
    val jarFile = File(jarPath)
    if (!jarFile.exists()) {
        println("Error: JAR file not found: $jarPath")
        return
    }
    
    if (oldString.length != newString.length) {
        println("Error: Old and new strings must be the same length!")
        println("Old: '${oldString}' (${oldString.length} chars)")
        println("New: '${newString}' (${newString.length} chars)")
        return
    }
    
    println("Modifying: $jarPath")
    println("Replacing: '$oldString' -> '$newString'")
    
    // Create backup
    val backupFile = File("${jarFile.absolutePath}.backup")
    if (!backupFile.exists()) {
        jarFile.copyTo(backupFile, overwrite = false)
        println("Created backup: ${backupFile.name}")
    }
    
    // Read JAR, modify strings, write back
    val tempJar = File("${jarFile.absolutePath}.tmp")
    var modified = false
    
    JarFile(jarFile).use { originalJar ->
        JarOutputStream(tempJar.outputStream()).use { newJar ->
            originalJar.entries().forEach { entry ->
                newJar.putNextEntry(JarEntry(entry.name))
                
                if (entry.name.endsWith(".class") || entry.name.endsWith(".properties") || entry.name.endsWith(".txt")) {
                    // Read entry content
                    val content = originalJar.getInputStream(entry).readBytes()
                    
                    // Convert to string and replace
                    val contentString = String(content, Charsets.UTF_8)
                    if (contentString.contains(oldString)) {
                        val modifiedContent = contentString.replace(oldString, newString)
                        val modifiedBytes = modifiedContent.toByteArray(Charsets.UTF_8)
                        newJar.write(modifiedBytes)
                        modified = true
                        println("  Modified: ${entry.name}")
                    } else {
                        newJar.write(content)
                    }
                } else {
                    // Copy binary files as-is
                    originalJar.getInputStream(entry).copyTo(newJar)
                }
                
                newJar.closeEntry()
            }
        }
    }
    
    if (modified) {
        // Replace original with modified
        tempJar.copyTo(jarFile, overwrite = true)
        tempJar.delete()
        println("✅ Success! JAR file modified.")
        println("Next: Copy to RSProx and test")
    } else {
        tempJar.delete()
        println("⚠️  String '$oldString' not found in JAR file")
    }
}





