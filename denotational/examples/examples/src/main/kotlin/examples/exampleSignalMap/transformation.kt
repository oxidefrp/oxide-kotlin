package examples.exampleSignalMap

import io.github.oxidefrp.semantics.Signal
import kotlin.math.sin

data class Output(
    val inputSignal: Signal<Double>,
    val mappedSignal: Signal<Double>,
)

fun transform(
    now: Signal<Double>,
): Output {
    val inputSignal = now.map { t ->
        sin(t) + 2.0
    }

    val mappedSignal = inputSignal.map { it / 2 }

    return Output(
        inputSignal = inputSignal,
        mappedSignal = mappedSignal,
    )
}
