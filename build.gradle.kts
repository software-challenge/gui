import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val minJavaVersion = JavaVersion.VERSION_11
plugins {
	// Declared twice because plugins block has its own scope
	val minJavaVersion = JavaVersion.VERSION_11
	require(JavaVersion.current() >= minJavaVersion) {
		"Building requires at least JDK $minJavaVersion - please look into the README."
	}
	
	application
	kotlin("jvm") version "1.3.71"
	id("org.openjfx.javafxplugin") version "0.0.9"
	id("com.github.johnrengelman.shadow") version "6.0.0"
	
	id("com.github.ben-manes.versions") version "0.31.0"
	id("se.patrikerdes.use-latest-versions") version "0.2.14"
}

group = "sc.gui"
version = "21.0.0-pre"
try {
	// Add hash suffix if git is available
	version = version.toString() + "-" + Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short", "--verify", "HEAD")).inputStream.reader().readText().trim()
} catch(_: java.io.IOException) {
}

application {
    mainClassName = "sc.gui.GuiAppKt"
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
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

val backend = gradle.includedBuilds.last()

dependencies {
	implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    
    implementation(fileTree(backend.name + "/server/build/runnable") { include("**/*.jar") })
}

tasks {
	compileJava {
		options.release.set(minJavaVersion.majorVersion.toInt())
	}
	withType<KotlinCompile> {
		dependsOn(backend.task(":server:deploy"))
		kotlinOptions.jvmTarget = minJavaVersion.toString()
	}
	
	javafx {
		version = "13"
		modules("javafx.controls", "javafx.fxml", "javafx.base", "javafx.graphics")
	}
	
	shadowJar {
		destinationDirectory.set(buildDir)
		archiveClassifier.set(OperatingSystem.current().familyName)
	}
}
