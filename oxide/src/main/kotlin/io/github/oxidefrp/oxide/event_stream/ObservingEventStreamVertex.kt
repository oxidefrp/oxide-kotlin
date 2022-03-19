package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option

abstract class ObservingEventStreamVertex<A> : SimpleEventStreamVertex<A>() {
    private var upstreamSubscription: Subscription? = null

    private var cachedCurrentOccurrence: Option<A>? = null

    final override fun update() {
        currentOccurrence
    }

    final override fun reset() {
        cachedCurrentOccurrence = null
    }

    final override fun execute() {
    }

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

    final override val currentOccurrence: Option<A>
        get() = cachedCurrentOccurrence ?: run {
            val occurrence = computeCurrentOccurrence()
            cachedCurrentOccurrence = occurrence
            occurrence
        }

    abstract fun observe(): Subscription

    abstract fun computeCurrentOccurrence(): Option<A>
}
