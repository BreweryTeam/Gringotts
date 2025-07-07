plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}

group = "dev.jsinco.gringotts"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    compileOnly("org.xerial:sqlite-jdbc:3.47.2.0")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.5")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.5")
}

tasks {

    shadowJar {
        relocate("eu.okaeri", "dev.jsinco.gringotts.okaeri")
        relocate("com.zaxxer.hikari", "dev.jsinco.gringotts.hikari")
        archiveClassifier.set("")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}