package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.test_framework.asTick
import io.github.oxidefrp.core.test_framework.asTime
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.Issue
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal abstract class Validator {
    abstract fun validate(): Issue?
}

internal fun <A> findEvents(
    startTick: Tick,
    stream: EventStream<A>,
): List<EventOccurrenceDesc<A>> = stream.occurrences
    .takeNotBefore(startTick.asTime)
    .takeNotAfter(Tick.maxTick.asTime)
    .toList().map {
        EventOccurrenceDesc(
            tick = it.time.asTick,
            event = it.event,
        )
    }

internal fun <S> findValueIssues(
    momentSpec: MomentSpec<S>,
    moment: Moment<S>,
): Map<Tick, Issue?> = momentSpec.expectedValues.mapValues { (tick, valueSpec) ->
    val subject = moment.pullDirectly(t = tick.asTime)

    valueSpec.bind(
        tick = tick,
        subject = subject,
    ).validate()
}
