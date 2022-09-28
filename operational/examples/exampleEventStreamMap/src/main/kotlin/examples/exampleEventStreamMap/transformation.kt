package examples.exampleEventStreamMap

import common.buildConsecutiveIntStream
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Moment

data class Output(
    val inputStream: EventStream<Int>,
    val mappedStream: EventStream<Int>,
)

fun transform(): Moment<Output> = buildConsecutiveIntStream(1.0).map { inputStream ->
    val mappedStream = inputStream.map { it * 2 }

    Output(
        inputStream = inputStream,
        mappedStream = mappedStream,
    )
}
