package io.github.oxidefrp.oxide.event_stream

abstract class Vertex {
    internal abstract val dependents: Set<Vertex>

    abstract fun update()

    abstract fun reset()

    abstract fun execute()
}
