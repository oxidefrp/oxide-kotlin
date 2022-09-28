package io.github.oxidefrp.oxide.core.test_framework.input

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.cell.ObservingCellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import io.github.oxidefrp.oxide.core.test_framework.TickStream
import io.github.oxidefrp.oxide.core.test_framework.shared.InputCellSpec

internal class InputCellVertex<A>(
    private val tickStream: TickStream,
    private val spec: InputCellSpec<A>,
) : ObservingCellVertex<A>() {
    override fun pullNewValueUncached(transaction: Transaction): Option<A> =
        Option.of(spec.getNewValueOccurrence(tick = tickStream.currentTick)?.event)

    override fun sampleOldValue(): A =
        spec.getOldValue(tick = tickStream.currentTick)

    override fun observe(): Subscription =
        tickStream.vertex.registerDependent(this)
}
