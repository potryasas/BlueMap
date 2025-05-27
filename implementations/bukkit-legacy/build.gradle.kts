plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.bluecolored"
version = "5.7-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.bluecolored.de/snapshots")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.md-5.net/content/repositories/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("org.bukkit:craftbukkit:1.5.2-R1.0")
    
    implementation(project(":core"))
    implementation(project(":api"))
    implementation(project(":common"))
    
    // Add Guava for Java 8 compatibility
    implementation("com.google.guava:guava:21.0")
    implementation("com.google.code.gson:gson:2.8.9")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Copy webapp resources before build
val copyWebapp = tasks.register<Copy>("copyWebapp") {
    from("${project.rootDir}/common/webapp")
    into("${project.buildDir}/resources/main/web")
    includeEmptyDirs = false
    
    // Add explicit dependencies on tasks from the common project that create the webapp
    dependsOn(":common:buildWebapp", ":common:npmInstall")
}

tasks.processResources {
    dependsOn(copyWebapp)
}

tasks.shadowJar {
    archiveBaseName.set("bluemap-bukkit-legacy")
    archiveClassifier.set("")
    
    dependencies {
        include(dependency(":core"))
        include(dependency(":api"))
        include(dependency(":common"))
        include(dependency("com.google.code.gson:gson"))
        include(dependency("com.google.guava:guava"))
    }
    
    relocate("com.google.gson", "de.bluecolored.bluemap.bukkit.legacy.lib.gson")
    relocate("com.google.common", "de.bluecolored.bluemap.bukkit.legacy.lib.guava")
    relocate("de.bluecolored.bluemap.core", "de.bluecolored.bluemap.bukkit.legacy.lib.core")
    relocate("de.bluecolored.bluemap.api", "de.bluecolored.bluemap.bukkit.legacy.lib.api")
    relocate("de.bluecolored.bluemap.common", "de.bluecolored.bluemap.bukkit.legacy.lib.common")
    
    minimize() // Remove unused classes
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "bluemap-${project.name}"
            version = "5.7-SNAPSHOT"

            from(components["java"])
        }
    }
} 