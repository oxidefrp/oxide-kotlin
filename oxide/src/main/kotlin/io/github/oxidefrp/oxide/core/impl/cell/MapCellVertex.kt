package io.github.oxidefrp.oxide.core.impl.cell

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.CellVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription

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
