package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.test_framework.recorders.MomentRecorder
import io.github.oxidefrp.core.test_framework.TickProvider
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.TestVertex
import io.github.oxidefrp.core.test_framework.shared.MomentIssue
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class MomentValidator<S>(
    private val momentSpec: MomentSpec<S>,
    private val moment: Moment<S>,
    startTick: Tick,
) : Validator(startTick = startTick) {
    override fun spawnDirectly(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex {
        val recorder = MomentRecorder(
            tickProvider = tickProvider,
            momentSpec = momentSpec,
            moment = moment,
        )

        return object : TestVertex() {
            override fun process(
                transaction: Transaction,
            ) {
                recorder.instantiateValidatorIfExpected(transaction = transaction)
            }

            override fun validateRecord(): MomentIssue? = MomentIssue.validate(
                momentSpec = momentSpec,
                valueIssues = recorder.getValueIssues(),
            )
        }
    }
}
