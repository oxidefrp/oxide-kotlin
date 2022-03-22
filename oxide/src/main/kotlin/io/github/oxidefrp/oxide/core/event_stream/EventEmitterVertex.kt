package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.None
import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Some
import io.github.oxidefrp.oxide.core.Transaction

internal class EventEmitterVertex<A> : RootEventStreamVertex<A>() {
    private var emittedOccurrence: Option<A> = None()

    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> =
        emittedOccurrence

    fun emit(event: A) {
        Transaction.wrap { transaction ->
            if (emittedOccurrence.isSome()) {
                throw IllegalStateException("Emitter is already emitting an event")
            }

            emittedOccurrence = Some(event)

            transaction.process(this)

            emittedOccurrence = None()
        }
    }
}
