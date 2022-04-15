import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.sin

private const val periodMs = 2500

fun main() {
    val now = performanceNow()

    val inputSignal = now.map { t ->
        sin((2 * PI * t) / periodMs)
    }

    val output = transform(
        signal = inputSignal,
    )

    val outputSignal = output.signal

    val ticks = animationFrameStream()

    val aMin = -3.125
    val aMax = 3.125

    val widget = Signal.map2(
        buildSignalMeter(
            signal = inputSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = outputSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
    ) { inputSignalMeter, outputSignalMeter ->
        Row(
            children = listOf(
                inputSignalMeter,
                outputSignalMeter,
            ),
            gap = 16.0,
        )
    }.sampleExternally()

    document.body!!.appendChild(widget.buildElement())
}
