package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction

internal abstract class EventStreamVertex<out A> : DependencyVertex() {
    abstract fun pullCurrentOccurrence(transaction: Transaction): Option<A>
}
