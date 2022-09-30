package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Transaction

internal abstract class ObservingEventStreamVertex<A> : CachingEventStreamVertex<A>() {
    private var upstreamSubscription: TransactionSubscription? = null

    final override fun onFirstDependencyAdded(transaction: Transaction) {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe(transaction)
    }

    final override fun onLastDependencyRemoved(transaction: Transaction) {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel(transaction = transaction)

        upstreamSubscription = null
    }

    abstract fun observe(transaction: Transaction): TransactionSubscription
}
