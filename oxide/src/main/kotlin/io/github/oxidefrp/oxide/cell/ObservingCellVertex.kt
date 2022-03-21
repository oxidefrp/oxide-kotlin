package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.event_stream.Subscription

internal abstract class ObservingCellVertex<A> : TransformingCellVertex<A>() {
    private var upstreamSubscription: Subscription? = null

    override fun onTransformationResumed() {
        if (upstreamSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered subscription")
        }

        upstreamSubscription = observe()
    }

    override fun onTransformationPaused() {
        val subscription = upstreamSubscription
            ?: throw RuntimeException("Critical: there's no remembered subscription")

        subscription.cancel()

        upstreamSubscription = null
    }

    abstract fun observe(): Subscription
}
