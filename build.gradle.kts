import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    application
    kotlin("plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "com.sapiens.localize.translate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation(kotlin("stdlib-jdk8"))
}