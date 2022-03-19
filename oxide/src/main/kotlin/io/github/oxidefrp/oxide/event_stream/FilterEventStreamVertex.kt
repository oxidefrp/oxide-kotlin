package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option

class FilterEventStreamVertex<A>(
    private val source: EventStreamVertex<A>,
    private val predicate: (A) -> Boolean,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription =
        source.registerDependency(this)

    override fun computeCurrentOccurrence(): Option<A> =
        source.currentOccurrence.filter(predicate)
}
