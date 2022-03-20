package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

interface Subscription {
    fun cancel()

    operator fun plus(other: Subscription): Subscription {
        val self = this

        return object : Subscription {
            override fun cancel() {
                self.cancel()
                other.cancel()
            }
        }
    }
}

internal abstract class EventStreamVertex<out A> : Vertex() {
    abstract val referenceCount: Int

    abstract fun registerDependent(dependent: Vertex): Subscription

    abstract fun pullCurrentOccurrence(transaction: Transaction): Option<A>
}
