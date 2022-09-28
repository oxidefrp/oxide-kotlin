package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.event_stream.EventEmitterVertex

class EventEmitter<A> : EventStream<A>() {
    override val vertex = EventEmitterVertex<A>()

    fun emitExternally(event: A) {
        vertex.emit(event)
    }
}
