package examples.exampleEventStreamProbe

import common.buildConsecutiveIntStream
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal
import kotlin.math.sin

data class Output(
    val inputStream: EventStream<Double>,
    val inputSignal: Signal<Double>,
    val probedStream: EventStream<Double>,
)

fun transform(
    now: Signal<Double>,
): Signal<Output> = buildConsecutiveIntStream(0.5).map { intStream ->
    val inputStream = intStream.map { (it % 24) / 12.0 }

    val inputSignal = now.map { t ->
        sin(t) + 2.0
    }

    val probedStream = inputStream.probe(inputSignal) { a, b ->
        a * b
    }

    Output(
        inputStream = inputStream,
        inputSignal = inputSignal,
        probedStream = probedStream,
    )
}
