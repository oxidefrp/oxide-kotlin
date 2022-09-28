package io.github.oxidefrp.core.test_framework.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.test_framework.validators.CellValidator
import io.github.oxidefrp.core.test_framework.validators.EventStreamValidator
import io.github.oxidefrp.core.test_framework.validators.MomentValidator
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.validators.ValueValidator
import io.github.oxidefrp.core.test_framework.cutOff

/**
 * A specification of a test subject, i.e. a set of expectations what the
 * subject should be like.
 *
 * @param S type of the test subject
 */
internal abstract class SubjectSpec<in S> {
    /**
     * Binds the specification with the actual test [subject] at [tick].
     * This specification should not contain any expectations about moments
     * before [tick].
     *
     * @return A validator validating the [subject] against this specification
     */
    abstract fun bind(
        tick: Tick,
        subject: S,
    ): Validator
}

internal data class EventStreamSpec<A>(
    val matchFrontEventsOnly: Boolean = false,
    val expectedEvents: List<EventOccurrenceDesc<A>>,
) : SubjectSpec<EventStream<A>>() {
    init {
        if (!expectedEvents.isMonotonicallyIncreasingBy { it.tick }) {
            throw IllegalArgumentException("Events stream expected events ticks need to be monotonically increasing")
        }
    }

    val lastTick: Tick
        get() = expectedEvents.lastOrNull()?.tick
            ?: throw IllegalArgumentException("Expected events list should not be empty")

    private val expectedEventByTick: Map<Tick, EventOccurrenceDesc<A>> = expectedEvents.associateBy { it.tick }

    fun getExpectedEvent(tick: Tick): EventOccurrenceDesc<A>? = expectedEventByTick[tick]

    override fun bind(
        tick: Tick,
        subject: EventStream<A>,
    ): Validator {
        if (expectedEvents.any { it.tick < tick }) {
            throw IllegalArgumentException("Can't bind with a tick later than any of the expected events")
        }

        return EventStreamValidator(
            streamSpec = this,
            stream = subject,
            startTick = tick,
        )
    }
}

internal class CellSpec<A>(
    /**
     * The expected initial value of the cell. At the time when the validation
     * starts, it will be verified whether sampling the cell yields that value.
     */
    val expectedInitialValue: A,
    val matchFrontValuesOnly: Boolean = false,
    /**
     * Description of the expected inner cell values. For each inner value,
     * it will be verified whether thew new values stream emits that value and
     * if sampling the cell during the next tick will yield that value.
     */
    val expectedInnerValues: List<CellValueDesc<A>>,
) : SubjectSpec<Cell<A>>() {
    init {
        if (!expectedInnerValues.isMonotonicallyIncreasingBy { it.tick }) {
            throw IllegalArgumentException("Cell expected inner values ticks need to be monotonically increasing")
        }
    }

    override fun bind(
        tick: Tick,
        subject: Cell<A>,
    ): Validator {
        if (expectedInnerValues.any { it.tick < tick }) {
            throw IllegalArgumentException("Can't bind with a tick later than any of the expected inner values")
        }

        return CellValidator(
            cellSpec = buildEffectiveSpec(initialTick = tick),
            cell = subject,
            startTick = tick,
        )
    }

    private fun buildEffectiveSpec(initialTick: Tick) = EffectiveCellSpec(
        sampleSpec = buildSampleSpec(
            initialTick = initialTick,
        ),
        newValuesSpec = EventStreamSpec(
            matchFrontEventsOnly = matchFrontValuesOnly,
            expectedEvents = expectedInnerValues.map {
                EventOccurrenceDesc(
                    tick = it.tick,
                    event = it.value,
                )
            },
        ),
    )

    private fun buildSampleSpec(
        initialTick: Tick,
    ): MomentSpec<A> {
        fun buildExpectedValuesTail(
            previousExpectedValue: A,
            remainingExpectedInnerValues: List<CellValueDesc<A>>,
        ): List<Pair<Tick, A>> {
            val (cellValueDesc, remainingInnerValuesTail) = remainingExpectedInnerValues.cutOff() ?: return emptyList()

            val tick = cellValueDesc.tick
            val newExpectedValue = cellValueDesc.value

            return listOfNotNull(
                // At the given tick, expect that the sampled value is the previous cell's expected value
                tick to previousExpectedValue,
                // At the next tick (if it doesn't exceed the simulation, which is a corner case), expect that the
                // sampled value is the new value. This might duplicate with the entry from the tail if two cells
                // values are tick-adjacent, but it should be exactly the same entry in such case
                tick.next?.let { nextTick -> nextTick to newExpectedValue },
            ) + buildExpectedValuesTail(
                previousExpectedValue = newExpectedValue,
                remainingExpectedInnerValues = remainingInnerValuesTail,
            )
        }

        val initialExpectedValue = this.expectedInitialValue
        val expectedInnerValues = this.expectedInnerValues

        val expectedValues = mapOf(
            initialTick to initialExpectedValue,
        ) + buildExpectedValuesTail(
            previousExpectedValue = initialExpectedValue,
            remainingExpectedInnerValues = expectedInnerValues,
        )

        return MomentSpec(
            expectedValues = expectedValues.mapValues { (_, expectedValue) ->
                ValueSpec(expected = expectedValue)
            },
        )
    }
}

internal data class EffectiveCellSpec<A>(
    val sampleSpec: MomentSpec<A>,
    val newValuesSpec: EventStreamSpec<A>,
)

internal class MomentSpec<S>(
    val expectedValues: Map<Tick, SubjectSpec<S>>,
) : SubjectSpec<Moment<S>>() {
    override fun bind(
        tick: Tick,
        subject: Moment<S>,
    ): Validator {
        if (expectedValues.keys.any { it < tick }) {
            throw IllegalArgumentException("Can't bind with a tick later than any of the expected values")
        }

        return MomentValidator(
            momentSpec = this,
            moment = subject,
            startTick = tick,
        )
    }
}

internal class ValueSpec<A>(
    val expected: A,
) : SubjectSpec<A>() {
    override fun bind(
        tick: Tick,
        subject: A,
    ): Validator = ValueValidator(
        valueSpec = this,
        value = subject,
        startTick = tick,
    )
}
