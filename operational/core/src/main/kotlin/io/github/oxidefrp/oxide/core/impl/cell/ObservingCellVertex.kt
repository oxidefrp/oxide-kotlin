package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription

internal abstract class ObservingCellVertex<A> : PausableCellVertex<A>() {
    private var upstreamSubscription: Subscription? = null

    override fun onResumed() {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe()
    }

    override fun onPaused() {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel()

        upstreamSubscription = null
    }

    abstract fun observe(): Subscription
}
