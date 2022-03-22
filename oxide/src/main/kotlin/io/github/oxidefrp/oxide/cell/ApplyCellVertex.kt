package io.github.oxidefrp.oxide.cell

import io.github.oxidefrp.oxide.Option
import io.github.oxidefrp.oxide.Transaction
import io.github.oxidefrp.oxide.event_stream.CellVertex
import io.github.oxidefrp.oxide.event_stream.Subscription
import io.github.oxidefrp.oxide.getOrElse

internal class ApplyCellVertex<A, B>(
    private val function: CellVertex<(A) -> B>,
    private val argument: CellVertex<A>,
) : ObservingCellVertex<B>() {
    override fun observe(): Subscription {
        val functionSubscription = function.registerDependent(this)
        val argumentSubscription = argument.registerDependent(this)
        return functionSubscription + argumentSubscription
    }

    override fun sampleOldValue(): B {
        val oldFunction = function.oldValue
        val oldArgument = argument.oldValue
        return oldFunction(oldArgument)
    }

    override fun pullNewValueUncached(transaction: Transaction): Option<B> {
        val newFunction = function.pullNewValue(transaction = transaction)
        val newArgument = argument.pullNewValue(transaction = transaction)

        return Option.test(newFunction.isSome() || newArgument.isSome()) {
            val newOrOldFunction = newFunction.getOrElse { function.oldValue }
            val newOrOldArgument = newArgument.getOrElse { argument.oldValue }

            newOrOldFunction(newOrOldArgument)
        }
    }
}
