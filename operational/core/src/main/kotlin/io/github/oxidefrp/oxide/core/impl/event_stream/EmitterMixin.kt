package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.None
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Some
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.Vertex

internal class EmitterMixin<A>(
    private val vertex: Vertex,
) {
    private var emittedOccurrence: Option<A> = None()

    val currentOccurrence: Option<A>
        get() = emittedOccurrence

    fun emitInTransaction(event: A) {
        Transaction.wrap { transaction ->
            if (emittedOccurrence.isSome()) {
                throw IllegalStateException("Emitter is already emitting an event")
            }

            emittedOccurrence = Some(event)

            transaction.enqueueForProcess(vertex)

            transaction.enqueueForReset {
                emittedOccurrence = None()
            }
        }
    }
}
