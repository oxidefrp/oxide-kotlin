package io.github.oxidefrp.core.test_framework.validators

import io.github.oxidefrp.core.test_framework.Validator
import io.github.oxidefrp.core.test_framework.shared.IncorrectValueIssue
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec

internal class ValueValidator<A>(
    private val valueSpec: ValueSpec<A>,
    private val value: A,
    @Suppress("UNUSED_PARAMETER") startTick: Tick,
) : Validator() {
    override fun validate() = IncorrectValueIssue.validate(
        valueSpec = valueSpec,
        value = value,
    )
}
