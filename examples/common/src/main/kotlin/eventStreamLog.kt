import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream

fun buildEventStreamLog(
    eventStream: EventStream<Any>,
    width: Double = 200.0,
): Column {
    fun buildRow(text: String) = Row(
        borderStyle = BorderStyle(
            style = BorderStyle.Style.solid,
            width = 2.0,
            color = "lightblue",
        ),
        padding = 4.0,
        gap = 8.0,
        children = listOf(
            Text(
                style = TextStyle(
                    fontStyle = TextStyle.FontStyle.italic,
                ),
                text = Cell.constant("Event:"),
            ),
            Text(
                style = TextStyle(
                    fontWeight = TextStyle.FontWeight.bold,
                ),
                text = Cell.constant(text),
            ),
        ),
    )

    return Column(
        borderStyle = BorderStyle(
            style = BorderStyle.Style.solid,
            width = 2.0,
            color = "blue",
        ),
        children = listOf(
            GrowableScrollView(
                width = width,
                height = 100.0,
                addChild = eventStream.map {
                    buildRow(it.toString())
                },
            ),
        ),
    )
}
