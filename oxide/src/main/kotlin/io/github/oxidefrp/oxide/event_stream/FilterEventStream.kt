package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.EventStream
import io.github.oxidefrp.oxide.Subscription

class FilterEventStream<A>(
    private val source: EventStream<A>,
    private val predicate: (A) -> Boolean,
) : ObservingEventStream<A>() {
    override fun observe(): Subscription =
        source.subscribe { event ->
            if (predicate(event)) {
                notifyListeners(event)
            }
        }
}
