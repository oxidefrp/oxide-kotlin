package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.test_framework.shared.Issue
import io.github.oxidefrp.core.test_framework.shared.TestCheckGroup

internal fun testSystem(block: TestContext.() -> TestCheckGroup) {
    val tickStream = TickStream()

    val testVertexByName = Transaction.wrap { transaction ->
        val testContext = TestContext(
            tickStream = tickStream,
        )

        val testCheckGroup = testContext.block()

        val testChecks = testCheckGroup.checks

        testChecks.associate { testCheck ->
            testCheck.name to testCheck.bind().spawn(
                tickProvider = tickStream,
                transaction = transaction,
            )
        }.apply {
            values.forEach(transaction::enqueueForProcess)
        }
    }

    val testVertices = testVertexByName.values

    while (true) {
        tickStream.prepareNextTick() ?: break

        Transaction.wrap { transaction ->
            transaction.enqueueForProcess(tickStream.vertex)

            testVertices.forEach {
                transaction.enqueueForProcess(it)
            }
        }
    }

    testVertexByName.forEach { (name, testVertex) ->
        Issue.assertNoIssue(
            name = name,
            issue = testVertex.validateRecord(),
        )
    }
}
