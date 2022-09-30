package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.Instant
import io.github.oxidefrp.core.S
import io.github.oxidefrp.core.StateSchedulerLayer
import io.github.oxidefrp.core.Time
import io.github.oxidefrp.core.ValueChange
import io.github.oxidefrp.core.momentState
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class CellSharedUnitTests {
    object Constant {
        @Test
        fun test() = testSystem {
            val constCell = Cell.constant(8)

            TestCheck(
                subject = constCell,
                name = "Const cell",
                spec = CellSpec(
                    expectedInitialValue = 8,
                    expectedInnerValues = emptyList(),
                ),
            )
        }
    }

    object Value {
        @Test
        fun test() {
            // TODO: Implement when the `Signal` semantics / implementation
            //       stabilizes
        }
    }

    object Changes {
        @Test
        fun test() = testSystem {
            val inputCell = buildInputCell(
                initialValue = 0,
                CellValueSpec(tick = Tick(t = 1), newValue = 10),
                CellValueSpec(tick = Tick(t = 2), newValue = 20),
                CellValueSpec(tick = Tick(t = 3), newValue = 30),
                CellValueSpec(tick = Tick(t = 5), newValue = 50),
            )

            val changeStream = inputCell.changes

            TestCheck(
                subject = changeStream,
                name = "Change stream",
                spec = EventStreamSpec(
                    expectedEvents = listOf(
                        EventOccurrenceDesc(
                            tick = Tick(t = 1),
                            event = ValueChange(
                                oldValue = 0,
                                newValue = 10,
                            ),
                        ),
                        EventOccurrenceDesc(
                            tick = Tick(t = 2),
                            event = ValueChange(
                                oldValue = 10,
                                newValue = 20,
                            ),
                        ),
                        EventOccurrenceDesc(
                            tick = Tick(t = 3),
                            event = ValueChange(
                                oldValue = 20,
                                newValue = 30,
                            ),
                        ),
                        EventOccurrenceDesc(
                            tick = Tick(t = 5),
                            event = ValueChange(
                                oldValue = 30,
                                newValue = 50,
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Map {
        @Test
        fun testFinite() = testSystem {
            val inputCell = buildInputCell(
                initialValue = 3,
                CellValueSpec(tick = Tick(t = 1), newValue = 4),
                CellValueSpec(tick = Tick(t = 2), newValue = 5),
                CellValueSpec(tick = Tick(t = 3), newValue = 6),
            )

            val mappedCell = inputCell.map { "#$it" }

            TestCheck(
                subject = mappedCell,
                name = "Mapped cell",
                spec = CellSpec(
                    expectedInitialValue = "#3",
                    expectedInnerValues = listOf(
                        CellValueDesc(tick = Tick(t = 1), value = "#4"),
                        CellValueDesc(tick = Tick(t = 2), value = "#5"),
                        CellValueDesc(tick = Tick(t = 3), value = "#6"),
                    ),
                ),
            )
        }

        @Test
        fun testInfinite() = testSystem {
            val inputCell = buildInputCell(
                FiniteInputCellSpec(
                    initialValue = 0,
                    CellValueSpec(tick = Tick(t = 1), newValue = 11),
                    CellValueSpec(tick = Tick(t = 2), newValue = 21),
                    CellValueSpec(tick = Tick(t = 3), newValue = 31),
                ).withTail {
                    it.t * 10
                }
            )

            val mappedCell = inputCell.map {
                it.toString()
            }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mappedCell,
                        name = "Mapped cell",
                        spec = CellSpec(
                            expectedInitialValue = "0",
                            matchFrontValuesOnly = true,
                            expectedInnerValues = listOf(
                                CellValueDesc(tick = Tick(t = 1), value = "11"),
                                CellValueDesc(tick = Tick(t = 2), value = "21"),
                                CellValueDesc(tick = Tick(t = 3), value = "31"),
                                CellValueDesc(tick = Tick(t = 4), value = "40"),
                                CellValueDesc(tick = Tick(t = 5), value = "50"),
                                CellValueDesc(tick = Tick(t = 6), value = "60"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Apply {
        @Test
        fun test() = testSystem {
            val functionCell: Cell<(Int) -> String> = buildInputCell(
                initialValue = fun(n: Int) = "&$n",
                CellValueSpec(
                    tick = Tick(t = 2),
                    newValue = fun(n: Int) = "%$n",
                ),
                CellValueSpec(
                    tick = Tick(t = 4),
                    newValue = fun(n: Int) = "^$n",
                ),
            )

            val argumentCell = buildInputCell(
                initialValue = 10,
                CellValueSpec(tick = Tick(t = 1), newValue = 20),
                CellValueSpec(tick = Tick(t = 2), newValue = 30),
                CellValueSpec(tick = Tick(t = 3), newValue = 40),
            )

            val appliedCell = Cell.apply(
                function = functionCell,
                argument = argumentCell,
            )

            TestCheck(
                subject = appliedCell,
                name = "Applied cell",
                spec = CellSpec(
                    expectedInitialValue = "&10",
                    expectedInnerValues = listOf(
                        CellValueDesc(tick = Tick(t = 1), value = "&20"),
                        CellValueDesc(tick = Tick(t = 2), value = "%30"),
                        CellValueDesc(tick = Tick(t = 3), value = "%40"),
                        CellValueDesc(tick = Tick(t = 4), value = "^40"),
                    ),
                ),
            )
        }
    }

    object PullEnter {
        private val stateSignalSpec = InputSignalSpec {
            when (it) {
                Tick(t = 20) -> S(sum = 20)
                Tick(t = 30) -> S(sum = 30)
                Tick(t = 35) -> S(sum = 35)
                else -> throw UnsupportedOperationException("Unexpected tick: $it")
            }
        }

        private val inputStateStreamSpec = FiniteInputStreamSpec(
            EventOccurrenceDesc(tick = Tick(10), event = S(sum = 10)),
            EventOccurrenceDesc(tick = Tick(25), event = S(sum = 25)),
            EventOccurrenceDesc(tick = Tick(40), event = S(sum = 40)),
            EventOccurrenceDesc(tick = Tick(50), event = S(sum = 50)),
        )

        private val sourceCellSpec = FiniteInputCellSpec(
            initialValue = momentState(n = 1),
            CellValueSpec(tick = Tick(t = 20), newValue = momentState(n = 2)),
            CellValueSpec(tick = Tick(t = 30), newValue = momentState(n = 3)),
            CellValueSpec(tick = Tick(t = 40), newValue = momentState(n = 4)),
            CellValueSpec(tick = Tick(t = 50), newValue = momentState(n = 5)),
        )

        @Test
        fun testNoCollision() = testSystem {
            // The time of entering does not "collide" with a cell's new value

            val stateSignal = buildInputSignal(stateSignalSpec)
            val inputStateStream = buildInputStream(inputStateStreamSpec)
            val sourceCell = buildInputCell(sourceCellSpec)

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val (outputLayer, outputCell) = Cell.pullEnter(sourceCell).constructDirectly(
                stateSignal = stateSignal,
            ).pullEnterDirectly(
                t = Time(35.0),
                oldState = inputLayer,
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = outputLayer.stateStream,
                        name = "Output state stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                                EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30 + 3)),
                                EventOccurrenceDesc(tick = Tick(t = 35), event = S(sum = 35 + 3)), // moment of entering
                                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40 + 4)),
                                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50 + 5)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = outputCell,
                        name = "Result cell",
                        spec = CellSpec(
                            expectedInitialValue = "35@35.0/3",
                            matchFrontValuesOnly = true,
                            expectedInnerValues = listOf(
                                CellValueDesc(tick = Tick(t = 40), value = "40@40.0/4"),
                                CellValueDesc(tick = Tick(t = 50), value = "50@50.0/5"),
                            ),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun testCollision() = testSystem {
            // The time of entering does "collide" with a cell's new value

            val stateSignal = buildInputSignal(stateSignalSpec)
            val inputStateStream = buildInputStream(inputStateStreamSpec)
            val sourceCell = buildInputCell(sourceCellSpec)

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val (outputLayer, outputCell) = Cell.pullEnter(sourceCell).constructDirectly(
                stateSignal = stateSignal,
            ).pullEnterDirectly(
                t = Time(40.0),
                oldState = inputLayer,
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = outputLayer.stateStream,
                        name = "Output state stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                                EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30 + 3)),
                                // Thought: Maybe just the new state should be entered? (the +4)
                                EventOccurrenceDesc(
                                    tick = Tick(t = 40),
                                    event = S(sum = 40 + 3 + 4)
                                ), // moment of entering
                                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50 + 5)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = outputCell,
                        name = "Result cell",
                        spec = CellSpec(
                            expectedInitialValue =  "40@40.0/3",
                            matchFrontValuesOnly = true,
                            expectedInnerValues = listOf(
                                CellValueDesc(tick = Tick(t = 40), value = "43@40.0/4"),
                                CellValueDesc(tick = Tick(t = 50), value = "50@50.0/5"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
