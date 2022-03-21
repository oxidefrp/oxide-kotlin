package io.github.oxidefrp.oxide

import kotlin.test.Test
import kotlin.test.assertEquals

class CellInput<A>(
    initialValue: A,
) {
    private val valueEmitter = EventEmitter<A>()

    val cell = valueEmitter.hold(initialValue)

    fun setValue(newValue: A) {
        valueEmitter.emitExternally(newValue)
    }
}

class CellTest {
    @Test
    fun testConstant() {
        val cell = Cell.constant(9)

        assertEquals(
            expected = 9,
            actual = cell.value.sampleExternally(),
        )
    }

    @Test
    fun testMap() {
        val source = MutableCell(initialValue = 1)

        val result = source.map { "$it" }

        assertEquals(
            expected = "1",
            actual = result.value.sampleExternally(),
        )

        source.setValueExternally(2)

        assertEquals(
            expected = "2",
            actual = result.value.sampleExternally(),
        )

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        assertEquals(
            expected = "2",
            actual = result.value.sampleExternally(),
        )

        source.setValueExternally(3)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "2",
                newValue = "3",
            ),
        )

        assertEquals(
            expected = "3",
            actual = result.value.sampleExternally(),
        )

        changesVerifier.dispose()

        assertEquals(
            expected = "3",
            actual = result.value.sampleExternally(),
        )
    }

    @Test
    fun testChanges() {
        val source = MutableCell(initialValue = 1)

        val changesVerifier = EventStreamVerifier(
            stream = source.changes,
        )

        assertEquals(
            expected = 1,
            actual = source.vertex.referenceCount,
        )

        changesVerifier.verifyNoReceivedEvents()

        source.setValueExternally(2)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 1,
                newValue = 2,
            ),
        )

        source.setValueExternally(3)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 2,
                newValue = 3,
            ),
        )

        changesVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = source.vertex.referenceCount,
        )
    }
}
