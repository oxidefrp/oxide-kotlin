package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.findEvents
import io.github.oxidefrp.core.test_framework.findValueIssues
import io.github.oxidefrp.core.test_framework.shared.CellIssue
import io.github.oxidefrp.core.test_framework.shared.EffectiveCellSpec
import io.github.oxidefrp.core.test_framework.shared.Issue
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class CellValidator<A>(
    private val cellSpec: EffectiveCellSpec<A>,
    private val cell: Cell<A>,
    private val startTick: Tick,
) : Validator() {
    override fun validate(): Issue? = CellIssue.validate(
        cellSpec = cellSpec,
        sampleIssues = findValueIssues(
            momentSpec = cellSpec.sampleSpec,
            moment = cell.sample(),
        ),
        newValues = findEvents(
            startTick = startTick,
            stream = cell.newValues,
        ),
    )
}
