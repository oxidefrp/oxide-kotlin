package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream

internal class DivertLateEventStreamVertex<A>(
    source: Cell<EventStream<A>>,
) : DivertEventStreamVertex<A>(
    outerStream = source.newValues,
    currentInnerStream = source.sample(),
)
