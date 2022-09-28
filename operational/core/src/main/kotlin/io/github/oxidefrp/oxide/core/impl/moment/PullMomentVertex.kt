package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.impl.Transaction

internal class PullMomentVertex<A>(
    private val source: MomentVertex<MomentVertex<A>>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        source.computeCurrentValue(transaction = transaction)
            .computeCurrentValue(transaction = transaction)
}
