package io.github.oxidefrp.oxide.core.cell

import io.github.oxidefrp.oxide.core.None
import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction

internal class ConstantCellVertex<A>(
    private val value: A,
) : RootCellVertex<A>() {
    override val oldValue: A
        get() = value

    override fun pullNewValue(transaction: Transaction): Option<A> = None()
}
