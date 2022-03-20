package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Transaction

internal class SourceSignalVertex<A>(
    private val sampleExternal: () -> A,
) : SamplingSignalVertex<A>() {
    override fun pullCurrentValueUncached(transaction: Transaction): A =
        sampleExternal()
}
