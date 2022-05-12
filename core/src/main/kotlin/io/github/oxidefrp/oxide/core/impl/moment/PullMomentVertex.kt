package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class PullMomentVertex<A>(
    private val source: MomentVertex<Moment<A>>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        source.computeCurrentValue(transaction = transaction).vertex
            .computeCurrentValue(transaction = transaction)
}
