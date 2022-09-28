package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.impl.Transaction

internal class ConstantSignalVertex<A>(
    private val value: A,
) : SignalVertex<A>() {
    override fun pullCurrentValue(transaction: Transaction): A = value
}
