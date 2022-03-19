package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.SimpleEventStream

class EventEmitter<A> : SimpleEventStream<A>() {
    override fun onFirstListenerAdded() {
    }

    override fun onLastListenerRemoved() {
    }

    fun emit(event: A) {
        notifyListeners(event)
    }
}
