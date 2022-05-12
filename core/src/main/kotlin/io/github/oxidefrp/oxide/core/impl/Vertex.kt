package io.github.oxidefrp.oxide.core.impl

internal abstract class Vertex {
    abstract fun getDependents(): Iterable<Vertex>

    abstract fun process(transaction: Transaction)
}

