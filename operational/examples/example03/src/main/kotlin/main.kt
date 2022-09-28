import io.github.oxidefrp.oxide.core.Moment
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

    val widget = Moment.map2(
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
            gap = 16.0,
            children = listOf(
                inputSignalMeter,
                outputSignalMeter,
            ),
            padding = 4.0,
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
