package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal abstract class DivertEventStreamVertex<A>(
    private val outerStream: EventStream<EventStream<A>>,
    private val currentInnerStream: Moment<EventStream<A>>,
) : ObservingEventStreamVertex<A>() {
    private var innerSubscription: TransactionSubscription? = null

    final override fun observe(transaction: Transaction): TransactionSubscription {
        val outerSubscription = outerStream.vertex.registerDependent(
            transaction = transaction,
            dependent = this,
        )

        val initialStream = currentInnerStream.pullCurrentValue(transaction = transaction)

        this.innerSubscription = initialStream.vertex.registerDependent(
            transaction = transaction,
            dependent = this,
        )

        return object : TransactionSubscription {
            override fun cancel(transaction: Transaction) {
                outerSubscription.cancel(transaction = transaction)

                val innerSubscription = this@DivertEventStreamVertex.innerSubscription
                    ?: throw RuntimeException("Critical: there's no remembered inner subscription")

                innerSubscription.cancel(transaction = transaction)
            }
        }
    }

    final override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> {
        val innerStream = currentInnerStream.pullCurrentValue(
            transaction = transaction,
        )

        return innerStream.vertex.pullCurrentOccurrence(
            transaction = transaction,
        )
    }

    override fun postProcess(transaction: Transaction) {
        outerStream.vertex.pullCurrentOccurrence(
            transaction = transaction,
        ).ifSome { newInnerStream ->
            val innerSubscription = this.innerSubscription
                ?: throw RuntimeException("Critical: there's no remembered inner subscription")

            innerSubscription.cancel(transaction = transaction)

            this.innerSubscription = newInnerStream.vertex.registerDependent(
                transaction = transaction,
                dependent = this,
            )
        }
    }
}
