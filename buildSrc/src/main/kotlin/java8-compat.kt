package de.bluecolored.bluemap.build 
 
import org.gradle.api.Plugin 
import org.gradle.api.Project 
import org.gradle.kotlin.dsl.* 
 
/** Пустая реализация плагина спотлесс для совместимости с Java 8 */ 
class SpotlessCompat : Plugin<Project> { 
    override fun apply(project: Project) { 
        // Пустая реализация, чтобы не требовать Java 11 
        project.logger.lifecycle("SpotlessCompat: Disabled spotless checks for Java 8 compatibility") 
    } 
} 
