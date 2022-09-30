package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.getOrElse

// Thought: Would `CachingCellVertex` be a better name?
internal abstract class PausableCellVertex<A> : ReactiveCellVertex<A>() {
    private var cachedOldValue: Option<A> = None()

    final override val oldValue: A
        get() = cachedOldValue.getOrElse { sampleOldValue() }

    final override fun onFirstDependencyAdded(transaction: Transaction) {
        cachedOldValue = Some(sampleOldValue())

        onResumed(transaction = transaction)
    }

    final override fun onLastDependencyRemoved(transaction: Transaction) {
        cachedOldValue = None()

        onPaused(transaction = transaction)
    }

    override fun storeNewValue(newValue: A) {
        if (cachedOldValue.isNone()) {
            throw IllegalStateException("Attempted to store a new value in an empty cache")
        }

        cachedOldValue = Some(newValue)

    }

    abstract fun onResumed(transaction: Transaction)

    abstract fun onPaused(transaction: Transaction)

    abstract fun sampleOldValue(): A
}
