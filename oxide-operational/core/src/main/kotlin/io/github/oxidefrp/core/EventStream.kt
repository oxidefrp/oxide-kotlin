package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.ExternalSubscription
import io.github.oxidefrp.core.impl.event_stream.FilterEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.MapEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.MapNotNullEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.MergeEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.NeverEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.ProbeEachEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.ProbeEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.PullEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.SourceEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.SparkMomentVertex
import io.github.oxidefrp.core.impl.event_stream.SquashWithEventStreamVertex
import io.github.oxidefrp.core.impl.event_stream.SubscriptionVertex
import io.github.oxidefrp.core.impl.event_stream.TransactionSubscription
import io.github.oxidefrp.core.impl.getOrNull
import io.github.oxidefrp.core.impl.moment.MomentVertex
import io.github.oxidefrp.core.impl.signal.HoldMomentVertex

data class EventOccurrence<out A>(
    val event: A,
)

abstract class EventStream<out A> {
    data class Loop1<A, R>(
        val streamA: EventStream<A>,
        val result: R,
    )

    internal abstract val vertex: EventStreamVertex<A>

    companion object {
        fun <A> source(
            subscribe: (emit: (A) -> Unit) -> ExternalSubscription,
        ): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> = SourceEventStreamVertex(
                    subscribe = subscribe,
                )
            }

        fun <A> never(): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> =
                    NeverEventStreamVertex()
            }

        fun <A> sample(stream: EventStream<Signal<A>>): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> by lazy {
                    ProbeEachEventStreamVertex(
                        stream = stream.vertex,
                    )
                }
            }

        fun <A> pull(stream: EventStream<Moment<A>>): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> by lazy {
                    PullEventStreamVertex(
                        source = stream.vertex,
                    )
                }
            }

        fun <A> spark(value: A): Moment<EventStream<A>> =
            object : Moment<EventStream<A>>() {
                override val vertex = SparkMomentVertex(
                    value = value,
                )
            }

        fun <A, R> pullLooped1(
            f: (streamA: EventStream<A>) -> Moment<Loop1<A, R>>,
        ): Moment<R> = object : Moment<R>() {
            override val vertex = object : MomentVertex<R>() {
                override fun computeCurrentValue(transaction: Transaction): R = object {
                    val streamALoop: EventStream<A> = object : EventStream<A>() {
                        override val vertex: EventStreamVertex<A> by lazy {
                            loop.streamA.vertex
                        }
                    }

                    val loop by lazy {
                        f(streamALoop).vertex.computeCurrentValue(transaction = transaction)
                    }
                }.loop.result
            }
        }
    }

    fun <B> map(transform: (A) -> B): EventStream<B> =
        object : EventStream<B>() {
            override val vertex: EventStreamVertex<B> by lazy {
                MapEventStreamVertex(
                    source = this@EventStream.vertex,
                    transform = transform,
                )
            }
        }

    fun filter(predicate: (A) -> Boolean): EventStream<A> =
        object : EventStream<A>() {
            override val vertex: EventStreamVertex<A> by lazy {
                FilterEventStreamVertex(
                    source = this@EventStream.vertex,
                    predicate = predicate,
                )
            }
        }

    // Thought: The number of primitives can probably be reduced
    fun <B, C> probe(signal: Signal<B>, combine: (A, B) -> C): EventStream<C> =
        object : EventStream<C>() {
            override val vertex: EventStreamVertex<C> by lazy {
                ProbeEventStreamVertex(
                    stream = this@EventStream.vertex,
                    signal = signal.vertex,
                    combine = combine,
                )
            }
        }

    fun <B> probe(signal: Signal<B>): EventStream<B> =
        probe(signal) { _, b -> b }

    fun <B> sampleOf(selector: (A) -> Signal<B>): EventStream<B> =
        sample(map(selector))

    fun <B> pullOf(selector: (A) -> Moment<B>): EventStream<B> =
        pull(map(selector))

    val currentOccurrence: Moment<EventOccurrence<A>?> = object : Moment<EventOccurrence<A>?>() {
        override val vertex = object : MomentVertex<EventOccurrence<A>?>() {
            override fun computeCurrentValue(transaction: Transaction): EventOccurrence<A>? =
                this@EventStream.vertex.pullCurrentOccurrence(transaction = transaction)
                    .map { EventOccurrence(event = it) }.getOrNull()
        }
    }

    internal fun subscribe(
        transaction: Transaction,
        listener: (A) -> Unit,
    ): TransactionSubscription = vertex.registerDependent(
        transaction = transaction,
        dependent = SubscriptionVertex(
            stream = this.vertex,
            listener = listener,
        ),
    )

    fun subscribeExternally(
        listener: (A) -> Unit,
    ): ExternalSubscription = Transaction.wrap {
        subscribe(
            transaction = it,
            listener = listener,
        ).toExternal()
    }

    fun subscribeExternallyIndefinitely(listener: (A) -> Unit) {
        subscribeExternally(listener)
    }
}

fun <A, B : Any> EventStream<A>.mapNotNull(transform: (A) -> B?): EventStream<B> =
    object : EventStream<B>() {
        override val vertex: EventStreamVertex<B> by lazy {
            MapNotNullEventStreamVertex(
                source = this@mapNotNull.vertex,
                transform = transform,
            )
        }
    }

fun <A : Any> EventStream<A?>.filterNotNull(): EventStream<A> = this.mapNotNull { it }

fun <A> EventStream<A>.mergeWith(
    other: EventStream<A>,
    combine: (A, A) -> A,
): EventStream<A> = object : EventStream<A>() {
    override val vertex: EventStreamVertex<A> = MergeEventStreamVertex(
        source1 = this@mergeWith.vertex,
        source2 = other.vertex,
        combine = combine,
    )
}

fun <A> EventStream<A>.hold(initialValue: A): Moment<Cell<A>> =
    object : Moment<Cell<A>>() {
        override val vertex: MomentVertex<Cell<A>> by lazy {
            HoldMomentVertex(
                steps = this@hold,
                initialValue = initialValue,
            )
        }
    }

fun <A, R> EventStream<A>.accum(
    initialValue: R,
    combine: (R, A) -> R,
): Moment<Cell<R>> = EventStream.pullLooped1 { stepsLoop: EventStream<R> ->
    stepsLoop.hold(initialValue).map { accumulator ->
        val steps = this.pullOf { a ->
            accumulator.sample().map { acc -> combine(acc, a) }
        }

        EventStream.Loop1(
            streamA = steps,
            result = accumulator,
        )
    }
}

fun <A, B, C> EventStream<A>.squashWith(
    other: EventStream<B>,
    ifFirst: (A) -> C,
    ifSecond: (B) -> C,
    ifBoth: (A, B) -> C,
): EventStream<C> = object : EventStream<C>() {
    override val vertex: EventStreamVertex<C> by lazy {
        SquashWithEventStreamVertex(
            source1 = this@squashWith.vertex,
            source2 = other.vertex,
            ifFirst = ifFirst,
            ifSecond = ifSecond,
            ifBoth = ifBoth,
        )
    }
}
