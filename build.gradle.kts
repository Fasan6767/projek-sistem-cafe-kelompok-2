plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "com.kopikita"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    
    // SQLite JDBC (Fitur Bonus Persistence)
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")

    // Testing: JUnit Jupiter 5
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("com.kopikita.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
