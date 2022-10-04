package io.github.oxidefrp.core.test_framework.recorders

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.test_framework.TickProvider
import io.github.oxidefrp.core.test_framework.TestVertex
import io.github.oxidefrp.core.test_framework.shared.Issue
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class MomentRecorder<out S>(
    private val tickProvider: TickProvider,
    private val momentSpec: MomentSpec<S>,
    private val moment: Moment<S>,
) {
    private val instantiatedValidators = mutableMapOf<Tick, TestVertex>()

    fun instantiateValidatorIfExpected(
        transaction: Transaction,
    ) {
        val tick = tickProvider.currentTick

        momentSpec.expectedValues[tick]?.let { valueSpec ->
            if (instantiatedValidators.containsKey(tick)) throw IllegalStateException()

            val value = moment.pullCurrentValue(transaction = transaction)

            val validator = valueSpec.bind(
                tick = tick,
                subject = value,
            ).spawn(
                tickProvider = tickProvider,
                transaction = transaction,
            )

            instantiatedValidators[tick] = validator
        }

        instantiatedValidators.values.forEach {
            it.process(transaction = transaction)
        }
    }

    fun getValueIssues(): Map<Tick, Issue?> = instantiatedValidators.mapValues { (_, validator) ->
        validator.validateRecord()
    }
}
