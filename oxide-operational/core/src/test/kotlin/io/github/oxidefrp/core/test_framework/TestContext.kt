package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.StatefulCellVertex
import io.github.oxidefrp.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.core.impl.signal.SignalVertex
import io.github.oxidefrp.core.test_framework.input.InputEventStreamVertex
import io.github.oxidefrp.core.test_framework.input.InputSignalVertex
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputCellSpec
import io.github.oxidefrp.core.test_framework.shared.InputMomentSpec
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec
import io.github.oxidefrp.core.test_framework.shared.InputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class TestContext(
    private val tickStream: TickStream,
) {
    fun <A> buildInputStream(
        spec: InputStreamSpec<A>,
    ): EventStream<A> = object : EventStream<A>() {
        override val vertex: EventStreamVertex<A> = InputEventStreamVertex(
            tickStream = tickStream,
            spec = spec,
        )
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
        override val currentValue: Moment<A> = buildInputMoment(
            spec = spec.currentValueSpec,
        )

        override val newValues: EventStream<A> = buildInputStream(
            spec = spec.newValuesSpec,
        )
    }

    fun <A> buildInputCell(
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
        override val vertex: SignalVertex<A> = InputSignalVertex(
            tickStream = tickStream,
            spec = spec,
        )
    }

    fun <A> buildInputSignal(
        provideValue: (tick: Tick) -> A,
    ): Signal<A> = buildInputSignal(
        spec = InputSignalSpec(provideValue = provideValue)
    )

    fun <A> buildInputMoment(
        spec: InputMomentSpec<A>,
    ): Moment<A> = object : Moment<A>() {
        override fun pullCurrentValue(
            transaction: Transaction,
        ): A = spec.getValue(tick = tickStream.currentTick)
    }

    fun getCurrentTick() = tickStream.currentOccurrence.map {
        val occurrence = it ?: throw IllegalStateException("No current tick")
        occurrence.event
    }
}
