import io.github.oxidefrp.core.EventStream
import kotlinx.browser.document

private const val inputStreamIntervalMs = 2347

fun consecutiveIntsStream(intervalMs: Int): EventStream<Int> {
    var nextNumber = 0

    return intervalStream(timeout = intervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }
}

fun main() {
    val inputStream = consecutiveIntsStream(intervalMs = inputStreamIntervalMs)

    val output = transform(
        inputStream = inputStream,
    )

    val outputCell = output.outputCell.pullExternally()

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildEventStreamLog(inputStream),
            buildCellMeter(outputCell),
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
