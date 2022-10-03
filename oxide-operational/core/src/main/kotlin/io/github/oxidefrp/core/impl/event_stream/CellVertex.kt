package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.DependencyVertex
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.getOrElse

internal abstract class CellVertex<out A> : DependencyVertex() {
    // Thought: Should this accept a `transaction` argument?
    abstract val oldValue: A

    abstract fun pullNewValue(transaction: Transaction): Option<A>

    // TODO: Figure out if this should be used
//    fun getNewValue(transaction: Transaction): A =
//        pullNewValue(transaction = transaction).getOrElse { oldValue }
}
