plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("org.example.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        // Use LTS Java 21 for Gradle compatibility
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // PostgreSQL driver
    implementation("org.postgresql:postgresql:42.7.1")
    
    // MongoDB driver
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}