package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.getOrElse

internal abstract class TransformingCellVertex<A> : ReactiveCellVertex<A>() {
    private var cachedOldValue: Option<A> = None()

    final override val oldValue: A
        get() = cachedOldValue.getOrElse { sampleOldValue() }

    final override fun onFirstDependencyAdded() {
        cachedOldValue = Some(sampleOldValue())

        onTransformationResumed()
    }

    final override fun onLastDependencyRemoved() {
        cachedOldValue = None()

        onTransformationPaused()
    }

    override fun storeNewValue(newValue: A) {
        if (cachedOldValue.isNone()) {
            throw IllegalStateException("Attempted to store a new value in an empty cache")
        }

        cachedOldValue = Some(newValue)

    }

    abstract fun onTransformationResumed()

    abstract fun onTransformationPaused()

    abstract fun sampleOldValue(): A
}
