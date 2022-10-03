package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.findValueIssues
import io.github.oxidefrp.core.test_framework.shared.MomentIssue
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.Tick

internal class MomentValidator<S>(
    private val momentSpec: MomentSpec<S>,
    private val moment: Moment<S>,
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate(): MomentIssue? = MomentIssue.validate(
        momentSpec = momentSpec,
        valueIssues = findValueIssues(
            momentSpec = momentSpec,
            moment = moment,
        ),
    )
}
