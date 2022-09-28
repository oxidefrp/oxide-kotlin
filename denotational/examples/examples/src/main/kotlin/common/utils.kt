package common

import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.Incident
import io.github.oxidefrp.semantics.PureSequence
import io.github.oxidefrp.semantics.Signal
import io.github.oxidefrp.semantics.Time

val now: Signal<Double> = object : Signal<Double>() {
    override fun at(t: Time): Double = t.t
}

fun buildConsecutiveIntStream(intervalS: Double): Signal<EventStream<Int>> =
    Signal.constant(
        EventStream.ofSequence(
            PureSequence.generate(
                seed = Incident(
                    time = Time(t = intervalS),
                    event = 1,
                ),
                nextFunction = {
                    Incident(
                        time = Time(t = it.time.t + intervalS),
                        event = it.event + 1,
                    )
                }
            ).take(100),
        ),
    )
