package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Transaction

internal abstract class RootEventStreamVertex<A> : EventStreamVertex<A>() {
    override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded(transaction: Transaction) {
    }

    final override fun onLastDependencyRemoved(transaction: Transaction) {
    }
}
