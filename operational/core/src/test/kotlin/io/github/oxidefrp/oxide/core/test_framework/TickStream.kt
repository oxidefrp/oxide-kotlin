package io.github.oxidefrp.oxide.core.test_framework

import io.github.oxidefrp.oxide.core.EventStream
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Some
import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.CachingEventStreamVertex
import io.github.oxidefrp.oxide.core.impl.event_stream.EventStreamVertex
import io.github.oxidefrp.oxide.core.test_framework.shared.Tick

internal interface TickProvider {
    val currentTick: Tick
}

internal class TickStream : EventStream<Tick>(), TickProvider {
    private var _currentTick: Tick = Tick.Zero

    override val currentTick: Tick
        get() = _currentTick

    fun prepareNextTick(): Tick? {
        val nextTick = _currentTick.next ?: return null

        _currentTick = nextTick

        return nextTick
    }

    override val vertex: EventStreamVertex<Tick> = object : CachingEventStreamVertex<Tick>() {
        override fun pullCurrentOccurrenceUncached(
            transaction: Transaction,
        ): Option<Tick> = Some(currentTick)

        override fun onFirstDependencyAdded() {}

        override fun onLastDependencyRemoved() {}
    }
}
