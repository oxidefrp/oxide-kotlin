package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.EventStream
import io.github.oxidefrp.oxide.Subscription

abstract class SimpleEventStream<A> : EventStream<A>() {
    private val listeners = mutableSetOf<(A) -> Unit>()

    protected fun notifyListeners(event: A) {
        listeners.forEach { listener ->
            listener(event)
        }
    }

    final override val referenceCount: Int
        get() = listeners.size

    final override fun subscribe(listener: (A) -> Unit): Subscription {
        val wasAdded = listeners.add(listener)

        if (!wasAdded) {
            throw IllegalStateException("Attempted to re-subscribe with the same listener")
        }

        if (listeners.size == 1) {
            onFirstListenerAdded()
        }

        return object : Subscription {
            override fun cancel() {
                val wasRemoved = listeners.remove(listener)

                if (!wasRemoved) {
                    throw IllegalStateException("Attempted to re-cancel a subscription")
                }

                if (listeners.isEmpty()) {
                    onLastListenerRemoved()
                }
            }
        }
    }

    abstract fun onFirstListenerAdded()

    abstract fun onLastListenerRemoved()
}
