package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class ConstantCellVertex<A>(
    private val value: A,
) : RootCellVertex<A>() {
    override val oldValue: A
        get() = value

    override fun pullNewValue(transaction: Transaction): Option<A> = None()
}
