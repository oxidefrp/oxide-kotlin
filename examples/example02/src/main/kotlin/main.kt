import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val periodMs = 5000

fun main() {
    val now = performanceNow()

    val xSignal = now.map { t ->
        sin((2 * PI * t) / periodMs)
    }

    val ySignal = now.map { t ->
        -cos((2 * PI * t) / periodMs)
    }

    val output = transform(
        xSignal = xSignal,
        ySignal = ySignal,
    )

    val outputSignal = output.point

    val ticks = animationFrameStream()

    val widget = Signal.map3(
        buildSignalMeter(
            signal = xSignal,
            aMin = -1.5,
            aMax = 1.5,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = ySignal,
            aMin = -1.5,
            aMax = 1.5,
            ticks = ticks,
        ),
        buildSignalMeter2d(
            signal = outputSignal,
            aRange = 1.5,
            ticks = ticks,
        )
    ) { xSignalMeter, ySignalMeter, xySignalMeter ->
        Row(
            children = listOf(
                xSignalMeter,
                ySignalMeter,
                xySignalMeter,
            ),
            gap = 16.0,
        )
    }.sampleExternally()

    document.body!!.appendChild(widget.buildElement())
}
