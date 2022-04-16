import examples.exampleSignalMap.transform
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document

fun main() {
    val now = performanceNowS()

    val output = transform(
        now = now,
    )

    val ticks = animationFrameStream()

    val aMin = 0.0
    val aMax = 4.0

    val widget = Signal.map2(
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
    }.sampleExternally()

    document.body!!.appendChild(widget.buildElement())
}
