package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class DivertLateEventStreamVertex<A>(
    source: CellVertex<EventStream<A>>,
) : DivertEventStreamVertex<A>(source = source) {
    override fun chooseCurrentStream(
        oldStream: EventStream<A>,
        newStreamOpt: Option<EventStream<A>>,
    ): EventStream<A> = oldStream
}
