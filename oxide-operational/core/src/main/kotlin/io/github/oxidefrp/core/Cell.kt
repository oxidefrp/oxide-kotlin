package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.DelayedStateMoment
import io.github.oxidefrp.core.impl.event_stream.DivertEarlyEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.DivertLateEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.core.impl.signal.SignalVertex
import io.github.oxidefrp.core.shared.MomentIo
import io.github.oxidefrp.core.shared.divertEarlyOf
import io.github.oxidefrp.core.shared.orElse
import io.github.oxidefrp.core.shared.pullOf

data class ValueChange<out A>(
    val oldValue: A,
    val newValue: A,
)

abstract class Cell<out A> {
    companion object {
        fun <A> constant(value: A): Cell<A> = object : Cell<A>() {
            override val currentValue: Moment<A> = Moment.pure(value)

            override val newValues: EventStream<A> = EventStream.never()
        }

        fun <A> switch(cell: Cell<Cell<A>>): Cell<A> = object : Cell<A>() {
            override val newValues = cell.divertEarlyOf { it.newValues }.orElse(
                cell.newValues.pullOf { it.sample() },
            )

            override val currentValue = DelayedStateMoment(
                moment = cell.sample().pullOf { it.sample() },
                steps = newValues,
            )
        }

        fun <A> divert(cell: Cell<EventStream<A>>): EventStream<A> = object : EventStream<A>() {
            override val vertex: EventStreamVertex<A> by lazy {
                DivertLateEventStreamVertex(
                    source = cell,
                )
            }
        }

        fun <A> divertEarly(cell: Cell<EventStream<A>>): EventStream<A> = object : EventStream<A>() {
            override val vertex: EventStreamVertex<A> by lazy {
                DivertEarlyEventStreamVertex(
                    source = cell,
                )
            }
        }

        fun <A, B> apply(
            function: Cell<(A) -> B>,
            argument: Cell<A>,
        ): Cell<B> = object : Cell<B>() {
            override val newValues: EventStream<B> = EventStream.pull(
                function.newValues.squashWith(other = argument.newValues, ifFirst = { fn ->
                    argument.sample().map { arg ->
                        fn(arg)
                    }
                }, ifSecond = { arg ->
                    function.sample().map { fn ->
                        fn(arg)
                    }
                }, ifBoth = { fn, arg ->
                    Moment.pure(fn(arg))
                }),
            )

            override val currentValue: Moment<B> = DelayedStateMoment(
                moment = Moment.map2(
                    function.sample(),
                    argument.sample(),
                ) { fn, arg ->
                    fn(arg)
                },
                steps = newValues,
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

    abstract val currentValue: Moment<A>

    abstract val newValues: EventStream<A>

    val referenceCount: Int
        get() = newValues.vertex.referenceCount

    val changes: EventStream<ValueChange<A>> by lazy {
        newValues.pullOf { newValue ->
            this@Cell.sample().map { oldValue ->
                ValueChange(oldValue = oldValue, newValue = newValue)
            }
        }
    }

    val value: Signal<A> = object : Signal<A>() {
        override val vertex: SignalVertex<A> = object : SignalVertex<A>() {
            override fun pullCurrentValue(transaction: Transaction): A =
                this@Cell.sample().pullCurrentValue(transaction = transaction)
        }
    }

    fun sample(): Moment<A> = currentValue

    fun sampleNew(): Moment<A> = newValues.currentOccurrence.pullOf {
        if (it != null) Moment.pure(it.event)
        else currentValue
    }

    fun sampleIo(): MomentIo<A> = MomentIo.lift(sample())

    fun <B> map(transform: (A) -> B): Cell<B> = object : Cell<B>() {
        override val newValues: EventStream<B> = this@Cell.newValues.map(transform)

        override val currentValue: Moment<B> = DelayedStateMoment(
            moment = this@Cell.sample().map(transform),
            steps = newValues,
        )
    }

    fun <B> switchOf(transform: (A) -> Cell<B>): Cell<B> = switch(map(transform))

    private fun reactExternally(
        action: (A) -> Unit,
    ) = Transaction.wrap { transaction ->
        val currentValue = value.sampleExternally()

        action(currentValue)

        newValues.subscribe(
            transaction = transaction,
            listener = action,
        )
    }

    fun reactExternallyIndefinitely(action: (A) -> Unit) {
        // TODO: Fix the leak
        reactExternally(action)
    }
}

fun <A, B> Cell<A>.divertEarlyOf(
    transform: (A) -> EventStream<B>,
): EventStream<B> = Cell.divertEarly(map(transform))
