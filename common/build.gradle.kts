import com.github.gradle.node.npm.task.NpmTask
import java.io.IOException

plugins {
    bluemap.base
    alias ( libs.plugins.node.gradle )
}

dependencies {
    api ( project( ":core" ) )

    api ( libs.adventure.api )

    compileOnly ( libs.brigadier )

    compileOnly ( libs.jetbrains.annotations )
    compileOnly ( libs.lombok )

    annotationProcessor ( libs.lombok )

    // tests
    testImplementation ( libs.junit.core )
    testRuntimeOnly ( libs.junit.engine )
    testRuntimeOnly ( libs.lombok )
    testAnnotationProcessor ( libs.lombok )
}

node {
    version = "20.14.0"
    download = true
    nodeProjectDir = file("webapp/")
}

tasks.register("buildWebapp", type = NpmTask::class) {
    dependsOn ("npmInstall")
    
    // Clean up dist directory first
    doFirst {
        val distDir = file("webapp/dist/")
        if (distDir.exists()) {
            logger.info("Cleaning up dist directory...")
            project.delete(distDir)
            // Wait a bit to ensure resources are released
            Thread.sleep(1000)
        }
    }
    
    args = listOf("run", "build")

    inputs.dir("webapp/")
    outputs.dir("webapp/dist/")
    
    // Add retry mechanism
    var attempts = 0
    val maxAttempts = 3
    
    doLast {
        while (attempts < maxAttempts) {
            try {
                if (!file("webapp/dist/").exists()) {
                    throw GradleException("Dist directory was not created")
                }
                break
            } catch (e: Exception) {
                attempts++
                if (attempts >= maxAttempts) {
                    throw GradleException("Failed to build webapp after $maxAttempts attempts", e)
                }
                logger.warn("Build attempt $attempts failed, retrying...")
                Thread.sleep(2000) // Wait before retry
                args = listOf("run", "build")
            }
        }
    }
}

tasks.register("zipWebapp", type = Zip::class) {
    dependsOn ("buildWebapp")
    
    doFirst {
        // Ensure the destination directory exists
        destinationDirectory.get().asFile.mkdirs()
    }
    
    from (fileTree("webapp/dist/"))
    archiveFileName = "webapp.zip"
    destinationDirectory = file("src/main/resources/de/bluecolored/bluemap/")

    val replacements = mapOf(
        "version" to project.version
    )

    inputs.properties(replacements)
    inputs.dir("webapp/dist/")
    outputs.file("src/main/resources/de/bluecolored/bluemap/webapp.zip")

    filesMatching(listOf(
        "index.html",
    )) { expand(properties) }
}

tasks.processResources {
    dependsOn("zipWebapp")
}

tasks.getByName("sourcesJar") {
    dependsOn("zipWebapp")
}

tasks.register<CopyFileTask>("release") {
    group = "publishing"
    dependsOn("zipWebapp")

    inputFile = tasks.getByName("zipWebapp").outputs.files.singleFile
    outputFile = releaseDirectory.resolve("bluemap-${project.version}-webapp.zip")
}

tasks.clean {
    doFirst {
        val distDir = file("webapp/dist/")
        if (distDir.exists()) {
            var attempts = 0
            val maxAttempts = 3
            
            while (attempts < maxAttempts) {
                try {
                    if (!distDir.deleteRecursively()) {
                        attempts++
                        if (attempts >= maxAttempts) {
                            throw IOException("Failed to delete build directory after $maxAttempts attempts!")
                        }
                        logger.warn("Failed to delete directory, retrying in 2 seconds...")
                        Thread.sleep(2000)
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    attempts++
                    if (attempts >= maxAttempts) {
                        throw IOException("Failed to delete build directory after $maxAttempts attempts!", e)
                    }
                    logger.warn("Error deleting directory: ${e.message}, retrying in 2 seconds...")
                    Thread.sleep(2000)
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "bluemap-${project.name}"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
        (this as StandardJavadocDocletOptions).apply {
            charSet = "UTF-8"
            docEncoding = "UTF-8"
            addStringOption("Xdoclint:none", "-quiet")
            addBooleanOption("html5", true)
        }
    }
    isFailOnError = false
}
