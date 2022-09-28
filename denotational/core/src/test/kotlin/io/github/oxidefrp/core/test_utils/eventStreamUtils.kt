package io.github.oxidefrp.core.test_utils

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Incident
import io.github.oxidefrp.core.PureSequence
import io.github.oxidefrp.core.Time

// Build event stream in form [(1.0, 1), (2.0, 2), (3.0, 3), ...]
fun generateIntegerEventStream(
    seed: Int = 1,
    step: Int = 1,
): EventStream<Int> = generateEventStream(
    seed = seed,
    nextFunction = { it + step },
)

fun <T> generateEventStream(
    t0: Double = 1.0,
    seed: T,
    nextFunction: (T) -> T,
): EventStream<T> = EventStream.ofSequence(
    generateOccurrencesSequence(
        t0 = t0,
        seed = seed,
        nextFunction = nextFunction,
    ),
)

fun <T> generateOccurrencesSequence(
    t0: Double = 1.0,
    seed: T,
    nextFunction: (T) -> T,
): PureSequence<Incident<T>> = PureSequence.generate(
    seed = Incident(
        time = Time(t = t0),
        event = seed,
    ),
    nextFunction = {
        Incident(
            time = Time(t = it.time.t + 1.0),
            event = nextFunction(it.event),
        )
    }
)