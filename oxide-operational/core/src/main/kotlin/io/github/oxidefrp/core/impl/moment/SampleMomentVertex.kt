package io.github.oxidefrp.core.impl.moment

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.signal.SignalVertex

internal class SampleMomentVertex<A>(
    private val signal: SignalVertex<A>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        signal.pullCurrentValue(transaction = transaction)
}
