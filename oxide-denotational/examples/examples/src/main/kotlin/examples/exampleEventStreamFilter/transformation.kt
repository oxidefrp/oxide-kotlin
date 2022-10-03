package examples.exampleEventStreamFilter

import common.buildConsecutiveIntStream
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal

data class Output(
    val inputStream: EventStream<Int>,
    val filteredStream: EventStream<Int>,
)

fun transform(): Signal<Output> = buildConsecutiveIntStream(1.0).map { eventStream ->
    val inputStream = eventStream.map { it % 5 + 1 }

    val filteredStream = inputStream.filter(::isEven)

    Output(
        inputStream = inputStream,
        filteredStream = filteredStream,
    )
}

private fun isEven(n: Int): Boolean =
    n % 2 == 0
