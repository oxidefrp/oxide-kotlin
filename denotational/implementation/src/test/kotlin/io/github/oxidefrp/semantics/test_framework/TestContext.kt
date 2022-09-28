package io.github.oxidefrp.semantics.test_framework

import io.github.oxidefrp.semantics.Cell
import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.TimelineSequence
import io.github.oxidefrp.semantics.test_framework.shared.CellValueSpec
import io.github.oxidefrp.semantics.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.semantics.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.semantics.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.semantics.test_framework.shared.InputCellSpec
import io.github.oxidefrp.semantics.test_framework.shared.InputStreamSpec
import io.github.oxidefrp.semantics.test_framework.shared.Tick

internal class TestContext(
    private val tickStream: EventStream<Tick>,
) {
    fun <A> buildInputStream(
        spec: InputStreamSpec<A>,
    ): EventStream<A> = object : EventStream<A>() {
        override val occurrences: TimelineSequence<A> = tickStream.occurrences.mapNotNull { _, tick ->
            spec.getOccurrence(tick = tick)
        }.map { _, it -> it.event }
    }

    fun <A> buildInputStream(
        vararg events: EventOccurrenceDesc<A>,
    ): EventStream<A> = buildInputStream(
        spec = FiniteInputStreamSpec(
            events = events,
        ),
    )

    fun <A> buildInputCell(
        spec: InputCellSpec<A>,
    ): Cell<A> = object : Cell<A>() {
        override val segmentSequence: FullSegmentSequence<A> = FullSegmentSequence(
            initialValue = spec.getOldValue(tick = Tick.Zero),
            innerValues = tickStream.occurrences.mapNotNull { _, tick ->
                spec.getNewValueOccurrence(tick = tick)
            }.map { _, newValueOccurrence -> newValueOccurrence.event }
        )
    }

    fun <A : Any> buildInputCell(
        initialValue: A,
        vararg innerValues: CellValueSpec<A>,
    ): Cell<A> = buildInputCell(
        spec = FiniteInputCellSpec(
            initialValue = initialValue,
            innerValues = innerValues,
        ),
    )
}
