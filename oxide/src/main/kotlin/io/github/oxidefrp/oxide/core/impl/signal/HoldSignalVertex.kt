package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.cell.HoldCellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.EventStreamVertex

internal class HoldSignalVertex<A>(
    private val steps: EventStreamVertex<A>,
    private val initialValue: A,
) : SamplingSignalVertex<Cell<A>>() {
    override fun pullCurrentValueUncached(transaction: Transaction): Cell<A> =
        object : Cell<A>() {
            override val vertex: CellVertex<A> = HoldCellVertex(
                steps = steps,
                initialValue = initialValue,
            )
        }
}
