package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.impl.Transaction

internal class MapSignalVertex<A, B>(
    private val source: SignalVertex<A>,
    private val transform: (A) -> B,
) : SamplingSignalVertex<B>() {
    override fun pullCurrentValueUncached(transaction: Transaction): B =
        transform(source.pullCurrentValue(transaction))
}
