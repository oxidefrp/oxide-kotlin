package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction

internal abstract class CellVertex<out A> : DependencyVertex() {
    abstract val oldValue: A

    abstract fun pullNewValue(transaction: Transaction): Option<A>
}
