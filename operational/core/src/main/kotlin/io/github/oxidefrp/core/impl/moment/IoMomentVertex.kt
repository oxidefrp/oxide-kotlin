package io.github.oxidefrp.core.impl.moment

import io.github.oxidefrp.core.Io
import io.github.oxidefrp.core.impl.Transaction

internal class IoMomentVertex<A>(
    private val io: Io<A>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        io.performExternally()
}
