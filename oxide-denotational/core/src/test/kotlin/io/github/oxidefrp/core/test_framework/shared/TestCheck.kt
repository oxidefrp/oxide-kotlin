package io.github.oxidefrp.core.test_framework.shared

import io.github.oxidefrp.core.test_framework.Validator

internal data class TestCheck<E>(
    /**
     * The test subject, i.e. object that's being checked
     */
    val subject: E,
    /**
     * Name of the subject
     */
    val name: String,
    /**
     * The spec to verify the subject against
     */
    val spec: SubjectSpec<E>,
) : TestCheckGroup {
    override val checks: List<TestCheck<*>>
        get() = listOf(this)

    fun bind(): Validator = spec.bind(
        tick = Tick.Zero,
        subject = subject,
    )
}
