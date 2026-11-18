plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.kopikita"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.kopikita.MainKt")
}

// Memungkinkan input interaktif dari keyboard saat dijalankan dengan ./gradlew run
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
