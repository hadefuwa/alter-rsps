plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javassist:javassist:3.30.2-GA")
}

application {
    mainClass.set("ClientEditor")
}

tasks.withType<JavaExec> {
    workingDir = rootProject.projectDir
}










