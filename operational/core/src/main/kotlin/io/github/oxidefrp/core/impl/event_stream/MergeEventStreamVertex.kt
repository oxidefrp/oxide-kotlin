package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.orElse

internal class MergeEventStreamVertex<A>(
    private val source1: EventStreamVertex<A>,
    private val source2: EventStreamVertex<A>,
    private val combine: (A, A) -> A,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription {
        val subscription1 = source1.registerDependent(this)
        val subscription2 = source2.registerDependent(this)
        return subscription1 + subscription2
    }

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        Option
            .map2(
                source1.pullCurrentOccurrence(transaction = transaction),
                source2.pullCurrentOccurrence(transaction = transaction),
                combine,
            )
            .orElse { source1.pullCurrentOccurrence(transaction = transaction) }
            .orElse { source2.pullCurrentOccurrence(transaction = transaction) }
}
