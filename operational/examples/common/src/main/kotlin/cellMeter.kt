import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.ValueChange

fun buildCellMeter(
    cell: Cell<Any>,
    width: Double = 200.0,
): Column {
    fun buildChangeRow(
        change: ValueChange<Any>,
    ) = Row(
        borderStyle = BorderStyle(
            style = BorderStyle.Style.solid,
            width = 2.0,
            color = "lightgreen",
        ),
        padding = 4.0,
        gap = 8.0,
        children = listOf(
            Text(
                style = TextStyle(
                    fontStyle = TextStyle.FontStyle.italic,
                ),
                text = Cell.constant("Change:"),
            ),
            Text(
                style = TextStyle(
                    fontWeight = TextStyle.FontWeight.bold,
                ),
                text = Cell.constant(change.oldValue.toString()),
            ),
            Text(
                style = TextStyle(
                    fontStyle = TextStyle.FontStyle.italic,
                ),
                text = Cell.constant("⇨"),
            ),
            Text(
                style = TextStyle(
                    fontWeight = TextStyle.FontWeight.bold,
                ),
                text = Cell.constant(change.newValue.toString()),
            ),
        ),
    )

    val statePreview = Row(
        borderStyle = BorderStyle(
            style = BorderStyle.Style.solid,
            width = 2.0,
            color = "green",
        ),
        padding = 4.0,
        gap = 8.0,
        children = listOf(
            Text(
                style = TextStyle(
                    fontStyle = TextStyle.FontStyle.italic,
                ),
                text = Cell.constant("State:"),
            ),
            Text(
                style = TextStyle(
                    fontWeight = TextStyle.FontWeight.bold,
                ),
                text = cell.map { it.toString() },
            ),
        ),
    )

    return Column(
        borderStyle = BorderStyle(
            style = BorderStyle.Style.solid,
            width = 2.0,
            color = "green",
        ),
        children = listOf(
            statePreview,
            GrowableScrollView(
                width = width,
                height = 100.0,
                addChild = cell.changes.map {
                    buildChangeRow(it)
                },
            ),
        ),
    )
}
