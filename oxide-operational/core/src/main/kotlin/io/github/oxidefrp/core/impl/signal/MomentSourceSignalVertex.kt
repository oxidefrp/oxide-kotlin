package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Transaction

internal class MomentSourceSignalVertex<A>(
    private val moment: Moment<A>,
) : SamplingSignalVertex<A>() {
    override fun pullCurrentValueUncached(transaction: Transaction): A =
        moment.pullCurrentValue(transaction = transaction)
}
