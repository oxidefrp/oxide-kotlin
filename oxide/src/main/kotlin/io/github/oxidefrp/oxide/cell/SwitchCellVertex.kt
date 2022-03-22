package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.Cell
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Some
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.event_stream.CellVertex
import io.github.oxidefrp.oxide.event_stream.Subscription
import io.github.oxidefrp.oxide.getOrElse

internal class SwitchCellVertex<A>(
    private val source: CellVertex<Cell<A>>,
) : TransformingCellVertex<A>() {
    private var outerSubscription: Subscription? = null

    private var innerSubscription: Subscription? = null

    override fun onTransformationResumed() {
        val innerCell = source.oldValue.vertex

        if (outerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered outer subscription")
        }

        if (innerSubscription != null) {
            throw RuntimeException("Critical: there's already a remembered innerSubscription subscription")
        }

        outerSubscription = source.registerDependent(this)
        innerSubscription = innerCell.registerDependent(this)
    }

    override fun onTransformationPaused() {
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
