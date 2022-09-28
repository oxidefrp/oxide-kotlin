import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import kotlin.math.PI
import kotlin.math.sin

private const val sinPeriodMs = 6961
private const val streamIntervalMs = 2069

fun main() {
    val now = performanceNow()

    val ticks = animationFrameStream()

    var nextNumber = 0

    val inputStream = intervalStream(timeout = streamIntervalMs).map {
        /// FIXME: This escapes the semantics
        // Replace this with accum when loops are implemented
        ++nextNumber
    }

    val inputSignal = now.map { t ->
        sin((2 * PI * t) / sinPeriodMs)
    }

    val output = transform(
        inputStream = inputStream,
        inputSignal = inputSignal,
    )

    val outputStream = output.outputStream

    val aMin = -1.25
    val aMax = 1.25

    val widget = Moment.map1(
        buildSignalMeter(
            signal = inputSignal,
            aMin = aMin,
            aMax = aMax,
            ticks = ticks,
        )
    ) { inputSignalMeter ->
        Row(
            gap = 16.0,
            children = listOf(
                buildEventStreamLog(
                    eventStream = inputStream,
                ),
                inputSignalMeter,
                buildEventStreamLog(
                    eventStream = outputStream,
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
