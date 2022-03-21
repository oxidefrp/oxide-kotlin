package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.EventEmitterVertex

class EventEmitter<A> : EventStream<A>() {
    override val vertex = EventEmitterVertex<A>()

    fun emitExternally(event: A) {
        vertex.emit(event)
    }
}
