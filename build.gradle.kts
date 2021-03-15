import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val minJavaVersion = JavaVersion.VERSION_11
plugins {
    val minJavaVersion = JavaVersion.VERSION_11 // Declared twice because plugins block has its own scope
    require(JavaVersion.current() >= minJavaVersion) {
        "Building requires at least JDK $minJavaVersion - please look into the README"
    }
    
    application
    kotlin("jvm") version "1.4.20"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    
    id("com.github.ben-manes.versions") version "0.36.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.15"
}

val backend = gradle.includedBuilds.last()

val versionFromBackend by lazy {
    val versions = Properties().apply { load(backend.projectDir.resolve("gradle.properties").inputStream()) }
    arrayOf("year", "minor", "patch").map { versions["socha.version.$it"].toString().toInt() }.joinToString(".")
}

group = "sc.gui"
version = try {
    Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags")).inputStream.reader().readText().trim().ifEmpty { null }
} catch (_: java.io.IOException) {
    null
} ?: "${versionFromBackend}-custom"
println("Current version: $version (Java version: ${JavaVersion.current()})")

application {
    mainClassName = "sc.gui.GuiAppKt" // not migrating from legacy because of https://github.com/johnrengelman/shadow/issues/609 - waiting for 6.2 release
    // these are required because of using JDK >8,
    // see https://github.com/controlsfx/controlsfx/wiki/Using-ControlsFX-with-JDK-9-and-above
    applicationDefaultJvmArgs = listOf(
        // For accessing VirtualFlow field from the base class in GridViewSkin
        "--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED",
        "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        // For accessing InputMap used in RangeSliderBehavior
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED"
    )
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    
    implementation(kotlin("reflect"))
    implementation("no.tornado", "tornadofx", "2.0.0-SNAPSHOT") { exclude("org.jetbrains.kotlin", "kotlin-reflect") }
    implementation("io.github.microutils", "kotlin-logging-jvm", "2.0.4")
    implementation("io.ktor:ktor-client-core:1.5.2")
    implementation("io.ktor:ktor-client-apache:1.5.2")
    
    implementation(fileTree(backend.name + "/server/build/runnable") { include("**/*.jar") })
}

tasks {
    compileJava {
        options.release.set(minJavaVersion.majorVersion.toInt())
    }
    processResources {
        doFirst {
            sourceSets.main.get().resources.srcDirs.single().resolve("version.txt").writeText(version.toString())
        }
    }
    withType<KotlinCompile> {
        dependsOn(backend.task(":server:deploy"))
        kotlinOptions {
            jvmTarget = minJavaVersion.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    
    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClassName
    }
    
    javafx {
        version = "13"
        modules("javafx.controls", "javafx.fxml", "javafx.base", "javafx.graphics")
    }
    
    shadowJar {
        destinationDirectory.set(buildDir)
        archiveClassifier.set(OperatingSystem.current().familyName)
    }
    
    run.configure {
        workingDir(buildDir.resolve("tmp"))
        doFirst {
            workingDir.mkdirs()
        }
    }
    
    val release by creating {
        dependsOn(check)
        group = "distribution"
        description = "Creates and pushes a tagged commit according to the backend version"
        doLast {
            val desc = project.properties["m"]?.toString()
                       ?: throw InvalidUserDataException("Das Argument -Pm=\"Beschreibung dieser Version\" wird benötigt")
            exec { commandLine("git", "commit", "-a", "-m", "release: $versionFromBackend") }
            exec { commandLine("git", "tag", versionFromBackend, "-m", desc) }
            exec { commandLine("git", "push", "--follow-tags") }
        }
    }
}
