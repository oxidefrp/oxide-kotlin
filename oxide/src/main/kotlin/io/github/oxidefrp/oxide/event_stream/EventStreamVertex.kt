package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.Option

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

abstract class EventStreamVertex<out A> : Vertex() {
    abstract val referenceCount: Int

    abstract fun registerDependent(dependent: Vertex): Subscription

    abstract val currentOccurrence: Option<A>
}
