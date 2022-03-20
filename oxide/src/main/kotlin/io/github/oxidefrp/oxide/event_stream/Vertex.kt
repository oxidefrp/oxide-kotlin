package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Transaction

internal abstract class Vertex {
    internal abstract val dependents: Set<Vertex>

    abstract fun process(transaction: Transaction)
}
