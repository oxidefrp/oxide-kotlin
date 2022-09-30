import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.Incident
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Time
import io.github.oxidefrp.core.hold
import io.github.oxidefrp.core.shared.pullOf

fun main() {
    val prompts = EventStream.ofOccurrences(
        Incident(time = Time(1.0), event = "World!"),
    )

    fun buildParagraph(): Moment<Cell<Element>> =
        prompts.map {
            Element(
                tagName = "p",
                child = Cell.constant(
                    Text(content = it),
                ),
            )
        }.hold(
            Element(
                tagName = "p",
                child = Cell.constant(
                    Text(content = "Hello!"),
                ),
            )
        )

    fun buildDocument(): Moment<Document> =
        buildParagraph().map { paragraph ->
            Document(
                root = Element(
                    tagName = "body",
                    child = Cell.constant(
                        Element(
                            tagName = "main",
                            child = paragraph,
                        ),
                    ),
                ),
            )
        }

    fun buildResult(): Moment<Pair<Cell<RealWorld>, RawNode>> =
        buildDocument().pullOf { document ->
            document.build()
                .seed(RealWorld.empty())
        }

    val (stateCell, rootNode) = buildResult()
        .pullDirectly(t = Time.zero)

    val realWorld0 = stateCell.sample().pullDirectly(Time(0.5))
    val realWorld1 = stateCell.sample().pullDirectly(Time(1.5))

    rootNode.print(realWorld0)
    println()
    rootNode.print(realWorld1)
}
