package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.CellVertex

internal abstract class ReactiveCellVertex<A> : CellVertex<A>() {
    private var cachedNewValue: Option<A>? = null

    final override fun process(transaction: Transaction) {
        cacheNewValue(transaction = transaction)
    }

    final override fun pullNewValue(transaction: Transaction): Option<A> =
        cacheNewValue(transaction)

    /**
     * Cache the new value if not already cached
     *
     * @return the cached value
     */
    private fun cacheNewValue(transaction: Transaction) = cachedNewValue ?: run {
        val newValue = pullNewValueUncached(transaction = transaction)

        cachedNewValue = newValue

        transaction.enqueueForReset {
            newValue.ifSome {
                storeNewValue(newValue = it)
            }

            cachedNewValue = null
        }

        newValue
    }

    protected abstract fun storeNewValue(newValue: A)

    protected abstract fun pullNewValueUncached(transaction: Transaction): Option<A>
}
