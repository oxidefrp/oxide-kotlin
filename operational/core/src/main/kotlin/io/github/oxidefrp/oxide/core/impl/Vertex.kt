package io.github.oxidefrp.oxide.core.impl

internal abstract class Vertex {
    companion object {
        private var nextId: Int = 0
    }

    val id = nextId++

    abstract fun getDependents(): Iterable<Vertex>

    // Thought: Does the unwritten contract of this method require all overrides
    // to be idempotent? Can transaction call `process` on a single vertex
    // multiple time in practice?
    abstract fun process(transaction: Transaction)

    override fun toString(): String = "Vertex{id = $id}"
}
