package io.github.oxidefrp.oxide.event_stream

class NeverEventStream<A> : SimpleEventStream<A>() {
    override fun onFirstListenerAdded() {
    }

    override fun onLastListenerRemoved() {
    }
}
