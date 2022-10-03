package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class FilterEventStreamVertex<A>(
    private val source: EventStreamVertex<A>,
    private val predicate: (A) -> Boolean,
) : ObservingEventStreamVertex<A>() {
    override fun observe(transaction: Transaction): TransactionSubscription =
        source.registerDependent(transaction = transaction, dependent = this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        source.pullCurrentOccurrence(transaction = transaction).filter(predicate)
}
