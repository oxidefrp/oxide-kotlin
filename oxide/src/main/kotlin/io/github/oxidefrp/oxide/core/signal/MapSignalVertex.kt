package io.github.oxidefrp.oxide.core.signal

import io.github.oxidefrp.oxide.core.Transaction

internal class MapSignalVertex<A, B>(
    private val source: SignalVertex<A>,
    private val transform: (A) -> B,
) : SamplingSignalVertex<B>() {
    override fun pullCurrentValueUncached(transaction: Transaction): B =
        transform(source.pullCurrentValue(transaction))
}
