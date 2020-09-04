import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    kotlin("jvm") version "1.3.41"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "sc.gui"
version = "1.0-SNAPSHOT"

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

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation(files("../server/socha-sdk/build/libs/sdk.jar"))
    implementation(files("../server/plugin/build/libs/blokus_2021.jar"))
    implementation(files("../server/server/build/runnable/server.jar"))
    implementation(fileTree("../server/server/build/runnable/lib") { include("*.jar") })
}


java {
    /*
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_1_8
     */
}

tasks.compileJava {
    options.release.set(9) // earlier versions do not work because of JavaFX support
}

/*
This does not work as intended (puts plugins dir in ./build, but we want it in the distributions):
tasks.register<Copy>("copyPlugins") {
    from("plugins")
    include("*.jar")
    into("$buildDir/plugins")
}

tasks.build {
    dependsOn("copyPlugins")
}
 */

javafx {
    version = "13"
    modules("javafx.controls", "javafx.fxml", "javafx.base", "javafx.graphics")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

