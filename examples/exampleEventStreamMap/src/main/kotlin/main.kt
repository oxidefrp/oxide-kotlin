import examples.exampleEventStreamMap.transform
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document

fun main() {
    val widget = Signal.map1(
        transform(),
    ) { output ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                buildEventStreamLog(
                    eventStream = output.inputStream,
                ),
                buildEventStreamLog(
                    eventStream = output.mappedStream,
                ),
            ),
        )
    }.sampleExternally()

    document.body!!.appendChild(widget.buildElement())
}
