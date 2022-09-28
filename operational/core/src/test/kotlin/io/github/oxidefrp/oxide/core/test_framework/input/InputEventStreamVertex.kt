package io.github.oxidefrp.oxide.core.test_framework.input

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.ObservingEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import io.github.oxidefrp.oxide.core.test_framework.TickStream
import io.github.oxidefrp.oxide.core.test_framework.shared.InputStreamSpec

internal class InputEventStreamVertex<A>(
    private val tickStream: TickStream,
    private val spec: InputStreamSpec<A>,
) : ObservingEventStreamVertex<A>() {
    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> =
        Option.of(spec.getOccurrence(tick = tickStream.currentTick)?.event)

    override fun observe(): Subscription =
        tickStream.vertex.registerDependent(this)
}
