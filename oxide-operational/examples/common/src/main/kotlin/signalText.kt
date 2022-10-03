import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

fun buildSignalText(
    text: Signal<String>,
    ticks: EventStream<Unit>,
) =
    text.discretize(ticks = ticks).map {
        Text(text = it)
    }
