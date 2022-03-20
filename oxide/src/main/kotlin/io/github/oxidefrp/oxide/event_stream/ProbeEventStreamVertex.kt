package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.signal.SignalVertex

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
