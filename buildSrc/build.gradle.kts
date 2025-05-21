plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.bluecolored.de/snapshots")
    maven("https://repo.codemc.org/repository/maven-public/")  // Для NBT-API
}

dependencies {
    fun plugin(dependency: Provider<PluginDependency>) = dependency.map {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    }

    // Отключаем spotless, который требует Java 11
    // implementation(plugin(libs.plugins.spotless))
    
    implementation(plugin(libs.plugins.shadow))
    implementation(plugin(libs.plugins.minotaur))
    implementation(plugin(libs.plugins.cursegradle))
    implementation(plugin(libs.plugins.hangar))
    implementation(plugin(libs.plugins.sponge.ore))

    // explicitly set guava version to resolve a build-dependency issue
    implementation(libs.guava)

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("de.tr7zw:item-nbt-api:2.12.2")  // NBT-API
}

// Устанавливаем совместимость с Java 8
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}
