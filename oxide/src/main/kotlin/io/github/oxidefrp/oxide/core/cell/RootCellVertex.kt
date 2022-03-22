package io.github.oxidefrp.oxide.core.cell

import io.github.oxidefrp.oxide.core.Transaction
import io.github.oxidefrp.oxide.core.event_stream.CellVertex

internal abstract class RootCellVertex<A> : CellVertex<A>() {
    final override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded() {
    }

    final override fun onLastDependencyRemoved() {
    }
}
