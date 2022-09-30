package io.github.oxidefrp.core.test_framework.input

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.cell.ObservingCellVertex
import io.github.oxidefrp.core.impl.event_stream.TransactionSubscription
import io.github.oxidefrp.core.test_framework.TickStream
import io.github.oxidefrp.core.test_framework.shared.InputCellSpec

internal class InputCellVertex<A>(
    private val tickStream: TickStream,
    private val spec: InputCellSpec<A>,
) : ObservingCellVertex<A>() {
    override fun pullNewValueUncached(transaction: Transaction): Option<A> =
        Option.of(spec.getNewValueOccurrence(tick = tickStream.currentTick)?.event)

    override fun sampleOldValue(): A =
        spec.getOldValue(tick = tickStream.currentTick)

    override fun observe(transaction: Transaction): TransactionSubscription =
        tickStream.vertex.registerDependent(transaction = transaction, dependent = this)
}
