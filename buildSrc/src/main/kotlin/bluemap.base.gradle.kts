plugins {
    java
    `java-library`
    `maven-publish`
}

group = "de.bluecolored"
version = if (project.hasProperty("bluemap.version")) {
    project.property("bluemap.version").toString()
} else {
    try {
        gitVersion()
    } catch (e: Exception) {
        logger.warn("Failed to determine Git version: ${e.message}")
        "0.0.0-dev"
    }
}

repositories {
    maven ("https://repo.bluecolored.de/releases") {
        content { includeGroupByRegex ("de\\.bluecolored.*") }
    }
    maven ("https://repo.bluecolored.de/snapshots") {
        content { includeGroupByRegex ("de\\.bluecolored.*") }
    }
    maven ("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content { includeGroup ("org.spigotmc") }
    }

    // lwjgl-freetype-3.3.3-natives-macos-patch.jar is not available on Maven
    // Central - pull it from the Minecraft library server instead.
    maven ("https://libraries.minecraft.net") {
        content { includeModule("org.lwjgl", "lwjgl-freetype") }
    }

    mavenCentral()
    maven ("https://libraries.minecraft.net")
    maven ( "https://maven.minecraftforge.net" )
    maven ("https://repo.papermc.io/repository/maven-public/")
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        links(
            "https://docs.oracle.com/javase/8/docs/api/",
            "https://javadoc.io/doc/com.flowpowered/flow-math/1.0.3/",
            "https://javadoc.io/doc/com.google.code.gson/gson/2.8.9/",
        )
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "bluecolored"
            url = uri("https://repo.bluecolored.de/releases")

            if (!gitIsRelease())
                url = uri("https://repo.bluecolored.de/snapshots")

            credentials {
                username = project.findProperty("bluecoloredUsername") as String? ?: System.getenv("BLUECOLORED_USERNAME")
                password = project.findProperty("bluecoloredPassword") as String? ?: System.getenv("BLUECOLORED_PASSWORD")
            }
        }
    }
}
