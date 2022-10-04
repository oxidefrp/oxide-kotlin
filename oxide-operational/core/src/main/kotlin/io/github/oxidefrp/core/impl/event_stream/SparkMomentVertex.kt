package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction

internal class SparkEventStreamVertex<A>(
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
