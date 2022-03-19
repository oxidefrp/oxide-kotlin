package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.None
import io.github.oxidefrp.oxide.Option

class NeverEventStreamVertex<A> : RootEventStreamVertex<A>() {
    override val currentOccurrence: Option<A> = None()
}
