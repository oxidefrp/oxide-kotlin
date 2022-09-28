package examples.exampleSignalMap2

import io.github.oxidefrp.semantics.Signal
import kotlin.math.cos
import kotlin.math.sin

data class Output(
    val inputSignal1: Signal<Double>,
    val inputSignal2: Signal<Double>,
    val mappedSignal: Signal<Double>,
)

fun transform(
    now: Signal<Double>,
): Output {
    val inputSignal1 = now.map { t ->
        sin(t) + 2.0
    }

    val inputSignal2 = now.map { t ->
        cos(t) + 2.0
    }

    val mappedSignal = Signal.map2(inputSignal1, inputSignal2) { a, b ->
        a + b
    }

    return Output(
        inputSignal1 = inputSignal1,
        inputSignal2 = inputSignal2,
        mappedSignal = mappedSignal,
    )
}
