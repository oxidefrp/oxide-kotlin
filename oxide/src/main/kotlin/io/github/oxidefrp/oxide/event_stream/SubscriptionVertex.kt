package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option

class SubscriptionVertex<A>(
    private val stream: EventStreamVertex<A>,
    private val listener: (A) -> Unit,
) : Vertex() {
    override val dependents: Set<Vertex> = emptySet()

    private var observedOccurrence: Option<A>? = null

    override fun update() {
        if (observedOccurrence != null) {
            throw IllegalStateException("There's already an observed occurrence")
        }

        observedOccurrence = stream.currentOccurrence
    }

    override fun reset() {
    }

    override fun execute() {
        val occurrence = observedOccurrence ?: throw IllegalStateException("There's no observed occurrence")

        occurrence.ifSome(listener)

        observedOccurrence = null
    }
}
