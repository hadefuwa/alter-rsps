import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.io.File

rootProject.name = "Alter"
pluginManagement {
    plugins {
        kotlin("jvm")
    }
}
plugins {
    id("de.fayard.refreshVersions") version ("0.51.0")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            files("gradle/libs.versions.toml")
        }
    }
}

include(":util")
include(":game-plugins")
include(":game-api")
include(":game-server")
include(":plugins")

includePlugins(project(":plugins"))
fun includePlugins(pluginProject: ProjectDescriptor) {
    val pluginPath = pluginProject.projectDir.toPath()
    Files.walkFileTree(pluginPath, object : SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (dir == pluginPath) return FileVisitResult.CONTINUE
            if (dir.fileName.toString() in setOf("build", ".gradle", "src", ".git", ".idea", "bin", "out")) {
                return FileVisitResult.SKIP_SUBTREE
            }
            if (Files.exists(dir.resolve("build.gradle.kts"))) {
                val relativePath = pluginPath.relativize(dir)
                val pluginName = relativePath.toString().replace(File.separator, ":")
                include("${pluginProject.name}:$pluginName")
            }
            return FileVisitResult.CONTINUE
        }
    })
}
