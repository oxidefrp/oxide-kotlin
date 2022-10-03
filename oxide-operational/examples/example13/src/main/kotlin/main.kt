import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.hold
import kotlinx.browser.document

private const val innerCell1IntervalMs = 2347
private const val innerCell2IntervalMs = 2801
private const val outerCellIntervalMs = 7919

fun consecutiveIntsStream(intervalMs: Int): EventStream<Int> {
    var nextNumber = 0

    return intervalStream(timeout = intervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }
}

data class InputCell(
    val name: String,
    val cell: Cell<Int>,
)

fun main() {
    val innerCell1 = InputCell(
        name = "c1",
        cell = consecutiveIntsStream(intervalMs = innerCell1IntervalMs)
            .hold(0).pullExternally(),
    )

    val innerCell2 = InputCell(
        name = "c2",
        cell = consecutiveIntsStream(intervalMs = innerCell2IntervalMs)
            .map { -it }
            .hold(0).pullExternally(),
    )

    val outerCell = consecutiveIntsStream(intervalMs = outerCellIntervalMs)
        .hold(0).pullExternally().map {
            when (it % 2) {
                0 -> innerCell1
                else -> innerCell2
            }
        }

    val output = transform(
        inputCell = outerCell.map { it.cell },
    )

    val outputCell = output.outputCell

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildCellMeter(innerCell1.cell),
            buildCellMeter(innerCell2.cell),
            buildCellMeter(outerCell.map { it.name }),
            buildCellMeter(outputCell, width = 300.0),
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
    parent = document.body!!,
    widget = widget,
)
}
