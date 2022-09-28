package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.test_framework.recorders.EventStreamRecorder
import io.github.oxidefrp.core.test_framework.recorders.MomentRecorder
import io.github.oxidefrp.core.test_framework.TickProvider
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.TestVertex
import io.github.oxidefrp.core.test_framework.shared.CellIssue
import io.github.oxidefrp.core.test_framework.shared.EffectiveCellSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class CellValidator<A>(
    private val cellSpec: EffectiveCellSpec<A>,
    private val cell: Cell<A>,
    startTick: Tick,
) : Validator(startTick = startTick) {
    override fun spawnDirectly(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex {
        val sampleRecorder = MomentRecorder(
            tickProvider = tickProvider,
            momentSpec = cellSpec.sampleSpec,
            moment = cell.sample(),
        )

        val newValuesRecorder = EventStreamRecorder.start(
            tickProvider = tickProvider,
            stream = cell.newValues,
            transaction = transaction,
        )

        return object : TestVertex() {
            override fun process(
                transaction: Transaction,
            ) {
                sampleRecorder.instantiateValidatorIfExpected(transaction = transaction)
            }

            override fun validateRecord(): CellIssue? = CellIssue.validate(
                cellSpec = cellSpec,
                sampleIssues = sampleRecorder.getValueIssues(),
                newValues = newValuesRecorder.getRecordedEvents(),
            )
        }
    }
}
