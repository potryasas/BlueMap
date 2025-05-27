import org.gradle.api.Project
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun Project.gitHash(): String {
    return runCommand("git rev-parse --verify HEAD", "unknown-hash")
}

fun Project.gitClean(): Boolean {
    try {
        if (runCommand("git update-index --refresh", "NOT-CLEAN").equals("NOT-CLEAN")) return false;
        return runCommand("git diff-index HEAD --", "NOT-CLEAN").isEmpty();
    } catch (e: Exception) {
        logger.warn("Git command failed, assuming clean state: ${e.message}")
        return true
    }
}

fun Project.gitVersion(): String {
    try {
        val lastTag = if (runCommand("git tag", "").isEmpty()) "" else runCommand("git describe --tags --abbrev=0", "")
        val lastVersion = if (lastTag.isEmpty()) "0.0" else lastTag.substring(1) // remove the leading 'v'
        val commits = runCommand("git rev-list --count $lastTag..HEAD", "0")
        val branch = runCommand("git branch --show-current", "master")
        val gitVersion = lastVersion +
                (if (branch == "master" || branch.isEmpty()) "" else "-${branch.replace('/', '.')}") +
                (if (commits == "0") "" else "-$commits") +
                (if (gitClean()) "" else "-dirty")

        logger.lifecycle("${project.name} version: $gitVersion")
        return gitVersion
    } catch (e: Exception) {
        logger.warn("Git command failed, using fallback version: ${e.message}")
        return "0.0.0-dev"
    }
}

fun Project.gitIsRelease(): Boolean {
    try {
        val lastTag = if (runCommand("git tag", "").isEmpty()) "" else runCommand("git describe --tags --abbrev=0", "")
        val commits = runCommand("git rev-list --count $lastTag..HEAD", "0")
        return commits == "0" && gitClean()
    } catch (e: Exception) {
        logger.warn("Git command failed, assuming non-release: ${e.message}")
        return false
    }
}

fun Project.releaseNotes(): String {
    val file = rootProject.projectDir.resolve("release.md")
    if (!file.exists()) return ""

    return file
        .readText()
        .replace("{version}", project.version.toString())
}

private fun Project.runCommand(cmd: String, fallback: String? = null): String {
    try {
        ProcessBuilder(cmd.split("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)".toRegex()))
            .directory(projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .apply {
                if (!waitFor(10, TimeUnit.SECONDS)) {
                    if (fallback != null) return fallback
                    throw TimeoutException("Failed to execute command: '$cmd'")
                }
            }
            .run {
                val exitCode = waitFor()
                if (exitCode == 0) return inputStream.bufferedReader().readText().trim()

                val error = errorStream.bufferedReader().readText().trim()
                logger.warn("Failed to execute command '$cmd': $error")
                if (fallback != null) return fallback
                throw IOException(error)
            }
    } catch (e: Exception) {
        logger.warn("Exception executing command '$cmd': ${e.message}")
        if (fallback != null) return fallback
        throw e
    }
}
