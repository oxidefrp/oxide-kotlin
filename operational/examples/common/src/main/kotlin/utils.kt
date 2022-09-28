import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import kotlinx.browser.window

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

fun performanceNowS(): Signal<Double> =
    performanceNow().map { it / 1000.0 }

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

fun Double.format(digits: Int): String =
    this.asDynamic().toFixed(digits) as String
