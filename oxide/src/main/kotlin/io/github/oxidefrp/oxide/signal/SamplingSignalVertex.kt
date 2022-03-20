package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.getOrElse

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
