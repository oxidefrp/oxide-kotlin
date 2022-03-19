package io.github.oxidefrp.oxide.event_stream

abstract class SimpleEventStreamVertex<A> : EventStreamVertex<A>() {
    private val _dependents = mutableSetOf<Vertex>()

    override val dependents: Set<Vertex>
        get() = _dependents

    final override val referenceCount: Int
        get() = dependents.size

    final override fun registerDependent(dependent: Vertex): Subscription {
        val wasAdded = _dependents.add(dependent)

        if (!wasAdded) {
            throw IllegalStateException("Attempted to re-register the same dependency")
        }

        if (_dependents.size == 1) {
            onFirstListenerAdded()
        }

        return object : Subscription {
            override fun cancel() {
                val wasRemoved = _dependents.remove(dependent)

                if (!wasRemoved) {
                    throw IllegalStateException("Attempted to re-cancel a subscription")
                }

                if (_dependents.isEmpty()) {
                    onLastListenerRemoved()
                }
            }
        }
    }

    abstract fun onFirstListenerAdded()

    abstract fun onLastListenerRemoved()
}
