package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Some
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import io.github.oxidefrp.oxide.core.impl.getOrElse

internal class SwitchCellVertex<A>(
    private val source: CellVertex<Cell<A>>,
) : PausableCellVertex<A>() {
    private var outerSubscription: Subscription? = null

    private var innerSubscription: Subscription? = null

    override fun onResumed() {
        val innerCellVertex = source.oldValue.vertex

        if (outerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered outer subscription")
        }

        if (innerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered innerSubscription subscription")
        }

        outerSubscription = source.registerDependent(this)
        innerSubscription = innerCellVertex.registerDependent(this)
    }

    override fun onPaused() {
        val outerSubscription = this.outerSubscription
            ?: throw RuntimeException("Critical: there's no remembered inner subscription")

        val innerSubscription = this.innerSubscription
            ?: throw RuntimeException("Critical: there's no remembered inner subscription")

        innerSubscription.cancel()
        outerSubscription.cancel()
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

                subscription.cancel()

                innerSubscription = newInnerCellVertex.registerDependent(this)

                Some(
                    newInnerCellVertex.pullNewValue(transaction = transaction)
                        .getOrElse { newInnerCellVertex.oldValue }
                )
            }
        )
}
