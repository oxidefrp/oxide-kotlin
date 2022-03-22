package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction

internal class MapEventStreamVertex<A, B>(
    private val source: EventStreamVertex<A>,
    private val transform: (A) -> B,
) : ObservingEventStreamVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<B> =
        source.pullCurrentOccurrence(transaction = transaction).map(transform)
}
