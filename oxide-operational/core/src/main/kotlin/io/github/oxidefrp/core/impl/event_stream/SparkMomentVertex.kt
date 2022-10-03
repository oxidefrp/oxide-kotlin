package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.moment.MomentVertex

internal class SparkMomentVertex<A>(
    private val value: A,
) : MomentVertex<EventStream<A>>() {
    override fun computeCurrentValue(
        transaction: Transaction,
    ): EventStream<A> = object : EventStream<A>() {
        override val vertex = SparkEventStreamVertex(
            transaction = transaction,
            value = value,
        )
    }
}

private class SparkEventStreamVertex<A>(
    transaction: Transaction,
    value: A,
) : RootEventStreamVertex<A>() {
    private var sparkValue: Option<A> = Some(value)

    init {
        transaction.enqueueForReset {
            sparkValue = None()
        }
    }

    override fun pullCurrentOccurrence(
        transaction: Transaction,
    ): Option<A> = sparkValue
}
