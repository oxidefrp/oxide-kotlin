package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.Vertex

internal class StateVertex<out A>(
    transaction: Transaction,
    initialValue: A,
    private val steps: EventStream<A>,
) : Vertex() {
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

    private var storedValue = initialValue

    val currentValue: A
        get() = storedValue

    init {
        // To ensure that a step occurring during the moment this vertex is
        // created is stored (as required by the semantics), we enqueue this
        // vertex for processing in this transaction. We can't just process it
        // now, as we don't want to depend on the steps stream vertex during
        // instantiation (as the step stream might be looped).
        transaction.enqueueForProcess(this)
    }

    override fun getDependents(): Iterable<Vertex> = listOf()

    override fun process(transaction: Transaction) {
        // Note: This will register a weak dependent on the steps stream during the
        // first moment
        stepsVertex.pullCurrentOccurrence(transaction = transaction).ifSome { newValue ->
            transaction.enqueueForReset {
                storedValue = newValue
            }
        }
    }

    override fun toString(): String {
        val stepsId = if (stepsVertexLazy.isInitialized()) stepsVertex.id else null
        return "StateVertex{id = $id, stepsVertex.id = ${stepsId ?: "<uninitialized>"}}"
    }
}

internal class DelayedStateMoment<out A>(
    private val moment: Moment<A>,
    private val steps: EventStream<A>,
) : Moment<A>() {
    private var cachedStateVertex: StateVertex<A>? = null

    override fun pullCurrentValue(transaction: Transaction): A {
        val stateVertex = this.cachedStateVertex ?: run {
            val initialValue = moment.pullCurrentValue(
                transaction = transaction,
            )

            val stateVertex = StateVertex(
                transaction = transaction,
                initialValue = initialValue,
                steps = steps,
            )

            cachedStateVertex = stateVertex

            stateVertex
        }

        return stateVertex.currentValue
    }
}
