package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.event_stream.FilterEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.MapEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.MergeEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.NeverEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.ProbeEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.Subscription
import io.github.oxidefrp.oxide.event_stream.SubscriptionVertex

abstract class EventStream<out A> {
    internal abstract val vertex: EventStreamVertex<A>

    companion object {
        fun <A> never(): EventStream<A> =
            object : EventStream<A>() {
                override val vertex: EventStreamVertex<A> =
                    NeverEventStreamVertex()
            }

        fun <A> sampleEach(stream: EventStream<Signal<A>>): EventStream<A> =
            TODO()
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

    fun <B> sampleEachOf(selector: (A) -> Signal<B>): EventStream<B> =
        sampleEach(map(selector))

    fun subscribe(listener: (A) -> Unit): Subscription =
        vertex.registerDependent(
            SubscriptionVertex(
                stream = this.vertex,
                listener = listener,
            ),
        )
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
