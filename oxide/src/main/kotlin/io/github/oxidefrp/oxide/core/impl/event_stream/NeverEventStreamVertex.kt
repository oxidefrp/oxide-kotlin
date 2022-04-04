package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.None
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class NeverEventStreamVertex<A> : RootEventStreamVertex<A>() {
    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> = None()
}
