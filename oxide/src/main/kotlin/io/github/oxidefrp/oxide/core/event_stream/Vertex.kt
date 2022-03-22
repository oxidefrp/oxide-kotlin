package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Transaction

internal abstract class Vertex {
    abstract fun getDependents(): Iterable<Vertex>

    abstract fun process(transaction: Transaction)
}
