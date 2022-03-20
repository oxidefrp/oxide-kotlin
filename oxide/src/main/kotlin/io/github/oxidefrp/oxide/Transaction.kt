package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.Vertex

internal class Transaction {
    private val resetQueue = mutableListOf<() -> Unit>()

    private val propagationQueue = mutableListOf<() -> Unit>()

    fun enqueueForReset(callback: () -> Unit) {
        resetQueue.add(callback)
    }

    fun enqueueForPropagation(callback: () -> Unit) {
        propagationQueue.add(callback)
    }

    fun run(root: Vertex) {
        val transitiveDependents = mutableSetOf<Vertex>()

        fun collectTransitiveDependents(vertex: Vertex) {
            vertex.dependents.forEach {
                transitiveDependents.add(it)
                collectTransitiveDependents(it)
            }
        }

        collectTransitiveDependents(root)

        transitiveDependents.forEach { it.process(transaction = this) }

        resetQueue.forEach { it() }

        propagationQueue.forEach { it() }
    }
}
