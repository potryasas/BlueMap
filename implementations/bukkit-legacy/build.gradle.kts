plugins {
    `java-library`
    `maven-publish`
}

group = "de.bluecolored"
version = "5.7-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://libraries.minecraft.net")
    maven("https://repo.md-5.net/content/repositories/public/")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("org.bukkit:craftbukkit:1.5.2-R1.0")
    
    implementation(project(":common"))
    implementation(project(":core"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
    withSourcesJar()
    withJavadocJar()
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