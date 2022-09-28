package examples.exampleSignal

import io.github.oxidefrp.semantics.Signal
import kotlin.math.abs
import kotlin.math.sin

data class Output(
    val signal: Signal<Double>,
)

fun transform(
    now: Signal<Double>,
): Output {
    fun adj(t: Double): Double {
        val period = 20
        val tp = t % period
        return abs(1 - tp / (period / 2))
    }

    val signal = now.map { t ->
        sin(t * 2) * adj(t) + 1.5
    }

    return Output(
        signal = signal,
    )
}
