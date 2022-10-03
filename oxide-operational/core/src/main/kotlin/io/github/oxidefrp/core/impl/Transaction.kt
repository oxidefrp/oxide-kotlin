package io.github.oxidefrp.core.impl

internal class Transaction private constructor() {
    companion object {
        fun <A> wrap(block: (Transaction) -> A): A {
            val transaction = Transaction()

            val result = block(transaction)

            transaction.finish()

            return result
        }
    }

    private val processQueue = ArrayDeque<Vertex>()

    private val resetQueue = mutableListOf<() -> Unit>()

    private val propagationQueue = mutableListOf<() -> Unit>()

    fun enqueueForProcess(vertex: Vertex) {
        // Idea: Verify if it isn't too late for enqueueing
        processQueue.add(vertex)
    }

    fun enqueueForReset(callback: () -> Unit) {
        resetQueue.add(callback)
    }

    fun enqueueForPropagation(callback: () -> Unit) {
        propagationQueue.add(callback)
    }

    private fun finish() {
        // Finishing the transaction is when most of the work takes place.

        // Process all queued vertices. Initially this is typically one vertex,
        // but more can be queued during the processing of the first one, etc.
        while (true) {
            val vertex = processQueue.removeFirstOrNull() ?: break
            process(root = vertex)
        }

        // Execute all reset actions. Every vertex that cached anything for
        // correctness or performance reasons resets to a clear state.
        resetQueue.forEach { it() }

        // Execute all propagation actions, i.e. effects on the external world.
        // Thought: Can this be nuked when the reactive I/O system is implemented?
        propagationQueue.forEach { it() }
    }

    private fun process(root: Vertex) {
        val transitiveDependents = mutableSetOf<Vertex>()

        // Thought: How are dependents added during the transaction handled? Should they be processed and how?

        fun collectTransitiveDependents(vertex: Vertex) {
            transitiveDependents.add(vertex)

            vertex.getDependents().forEach {
                collectTransitiveDependents(it)
            }
        }

        collectTransitiveDependents(root)

        transitiveDependents.forEach { it.process(transaction = this) }
    }
}
