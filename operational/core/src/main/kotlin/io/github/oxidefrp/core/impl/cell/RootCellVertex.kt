package io.github.oxidefrp.core.impl.cell

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.event_stream.CellVertex

internal abstract class RootCellVertex<A> : CellVertex<A>() {
    final override fun process(transaction: Transaction) {
    }

    final override fun onFirstDependencyAdded() {
    }

    final override fun onLastDependencyRemoved() {
    }
}
