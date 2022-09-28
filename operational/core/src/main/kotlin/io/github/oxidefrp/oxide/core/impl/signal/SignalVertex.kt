package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class SignalVertex<out A> {
    abstract fun pullCurrentValue(transaction: Transaction): A
}
