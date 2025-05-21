logger.lifecycle("""
## Building BlueMap ...
Java: ${System.getProperty("java.version")}
JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")})
Arch: ${System.getProperty("os.arch")} 
""")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.bluecolored.de/releases")
        maven("https://repo.bluecolored.de/snapshots")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.bluecolored.de/releases")
        maven("https://repo.bluecolored.de/snapshots")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "bluemap"

include("api")
include("common")
include("core")
include("bukkit-legacy")
implementation("bukkit-legacy")

// Exclude modules that require Java 17+
// include("bukkit")
// include("forge")
// include("fabric")
// include("sponge")
// include("velocity")
// include("bungee")
// include("standalone")

// implementation("cli")
// implementation("fabric")
// implementation("forge")
// implementation("neoforge")
// implementation("paper")
// implementation("spigot")

fun implementation(name: String) {
    val project = ":$name"
    include(project)
    project(project).projectDir = file("implementations/$name")
}
