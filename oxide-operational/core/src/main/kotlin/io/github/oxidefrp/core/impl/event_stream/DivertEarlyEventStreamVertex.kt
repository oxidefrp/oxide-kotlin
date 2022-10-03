package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.getOrElse

internal class DivertEarlyEventStreamVertex<A>(
    source: CellVertex<EventStream<A>>,
) : DivertEventStreamVertex<A>(source = source) {
    override fun chooseCurrentStream(
        oldStream: EventStream<A>,
        newStreamOpt: Option<EventStream<A>>,
    ): EventStream<A> = newStreamOpt.getOrElse { oldStream }
}
