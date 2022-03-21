package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.Transaction

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
