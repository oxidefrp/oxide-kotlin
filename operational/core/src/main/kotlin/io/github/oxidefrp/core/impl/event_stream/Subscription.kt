package io.github.oxidefrp.core.impl.event_stream

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
