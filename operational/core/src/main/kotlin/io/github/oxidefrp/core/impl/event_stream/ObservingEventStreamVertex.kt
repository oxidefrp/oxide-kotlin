package io.github.oxidefrp.core.impl.event_stream

internal abstract class ObservingEventStreamVertex<A> : CachingEventStreamVertex<A>() {
    private var upstreamSubscription: Subscription? = null

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

    abstract fun observe(): Subscription
}
