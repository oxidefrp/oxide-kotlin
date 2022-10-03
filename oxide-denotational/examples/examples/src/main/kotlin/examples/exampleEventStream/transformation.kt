package examples.exampleEventStream

import common.buildConsecutiveIntStream
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

data class Output(
    val eventStream: EventStream<Int>,
)

fun transform(): Signal<Output> =
    buildConsecutiveIntStream(1.0).map { eventStream ->
        Output(
            eventStream = eventStream.filter { it < 4 },
        )
    }
