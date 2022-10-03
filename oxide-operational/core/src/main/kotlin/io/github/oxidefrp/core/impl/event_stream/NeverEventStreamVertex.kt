package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.None
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal class NeverEventStreamVertex<A> : RootEventStreamVertex<A>() {
    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> = None()
}
