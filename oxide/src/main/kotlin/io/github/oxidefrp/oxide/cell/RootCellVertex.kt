package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.event_stream.CellVertex

internal abstract class RootCellVertex<A> : CellVertex<A>() {
    final override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded() {
    }

    final override fun onLastDependencyRemoved() {
    }
}
