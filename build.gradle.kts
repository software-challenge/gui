import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("application")
    kotlin("jvm") version "1.3.41"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "sc.gui"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "sc.gui.GuiAppKt"
    applicationDefaultJvmArgs = listOf(
        // For accessing VirtualFlow field from the base class in GridViewSkin
        "--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED",
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

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation(files("../server/socha-sdk/build/libs/sdk.jar"))
    implementation(files("../server/plugin/build/libs/blokus_2021.jar"))
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

javafx {
    version = "13"
    modules("javafx.controls", "javafx.fxml", "javafx.base", "javafx.graphics")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

