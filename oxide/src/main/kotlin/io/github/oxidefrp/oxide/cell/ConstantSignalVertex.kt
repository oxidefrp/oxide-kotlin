package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal class ConstantCellVertex<A>(
    private val value: A,
) : RootCellVertex<A>() {
    override val oldValue: A
        get() = value

    override fun pullNewValue(transaction: Transaction): Option<A> = None()
}
