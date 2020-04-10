import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("application")
    kotlin("jvm") version "1.3.41"
    id("org.openjfx.javafxplugin") version "0.0.8"
}

group = "sc.gui"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "sc.gui.GuiApp"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.17")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.base")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
