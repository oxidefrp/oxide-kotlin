package examples.exampleEventStreamMap

import common.buildConsecutiveIntStream
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

data class Output(
    val inputStream: EventStream<Int>,
    val mappedStream: EventStream<Int>,
)

fun transform(): Signal<Output> = buildConsecutiveIntStream(1.0).map { eventStream ->
    val inputStream = eventStream.filter { it < 4 }

    val mappedStream = inputStream.map { it * 2 }

    Output(
        inputStream = inputStream,
        mappedStream = mappedStream,
    )
}
