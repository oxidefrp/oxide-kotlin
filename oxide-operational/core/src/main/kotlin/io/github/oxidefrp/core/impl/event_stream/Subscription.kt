package io.github.oxidefrp.core.impl.event_stream

import io.github.oxidefrp.core.impl.Transaction

interface ExternalSubscription {
    fun cancel()

}

internal fun ExternalSubscription.toTransaction(): TransactionSubscription = object : TransactionSubscription {
    override fun cancel(transaction: Transaction) {
        this@toTransaction.cancel()
    }
}

internal interface TransactionSubscription {
    fun cancel(transaction: Transaction)

    operator fun plus(other: TransactionSubscription): TransactionSubscription {
        val self = this

        return object : TransactionSubscription {
            override fun cancel(transaction: Transaction) {
                self.cancel(transaction = transaction)
                other.cancel(transaction = transaction)
            }
        }
    }

    fun toExternal(): ExternalSubscription = object : ExternalSubscription {
        override fun cancel() {
            Transaction.wrap {
                this@TransactionSubscription.cancel(transaction = it)
            }
        }
    }
}
