package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal abstract class CachingEventStreamVertex<A> : EventStreamVertex<A>() {
    private var cachedCurrentOccurrence: Option<A>? = null

    final override fun process(transaction: Transaction) {
        pullCurrentOccurrence(transaction = transaction)
    }

    final override fun pullCurrentOccurrence(transaction: Transaction): Option<A> =
        cachedCurrentOccurrence ?: run {
            val currentOccurrence = pullCurrentOccurrenceUncached(transaction = transaction)

            cachedCurrentOccurrence = currentOccurrence

            transaction.enqueueForReset {
                cachedCurrentOccurrence = null
            }

            currentOccurrence
        }

    abstract fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A>
}
