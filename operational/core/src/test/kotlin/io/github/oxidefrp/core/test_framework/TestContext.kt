package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.event_stream.CellVertex
import io.github.oxidefrp.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.core.test_framework.input.InputCellVertex
import io.github.oxidefrp.core.test_framework.input.InputEventStreamVertex
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputCellSpec
import io.github.oxidefrp.core.test_framework.shared.InputStreamSpec

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
        override val vertex: CellVertex<A> = InputCellVertex(
            tickStream = tickStream,
            spec = spec,
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
}
