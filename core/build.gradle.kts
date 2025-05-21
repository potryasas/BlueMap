plugins {
    `java-library`
    `maven-publish`
}

group = "de.bluecolored"
version = "5.7-SNAPSHOT"

repositories {
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.bluecolored.de/snapshots")
    mavenCentral()
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.minecraftforge.net/") }
}

dependencies {
    api(project(":api"))

    api(libs.aircompressor)
    api(libs.caffeine)
    api(libs.commons.dbcp2)
    api(libs.configurate.hocon)
    api(libs.configurate.gson)
    api(libs.lz4)
    implementation("de.tr7zw:item-nbt-api-plugin:2.12.2")

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    // tests
    testImplementation(libs.junit.core)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
    withSourcesJar()
    withJavadocJar()
}

tasks.register("zipResourceExtensions", type = Zip::class) {
    from(fileTree("src/main/resourceExtensions"))
    archiveFileName = "resourceExtensions.zip"
    destinationDirectory = file("src/main/resources/de/bluecolored/bluemap/")
}

tasks.processResources {
    dependsOn("zipResourceExtensions")

    from("src/main/resources") {
        include("de/bluecolored/bluemap/version.json")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        expand(
            "version" to "5.7-SNAPSHOT",
            "gitHash" to gitHash() + if (gitClean()) "" else " (dirty)",
        )
    }
}

tasks.getByName("sourcesJar") {
    dependsOn("zipResourceExtensions")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "bluemap-${project.name}"
            version = "5.7-SNAPSHOT"

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionResult()
                }
            }
        }
    }
}
