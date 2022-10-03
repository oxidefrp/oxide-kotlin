import examples.exampleSignalMap2.transform
import io.github.oxidefrp.core.Moment
import kotlinx.browser.document

fun main() {
    val now = performanceNowS()

    val output = transform(
        now = now,
    )

    val ticks = animationFrameStream()

    val aMin = 0.0
    val aMax = 6.0

    val widget = Moment.map3(
        buildSignalMeter(
            signal = output.inputSignal1,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = output.inputSignal2,
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
    ) { inputSignal1Meter, inputSignal2Meter, mappedSignalMeter ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                inputSignal1Meter,
                inputSignal2Meter,
                mappedSignalMeter,
            ),
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
