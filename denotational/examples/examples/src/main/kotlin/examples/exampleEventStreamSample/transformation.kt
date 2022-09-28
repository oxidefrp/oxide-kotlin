package examples.exampleEventStreamSample

import common.buildConsecutiveIntStream
import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.Signal
import kotlin.math.cos
import kotlin.math.sin

data class Output(
    val inputSignal1: Signal<Double>,
    val inputSignal2: Signal<Double>,
    val inputSignal3: Signal<Double>,
    val signalNameStream: EventStream<String>,
    val signalStream: EventStream<Signal<Double>>,
    val sampleStream: EventStream<Double>,
)

data class InputSignal(
    val name: String,
    val signal: Signal<Double>,
)

fun transform(
    now: Signal<Double>,
): Signal<Output> = buildConsecutiveIntStream(1.0).map { intStream ->
    val inputSignal1 = InputSignal(
        name = "s1",
        signal = now.map { t ->
            0.5 * sin(t) + 1.0
        }
    )

    val inputSignal2 = InputSignal(
        name = "s2",
        signal = now.map { t ->
            0.5 * cos(t) + 3.0
        }
    )

    val inputSignal3 = InputSignal(
        name = "s3",
        signal = now.map { t ->
            0.5 * sin(t) + 5.0
        }
    )

    val inputSignalStream = intStream.map {
        when ((it - 1) % 3) {
            0 -> inputSignal1
            1 -> inputSignal2
            else -> inputSignal3
        }
    }

    val signalNameStream = inputSignalStream.map { it.name }

    val signalStream = inputSignalStream.map { it.signal }

    val sampleStream = EventStream.sample(signalStream)

    Output(
        inputSignal1 = inputSignal1.signal,
        inputSignal2 = inputSignal2.signal,
        inputSignal3 = inputSignal3.signal,
        signalNameStream = signalNameStream,
        signalStream = signalStream,
        sampleStream = sampleStream,
    )
}
