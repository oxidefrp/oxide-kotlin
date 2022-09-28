import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Signal

data class Output(
    val outputStream: EventStream<Double>,
)

fun transform(
    inputStream: EventStream<Signal<Double>>,
): Output =
    Output(
        outputStream = EventStream.sample(inputStream)
    )
