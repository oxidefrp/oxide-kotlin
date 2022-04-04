package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.EventStreamVertex

internal class HoldCellVertex<A>(
    private val steps: EventStreamVertex<A>,
    initialValue: A,
) : ReactiveCellVertex<A>() {
    private var stepsSubscription =
        steps.registerDependentWeak(this)

    private var storedOldValue: A = initialValue

    override fun onFirstDependencyAdded() {
        stepsSubscription.cancel()
        stepsSubscription = steps.registerDependent(this)
    }

    override fun onLastDependencyRemoved() {
        stepsSubscription.cancel()
        stepsSubscription = steps.registerDependentWeak(this)
    }

    override val oldValue: A
        get() = storedOldValue

    override fun storeNewValue(newValue: A) {
        storedOldValue = newValue
    }

    override fun pullNewValueUncached(transaction: Transaction): Option<A> =
        steps.pullCurrentOccurrence(transaction = transaction)
}
