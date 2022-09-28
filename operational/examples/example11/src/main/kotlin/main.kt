import common.buildConsecutiveIntStream
import io.github.oxidefrp.oxide.core.hold
import io.github.oxidefrp.oxide.core.pullOf
import kotlinx.browser.document

fun main() {
    val inputCell = buildConsecutiveIntStream(intervalS = 2.0).pullOf {
        it.hold(0)
    }.pullExternally()

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
