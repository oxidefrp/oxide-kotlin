import examples.exampleEventStream.transform
import io.github.oxidefrp.core.Moment
import kotlinx.browser.document

fun main() {
    val widget = Moment.map1(
        transform(),
    ) { output ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                buildEventStreamLog(
                    eventStream = output.eventStream,
                ),
            ),
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
