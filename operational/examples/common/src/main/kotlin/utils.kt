import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.impl.event_stream.ExternalSubscription
import kotlinx.browser.window

fun intervalStream(timeout: Int): EventStream<Unit> =
    EventStream.source { emit ->
        val handle = window.setInterval(
            handler = { emit(Unit) },
            timeout = timeout,
        )

        object : ExternalSubscription {
            override fun cancel() {
                window.clearInterval(handle = handle)
            }
        }
    }

fun performanceNow(): Signal<Double> =
    Signal.source(window.performance::now)

fun performanceNowS(): Signal<Double> =
    performanceNow().map { it / 1000.0 }

fun intervalNowStream(timeout: Int): EventStream<Double> {
    val now = performanceNow()
    val stream = intervalStream(timeout = timeout)
    return stream.probe(now)
}

private fun subscribeToAnimationFrames(callback: () -> Unit): ExternalSubscription =
    object : ExternalSubscription {
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

fun Double.format(digits: Int): String =
    this.asDynamic().toFixed(digits) as String
