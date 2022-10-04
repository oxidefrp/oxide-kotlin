package io.github.oxidefrp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class CellOperatorsOperationalUnitTests {
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
            actual = source.referenceCount,
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
            actual = source.referenceCount,
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

        result.changes.subscribeExternally { }

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
        val oldCell = Cell.constant(1)
        val newCell = Cell.constant(2)

        val source = MutableCell(
            initialValue = oldCell,
        )

        val result = Cell.switch(source)

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        assertEquals(
            expected = 1,
            actual = oldCell.referenceCount,
        )

        assertEquals(
            expected = 0,
            actual = newCell.referenceCount,
        )

        source.setValueExternally(newCell)

        assertEquals(
            expected = 0,
            actual = oldCell.referenceCount,
        )

        assertEquals(
            expected = 1,
            actual = newCell.referenceCount,
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

        changesVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = oldCell.referenceCount,
        )
    }

    @Test
    fun testSwitchChangeInstantaneous() {
        val sourceStream = EventEmitter<Int>()

        val cell1 = sourceStream
            .hold(initialValue = 1).pullExternally()

        val cell2 = sourceStream.map { -it }
            .hold(initialValue = -1).pullExternally()

        val source = sourceStream.map { cell2 }
            .hold(cell1).pullExternally()

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

    @Test
    fun testApplySampleUnreferenced() {
        val functionCell = MutableCell(
            initialValue = fun(i: Int): String = "#$i",
        )

        val argumentCell = MutableCell(
            initialValue = 1,
        )

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        assertEquals(
            expected = "#1",
            actual = result.value.sampleExternally(),
        )
    }

    @Test
    fun testApplySampleReferenced() {
        val functionCell = MutableCell(
            initialValue = fun(i: Int): String = "#$i",
        )

        val argumentCell = MutableCell(
            initialValue = 1,
        )

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        result.changes.subscribeExternally { }

        assertEquals(
            expected = "#1",
            actual = result.value.sampleExternally(),
        )
    }

    @Test
    fun testApplyObservation() {
        val functionCell = MutableCell(
            initialValue = fun(i: Int): String = "#$i",
        )

        val argumentCell = MutableCell(
            initialValue = 1,
        )

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        val subscription = result.changes.subscribeExternally { }

        assertEquals(
            expected = 1,
            actual = functionCell.referenceCount,
        )

        assertEquals(
            expected = 1,
            actual = functionCell.referenceCount,
        )

        subscription.cancel()

        assertEquals(
            expected = 0,
            actual = functionCell.referenceCount,
        )

        assertEquals(
            expected = 0,
            actual = functionCell.referenceCount,
        )
    }

    @Test
    fun testApplyFunctionChange() {
        val functionCell = MutableCell(
            initialValue = fun(i: Int): String = "#$i",
        )

        val argumentCell = MutableCell(
            initialValue = 1,
        )

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        functionCell.setValueExternally(
            fun(i: Int): String = "@$i"
        )

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "#1",
                newValue = "@1"
            )
        )
    }

    @Test
    fun testApplyArgumentChange() {
        val functionCell = MutableCell(
            initialValue = fun(i: Int): String = "#$i",
        )

        val argumentCell = MutableCell(
            initialValue = 1,
        )

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        argumentCell.setValueExternally(2)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "#1",
                newValue = "#2"
            )
        )
    }

    @Test
    fun testApplyFunctionArgumentChangeInstantaneous() {
        val eventEmitter = EventEmitter<Int>()

        val functionCell = eventEmitter
            .map {
                if (it % 2 == 0) fun(i: Int): String = "even:$i"
                else fun(i: Int): String = "odd:$i"
            }
            .hold(initialValue = fun(i: Int): String = ":$i")
            .pullExternally()

        val argumentCell = eventEmitter
            .map { -it }
            .hold(initialValue = 0)
            .pullExternally()

        val result = Cell.apply(
            function = functionCell,
            argument = argumentCell,
        )

        val changesVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        eventEmitter.emitExternally(1)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = ":0",
                newValue = "odd:-1",
            ),
        )

        eventEmitter.emitExternally(2)

        changesVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = "odd:-1",
                newValue = "even:-2",
            ),
        )
    }
}
