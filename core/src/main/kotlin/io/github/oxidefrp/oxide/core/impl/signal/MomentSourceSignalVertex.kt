package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class MomentSourceSignalVertex<A>(
    private val moment: Moment<A>,
) : SamplingSignalVertex<A>() {
    override fun pullCurrentValueUncached(transaction: Transaction): A =
        moment.vertex.computeCurrentValue(transaction = transaction)
}
