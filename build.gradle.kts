import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val minJavaVersion = JavaVersion.VERSION_11
val targetJavaVersion = JavaVersion.current() // minJavaVersion can be set for compatibility
plugins {
    val minJavaVersion = JavaVersion.VERSION_11 // Declared twice because plugins block has its own scope
    require(JavaVersion.current() >= minJavaVersion) {
        "Building requires at least JDK $minJavaVersion - please look into the README"
    }
    
    application
    kotlin("jvm") version "2.3.0"
    id("idea")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.1.0"
    
    id("com.github.ben-manes.versions") version "0.53.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
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
    mainClass.set("sc.gui.GuiAppKt")
}

repositories {
    mavenCentral()
    maven("https://maven.wso2.org/nexus/content/groups/wso2-public/")
    // TornadoFX
    // maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

// ./gradlew run -Pdebug for debug tools and logging
val debug = project.hasProperty("debug")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    
    implementation(kotlin("reflect"))
    
    implementation(files("./gradle/tornadofx2-21e933fd41.jar"))
    // implementation("no.tornado", "tornadofx", "2.0.0-SNAPSHOT") { exclude("org.jetbrains.kotlin", "kotlin-reflect") }
    // implementation("com.github.software-challenge.tornadofx2", "tornadofx2", "2.0.0")
    // implementation("com.github.edvin", "tornadofx2", "master-SNAPSHOT")
    // implementation("com.github.edvin", "tornadofx2", "21e933fd41")

    implementation("ch.qos.logback", "logback-classic", "1.5.32")
    implementation("io.github.oshai", "kotlin-logging-jvm", "8.0.01")
    
    implementation("software-challenge", "server")
    implementation("software-challenge", "plugin2023")
    implementation("software-challenge", "plugin2024")
    implementation("software-challenge", "plugin2025")
    implementation("software-challenge", "plugin2026")
    
    if(debug) {
        // hold Ctrl to view component hierarchy and bounds
        implementation("com.tangorabox", "component-inspector-fx", "1.1.0")
    }
}

tasks {
    compileJava {
        options.release.set(targetJavaVersion.majorVersion.toInt())
    }
    processResources {
        if(!debug)
            exclude("logback-test.xml")
        doLast {
            destinationDir.resolve("version.txt").writeText(version.toString())
        }
    }
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
            //freeCompilerArgs.addAll("-jvm-default=all")
        }
    }
    
    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClass.get()
    }
    
    javafx {
        version = "17.0.15"
        val mods = mutableListOf(
            "javafx.base", "javafx.controls", "javafx.fxml",
            "javafx.web", "javafx.media", "javafx.swing"
        )
        // included because of tornadofx already
        // if(debug) mods.addAll(listOf("javafx.swing"))
        modules = mods
    }
    
    shadowJar {
        destinationDirectory.set(layout.buildDirectory.asFile.get())
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
        dependsOn(backend.task(":server:makeRunnable"))
        workingDir(layout.buildDirectory.asFile.get().resolve("run"))
        doFirst {
            workingDir.mkdirs()
        }
        args = System.getProperty("args", "").split(" ")
    }
    
    val release by registering {
        dependsOn(clean, check)
        group = "distribution"
        description = "Create and push a tagged commit matching the backend version"
        doLast {
            val desc = project.properties["m"]?.toString()
                       ?: throw InvalidUserDataException("Das Argument -Pm=\"Beschreibung dieser Version\" wird benötigt")
            
            providers.exec { commandLine("git", "add", "CHANGELOG.md") }
            providers.exec { commandLine("git", "commit", "-m", "release: v$versionFromBackend") }
            providers.exec { commandLine("git", "tag", versionFromBackend, "-m", desc) }
            providers.exec { commandLine("git", "push", "--follow-tags", "--recurse-submodules=on-demand") }
        }
    }
}
