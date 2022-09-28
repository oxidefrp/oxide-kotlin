package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.impl.None
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Some
import io.github.oxidefrp.oxide.core.impl.getOrElse

// Thought: Would `CachingCellVertex` be a better name?
internal abstract class PausableCellVertex<A> : ReactiveCellVertex<A>() {
    private var cachedOldValue: Option<A> = None()

    final override val oldValue: A
        get() = cachedOldValue.getOrElse { sampleOldValue() }

    final override fun onFirstDependencyAdded() {
        cachedOldValue = Some(sampleOldValue())

        onResumed()
    }

    final override fun onLastDependencyRemoved() {
        cachedOldValue = None()

        onPaused()
    }

    override fun storeNewValue(newValue: A) {
        if (cachedOldValue.isNone()) {
            throw IllegalStateException("Attempted to store a new value in an empty cache")
        }

        cachedOldValue = Some(newValue)

    }

    abstract fun onResumed()

    abstract fun onPaused()

    abstract fun sampleOldValue(): A
}
