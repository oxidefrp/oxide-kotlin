package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.EventStream
import io.github.oxidefrp.oxide.Subscription

class MapEventStream<A, B>(
    private val source: EventStream<A>,
    private val transform: (A) -> B,
) : ObservingEventStream<B>() {
    override fun observe(): Subscription =
        source.subscribe { event ->
            notifyListeners(transform(event))
        }
}
