package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.TransactionSubscription

internal abstract class ObservingCellVertex<A> : PausableCellVertex<A>() {
    private var upstreamSubscription: TransactionSubscription? = null

    override fun onResumed(transaction: Transaction) {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe(transaction = transaction)
    }

    override fun onPaused(transaction: Transaction) {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel(transaction = transaction)

        upstreamSubscription = null
    }

    abstract fun observe(transaction: Transaction): TransactionSubscription
}
