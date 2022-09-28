import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.hold
import kotlinx.browser.document

private const val cell1IntervalMs = 7103
private const val cell2IntervalMs = 5351

fun consecutiveIntsStream(intervalMs: Int): EventStream<Int> {
    var nextNumber = 0

    return intervalStream(timeout = intervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }
}

fun main() {
    val inputCell1 = consecutiveIntsStream(intervalMs = cell1IntervalMs)
        .hold(0).pullExternally()

    val inputCell2 = consecutiveIntsStream(intervalMs = cell2IntervalMs)
        .hold(100).pullExternally()

    val output = transform(
        inputCell1 = inputCell1,
        inputCell2 = inputCell2,
    )

    val outputCell = output.outputCell

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildCellMeter(inputCell1),
            buildCellMeter(inputCell2),
            buildCellMeter(outputCell, width = 300.0),
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
