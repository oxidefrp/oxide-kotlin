package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.orElse

internal class SquashWithEventStreamVertex<A, B, C>(
    private val source1: EventStreamVertex<A>,
    private val source2: EventStreamVertex<B>,
    private val ifFirst: (A) -> C,
    private val ifSecond: (B) -> C,
    private val ifBoth: (A, B) -> C,
) : ObservingEventStreamVertex<C>() {
    override fun observe(transaction: Transaction): TransactionSubscription {
        val subscription1 = source1.registerDependent(transaction = transaction, dependent = this)
        val subscription2 = source2.registerDependent(transaction = transaction, dependent = this)
        return subscription1 + subscription2
    }

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<C> {
        val occurrence1 = source1.pullCurrentOccurrence(transaction = transaction)
        val occurrence2 = source2.pullCurrentOccurrence(transaction = transaction)

        return Option
            .map2(occurrence1, occurrence2, ifBoth)
            .orElse { occurrence1.map(ifFirst) }
            .orElse { occurrence2.map(ifSecond) }
    }
}
