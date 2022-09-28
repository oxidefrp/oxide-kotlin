package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.impl.Transaction

internal abstract class SignalVertex<out A> {
    abstract fun pullCurrentValue(transaction: Transaction): A
}
