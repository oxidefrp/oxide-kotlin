package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.FilterEventStream
import io.github.oxidefrp.oxide.event_stream.MapEventStream
import io.github.oxidefrp.oxide.event_stream.MergeEventStream
import io.github.oxidefrp.oxide.event_stream.NeverEventStream

interface Subscription {
    fun cancel()

    operator fun plus(other: Subscription): Subscription {
        val self = this

        return object : Subscription {
            override fun cancel() {
                self.cancel()
                other.cancel()
            }
        }
    }
}

abstract class EventStream<out A> {
    companion object {
        fun <A> never(): EventStream<A> = NeverEventStream()
    }

    fun <B> map(transform: (A) -> B): EventStream<B> =
        MapEventStream(
            source = this,
            transform = transform,
        )

    fun filter(predicate: (A) -> Boolean): EventStream<A> =
        FilterEventStream(
            source = this,
            predicate = predicate,
        )

    abstract val referenceCount: Int

    abstract fun subscribe(listener: (A) -> Unit): Subscription
}

fun <A> EventStream<A>.mergeWith(
    other: EventStream<A>,
    combine: (A, A) -> A,
): EventStream<A> = MergeEventStream(
    source1 = this,
    source2 = other,
    combine = combine,
)
