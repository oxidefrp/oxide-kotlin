package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction

internal class MutableCellVertex<A>(
    initialValue: A,
) : RootCellVertex<A>() {
    private var value: A = initialValue

    private var newValue: Option<A> = None()

    override val oldValue: A
        get() = value

    override fun pullNewValue(transaction: Transaction): Option<A> =
        newValue

    fun setValue(newValue: A) {
        // Thought: Can the number of custom wrapped transactions be reduced?
        // Could `Transaction.pull` replace `Transaction.wrap`?
        Transaction.wrap { transaction ->
            if (this.newValue.isSome()) {
                throw IllegalStateException("New value is already being set")
            }

            this.newValue = Some(newValue)

            transaction.enqueueForProcess(this)

            transaction.enqueueForReset {
                value = newValue
                this.newValue = None()
            }
        }
    }
}
