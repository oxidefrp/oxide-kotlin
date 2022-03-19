package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option

class MapEventStreamVertex<A, B>(
    private val source: EventStreamVertex<A>,
    private val transform: (A) -> B,
) : ObservingEventStreamVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependency(this)

    override fun computeCurrentOccurrence(): Option<B> =
        source.currentOccurrence.map(transform)
}
