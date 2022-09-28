package examples.exampleEventStream

import common.buildConsecutiveIntStream
import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.Moment

data class Output(
    val eventStream: EventStream<Int>,
)

fun transform(): Moment<Output> =
    buildConsecutiveIntStream(1.0).map { eventStream ->
        Output(
            eventStream = eventStream,
        )
    }
