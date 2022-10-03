package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.Vertex

internal class SubscriptionVertex<A>(
    private val stream: EventStreamVertex<A>,
    private val listener: (A) -> Unit,
) : Vertex() {
    override fun getDependents(): Iterable<Vertex> = emptyList()

    override fun process(transaction: Transaction) {
        val occurrence = stream.pullCurrentOccurrence(transaction = transaction)

        occurrence.ifSome { a ->
            transaction.enqueueForPropagation {
                listener(a)
            }
        }
    }
}
