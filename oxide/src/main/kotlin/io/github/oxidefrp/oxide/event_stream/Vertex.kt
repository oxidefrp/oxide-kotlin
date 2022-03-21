package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Transaction

internal abstract class Vertex {
    abstract fun getDependents(): Iterable<Vertex>

    abstract fun process(transaction: Transaction)
}
