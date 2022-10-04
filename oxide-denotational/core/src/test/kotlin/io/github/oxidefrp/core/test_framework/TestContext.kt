package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.Time
import io.github.oxidefrp.core.TimelineSequence
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputCellSpec
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec
import io.github.oxidefrp.core.test_framework.shared.InputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import java.lang.IllegalStateException

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
            initialValue = spec.currentValueSpec.getValue(tick = Tick.Zero),
            innerValues = tickStream.occurrences.mapNotNull { _, tick ->
                spec.newValuesSpec.getOccurrence(tick = tick)
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

    fun <A> buildInputSignal(
        spec: InputSignalSpec<A>,
    ): Signal<A> = object : Signal<A>() {
        override fun at(t: Time): A = spec.getValue(tick = t.asTick)
    }

    fun <A> buildInputSignal(
        provideValue: (tick: Tick) -> A,
    ): Signal<A> = buildInputSignal(
        spec = InputSignalSpec(provideValue = provideValue)
    )

    fun getCurrentTick() = tickStream.currentOccurrence.map {
        val occurrence = it ?: throw IllegalStateException("No current tick")
        occurrence.event
    }
}
