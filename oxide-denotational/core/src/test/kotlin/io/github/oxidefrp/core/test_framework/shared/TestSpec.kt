package io.github.oxidefrp.core.test_framework.shared

internal interface TestCheckGroup {
    val checks: List<TestCheck<*>>
}

internal data class TestSpec(
    override val checks: List<TestCheck<*>>,
) : TestCheckGroup
