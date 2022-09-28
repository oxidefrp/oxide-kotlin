import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val sinPeriodMs = 5011
private const val cosPeriodMs = 7901
private const val streamIntervalMs = 2000

data class InputSignal(
    val name: String,
    val signal: Signal<Double>,
)

fun main() {
    val now = performanceNow()

    val ticks = animationFrameStream()

    var nextNumber = 0

    val baseStream = intervalStream(timeout = streamIntervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }

    val inputSignal1 = InputSignal(
        name = "s1",
        signal = now.map { t ->
            sin((2 * PI * t) / sinPeriodMs)
        },
    )

    val inputSignal2 = InputSignal(
        name = "s2",
        signal = now.map { t ->
            cos((2 * PI * t) / cosPeriodMs)
        },
    )

    val inputSignal3 = InputSignal(
        name = "s3",
        signal = Signal.constant(0.5),
    )

    val inputStream = baseStream.map {
        when (it % 3) {
            0 -> inputSignal1
            1 -> inputSignal2
            else -> inputSignal3
        }
    }

    val output = transform(
        inputStream = inputStream.map { it.signal },
    )

    val outputStream = output.outputStream

    val aMin = -1.25
    val aMax = 1.25

    val widget = Moment.map3(
        buildSignalMeter(
            signal = inputSignal1.signal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = inputSignal2.signal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
        buildSignalMeter(
            signal = inputSignal3.signal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        ),
    ) { inputSignalMeter1, inputSignalMeter2, inputSignalMeter3 ->
        Row(
            gap = 16.0,
            children = listOf(
                inputSignalMeter1,
                inputSignalMeter2,
                inputSignalMeter3,
                buildEventStreamLog(
                    eventStream = inputStream.map { it.name },
                ),
                buildEventStreamLog(
                    eventStream = outputStream.map { it.format(2) },
                ),
            ),
            padding = 4.0,
        )
    }.pullExternally()

    HtmlGenericWidget.embed(
        parent = document.body!!,
        widget = widget,
    )
}
