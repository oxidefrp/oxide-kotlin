package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class HoldCellVertex<A>(
    transaction: Transaction,
    private val steps: EventStream<A>,
    initialValue: A,
) : ReactiveCellVertex<A>() {
    private val stepsVertexLazy = lazy {
        steps.vertex.also {
            // Evaluating the steps vertex has a side effect of registering a
            // weak dependent on it, ensuring that this vertex will be processed
            // at each moment when a step occurs, updating the stored value.
            // The dependent is weak, so when nobody holds the reference to this
            // vertex anymore, it can be garbage collected. If nobody can ask
            // for the stored value, then there's no sense to update it forever.
            it.registerDependentWeak(
                transaction = transaction,
                dependent = this,
            )
        }
    }

    // Evaluate the steps vertex lazily, so creating this vertex doesn't depend
    // on the steps stream.
    private val stepsVertex by stepsVertexLazy

    private var storedOldValue: A = initialValue

    init {
        // To ensure that a step occurring during the moment this vertex is
        // created is stored (as required by the semantics), we enqueue this
        // vertex for processing in this transaction. We can't just process it
        // now, as we don't want to depend on the steps stream vertex during
        // instantiation (as the step stream might be looped).
        transaction.enqueueForProcess(this)
    }

    override fun onFirstDependencyAdded(transaction: Transaction) {
    }

    override fun onLastDependencyRemoved(transaction: Transaction) {
    }

    // Note that the old value can be accessed without relying on the steps
    // stream in the first moment of this vertex lifetime, which reflects the
    // semantics.
    override val oldValue: A
        get() = storedOldValue

    override fun storeNewValue(newValue: A) {
        storedOldValue = newValue
    }

    override fun pullNewValueUncached(transaction: Transaction): Option<A> =
    // Note: This will register the dependent on the steps stream during the
        // first moment
        stepsVertex.pullCurrentOccurrence(transaction = transaction)

    override fun toString(): String {
        val stepsId = if (stepsVertexLazy.isInitialized()) stepsVertex.id else null
        return "HoldCellVertex{id = $id, stepsVertex.id = ${stepsId ?: "<uninitialized>"}}"
    }
}
