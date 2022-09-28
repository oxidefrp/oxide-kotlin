package io.github.oxidefrp.semantics.test_framework.shared

internal interface TestCheckGroup {
    val checks: List<TestCheck<*>>
}

internal data class TestSpec(
    override val checks: List<TestCheck<*>>,
) : TestCheckGroup
