import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.hold
import kotlinx.browser.document

private const val innerStream1IntervalMs = 2347
private const val innerStream2IntervalMs = 2801
private const val outerCellIntervalMs = 7919

fun consecutiveIntsStream(intervalMs: Int): EventStream<Int> {
    var nextNumber = 0

    return intervalStream(timeout = intervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }
}

data class InputStream(
    val name: String,
    val stream: EventStream<Int>,
)

fun main() {
    val innerStream1 = InputStream(
        name = "s1",
        stream = consecutiveIntsStream(intervalMs = innerStream1IntervalMs),
    )

    val innerStream2 = InputStream(
        name = "s2",
        stream = consecutiveIntsStream(intervalMs = innerStream2IntervalMs).map { -it },
    )

    val outerCell = consecutiveIntsStream(intervalMs = outerCellIntervalMs)
        .hold(0).pullExternally().map {
            when (it % 2) {
                0 -> innerStream1
                else -> innerStream2
            }
        }

    val output = transform(
        inputCell = outerCell.map { it.stream },
    )

    val outputStream = output.outputStream

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildEventStreamLog(innerStream1.stream),
            buildEventStreamLog(innerStream2.stream),
            buildCellMeter(outerCell.map { it.name }),
            buildEventStreamLog(outputStream),
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
