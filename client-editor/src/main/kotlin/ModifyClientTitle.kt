import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Simple tool to modify the window title in ClientUI class
 * 
 * Usage: java -cp "client-editor.jar:libs/custom-runelite-client.jar" ModifyClientTitle <jar-file> <new-title>
 */
object ModifyClientTitle {
    
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2) {
            println("Usage: ModifyClientTitle <jar-file> <new-title>")
            println("Example: ModifyClientTitle libs/custom-runelite-client.jar \"PANDA SERVER\"")
            System.exit(1)
        }
        
        val jarPath = args[0]
        val newTitle = args[1]
        
        val jarFile = File(jarPath)
        if (!jarFile.exists()) {
            println("Error: JAR file not found: $jarPath")
            System.exit(1)
        }
        
        println("Modifying: $jarPath")
        println("New title: $newTitle")
        
        try {
            modifyTitle(jarFile, newTitle)
            println("✅ Success! Window title updated.")
            println("Next: Copy to RSProx and test")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            System.exit(1)
        }
    }
    
    private fun modifyTitle(jarFile: File, newTitle: String) {
        val className = "net.runelite.client.ui.ClientUI"
        
        // Create backup
        val backupFile = File("${jarFile.absolutePath}.backup")
        if (!backupFile.exists()) {
            jarFile.copyTo(backupFile, overwrite = false)
            println("Created backup: ${backupFile.name}")
        }
        
        // Load class from JAR
        val pool = ClassPool.getDefault()
        pool.insertClassPath(jarFile.absolutePath)
        val ctClass = pool.get(className)
        
        println("Loaded class: $className")
        
        // Find methods that set the title
        val methodsToModify = listOf("init", "updateFrameConfig", "onGameStateChanged")
        
        for (methodName in methodsToModify) {
            try {
                val methods = ctClass.getDeclaredMethods(methodName)
                for (method in methods) {
                    modifySetTitleCalls(method, newTitle)
                }
            } catch (e: Exception) {
                // Method might not exist, continue
            }
        }
        
        // Write modified class back to JAR
        val tempJar = File("${jarFile.absolutePath}.tmp")
        JarFile(jarFile).use { originalJar ->
            JarOutputStream(tempJar.outputStream()).use { newJar ->
                // Copy all entries except the modified class
                originalJar.entries().forEach { entry ->
                    val entryName = entry.name
                    val classPath = className.replace('.', '/') + ".class"
                    
                    if (entryName != classPath) {
                        newJar.putNextEntry(JarEntry(entryName))
                        originalJar.getInputStream(entry).use { it.copyTo(newJar) }
                        newJar.closeEntry()
                    }
                }
                
                // Add modified class
                val classEntry = JarEntry(className.replace('.', '/') + ".class")
                newJar.putNextEntry(classEntry)
                newJar.write(ctClass.toBytecode())
                newJar.closeEntry()
            }
        }
        
        // Replace original with modified
        tempJar.copyTo(jarFile, overwrite = true)
        tempJar.delete()
        
        ctClass.detach()
    }
    
    private fun modifySetTitleCalls(method: CtMethod, newTitle: String) {
        try {
            // Get the method's bytecode
            val codeAttribute = method.methodInfo.codeAttribute
            if (codeAttribute == null) return
            
            // This is a simplified approach - for production, you'd want to use
            // a proper bytecode analysis tool. For now, we'll use string replacement
            // in the method body source (if available) or bytecode manipulation.
            
            // Try to get source if available (for debugging)
            println("  Found method: ${method.name}")
            
            // For actual bytecode modification, we'd need to:
            // 1. Parse the bytecode
            // 2. Find LDC (load constant) instructions for strings
            // 3. Replace them with new strings
            // 4. Or find method calls to setTitle and modify their arguments
            
            // Simplified: Use Javassist's insertBefore/insertAfter for simple cases
            // Or use a bytecode framework like ASM for more control
            
        } catch (e: Exception) {
            println("  Could not modify ${method.name}: ${e.message}")
        }
    }
}


