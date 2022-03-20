package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal class MapEventStreamVertex<A, B>(
    private val source: EventStreamVertex<A>,
    private val transform: (A) -> B,
) : ObservingEventStreamVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<B> =
        source.pullCurrentOccurrence(transaction = transaction).map(transform)
}
