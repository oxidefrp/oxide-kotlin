package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.impl.Transaction

internal class ConstantSignalVertex<A>(
    private val value: A,
) : SignalVertex<A>() {
    override fun pullCurrentValue(transaction: Transaction): A = value
}
