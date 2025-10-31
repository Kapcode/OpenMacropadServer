plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.kapcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JAR from project's libs folder
    implementation(files("libs/KotlinNetworkLibrary-1.0.0.jar"))
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
    implementation("com.formdev:flatlaf:3.4.1")
    implementation("com.kitfox.svg:svg-salamander:1.0")
    implementation("org.json:json:20250517")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}