import examples.exampleSignalSample.transform
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import kotlinx.browser.document

fun main() {
    val now = performanceNowS()

    val output = transform(
        now = now,
    )

    val ticks = animationFrameStream()

    val aMin = 0.25
    val aMax = 2.75

    val widget = Moment.map4(
        buildSignalMeter(
            signal = output.innerSignal1,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = output.innerSignal2,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalText(
            text = output.sampledSignalName,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = output.sampledSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
    ) { innerSignal1Meter, innerSignal2Meter, sampledSignalName, sampledSignalMeter ->
        Row(
            gap = 16.0,
            padding = 4.0,
            children = listOf(
                innerSignal1Meter,
                innerSignal2Meter,
                sampledSignalName,
                sampledSignalMeter,
            ),
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
