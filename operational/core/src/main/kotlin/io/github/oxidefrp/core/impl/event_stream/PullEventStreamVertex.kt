package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class PullEventStreamVertex<A>(
    private val source: EventStreamVertex<Moment<A>>,
) : ObservingEventStreamVertex<A>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        source.pullCurrentOccurrence(transaction = transaction).map { moment ->
            moment.vertex.computeCurrentValue(transaction = transaction)
        }

    override fun toString(): String = "PullMomentVertex{id = $id, source.id = ${source.id}}"
}
