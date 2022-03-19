package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.EventStream
import io.github.oxidefrp.oxide.Subscription

class MergeEventStream<A>(
    private val source1: EventStream<A>,
    private val source2: EventStream<A>,
    private val combine: (A, A) -> A,
) : ObservingEventStream<A>() {
    override fun observe(): Subscription =
        source1.subscribe { event ->
            notifyListeners(event)
        } + source2.subscribe { event ->
            notifyListeners(event)
        }
}
