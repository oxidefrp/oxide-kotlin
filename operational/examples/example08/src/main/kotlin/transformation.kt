import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

data class Output(
    val outputStream: EventStream<Double>,
)

fun transform(
    inputStream: EventStream<Signal<Double>>,
): Output =
    Output(
        outputStream = EventStream.sample(inputStream)
    )
