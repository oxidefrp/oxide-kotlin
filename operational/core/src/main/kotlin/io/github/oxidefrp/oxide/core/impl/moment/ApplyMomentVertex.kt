package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.impl.Transaction

internal class ApplyMomentVertex<A, B>(
    private val function: MomentVertex<(A) -> B>,
    private val argument: MomentVertex<A>,
) : MomentVertex<B>() {
    override fun computeCurrentValue(transaction: Transaction): B {
        val currentFunction = function.computeCurrentValue(transaction = transaction)
        val currentArgument = argument.computeCurrentValue(transaction = transaction)

        return currentFunction(currentArgument)
    }
}
