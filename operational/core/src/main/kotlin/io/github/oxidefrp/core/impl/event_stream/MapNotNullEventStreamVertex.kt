package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class MapNotNullEventStreamVertex<A, B : Any>(
    private val source: EventStreamVertex<A>,
    private val transform: (A) -> B?,
) : ObservingEventStreamVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<B> =
        source.pullCurrentOccurrence(transaction = transaction).flatMap {
            Option.of(transform(it))
        }

    override fun toString(): String = "MapNotNullEventStreamVertex{id = $id, source.id = ${source.id}}"
}
