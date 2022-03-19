package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.FilterEventStream
import io.github.oxidefrp.oxide.event_stream.MapEventStream

interface Subscription {
    fun cancel()
}

abstract class EventStream<out A> {
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
