allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.bluecolored.de/releases")
        maven("https://repo.bluecolored.de/snapshots")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        flatDir {
            dirs("${rootProject.projectDir}/libs")
        }
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
} 