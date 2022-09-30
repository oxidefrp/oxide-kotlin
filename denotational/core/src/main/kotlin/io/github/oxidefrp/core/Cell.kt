package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState
import io.github.oxidefrp.core.shared.State
import io.github.oxidefrp.core.shared.StateSchedulerLayer
import io.github.oxidefrp.core.shared.StateStructure
import io.github.oxidefrp.core.shared.construct
import io.github.oxidefrp.core.shared.enter
import io.github.oxidefrp.core.shared.pull
import io.github.oxidefrp.core.shared.pullEnter
import io.github.oxidefrp.core.shared.unzip2

data class ValueChange<out A>(
    val oldValue: A,
    val newValue: A,
)

abstract class Cell<out A> {
    sealed interface SegmentSequence<out A>

    data class FullSegmentSequence<out A>(
        val initialValue: A,
        val innerValues: TimelineSequence<A>,
    ) : SegmentSequence<A>

    companion object {
        fun <A> strict(
            initialValue: A,
            newValues: EventStream<A>,
        ): Cell<A> = object : Cell<A>() {
            override val segmentSequence: FullSegmentSequence<A> = FullSegmentSequence(
                initialValue = initialValue,
                innerValues = newValues.occurrences,
            )
        }

        fun <A> ofInstants(
            initialValue: A,
            vararg occurrences: Instant<A>,
        ): Cell<A> = strict(
            initialValue = initialValue,
            newValues = EventStream.ofInstants(*occurrences),
        )

        fun <A> loop(
            cell: () -> Cell<A>,
        ): Cell<A> = object : Cell<A>() {
            override val segmentSequence: FullSegmentSequence<A> by lazy {
                cell().segmentSequence
            }
        }

        fun <A> constant(value: A): Cell<A> = object : Cell<A>() {
            override val segmentSequence: FullSegmentSequence<A> = FullSegmentSequence(
                initialValue = value,
                innerValues = TimelineSequence.empty(),
            )
        }

        fun <A> switch(cell: Cell<Cell<A>>): Cell<A> = object : Cell<A>() {
            override val segmentSequence: FullSegmentSequence<A> by lazy {
                val initialCell = cell.initialValue
                val newCells = cell.innerValues

                FullSegmentSequence(
                    initialValue = initialCell.initialValue,
                    innerValues = initialCell.innerValues.switchTo(
                        newCells = newCells,
                    )
                )
            }
        }

        fun <A> divert(
            cell: Cell<EventStream<A>>,
        ): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> by lazy {
                val initialStream = cell.initialValue
                val newStreams = cell.innerValues

                initialStream.occurrences.divertTo(
                    newStreams = newStreams,
                )
            }
        }

        fun <A> divertEarly(
            cell: Cell<EventStream<A>>,
        ): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> by lazy {
                val initialStream = cell.initialValue
                val newStreams = cell.innerValues

                initialStream.occurrences.divertEarlyTo(
                    newStreams = newStreams,
                )
            }
        }

        fun <A, B> apply(
            function: Cell<(A) -> B>,
            argument: Cell<A>,
        ): Cell<B> = object : Cell<B>() {
            override val segmentSequence: FullSegmentSequence<B> by lazy {
                FullSegmentSequence(
                    initialValue = function.initialValue(argument.initialValue),
                    innerValues = mergeTimelineSequences(
                        eventSequenceA = function.segmentSequence.innerValues,
                        eventSequenceB = argument.segmentSequence.innerValues,
                        transformA = { time, fn ->
                            val arg = argument.sample().pullDirectly(time)
                            fn(arg)
                        },
                        transformB = { time, arg ->
                            val fn = function.sample().pullDirectly(time)
                            fn(arg)
                        },
                        combine = { _, fn, arg ->
                            fn(arg)
                        },
                    ),
                )
            }
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

        fun <A, R> looped1(
            f: (cellA: Cell<A>) -> Loop1<A, R>,
        ): R = object {
            val cellALoop: Cell<A> = Cell.loop { loop.cellA }

            val loop by lazy { f(cellALoop) }

            val result = loop.result
        }.result

        fun <A, R> pullLooped1(
            f: (cellA: Cell<A>) -> Moment<Loop1<A, R>>,
        ): Moment<R> = object : Moment<R>() {
            override fun pullDirectly(t: Time): R = object {
                val cellALoop: Cell<A> = Cell.loop { loop.cellA }

                val loop by lazy { f(cellALoop).pullDirectly(t) }
            }.loop.result
        }
    }

    data class Loop1<A, R>(
        val cellA: Cell<A>,
        val result: R,
    )

    val value: Signal<A> = object : Signal<A>() {
        override fun at(t: Time): A =
            this@Cell.sample().pullDirectly(t = t)
    }

    val newValues: EventStream<A> = object : EventStream<A>() {
        override val occurrences: TimelineSequence<A> by lazy { innerValues }
    }

    abstract val segmentSequence: FullSegmentSequence<A>

    open fun sample(): Moment<A> = object : Moment<A>() {
        override fun pullDirectly(t: Time): A {
            val nonFutureValues = innerValues.takeBefore(t).occurrences
            return nonFutureValues.lastOrNull()?.event ?: initialValue
        }
    }

    fun sampleNew(): Moment<A> = object : Moment<A>() {
        override fun pullDirectly(t: Time): A {
            val nonFutureValues = innerValues.takeNotAfter(t).occurrences
            return nonFutureValues.lastOrNull()?.event ?: initialValue
        }
    }

    val initialValue: A
        get() = segmentSequence.initialValue

    val innerValues: TimelineSequence<A>
        get() = segmentSequence.innerValues

    val changes: EventStream<ValueChange<A>> = object : EventStream<ValueChange<A>>() {
        override val occurrences: TimelineSequence<ValueChange<A>> by lazy {
            segmentSequence.innerValues.runningStatefulFold(
                initialState = initialValue,
                operation = { previousValue, _, newValue ->
                    Pair(
                        newValue, ValueChange(
                            oldValue = previousValue,
                            newValue = newValue,
                        )
                    )
                },
            )
        }
    }

    fun <B> map(transform: (A) -> B): Cell<B> = object : Cell<B>() {
        override val segmentSequence: FullSegmentSequence<B> by lazy {
            FullSegmentSequence(
                initialValue = transform(this@Cell.initialValue),
                innerValues = this@Cell.innerValues.map { _, a -> transform(a) },
            )
        }
    }

    fun <S, B> enterOf(
        transform: (A) -> State<S, B>,
    ): StateStructure<S, Cell<B>> = enter(map(transform))

    fun <B> pullOf(
        transform: (A) -> Moment<B>,
    ): Moment<Cell<B>> = pull(map(transform))

    fun <S, B> pullEnterOf(
        transform: (A) -> MomentState<S, B>,
    ): StateStructure<S, Cell<B>> = pullEnter(map(transform))

    fun <S, B> constructOf(
        transform: (A) -> StateStructure<S, B>,
    ): StateStructure<S, Cell<B>> = construct(map(transform))

    fun <B> divertOf(transform: (A) -> EventStream<B>): EventStream<B> = divert(map(transform))

}

fun <A, B> Cell<A>.switchOf(transform: (A) -> Cell<B>): Cell<B> = Cell.switch(map(transform))
