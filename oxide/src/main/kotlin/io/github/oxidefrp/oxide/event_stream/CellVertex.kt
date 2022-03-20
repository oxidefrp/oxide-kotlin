package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal abstract class CellVertex<out A> : DependencyVertex() {
    abstract val oldValue: A

    abstract fun pullNewValue(transaction: Transaction): Option<A>
}
