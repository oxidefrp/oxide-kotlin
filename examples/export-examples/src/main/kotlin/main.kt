import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

private const val siteHostname = "oxidefrp.github.io"

private val examplesRelativePath: Path =
    Paths.get("examples")

private val indexRelativePath: Path =
    Paths.get("src", "main", "resources", "index.html")

private fun jsBundleRelativePath(exampleName: String): Path =
    Paths.get("build", "distributions", "$exampleName.js")

private val targetPath =
    Paths.get("..", siteHostname, "docs", "examples")

fun main() {
    if (targetPath.notExists()) {
        throw IllegalStateException("Target path ($targetPath) must exist")
    }

    val git = Git.open(File("."))
    val head = git.repository.resolve(Constants.HEAD)
    val headSha1 = head.name

    println("HEAD SHA1: $headSha1")

    examplesRelativePath.listDirectoryEntries().filter {
        it.isDirectory() && it.name.startsWith("example")
    }.forEach { examplePath ->
        val exampleName = examplePath.name

        val indexPath = examplePath.resolve(indexRelativePath)
        val jsBundlePath = examplePath.resolve(jsBundleRelativePath(exampleName = exampleName))

        val targetExamplePath = targetPath.resolve(exampleName)

        targetExamplePath.createDirectories()

        val targetIndexPath = targetExamplePath.resolve(indexPath.name)
        val targetJsBundlePath = targetExamplePath.resolve(jsBundlePath.name)

        println("Copying $indexPath to $targetIndexPath")
        println("Copying $jsBundlePath to $targetJsBundlePath")
        println()

        indexPath.copyTo(
            target = targetIndexPath,
            overwrite = true,
        )

        jsBundlePath.copyTo(
            target = targetJsBundlePath,
            overwrite = true,
        )

        println("[(source code)](https://github.com/oxidefrp/oxide-kotlin/blob/main/examples/$exampleName/src/main/kotlin/examples/$exampleName/transformation.kt) [(live preview)](https://$siteHostname/examples/$exampleName/)")

        println()
    }
}
