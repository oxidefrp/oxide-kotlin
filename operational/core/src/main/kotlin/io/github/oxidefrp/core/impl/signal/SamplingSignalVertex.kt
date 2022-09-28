package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.getOrElse

internal abstract class SamplingSignalVertex<A> : SignalVertex<A>() {
    private var cachedCurrentValue: Option<A> = None()

    override fun pullCurrentValue(transaction: Transaction): A =
        cachedCurrentValue.getOrElse {
            val value = pullCurrentValueUncached(transaction = transaction)

            cachedCurrentValue = Some(value)

            transaction.enqueueForReset {
                cachedCurrentValue = None()
            }

            value
        }

    abstract fun pullCurrentValueUncached(transaction: Transaction): A
}
