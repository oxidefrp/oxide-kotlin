package io.github.oxidefrp.oxide.core.impl.event_stream

import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class EventEmitterVertex<A> : RootEventStreamVertex<A>() {
    private val emitterMixin = EmitterMixin<A>(this)

    override fun pullCurrentOccurrence(transaction: Transaction): Option<A> =
        emitterMixin.currentOccurrence

    fun emit(event: A) {
        emitterMixin.emitInTransaction(event)
    }
}
