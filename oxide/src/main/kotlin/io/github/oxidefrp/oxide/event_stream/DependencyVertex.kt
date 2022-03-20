package io.github.oxidefrp.oxide.event_stream

internal abstract class DependencyVertex : Vertex() {
    private val _dependents = mutableSetOf<Vertex>()

    override val dependents: Set<Vertex>
        get() = _dependents

    val referenceCount: Int
        get() = dependents.size

    fun registerDependent(dependent: Vertex): Subscription {
        val wasAdded = _dependents.add(dependent)

        if (!wasAdded) {
            throw IllegalStateException("Attempted to re-register the same dependency")
        }

        if (_dependents.size == 1) {
            onFirstDependencyAdded()
        }

        return object : Subscription {
            override fun cancel() {
                val wasRemoved = _dependents.remove(dependent)

                if (!wasRemoved) {
                    throw IllegalStateException("Attempted to re-cancel a subscription")
                }

                if (_dependents.isEmpty()) {
                    onLastDependencyRemoved()
                }
            }
        }
    }

    abstract fun onFirstDependencyAdded()

    abstract fun onLastDependencyRemoved()
}
