package io.github.oxidefrp.core.impl.signal

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.cell.HoldCellVertex
import io.github.oxidefrp.core.impl.event_stream.CellVertex
import io.github.oxidefrp.core.impl.moment.MomentVertex

internal class HoldMomentVertex<A>(
    private val steps: EventStream<A>,
    private val initialValue: A,
) : MomentVertex<Cell<A>>() {
    override fun computeCurrentValue(
        transaction: Transaction,
    ): Cell<A> {
        // Idea: Pass `transaction` to the constructor?
        val cellVertex = HoldCellVertex(
            transaction = transaction,
            steps = steps,
            initialValue = initialValue,
        )


        return object : Cell<A>() {
            override val vertex: CellVertex<A> = cellVertex
        }
    }
}
