package io.github.oxidefrp.semantics.test_framework

import io.github.oxidefrp.semantics.Cell
import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.Moment
import io.github.oxidefrp.semantics.test_framework.shared.CellIssue
import io.github.oxidefrp.semantics.test_framework.shared.EffectiveCellSpec
import io.github.oxidefrp.semantics.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.semantics.test_framework.shared.EventStreamIssue
import io.github.oxidefrp.semantics.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.semantics.test_framework.shared.IncorrectValueIssue
import io.github.oxidefrp.semantics.test_framework.shared.Issue
import io.github.oxidefrp.semantics.test_framework.shared.MomentIssue
import io.github.oxidefrp.semantics.test_framework.shared.MomentSpec
import io.github.oxidefrp.semantics.test_framework.shared.Tick
import io.github.oxidefrp.semantics.test_framework.shared.ValueSpec

internal abstract class Validator {
    abstract fun validate(): Issue?
}

internal class EventStreamValidator<A>(
    private val streamSpec: EventStreamSpec<A>,
    private val stream: EventStream<A>,
    /**
     * [startTick] could be used to more explicitly reject events that happened
     * before the validation start time, but such events will still be rejected
     * as non-matching the specification.
     */
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate() = EventStreamIssue.validate(
        streamSpec = streamSpec,
        events = findEvents(
            stream = stream,
        ),
    )
}

internal class CellValidator<A>(
    private val cellSpec: EffectiveCellSpec<A>,
    private val cell: Cell<A>,
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate(): Issue? = CellIssue.validate(
        cellSpec = cellSpec,
        sampleIssues = findValueIssues(
            momentSpec = cellSpec.sampleSpec,
            moment = cell.sample(),
        ),
        newValues = findEvents(
            stream = cell.newValues,
        ),
    )
}

internal class MomentValidator<S>(
    private val momentSpec: MomentSpec<S>,
    private val moment: Moment<S>,
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate(): MomentIssue? = MomentIssue.validate(
        momentSpec = momentSpec,
        valueIssues = findValueIssues(
            momentSpec = momentSpec,
            moment = moment,
        ),
    )
}

internal class ValueValidator<A>(
    private val valueSpec: ValueSpec<A>,
    private val value: A,
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate() = IncorrectValueIssue.validate(
        valueSpec = valueSpec,
        value = value,
    )
}

private fun <A> findEvents(
    stream: EventStream<A>,
): List<EventOccurrenceDesc<A>> = stream.occurrences
    .takeNotAfter(Tick.maxTick.asTime)
    .toList().map {
        EventOccurrenceDesc(
            tick = it.time.asTick,
            event = it.event,
        )
    }

private fun <S> findValueIssues(
    momentSpec: MomentSpec<S>,
    moment: Moment<S>,
): Map<Tick, Issue?> = momentSpec.expectedValues.mapValues { (tick, valueSpec) ->
    val subject = moment.pullDirectly(t = tick.asTime)

    valueSpec.bind(
        tick = tick,
        subject = subject,
    ).validate()
}
