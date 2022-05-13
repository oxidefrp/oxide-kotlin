import kotlinx.browser.document

fun main() {
    var nextNumber = 0

    val inputStream = intervalStream(timeout = 3000).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }

    val output = transform(
        inputStream = inputStream,
    )

    val outputStream = output.outputStream

    val widget = Row(
        gap = 16.0,
        children = listOf(
            buildEventStreamLog(
                eventStream = inputStream,
            ),
            buildEventStreamLog(
                eventStream = outputStream,
            ),
        ),
        padding = 4.0,
    )

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
