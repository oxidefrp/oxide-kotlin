package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.impl.Transaction

internal class PureMomentVertex<A>(
    private val value: A,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A = value
}
