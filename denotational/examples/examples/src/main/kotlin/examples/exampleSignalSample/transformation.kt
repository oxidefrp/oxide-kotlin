package examples.exampleSignalSample

import io.github.oxidefrp.semantics.Signal
import io.github.oxidefrp.semantics.sampleOf
import kotlin.math.PI
import kotlin.math.sin

private const val innerSignal1Period = 2.0
private const val innerSignal2Period = 6.0

data class Output(
    val innerSignal1: Signal<Double>,
    val innerSignal2: Signal<Double>,
    val sampledSignalName: Signal<String>,
    val sampledSignal: Signal<Double>,
)

data class InnerSignal(
    val name: String,
    val signal: Signal<Double>,
)

fun transform(
    now: Signal<Double>,
): Output {
    val innerSignal1 = InnerSignal(
        name = "s1",
        signal = now.map { t ->
            sin((2 * PI * t) / innerSignal1Period) + 1.5
        },
    )

    val innerSignal2 = InnerSignal(
        name = "s2",
        signal = now.map { t ->
            sin((2 * PI * t) / innerSignal2Period) + 1.5
        },
    )

    val outerSignal = now.map {
        val tp = it % 12
        when {
            tp < 6 -> innerSignal1
            else -> innerSignal2
        }
    }

    return Output(
        innerSignal1 = innerSignal1.signal,
        innerSignal2 = innerSignal2.signal,
        sampledSignalName = outerSignal.map { it.name },
        sampledSignal = outerSignal.sampleOf { it.signal },
    )
}
