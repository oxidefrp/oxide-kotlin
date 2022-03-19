package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.event_stream.FilterEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.MapEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.MergeEventStreamVertex
import io.github.oxidefrp.oxide.event_stream.NeverEventStreamVertex
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

    fun subscribe(listener: (A) -> Unit): Subscription =
        vertex.registerDependency(
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
