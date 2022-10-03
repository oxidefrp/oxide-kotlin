package io.github.oxidefrp.core.impl.moment

import io.github.oxidefrp.core.impl.Transaction

internal class MapMomentVertex<A, B>(
    private val source: MomentVertex<A>,
    private val transform: (A) -> B,
) : MomentVertex<B>() {
    override fun computeCurrentValue(transaction: Transaction): B =
        transform(source.computeCurrentValue(transaction = transaction))
}
