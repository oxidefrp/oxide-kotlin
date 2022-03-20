package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.event_stream.CellVertex
import io.github.oxidefrp.oxide.getOrElse

internal abstract class CachingCellVertex<A> : CellVertex<A>() {
    private var cachedOldValue: Option<A> = None()

    private var cachedNewValue: Option<A>? = null

    final override fun process(transaction: Transaction) {
        pullNewValue(transaction = transaction)
    }

    final override val oldValue: A
        get() = cachedOldValue.getOrElse { sampleOldValue() }

    final override fun pullNewValue(transaction: Transaction): Option<A> =
        cachedNewValue ?: run {
            val newValue = pullNewValueUncached(transaction = transaction)

            cachedNewValue = newValue

            transaction.enqueueForReset {
                newValue.ifSome {
                    cachedOldValue = Some(it)
                }

                cachedNewValue = null
            }

            newValue
        }

    abstract fun sampleOldValue(): A

    abstract fun pullNewValueUncached(transaction: Transaction): Option<A>
}
