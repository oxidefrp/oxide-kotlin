package io.github.oxidefrp.semantics.test_framework

import io.github.oxidefrp.semantics.EventStream
import io.github.oxidefrp.semantics.Instant
import io.github.oxidefrp.semantics.PureSequence
import io.github.oxidefrp.semantics.Time
import io.github.oxidefrp.semantics.TimelineSequence
import io.github.oxidefrp.semantics.test_framework.shared.Issue
import io.github.oxidefrp.semantics.test_framework.shared.TestCheckGroup
import io.github.oxidefrp.semantics.test_framework.shared.Tick

internal fun testSystem(block: TestContext.() -> TestCheckGroup) {
    val tickInstants = PureSequence.generate(seed = 0) { it + 1 }.map { t ->
        Instant.strictNonNull(
            time = Time(t = t.toDouble()),
            // This stream has an infinite number of tail imaginary instants
            element = Tick.fromT(t),
        )
    }

    val tickStream = EventStream.strict(
        occurrences = TimelineSequence.ofSequence(tickInstants),
    )

    val testContext = TestContext(
        tickStream = tickStream,
    )

    val testCheckGroup = testContext.block()

    val testChecks = testCheckGroup.checks

    val issueByName = testChecks.associate { testCheck ->
        testCheck.name to testCheck.bind().validate()
    }

    issueByName.forEach { (name, issue) ->
        Issue.assertNoIssue(
            name = name,
            issue = issue,
        )
    }
}
