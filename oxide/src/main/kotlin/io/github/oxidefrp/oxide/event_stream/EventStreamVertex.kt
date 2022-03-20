package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal abstract class EventStreamVertex<out A> : DependencyVertex() {
    abstract fun pullCurrentOccurrence(transaction: Transaction): Option<A>
}
