package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Transaction

internal class ApplySignalVertex<A, B>(
    private val function: SignalVertex<(A) -> B>,
    private val argument: SignalVertex<A>,
) : SamplingSignalVertex<B>() {
    override fun pullCurrentValueUncached(transaction: Transaction): B {
        val currentFunction = function.pullCurrentValue(transaction = transaction)
        val currentArgument = argument.pullCurrentValue(transaction = transaction)

        return currentFunction(currentArgument)
    }
}
