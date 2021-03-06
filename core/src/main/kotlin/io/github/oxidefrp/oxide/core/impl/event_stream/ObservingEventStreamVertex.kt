package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class ObservingEventStreamVertex<A> : EventStreamVertex<A>() {
    private var upstreamSubscription: Subscription? = null

    private var cachedCurrentOccurrence: Option<A>? = null

    final override fun process(transaction: Transaction) {
        pullCurrentOccurrence(transaction = transaction)
    }

    final override fun onFirstDependencyAdded() {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe()
    }

    final override fun onLastDependencyRemoved() {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel()

        upstreamSubscription = null
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

    abstract fun observe(): Subscription

    abstract fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A>
}
