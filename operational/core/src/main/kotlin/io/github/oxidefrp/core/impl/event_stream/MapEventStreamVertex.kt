package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class MapEventStreamVertex<A, B>(
    private val source: EventStreamVertex<A>,
    private val transform: (A) -> B,
) : ObservingEventStreamVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<B> =
        source.pullCurrentOccurrence(transaction = transaction).map(transform)

    override fun toString(): String = "MapEventStreamVertex{id = $id, source.id = ${source.id}}"
}
