package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some

class EventEmitterVertex<A> : RootEventStreamVertex<A>() {
    private var emittedOccurrence: Option<A> = None()

    override val currentOccurrence: Option<A>
        get() = emittedOccurrence

    fun emit(event: A) {
        if (emittedOccurrence.isSome()) {
            throw IllegalStateException("Emitter is already emitting an event")
        }

        emittedOccurrence = Some(event)

        val transitiveDependents = mutableSetOf<Vertex>()

        fun addTransitiveDependents(vertex: Vertex) {
            vertex.dependents.forEach {
                transitiveDependents.add(it)
                addTransitiveDependents(it)
            }
        }

        addTransitiveDependents(this)

        transitiveDependents.forEach { it.update() }

        transitiveDependents.forEach { it.reset() }

        transitiveDependents.forEach { it.execute() }

        emittedOccurrence = None()
    }
}
