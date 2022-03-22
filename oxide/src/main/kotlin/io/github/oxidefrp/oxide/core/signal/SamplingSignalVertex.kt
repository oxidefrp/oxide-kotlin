package io.github.oxidefrp.oxide.core.signal

import io.github.oxidefrp.oxide.core.None
import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Some
import io.github.oxidefrp.oxide.core.Transaction
import io.github.oxidefrp.oxide.core.getOrElse

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
