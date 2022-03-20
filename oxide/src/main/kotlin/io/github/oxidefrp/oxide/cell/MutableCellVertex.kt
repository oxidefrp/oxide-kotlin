package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.Transaction

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
        Transaction.wrap { transaction ->
            if (this.newValue.isSome()) {
                throw IllegalStateException("New value is already being set")
            }

            this.newValue = Some(newValue)

            transaction.enqueueForReset {
                value = newValue
                this.newValue = None()
            }

            transaction.process(this)
        }
    }
}
