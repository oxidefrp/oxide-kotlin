package io.github.oxidefrp.oxide.core.test_framework.validators

import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.test_framework.TickProvider
import io.github.oxidefrp.oxide.core.test_framework.Validator
import io.github.oxidefrp.oxide.core.test_framework.TestVertex
import io.github.oxidefrp.oxide.core.test_framework.shared.IncorrectValueIssue
import io.github.oxidefrp.oxide.core.test_framework.shared.Tick
import io.github.oxidefrp.oxide.core.test_framework.shared.ValueSpec

internal class ValueValidator<A>(
    private val valueSpec: ValueSpec<A>,
    private val value: A,
    startTick: Tick,
) : Validator(startTick = startTick) {
    override fun spawnDirectly(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex = object : TestVertex() {
        override fun validateRecord(): IncorrectValueIssue<A>? = IncorrectValueIssue.validate(
            valueSpec = valueSpec,
            value = value,
        )
    }
}
