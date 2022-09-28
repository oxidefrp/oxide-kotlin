package io.github.oxidefrp.oxide.core

import kotlin.test.Test
import kotlin.test.assertEquals

class OperatorsOperationalIntegrationTests {
    @Test
    fun testCellMapPlusSwitchOf() {
        val mutCell1 = MutableCell(initialValue = 1)

        val mutCell2 = MutableCell(initialValue = "a")

        val result = mutCell1.switchOf { i ->
            mutCell2.map { s -> "$i-$s" }
        }

        assertEquals(
            expected = "1-a",
            actual = result.value.sampleExternally(),
        )

        mutCell1.setValueExternally(2)

        assertEquals(
            expected = "2-a",
            actual = result.value.sampleExternally(),
        )

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        assertEquals(
            expected = "2-a",
            actual = result.value.sampleExternally(),
        )

        mutCell1.setValueExternally(3)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "2-a",
                newValue = "3-a",
            ),
        )

        assertEquals(
            expected = "3-a",
            actual = result.value.sampleExternally(),
        )
        mutCell2.setValueExternally("b")

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "3-a",
                newValue = "3-b",
            ),
        )

        assertEquals(
            expected = "3-b",
            actual = result.value.sampleExternally(),
        )

        changesVerifier.dispose()

        assertEquals(
            expected = "3-b",
            actual = result.value.sampleExternally(),
        )
    }
}
