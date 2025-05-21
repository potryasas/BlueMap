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
}

dependencies {
    api(libs.flow.math)
    api(libs.gson)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
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
            artifactId = project.name
            version = "5.7-SNAPSHOT"

            from(components["java"])
        }
    }
}
