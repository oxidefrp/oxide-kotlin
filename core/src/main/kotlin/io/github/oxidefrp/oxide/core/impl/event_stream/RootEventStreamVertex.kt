package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class RootEventStreamVertex<A> : EventStreamVertex<A>() {
    override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded() {
    }

    final override fun onLastDependencyRemoved() {
    }
}
