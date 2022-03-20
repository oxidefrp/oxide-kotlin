package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Transaction

internal class MapSignalVertex<A, B>(
    private val source: SignalVertex<A>,
    private val transform: (A) -> B,
) : SamplingSignalVertex<B>() {
    override fun pullCurrentValueUncached(transaction: Transaction): B =
        transform(source.pullCurrentValue(transaction))
}
