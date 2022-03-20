package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Transaction

internal abstract class SignalVertex<out A> {
    abstract fun pullCurrentValue(transaction: Transaction): A
}
