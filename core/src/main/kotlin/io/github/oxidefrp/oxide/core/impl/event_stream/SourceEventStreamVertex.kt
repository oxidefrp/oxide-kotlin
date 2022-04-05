package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class SourceEventStreamVertex<A>(
    private val subscribe: (emit: (A) -> Unit) -> Subscription,
) : ObservingEventStreamVertex<A>() {
    private val emitterMixin = EmitterMixin<A>(this)

    override fun observe(): Subscription = subscribe { event ->
        emitterMixin.emitInTransaction(event)
    }

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        emitterMixin.currentOccurrence
}
