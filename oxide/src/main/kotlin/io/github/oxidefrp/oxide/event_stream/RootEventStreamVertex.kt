package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Transaction

internal abstract class RootEventStreamVertex<A> : SimpleEventStreamVertex<A>() {
    override fun process(transaction: Transaction) {
    }

    final override fun onFirstListenerAdded() {
    }

    final override fun onLastListenerRemoved() {
    }
}
