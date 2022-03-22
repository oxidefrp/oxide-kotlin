package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.cell.ApplyCellVertex
import io.github.oxidefrp.oxide.core.cell.ConstantCellVertex
import io.github.oxidefrp.oxide.core.cell.MapCellVertex
import io.github.oxidefrp.oxide.core.cell.SwitchCellVertex
import io.github.oxidefrp.oxide.core.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.event_stream.DivertEventStreamVertex
import io.github.oxidefrp.oxide.core.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.core.event_stream.ObservingEventStreamVertex
import io.github.oxidefrp.oxide.core.event_stream.Subscription
import io.github.oxidefrp.oxide.core.signal.SamplingSignalVertex
import io.github.oxidefrp.oxide.core.signal.SignalVertex

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

        fun <A> divert(cell: Cell<EventStream<A>>): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> =
                    DivertEventStreamVertex(
                        source = cell.vertex,
                    )
            }

        fun <A, B> apply(
            function: Cell<(A) -> B>,
            argument: Cell<A>,
        ): Cell<B> =
            object : Cell<B>() {
                override val vertex: CellVertex<B> =
                    ApplyCellVertex(
                        function = function.vertex,
                        argument = argument.vertex,
                    )
            }

        fun <A, B> map1(
            ca: Cell<A>,
            f: (a: A) -> B,
        ): Cell<B> {
            fun g(a: A) = f(a)

            return ca.map(::g)
        }

        fun <A, B, C> map2(
            ca: Cell<A>,
            cb: Cell<B>,
            f: (a: A, b: B) -> C,
        ): Cell<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                ca.map(::g),
                cb,
            )
        }

        fun <A, B, C, D> map3(
            ca: Cell<A>,
            cb: Cell<B>,
            cc: Cell<C>,
            f: (a: A, b: B, c: C) -> D,
        ): Cell<D> {
            fun g(a: A) = fun(b: B) = fun(c: C) = f(a, b, c)

            return apply(
                apply(
                    ca.map(::g),
                    cb,
                ),
                cc,
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
