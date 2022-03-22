package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.cell.ConstantCellVertex
import io.github.oxidefrp.oxide.event_stream.CellVertex
import io.github.oxidefrp.oxide.cell.MapCellVertex
import io.github.oxidefrp.oxide.cell.SwitchCellVertex
import io.github.oxidefrp.oxide.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.event_stream.ObservingEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.Subscription
import io.github.oxidefrp.oxide.signal.SamplingSignalVertex
import io.github.oxidefrp.oxide.signal.SignalVertex

internal class CellSampleSignalVertex<A>(
    private val cell: CellVertex<A>,
) : SamplingSignalVertex<A>() {
    override fun pullCurrentValueUncached(transaction: Transaction): A =
        cell.oldValue
}

internal class CellChangesEventStreamVertex<A>(
    private val cell: CellVertex<A>,
) : ObservingEventStreamVertex<ValueChange<A>>() {
    override fun observe(): Subscription =
        cell.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<ValueChange<A>> =
        cell.pullNewValue(transaction = transaction).map { newValue ->
            ValueChange(
                oldValue = cell.oldValue,
                newValue = newValue,
            )
        }
}

data class ValueChange<out A>(
    val oldValue: A,
    val newValue: A,
)

abstract class Cell<out A> {
    companion object {
        fun <A> constant(value: A): Cell<A> =
            object : Cell<A>() {
                override val vertex: CellVertex<A> =
                    ConstantCellVertex(value = value)
            }

        fun <A> switch(cell: Cell<Cell<A>>): Cell<A> =
            object : Cell<A>() {
                override val vertex: CellVertex<A> =
                    SwitchCellVertex(
                        source = cell.vertex,
                    )
            }
    }

    internal abstract val vertex: CellVertex<A>

    val changes: EventStream<ValueChange<A>> by lazy {
        object : EventStream<ValueChange<A>>() {
            override val vertex: EventStreamVertex<ValueChange<A>> =
                CellChangesEventStreamVertex(
                    cell = this@Cell.vertex,
                )
        }
    }

    val value: Signal<A> by lazy {
        object : Signal<A>() {
            override val vertex: SignalVertex<A> =
                CellSampleSignalVertex(cell = this@Cell.vertex)
        }
    }

    fun <B> map(transform: (A) -> B): Cell<B> =
        object : Cell<B>() {
            override val vertex: CellVertex<B> =
                MapCellVertex(
                    source = this@Cell.vertex,
                    transform = transform,
                )
        }

    fun <B> switchOf(transform: (A) -> Cell<B>): Cell<B> =
        switch(map(transform))
}
