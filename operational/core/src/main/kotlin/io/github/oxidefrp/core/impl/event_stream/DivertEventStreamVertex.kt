package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.impl.Option
import io.github.oxidefrp.core.impl.Transaction

internal abstract class DivertEventStreamVertex<A>(
    private val source: CellVertex<EventStream<A>>,
) : ObservingEventStreamVertex<A>() {
    private var innerSubscription: TransactionSubscription? = null

    final override fun observe(transaction: Transaction): TransactionSubscription {
        val innerStreamVertex = source.oldValue.vertex

        val outerSubscription = source.registerDependent(transaction = transaction, dependent = this)

        this.innerSubscription = innerStreamVertex.registerDependent(transaction = transaction, dependent = this)

        val innerMetaSubscription = object : TransactionSubscription {
            override fun cancel(transaction: Transaction) {
                val innerSubscription = this@DivertEventStreamVertex.innerSubscription
                    ?: throw RuntimeException("Critical: there's no remembered inner subscription")

                innerSubscription.cancel(transaction = transaction)
            }
        }

        return outerSubscription + innerMetaSubscription
    }

    final override fun pullCurrentOccurrenceUncached(transaction: Transaction): Option<A> {
        val newStreamOpt = source.pullNewValue(transaction = transaction)

        newStreamOpt.ifSome { newInnerStream ->
            val newInnerStreamVertex = newInnerStream.vertex

            val subscription = innerSubscription
                ?: throw RuntimeException("Critical: there's no remembered inner subscription")

            subscription.cancel(transaction = transaction)

            innerSubscription = newInnerStreamVertex.registerDependent(transaction = transaction, dependent = this)
        }

        val currentStream = chooseCurrentStream(
            oldStream = source.oldValue,
            newStreamOpt = newStreamOpt,
        )

        val currentInnerStreamVertex = currentStream.vertex

        return currentInnerStreamVertex.pullCurrentOccurrence(transaction = transaction)
    }

    abstract fun chooseCurrentStream(
        oldStream: EventStream<A>,
        newStreamOpt: Option<EventStream<A>>,
    ): EventStream<A>
}
