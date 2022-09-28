package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.impl.Transaction

internal class SourceSignalVertex<A>(
    private val sampleExternal: () -> A,
) : SamplingSignalVertex<A>() {
    override fun pullCurrentValueUncached(transaction: Transaction): A =
        sampleExternal()
}
