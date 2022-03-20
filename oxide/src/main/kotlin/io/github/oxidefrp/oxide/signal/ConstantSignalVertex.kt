package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Transaction

internal class ConstantSignalVertex<A>(
    private val value: A,
) : SignalVertex<A>() {
    override fun pullCurrentValue(transaction: Transaction): A = value
}
