import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal

data class Output(
    val outputStream: EventStream<String>,
)

fun transform(
    inputSignal: Signal<Double>,
    inputStream: EventStream<Int>,
): Output =
    Output(
        outputStream = inputStream.probe(inputSignal) { n, d ->
            "${d.format(2)} @ $n"
        }
    )
