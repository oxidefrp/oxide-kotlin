package io.github.oxidefrp.oxide.core.signal

import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.Transaction
import io.github.oxidefrp.oxide.core.cell.HoldCellVertex
import io.github.oxidefrp.oxide.core.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.event_stream.EventStreamVertex

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
