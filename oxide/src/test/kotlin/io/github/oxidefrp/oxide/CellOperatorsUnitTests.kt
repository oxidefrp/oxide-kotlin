package io.github.oxidefrp.oxide

import kotlin.test.Test
import kotlin.test.assertEquals

class CellOperatorsUnitTests {
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

    @Test
    fun testSwitchSampleUnreferenced() {
        val source = Cell.constant(
            Cell.constant(1),
        )

        val result = Cell.switch(source)

        assertEquals(
            expected = 1,
            actual = result.value.sampleExternally(),
        )
    }

    @Test
    fun testSwitchSampleReferenced() {
        val source = Cell.constant(
            Cell.constant(1),
        )

        val result = Cell.switch(source)

        result.changes.subscribe { }

        assertEquals(
            expected = 1,
            actual = result.value.sampleExternally(),
        )
    }

    @Test
    fun testSwitchChangeInner() {
        val innerSource = MutableCell(initialValue = 1)

        val source = Cell.constant(innerSource)

        val result = Cell.switch(source)

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        innerSource.setValueExternally(2)

        assertEquals(
            expected = 2,
            actual = result.value.sampleExternally(),
        )

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 1,
                newValue = 2,
            ),
        )
    }

    @Test
    fun testSwitchChangeOuter() {
        val source = MutableCell(
            initialValue = Cell.constant(1),
        )

        val result = Cell.switch(source)

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        source.setValueExternally(
            Cell.constant(2),
        )

        assertEquals(
            expected = 2,
            actual = result.value.sampleExternally(),
        )

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 1,
                newValue = 2,
            ),
        )
    }

    @Test
    fun testSwitchChangeInstantaneous() {
        val sourceStream = EventEmitter<Int>()

        val cell1 = sourceStream.hold(initialValue = 1)
        val cell2 = sourceStream.map { -it }.hold(initialValue = -1)

        val source = sourceStream.map { cell2 }.hold(cell1)

        val result = Cell.switch(source)

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        assertEquals(
            expected = 1,
            actual = result.value.sampleExternally(),
        )

        sourceStream.emitExternally(2)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 1,
                newValue = -2,
            ),
        )

        assertEquals(
            expected = -2,
            actual = result.value.sampleExternally(),
        )
    }
}
