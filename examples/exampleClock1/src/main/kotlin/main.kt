import examples.exampleClock1.buildClock
import io.github.oxidefrp.oxide.core.Signal
import kotlinx.browser.document
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

val timeZone: TimeZone = TimeZone.currentSystemDefault()

val now: Signal<Instant> = Signal.source {
    Clock.System.now()
}

fun main() {
    val widget = Row(
        gap = 4.0,
        padding = 4.0,
        children = listOf(
            buildClock(
                timeZone = timeZone,
                now = now,
            ),
        ),
    )

    document.body!!.appendChild(widget.buildElementExternally())
}
