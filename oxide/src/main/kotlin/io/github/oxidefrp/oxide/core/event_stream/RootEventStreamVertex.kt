package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Transaction

internal abstract class RootEventStreamVertex<A> : EventStreamVertex<A>() {
    override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded() {
    }

    final override fun onLastDependencyRemoved() {
    }
}
