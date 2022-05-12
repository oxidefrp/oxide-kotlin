package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.FilterEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.MapEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.MergeEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.NeverEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.ProbeEachEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.ProbeEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.SourceEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import io.github.oxidefrp.oxide.core.impl.event_stream.SubscriptionVertex
import io.github.oxidefrp.oxide.core.impl.signal.HoldSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.SignalVertex

abstract class EventStream<out A> {
    internal abstract val vertex: EventStreamVertex<A>

    companion object {
        fun <A> source(
            subscribe: (emit: (A) -> Unit) -> Subscription,
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
                override val vertex: EventStreamVertex<A> = ProbeEachEventStreamVertex(
                    stream = stream.vertex,
                )
            }
    }

    fun <B> map(transform: (A) -> B): EventStream<B> =
        object : EventStream<B>() {
            override val vertex: EventStreamVertex<B> = MapEventStreamVertex(
                source = this@EventStream.vertex,
                transform = transform,
            )
        }

    fun filter(predicate: (A) -> Boolean): EventStream<A> =
        object : EventStream<A>() {
            override val vertex: EventStreamVertex<A> = FilterEventStreamVertex(
                source = this@EventStream.vertex,
                predicate = predicate,
            )
        }

    fun <B, C> probe(signal: Signal<B>, combine: (A, B) -> C): EventStream<C> =
        object : EventStream<C>() {
            override val vertex: EventStreamVertex<C> = ProbeEventStreamVertex(
                stream = this@EventStream.vertex,
                signal = signal.vertex,
                combine = combine,
            )
        }

    fun <B> probe(signal: Signal<B>): EventStream<B> =
        probe(signal) { _, b -> b }

    fun <B> sampleOf(selector: (A) -> Signal<B>): EventStream<B> =
        sample(map(selector))

    fun subscribe(listener: (A) -> Unit): Subscription =
        vertex.registerDependent(
            SubscriptionVertex(
                stream = this.vertex,
                listener = listener,
            ),
        )

    fun subscribeIndefinitely(listener: (A) -> Unit) {
        subscribe(listener)
    }
}

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

fun <A> EventStream<A>.holdS(initialValue: A): Signal<Cell<A>> =
    object : Signal<Cell<A>>() {
        override val vertex: SignalVertex<Cell<A>> =
            HoldSignalVertex(
                steps = this@holdS.vertex,
                initialValue = initialValue,
            )
    }
