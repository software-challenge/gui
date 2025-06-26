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
    kotlin("jvm") version "1.9.25"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    
    id("com.github.ben-manes.versions") version "0.47.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
}

val backend = gradle.includedBuilds.last()

val versionFromBackend by lazy {
    val versions = Properties().apply { load(backend.projectDir.resolve("gradle.properties").inputStream()) }
    val suffix = versions["socha.version.suffix"].toString().takeUnless { it.isBlank() }?.let { "-$it" }.orEmpty()
    arrayOf("year", "minor", "patch").map { versions["socha.version.$it"].toString().toInt() }.joinToString(".") + suffix
}

group = "sc.gui"
version = try {
    Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags"))
        .inputStream.reader().readText().trim().ifEmpty { null }
} catch(e: java.io.IOException) {
    println(e)
} ?: "${versionFromBackend}-${System.getenv("GITHUB_SHA")?.takeUnless { it.isEmpty() } ?: "custom"}"
println("Current version: $version (Java version: ${JavaVersion.current()})")

application {
    mainClassName = "sc.gui.GuiAppKt" // needs shadow-update which needs gradle update to 7.0
}

repositories {
    mavenCentral()
    maven("https://maven.wso2.org/nexus/content/groups/wso2-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val debug = project.hasProperty("debug")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    
    implementation(kotlin("reflect"))
    implementation("no.tornado", "tornadofx", "2.0.0-SNAPSHOT") { exclude("org.jetbrains.kotlin", "kotlin-reflect") }
    implementation("ch.qos.logback", "logback-classic", "1.5.18")
    implementation("io.github.oshai", "kotlin-logging-jvm", "6.0.9") // TODO version 7 with kotlin 2
    
    implementation("software-challenge", "server")
    implementation("software-challenge", "plugin2024")
    implementation("software-challenge", "plugin2025")
    implementation("software-challenge", "plugin")
    
    if(debug)
        implementation("com.tangorabox", "component-inspector-fx", "1.1.0")
}

tasks {
    compileJava {
        options.release.set(minJavaVersion.majorVersion.toInt())
    }
    processResources {
        if(!debug)
            exclude("logback-test.xml")
        doLast {
            destinationDir.resolve("version.txt").writeText(version.toString())
        }
    }
    withType<KotlinCompile> {
        dependsOn(backend.task(":server:bundle"))
        kotlinOptions {
            jvmTarget = minJavaVersion.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    
    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClassName
    }
    
    javafx {
        version = "17.0.15"
        val mods = mutableListOf(
            "javafx.base", "javafx.controls", "javafx.fxml",
            "javafx.web", "javafx.media", "javafx.swing"
        ) // included because of tornadofx already
        // if(debug) mods.addAll(listOf("javafx.swing"))
        modules = mods
    }
    
    shadowJar {
        destinationDirectory.set(buildDir)
        archiveClassifier.set(
            "${
                OperatingSystem.current().familyName.replace(
                    " ",
                    ""
                )
            }-${System.getProperty("os.arch")}"
        )
        manifest {
            attributes(
                "Add-Opens" to arrayOf(
                    "javafx.controls/javafx.scene.control.skin",
                    "javafx.controls/javafx.scene.control",
                    "javafx.graphics/javafx.scene",
                    // For accessing InputMap used in RangeSliderBehavior
                    "javafx.controls/com.sun.javafx.scene.control.inputmap",
                    // Expose list internals for xstream conversion: https://github.com/x-stream/xstream/issues/253
                    "java.base/java.util"
                ).joinToString(" ")
            )
        }
        mergeServiceFiles() // This requires downgrading to JDK 11 to function properly!
    }
    
    run.configure {
        workingDir(buildDir.resolve("run"))
        doFirst {
            workingDir.mkdirs()
        }
        args = System.getProperty("args", "").split(" ")
    }
    
    val release by creating {
        dependsOn(clean, check)
        group = "distribution"
        description = "Create and push a tagged commit matching the backend version"
        doLast {
            val desc = project.properties["m"]?.toString()
                       ?: throw InvalidUserDataException("Das Argument -Pm=\"Beschreibung dieser Version\" wird benötigt")
            exec { commandLine("git", "add", "CHANGELOG.md") }
            exec { commandLine("git", "commit", "-m", "release: v$versionFromBackend") }
            exec { commandLine("git", "tag", versionFromBackend, "-m", desc) }
            exec { commandLine("git", "push", "--follow-tags", "--recurse-submodules=on-demand") }
        }
    }
}
