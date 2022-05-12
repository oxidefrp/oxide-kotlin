package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.cell.HoldCellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.core.impl.moment.MomentVertex

internal class HoldMomentVertex<A>(
    private val steps: EventStreamVertex<A>,
    private val initialValue: A,
) : MomentVertex<Cell<A>>() {
    override fun computeCurrentValue(
        transaction: Transaction,
    ): Cell<A> = object : Cell<A>() {
        override val vertex: CellVertex<A> = HoldCellVertex(
            steps = steps,
            initialValue = initialValue,
        )
    }
}
