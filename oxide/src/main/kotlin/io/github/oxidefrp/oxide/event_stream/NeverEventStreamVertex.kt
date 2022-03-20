package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal class NeverEventStreamVertex<A> : RootEventStreamVertex<A>() {
    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> = None()
}
