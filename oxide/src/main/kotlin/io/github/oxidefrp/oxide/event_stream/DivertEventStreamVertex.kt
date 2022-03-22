package io.github.oxidefrp.oxide.event_stream

import io.github.oxidefrp.oxide.EventStream
import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction

internal class DivertEventStreamVertex<A>(
    private val source: CellVertex<EventStream<A>>,
) : ObservingEventStreamVertex<A>() {
    private var innerSubscription: Subscription? = null

    override fun observe(): Subscription {
        val innerStreamVertex = source.oldValue.vertex

        val outerSubscription = source.registerDependent(this)

        this.innerSubscription = innerStreamVertex.registerDependent(this)

        val innerMetaSubscription = object : Subscription {
            override fun cancel() {
                val innerSubscription = this@DivertEventStreamVertex.innerSubscription
                    ?: throw RuntimeException("Critical: there's no remembered inner subscription")

                innerSubscription.cancel()
            }
        }

        return outerSubscription + innerMetaSubscription
    }

    override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> {
        val oldInnerStreamVertex = source.oldValue.vertex

        source.pullNewValue(transaction = transaction).ifSome { newInnerStream ->
            val newInnerStreamVertex = newInnerStream.vertex

            val subscription = innerSubscription
                ?: throw RuntimeException("Critical: there's no remembered inner subscription")

            subscription.cancel()

            innerSubscription = newInnerStreamVertex.registerDependent(this)
        }

        return oldInnerStreamVertex.pullCurrentOccurrence(transaction = transaction)
    }
}
