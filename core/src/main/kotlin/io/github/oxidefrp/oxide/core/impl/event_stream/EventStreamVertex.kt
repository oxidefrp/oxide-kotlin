package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.DependencyVertex
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class EventStreamVertex<out A> : DependencyVertex() {
    abstract fun pullCurrentOccurrence(transaction: Transaction): Option<A>
}
