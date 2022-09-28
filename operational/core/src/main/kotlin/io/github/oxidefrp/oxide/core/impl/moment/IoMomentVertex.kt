package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.Io
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class IoMomentVertex<A>(
    private val io: Io<A>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        io.performExternally()
}
