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

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    
    tasks.withType<Javadoc> {
        options {
            encoding = "UTF-8"
            (this as StandardJavadocDocletOptions).apply {
                charSet = "UTF-8"
                docEncoding = "UTF-8"
                addStringOption("Xdoclint:none", "-quiet")
            }
        }
        isFailOnError = false // Temporarily disable Javadoc errors failing the build
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