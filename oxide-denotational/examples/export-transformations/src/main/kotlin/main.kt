import examples.allExamples
import java.nio.file.Paths
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val oxideProjectName = "oxide-kotlin"

private val sourcePath =
    Paths.get("examples", "examples", "src", "main", "kotlin", "examples")

private val targetPath =
    Paths.get("..", oxideProjectName, "examples")

fun main() {
    if (sourcePath.notExists()) {
        throw IllegalStateException("Source path ($sourcePath) must exist")
    }

    if (targetPath.notExists()) {
        throw IllegalStateException("Target path ($targetPath) must exist")
    }

    allExamples.forEach { example ->
        val sourceTransformationPath =
            sourcePath.resolve(Paths.get(example.name, "transformation.kt"))

        val targetTransformationPath = targetPath.resolve(
            Paths.get(example.name, "src", "main", "kotlin", "examples", example.name, "transformation.kt"),
        )

        println("Copying transformation $sourceTransformationPath to $targetTransformationPath")

        val sourceTransformationContent = sourceTransformationPath.readBytes().toString(Charsets.UTF_8)

        val targetTransformationContent = sourceTransformationContent.replace(
            oldValue = "io.github.oxidefrp.core",
            newValue = "io.github.oxidefrp.oxide.core",
        )

        targetTransformationPath.writeBytes(targetTransformationContent.toByteArray(Charsets.UTF_8))
    }
}
