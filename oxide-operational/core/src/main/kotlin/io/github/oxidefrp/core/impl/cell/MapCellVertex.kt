package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.CellVertex
import io.github.oxidefrp.core.impl.event_stream.TransactionSubscription

internal class MapCellVertex<A, B>(
    private val source: CellVertex<A>,
    private val transform: (A) -> B,
) : ObservingCellVertex<B>() {
    override fun observe(transaction: Transaction): TransactionSubscription =
        source.registerDependent(transaction = transaction, dependent = this)

    override fun sampleOldValue(): B =
        transform(source.oldValue)

    override fun pullNewValueUncached(transaction: Transaction): Option<B> =
        source.pullNewValue(transaction = transaction).map(transform)
}
