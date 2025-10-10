import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}

group = "dev.jsinco.gringotts"
version = "0.1-BETA"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT") // repo.papermc.io
    compileOnly("org.projectlombok:lombok:1.18.30") // mavenCentral
    compileOnly("com.drtshock.playervaults:PlayerVaultsX:4.4.7") // repo.jsinco.dev
    compileOnly("org.xerial:sqlite-jdbc:3.47.2.0") // mavenCentral

    implementation("com.zaxxer:HikariCP:6.3.0") // mavenCentral
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.5") // storehouse.okaeri.eu
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.5")  // storehouse.okaeri.eu
    implementation("org.bstats:bstats-bukkit:3.0.2") // mavenCentral

    annotationProcessor("org.projectlombok:lombok:1.18.30") // mavenCentral
}

tasks {

    shadowJar {
        relocate("eu.okaeri", "dev.jsinco.gringotts.okaeri")
        relocate("com.zaxxer.hikari", "dev.jsinco.gringotts.hikari")
        relocate("org.bstats", "dev.jsinco.gringotts.integration.bstats")
        archiveClassifier.set("")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version.toString()),
            "beginToken" to "\${",
            "endToken" to "}"
        )).filteringCharset = Charsets.UTF_8.name()
    }

    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }
}