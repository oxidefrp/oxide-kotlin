package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Subscription

abstract class ObservingEventStream<A> : SimpleEventStream<A>() {
    private var upstreamSubscription: Subscription? = null

    final override fun onFirstListenerAdded() {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe()
    }

    final override fun onLastListenerRemoved() {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel()

        upstreamSubscription = null
    }

    abstract fun observe(): Subscription
}
