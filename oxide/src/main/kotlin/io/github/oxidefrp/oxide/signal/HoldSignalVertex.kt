package io.github.oxidefrp.oxide.signal

import io.github.oxidefrp.oxide.Cell
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.cell.HoldCellVertex
import io.github.oxidefrp.oxide.event_stream.CellVertex
import io.github.oxidefrp.oxide.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.hold

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
