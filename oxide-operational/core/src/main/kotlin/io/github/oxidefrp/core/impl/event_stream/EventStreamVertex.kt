package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.DependencyVertex
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal abstract class EventStreamVertex<out A> : DependencyVertex() {
    abstract fun pullCurrentOccurrence(transaction: Transaction): Option<A>
}
