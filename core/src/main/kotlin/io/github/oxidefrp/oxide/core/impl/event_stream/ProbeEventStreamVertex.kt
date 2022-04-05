package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.signal.SignalVertex

internal class ProbeEventStreamVertex<A, B, C>(
    private val stream: EventStreamVertex<A>,
    private val signal: SignalVertex<B>,
    private val combine: (A, B) -> C,
) : ObservingEventStreamVertex<C>() {
    override fun observe(): Subscription =
        stream.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<C> =
        stream.pullCurrentOccurrence(transaction = transaction).map { a ->
            val b = signal.pullCurrentValue(transaction = transaction)
            combine(a, b)
        }
}
