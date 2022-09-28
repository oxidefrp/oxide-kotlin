import examples.allExamples
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

private const val siteHostname = "oxidefrp.github.io"

private val targetPath =
    Paths.get("..", "..", siteHostname, "docs", "examples")

fun main() {
    if (targetPath.notExists()) {
        throw IllegalStateException("Target path ($targetPath) must exist")
    }

    allExamples.forEach { example ->
        println("Processing example ${example.name}...\n")

        val targetPlotsPath = targetPath.resolve(Paths.get(example.name, "plots"))

        targetPlotsPath.createDirectories()

        example.plots.forEach { plot ->
            val plotPath = targetPlotsPath.resolve("${plot.name}.svg")

            println("<img title=\"\" src=\"https://oxidefrp.github.io/examples/${example.name}/plots/${plot.name}.svg\" alt=\"\" data-align=\"center\">")

            plot.graphic.writeToFile(plotPath)
        }

        println()
    }
}
