import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.sin

private const val sinPeriodMs = 5000

fun main() {
    val now = performanceNow()

    val inputSignal = now.map { t ->
        sin((2 * PI * t) / sinPeriodMs)
    }

    val output = transform(
        signal = inputSignal,
    )

    val outputSignal = output.signal

    val ticks = animationFrameStream()

    val widget = buildSignalMeter(
        signal = outputSignal,
        aMin = -1.5,
        aMax = 1.5,
        ticks = ticks,
    ).map { signalMeter ->
        Column(
            children = listOf(
                signalMeter,
            ),
        )
    }.sampleExternally()

    document.body!!.appendChild(widget.buildElement())
}
