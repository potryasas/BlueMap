plugins {
    id("bluemap.base")
}

dependencies {
    // API dependency
    api(project(":api"))
    
    // Bukkit dependency
    compileOnly("org.bukkit:bukkit:1.5.2-R1.0")
    
    // Other dependencies if needed
    implementation("com.google.guava:guava:21.0") // Last version compatible with Java 8
    implementation("com.google.code.gson:gson:2.8.9") // Compatible with Java 8
}

// Set Java compatibility level
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Skip tests if needed
tasks.test {
    enabled = false
}

// Skip javadoc to avoid encoding issues
tasks.javadoc {
    enabled = false
}

// Package the JAR
tasks.jar {
    archiveBaseName.set("BlueMap-Legacy")
    
    // Include all dependencies in the JAR
    from({ 
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
    
    // Exclude META-INF files from dependencies
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
} 