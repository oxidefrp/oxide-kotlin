package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.DependencyVertex
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class CellVertex<out A> : DependencyVertex() {
    abstract val oldValue: A

    abstract fun pullNewValue(transaction: Transaction): Option<A>
}
