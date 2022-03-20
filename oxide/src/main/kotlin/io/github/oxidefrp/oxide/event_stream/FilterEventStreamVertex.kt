package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal class FilterEventStreamVertex<A>(
    private val source: EventStreamVertex<A>,
    private val predicate: (A) -> Boolean,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        source.pullCurrentOccurrence(transaction = transaction).filter(predicate)
}
