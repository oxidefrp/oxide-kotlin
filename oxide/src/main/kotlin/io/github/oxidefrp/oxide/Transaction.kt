package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.event_stream.Vertex

internal class Transaction {
    companion object {
        fun <A> wrap(block: (Transaction) -> A): A {
            val transaction = Transaction()

            val result = block(transaction)

            transaction.finish()

            return result
        }
    }

    private val resetQueue = mutableListOf<() -> Unit>()

    private val propagationQueue = mutableListOf<() -> Unit>()

    fun enqueueForReset(callback: () -> Unit) {
        resetQueue.add(callback)
    }

    fun enqueueForPropagation(callback: () -> Unit) {
        propagationQueue.add(callback)
    }

    fun process(root: Vertex) {
        val transitiveDependents = mutableSetOf<Vertex>()

        fun collectTransitiveDependents(vertex: Vertex) {
            vertex.getDependents().forEach {
                transitiveDependents.add(it)
                collectTransitiveDependents(it)
            }
        }

        collectTransitiveDependents(root)

        transitiveDependents.forEach { it.process(transaction = this) }
    }

    private fun finish() {
        resetQueue.forEach { it() }

        propagationQueue.forEach { it() }
    }
}
