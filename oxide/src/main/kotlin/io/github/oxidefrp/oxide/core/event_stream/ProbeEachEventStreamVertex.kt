package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.Transaction

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
