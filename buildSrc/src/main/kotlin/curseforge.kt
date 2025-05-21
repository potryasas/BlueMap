import com.matthewprenger.cursegradle.CurseProject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf

fun Project.curseforgeBlueMap(configuration: Action<CurseProject>) {
    extensions.findByName("cursegradle")?.let { curseforge ->
        val project = curseforge.javaClass.getMethod("project", Any::class.java)
            .invoke(curseforge, closureOf<CurseProject> {
                id = "406463"
                changelogType = "markdown"
                changelog = project.releaseNotes()
                releaseType = "release"
                mainArtifact(tasks.getByName("release").outputs.files.singleFile)

                configuration.execute(this)
            })
    }
}