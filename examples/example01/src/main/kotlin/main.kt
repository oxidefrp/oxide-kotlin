import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.PI
import kotlin.math.sin

private const val sinPeriodMs = 5000

fun intervalStream(timeout: Int): EventStream<Unit> =
    EventStream.source { emit ->
        val handle = window.setInterval(
            handler = { emit(Unit) },
            timeout = timeout,
        )

        object : Subscription {
            override fun cancel() {
                window.clearInterval(handle = handle)
            }
        }
    }

fun performanceNow(): Signal<Double> =
    Signal.source(window.performance::now)

fun intervalNowStream(timeout: Int): EventStream<Double> {
    val now = performanceNow()
    val stream = intervalStream(timeout = timeout)
    return stream.probe(now)
}

private fun subscribeToAnimationFrames(callback: () -> Unit): Subscription =
    object : Subscription {
        fun requestFrame(): Int = window.requestAnimationFrame {
            callback()
            requestNextFrame()
        }

        fun requestNextFrame() {
            handle = requestFrame()
        }

        private var handle: Int = requestFrame()

        override fun cancel() {
            window.cancelAnimationFrame(handle)
        }
    }

fun animationFrameStream(): EventStream<Unit> =
    EventStream.source { emit ->
        subscribeToAnimationFrames {
            emit(Unit)
        }
    }

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
