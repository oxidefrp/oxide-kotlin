package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

object SwitchOperatorUnitTests {
    private val firstSourceCellSpec = FiniteInputCellSpec(
        initialValue = -1,
        CellValueSpec(tick = Tick(t = 10), newValue = -10),
        CellValueSpec(tick = Tick(t = 20), newValue = -20),
        CellValueSpec(tick = Tick(t = 30), newValue = -30),
        CellValueSpec(tick = Tick(t = 40), newValue = -40),
    )

    private val secondSourceCellSpec = FiniteInputCellSpec(
        initialValue = 1,
        CellValueSpec(tick = Tick(t = 11), newValue = 10),
        CellValueSpec(tick = Tick(t = 21), newValue = 20),
        CellValueSpec(tick = Tick(t = 30), newValue = 30),
        CellValueSpec(tick = Tick(t = 41), newValue = 40),
    )

    @Test
    fun testSwitchingCollidingNone() = testCase(
        switchTick = Tick(t = 25),
        expectedInnerValues = listOf(
            CellValueDesc(tick = Tick(t = 10), value = -10),
            CellValueDesc(tick = Tick(t = 20), value = -20),
            CellValueDesc(tick = Tick(t = 25), value = 20),
            CellValueDesc(tick = Tick(t = 30), value = 30),
            CellValueDesc(tick = Tick(t = 41), value = 40),
        ),
    )

    @Test
    fun testSwitchingCollidingFirst() = testCase(
        switchTick = Tick(t = 20),
        expectedInnerValues = listOf(
            CellValueDesc(tick = Tick(t = 10), value = -10),
            CellValueDesc(tick = Tick(t = 20), value = 10),
            CellValueDesc(tick = Tick(t = 21), value = 20),
            CellValueDesc(tick = Tick(t = 30), value = 30),
            CellValueDesc(tick = Tick(t = 41), value = 40),
        ),
    )

    @Test
    fun testSwitchingCollidingSecond() = testCase(
        switchTick = Tick(t = 21),
        expectedInnerValues = listOf(
            CellValueDesc(tick = Tick(t = 10), value = -10),
            CellValueDesc(tick = Tick(t = 20), value = -20),
            CellValueDesc(tick = Tick(t = 21), value = 20),
            CellValueDesc(tick = Tick(t = 30), value = 30),
            CellValueDesc(tick = Tick(t = 41), value = 40),
        ),
    )

    @Test
    fun testSwitchingCollidingBoth() = testCase(
        switchTick = Tick(t = 30),
        expectedInnerValues = listOf(
            CellValueDesc(tick = Tick(t = 10), value = -10),
            CellValueDesc(tick = Tick(t = 20), value = -20),
            CellValueDesc(tick = Tick(t = 30), value = 30),
            CellValueDesc(tick = Tick(t = 41), value = 40),
        ),
    )

    private fun testCase(
        switchTick: Tick,
        expectedInnerValues: List<CellValueDesc<Int>>,
    ) = testSystem {
        val firstSourceCell = buildInputCell(firstSourceCellSpec)

        val secondSourceCell = buildInputCell(secondSourceCellSpec)

        val switchingCell = buildInputCell(
            initialValue = firstSourceCell,
            CellValueSpec(tick = switchTick, newValue = secondSourceCell),
        )

        val switchedCell = Cell.switch(switchingCell)

        TestCheck(
            subject = switchedCell,
            name = "Switched cell",
            spec = CellSpec(
                expectedInitialValue = -1,
                expectedInnerValues = expectedInnerValues,
            ),
        )
    }
}
