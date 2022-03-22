package io.github.oxidefrp.oxide.core.cell

import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction
import io.github.oxidefrp.oxide.core.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.event_stream.Subscription

internal class MapCellVertex<A, B>(
    private val source: CellVertex<A>,
    private val transform: (A) -> B,
) : ObservingCellVertex<B>() {
    override fun observe(): Subscription =
        source.registerDependent(this)

    override fun sampleOldValue(): B =
        transform(source.oldValue)

    override fun pullNewValueUncached(transaction: Transaction): Option<B> =
        source.pullNewValue(transaction = transaction).map(transform)
}
