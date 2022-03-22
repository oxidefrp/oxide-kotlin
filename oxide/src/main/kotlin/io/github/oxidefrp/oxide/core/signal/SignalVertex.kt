package io.github.oxidefrp.oxide.core.signal

import io.github.oxidefrp.oxide.core.Transaction

internal abstract class SignalVertex<out A> {
    abstract fun pullCurrentValue(transaction: Transaction): A
}
