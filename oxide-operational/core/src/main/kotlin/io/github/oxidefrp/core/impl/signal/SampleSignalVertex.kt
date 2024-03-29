package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.impl.Transaction

internal class SampleSignalVertex<A>(
    private val signal: SignalVertex<Signal<A>>,
) : SignalVertex<A>() {
    override fun pullCurrentValue(transaction: Transaction): A {
        val innerSignal = signal.pullCurrentValue(transaction = transaction)
        return innerSignal.vertex.pullCurrentValue(transaction = transaction)
    }
}
