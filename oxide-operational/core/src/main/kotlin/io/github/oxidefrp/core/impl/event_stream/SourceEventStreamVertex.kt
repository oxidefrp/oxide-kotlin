package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class SourceEventStreamVertex<A>(
    private val subscribe: (emit: (A) -> Unit) -> ExternalSubscription,
) : ObservingEventStreamVertex<A>() {
    private val emitterMixin = EmitterMixin<A>(this)

    override fun observe(
        transaction: Transaction,
    ): TransactionSubscription = subscribe { event ->
        emitterMixin.emitInTransaction(event)
    }.toTransaction()

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        emitterMixin.currentOccurrence
}
