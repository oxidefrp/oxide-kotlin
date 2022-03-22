package io.github.oxidefrp.oxide.core.event_stream

import io.github.oxidefrp.oxide.core.None
import io.github.oxidefrp.oxide.core.Option
import io.github.oxidefrp.oxide.core.Transaction

internal class NeverEventStreamVertex<A> : RootEventStreamVertex<A>() {
    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> = None()
}
