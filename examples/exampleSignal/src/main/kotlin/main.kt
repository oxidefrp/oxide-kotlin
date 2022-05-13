import examples.exampleSignal.transform
import kotlinx.browser.document

fun main() {
    val now = performanceNowS()

    val output = transform(
        now = now,
    )

    val outputSignal = output.signal

    val ticks = animationFrameStream()

    val widget = buildSignalMeter(
        signal = outputSignal,
        aMin = 0.25,
        aMax = 2.75,
        ticks = ticks,
    ).map { signalMeter ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                signalMeter,
            ),
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
