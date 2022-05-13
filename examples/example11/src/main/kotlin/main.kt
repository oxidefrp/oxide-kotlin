import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.hold
import kotlinx.browser.document

private const val streamIntervalMs = 2000

fun consecutiveIntsStream(): EventStream<Int> {
    var nextNumber = 0

    return intervalStream(timeout = streamIntervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }
}

fun main() {
    val inputCell = consecutiveIntsStream()
        .hold(0).pullExternally()

    val output = transform(
        inputCell = inputCell,
    )

    val outputStream = output.outputStream

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildCellMeter(inputCell),
            buildEventStreamLog(outputStream, width = 400.0)
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
