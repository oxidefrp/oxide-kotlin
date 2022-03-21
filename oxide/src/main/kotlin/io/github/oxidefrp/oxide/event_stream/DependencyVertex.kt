package io.github.oxidefrp.oxide.event_stream

external class WeakRef<T : Any>(target: T) {
    fun deref(): T?
}

internal sealed interface VertexRef {
    val vertex: Vertex?

    data class Strong(override val vertex: Vertex) : VertexRef

    class Weak(targetVertex: Vertex) : VertexRef {
        private val weakRef = WeakRef(targetVertex)

        override val vertex: Vertex?
            get() = weakRef.deref()
    }
}

internal abstract class DependencyVertex : Vertex() {
    private val _dependents = mutableSetOf<VertexRef>()

    override fun getDependents(): List<Vertex> {
        _dependents.removeAll { it.vertex == null }

        return _dependents.map {
            it.vertex ?: throw RuntimeException("Weakref isn't expected to be collected mid-task")
        }
    }

    val referenceCount: Int
        get() = _dependents.size

    fun registerDependent(dependent: Vertex): Subscription =
        registerDependentRef(
            dependentRef = VertexRef.Strong(vertex = dependent)
        )

    fun registerDependentWeak(dependent: Vertex): Subscription =
        registerDependentRef(
            dependentRef = VertexRef.Weak(targetVertex = dependent)
        )

    private fun registerDependentRef(dependentRef: VertexRef): Subscription {
        val wasAdded = _dependents.add(dependentRef)

        if (!wasAdded) {
            throw IllegalStateException("Attempted to re-register the same dependency")
        }

        if (_dependents.size == 1) {
            onFirstDependencyAdded()
        }

        return object : Subscription {
            override fun cancel() {
                val wasRemoved = _dependents.remove(dependentRef)

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
