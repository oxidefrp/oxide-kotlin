import examples.exampleSignalMap.transform
import io.github.oxidefrp.oxide.core.Moment
import kotlinx.browser.document

fun main() {
    val now = performanceNowS()

    val output = transform(
        now = now,
    )

    val ticks = animationFrameStream()

    val aMin = 0.0
    val aMax = 4.0

    val widget = Moment.map2(
        buildSignalMeter(
            signal = output.inputSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = output.mappedSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
    ) { inputSignalMeter, mappedSignalMeter ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                inputSignalMeter,
                mappedSignalMeter,
            ),
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
