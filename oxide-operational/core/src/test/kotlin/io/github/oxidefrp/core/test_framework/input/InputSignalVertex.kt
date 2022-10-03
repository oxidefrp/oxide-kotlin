package io.github.oxidefrp.core.test_framework.input

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.signal.SignalVertex
import io.github.oxidefrp.core.test_framework.TickStream
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec

internal class InputSignalVertex<A>(
    private val tickStream: TickStream,
    private val spec: InputSignalSpec<A>,
) : SignalVertex<A>() {
    override fun pullCurrentValue(transaction: Transaction): A =
        spec.getValue(tick = tickStream.currentTick)
}
