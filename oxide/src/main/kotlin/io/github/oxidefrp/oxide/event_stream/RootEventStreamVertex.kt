package io.github.oxidefrp.oxide.event_stream

abstract class RootEventStreamVertex<A> : SimpleEventStreamVertex<A>() {
    final override fun update() {
    }

    final override fun reset() {
    }

    final override fun execute() {
    }

    final override fun onFirstListenerAdded() {
    }

    final override fun onLastListenerRemoved() {
    }
}
