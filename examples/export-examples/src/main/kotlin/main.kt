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

        indexPath.copyTo(
            target = targetIndexPath,
            overwrite = true,
        )

        jsBundlePath.copyTo(
            target = targetJsBundlePath,
            overwrite = true,
        )
    }
}
