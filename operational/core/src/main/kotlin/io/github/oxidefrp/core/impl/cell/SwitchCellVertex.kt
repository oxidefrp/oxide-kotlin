package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Some
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.CellVertex
import io.github.oxidefrp.core.impl.event_stream.TransactionSubscription
import io.github.oxidefrp.core.impl.getOrElse

internal class SwitchCellVertex<A>(
    private val source: CellVertex<Cell<A>>,
) : PausableCellVertex<A>() {
    private var outerSubscription: TransactionSubscription? = null

    private var innerSubscription: TransactionSubscription? = null

    override fun onResumed(transaction: Transaction) {
        val innerCellVertex = source.oldValue.vertex

        if (outerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered outer subscription")
        }

        if (innerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered innerSubscription subscription")
        }

        outerSubscription = source.registerDependent(transaction = transaction, dependent = this)
        innerSubscription = innerCellVertex.registerDependent(transaction = transaction, dependent = this)
    }

    override fun onPaused(transaction: Transaction) {
        val outerSubscription = this.outerSubscription
            ?: throw RuntimeException("Critical: there's no remembered inner subscription")

        val innerSubscription = this.innerSubscription
            ?: throw RuntimeException("Critical: there's no remembered inner subscription")

        innerSubscription.cancel(transaction = transaction)
        outerSubscription.cancel(transaction = transaction)
    }

    override fun sampleOldValue(): A =
        source.oldValue.vertex.oldValue

    override fun pullNewValueUncached(transaction: Transaction): Option<A> =
        source.pullNewValue(transaction = transaction).fold(
            ifNone = {
                val oldInnerCellVertex = source.oldValue.vertex

                oldInnerCellVertex.pullNewValue(transaction = transaction)
            },
            ifSome = { newInnerCell ->
                val newInnerCellVertex = newInnerCell.vertex

                val subscription = innerSubscription
                    ?: throw RuntimeException("Critical: there's no remembered inner subscription")

                subscription.cancel(transaction = transaction)

                innerSubscription = newInnerCellVertex.registerDependent(transaction = transaction, dependent = this)

                Some(
                    newInnerCellVertex.pullNewValue(transaction = transaction)
                        .getOrElse { newInnerCellVertex.oldValue }
                )
            }
        )
}
