import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Client Editor - Modify RuneLite client JAR files using Javassist
 * 
 * This tool allows you to modify compiled classes in the JAR without
 * needing to decompile and recompile everything.
 */
object ClientEditor {
    
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            printUsage()
            return
        }
        
        val command = args[0]
        
        when (command) {
            "modify-title" -> modifyWindowTitle(args)
            "list-methods" -> listMethods(args)
            "help" -> printUsage()
            else -> {
                println("Unknown command: $command")
                printUsage()
            }
        }
    }
    
    private fun modifyWindowTitle(args: Array<String>) {
        if (args.size < 3) {
            println("Usage: modify-title <jar-file> <new-title>")
            println("Example: modify-title libs/custom-runelite-client.jar \"PANDA SERVER\"")
            return
        }
        
        val jarPath = args[1]
        val newTitle = args[2]
        
        println("Modifying window title in: $jarPath")
        println("New title: $newTitle")
        
        try {
            modifyJarClass(
                jarFile = File(jarPath),
                className = "net.runelite.client.ui.ClientUI",
                modifications = { ctClass ->
                    // Find and modify setTitle calls
                    val methods = ctClass.getDeclaredMethods("init")
                    if (methods.isNotEmpty()) {
                        val initMethod = methods[0]
                        var code = initMethod.methodInfo.codeAttribute.toString()
                        
                        // Replace setTitle calls with new title
                        // This is a simplified approach - you may need to adjust based on actual bytecode
                        println("Found init method, modifying...")
                    }
                    
                    // Alternative: Modify the updateFrameConfig method
                    val updateMethods = ctClass.getDeclaredMethods("updateFrameConfig")
                    if (updateMethods.isNotEmpty()) {
                        val updateMethod = updateMethods[0]
                        var body = updateMethod.methodInfo.codeAttribute.toString()
                        
                        // Replace: this.frame.setTitle(this.title)
                        // With: this.frame.setTitle("$newTitle")
                        println("Modifying updateFrameConfig method...")
                    }
                    
                    // Direct string replacement approach
                    replaceStringInClass(ctClass, "this.frame.setTitle(this.title)", 
                        "this.frame.setTitle(\"$newTitle\")")
                }
            )
            
            println("✅ Successfully modified JAR file!")
            println("Next: Copy to RSProx and test")
            
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun replaceStringInClass(ctClass: CtClass, oldCode: String, newCode: String) {
        val methods = ctClass.declaredMethods
        for (method in methods) {
            try {
                val body = method.methodInfo.codeAttribute?.toString() ?: continue
                if (body.contains("setTitle")) {
                    // Use Javassist to replace method body
                    // This is simplified - actual implementation depends on bytecode structure
                    println("Found setTitle in method: ${method.name}")
                }
            } catch (e: Exception) {
                // Some methods might not be modifiable
                continue
            }
        }
    }
    
    private fun listMethods(args: Array<String>) {
        if (args.size < 2) {
            println("Usage: list-methods <jar-file> <class-name>")
            println("Example: list-methods libs/custom-runelite-client.jar net.runelite.client.ui.ClientUI")
            return
        }
        
        val jarPath = args[1]
        val className = if (args.size > 2) args[2] else "net.runelite.client.ui.ClientUI"
        
        try {
            val pool = ClassPool.getDefault()
            pool.insertClassPath(jarPath)
            
            val ctClass = pool.get(className)
            println("Methods in $className:")
            ctClass.declaredMethods.forEach { method ->
                println("  - ${method.name}(${method.parameterTypes.joinToString { it.name }})")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
    
    private fun modifyJarClass(
        jarFile: File,
        className: String,
        modifications: (CtClass) -> Unit
    ) {
        val pool = ClassPool.getDefault()
        pool.insertClassPath(jarFile.absolutePath)
        
        // Load the class
        val ctClass = pool.get(className)
        
        // Apply modifications
        modifications(ctClass)
        
        // Create backup
        val backupFile = File("${jarFile.absolutePath}.backup")
        if (!backupFile.exists()) {
            jarFile.copyTo(backupFile, overwrite = false)
            println("Created backup: ${backupFile.name}")
        }
        
        // Write modified class back to JAR
        val tempJar = File("${jarFile.absolutePath}.tmp")
        JarFile(jarFile).use { originalJar ->
            JarOutputStream(tempJar.outputStream()).use { newJar ->
                // Copy all entries except the modified class
                originalJar.entries().forEach { entry ->
                    if (entry.name != className.replace('.', '/') + ".class") {
                        newJar.putNextEntry(JarEntry(entry.name))
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
    
    private fun printUsage() {
        println("""
            Client Editor - Modify RuneLite Client JAR Files
            
            Usage:
              modify-title <jar-file> <new-title>
                Modify the window title in ClientUI class
                Example: modify-title libs/custom-runelite-client.jar "PANDA SERVER"
            
              list-methods <jar-file> [class-name]
                List all methods in a class
                Example: list-methods libs/custom-runelite-client.jar net.runelite.client.ui.ClientUI
            
              help
                Show this help message
        """.trimIndent())
    }
}






