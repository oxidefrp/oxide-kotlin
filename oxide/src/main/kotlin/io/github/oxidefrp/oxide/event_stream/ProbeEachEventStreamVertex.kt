package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Signal
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.signal.SignalVertex

internal class ProbeEachEventStreamVertex<A>(
    private val stream: EventStreamVertex<Signal<A>>,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription =
        stream.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        stream.pullCurrentOccurrence(transaction = transaction).map { signal ->
            signal.vertex.pullCurrentValue(transaction = transaction)
        }
}
