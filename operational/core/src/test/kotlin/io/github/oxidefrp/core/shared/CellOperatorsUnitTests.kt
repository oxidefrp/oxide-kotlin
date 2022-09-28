package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.ValueChange
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.FiniteInputCellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import io.github.oxidefrp.core.test_utils.shared.DivertOperator
import kotlin.test.Test

class CellOperatorsUnitTests {
    @Test
    fun testConstant() = testSystem {
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

    @Test
    fun testValue() {
        // TODO: Implement when the `Signal` semantics / implementation
        //       stabilizes
    }

    @Test
    fun testChanges() = testSystem {
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

    @Test
    fun testApply() = testSystem {
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

    object Diverts {
        private val collidingNoneDiversionTick = Tick(t = 25)

        private val collidingFirstDiversionTick = Tick(t = 20)

        private val collidingSecondDiversionTick = Tick(t = 21)

        private val collidingBothDiversionTick = Tick(t = 30)

        private val firstSourceStreamSpec = buildSourceStreamSpec(
            events = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "c"),
                EventOccurrenceDesc(tick = Tick(t = 40), event = "d"),
            ),
            collidingTick = collidingFirstDiversionTick,
            nonCollidingTick = collidingSecondDiversionTick,
        )

        private val secondSourceStreamSpec = buildSourceStreamSpec(
            events = listOf(
                EventOccurrenceDesc(tick = Tick(t = 11), event = "A"),
                EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            ),
            collidingTick = collidingSecondDiversionTick,
            nonCollidingTick = collidingFirstDiversionTick,
        )

        object Divert {
            @Test
            fun testDiversionCollidingNone() = testCase(
                diversionTick = collidingNoneDiversionTick,
                divertOperator = DivertOperator.Divert,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingFirst() = testCase(
                diversionTick = collidingFirstDiversionTick,
                divertOperator = DivertOperator.Divert,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingSecond() = testCase(
                diversionTick = collidingSecondDiversionTick,
                divertOperator = DivertOperator.Divert,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingBoth() = testCase(
                diversionTick = collidingBothDiversionTick,
                divertOperator = DivertOperator.Divert,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "c"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )
        }

        object DivertEarly {
            @Test
            fun testDiversionCollidingNone() = testCase(
                diversionTick = collidingNoneDiversionTick,
                divertOperator = DivertOperator.DivertEarly,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingFirst() = testCase(
                diversionTick = collidingFirstDiversionTick,
                divertOperator = DivertOperator.DivertEarly,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingSecond() = testCase(
                diversionTick = collidingSecondDiversionTick,
                divertOperator = DivertOperator.DivertEarly,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )

            @Test
            fun testDiversionCollidingBoth() = testCase(
                diversionTick = collidingBothDiversionTick,
                divertOperator = DivertOperator.DivertEarly,
                expectedEvents = listOf(
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                    EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
                )
            )
        }

        private fun buildSourceStreamSpec(
            events: List<EventOccurrenceDesc<String>>,
            collidingTick: Tick,
            nonCollidingTick: Tick,
        ): FiniteInputStreamSpec<String> {
            val spec = FiniteInputStreamSpec(events = events)

            if (
                spec.hasOccurrence(collidingNoneDiversionTick) ||
                spec.hasOccurrence(nonCollidingTick) ||
                !spec.hasOccurrence(collidingBothDiversionTick) ||
                !spec.hasOccurrence(collidingTick)
            ) {
                throw IllegalArgumentException()
            }

            return spec
        }

        private fun testCase(
            diversionTick: Tick,
            divertOperator: DivertOperator,
            expectedEvents: List<EventOccurrenceDesc<String>>,
        ) = testSystem {
            val firstSourceStream = buildInputStream(firstSourceStreamSpec)
            val secondSourceStream = buildInputStream(secondSourceStreamSpec)

            val diversionCell = buildInputCell(
                initialValue = firstSourceStream,
                CellValueSpec(tick = diversionTick, newValue = secondSourceStream),
            )

            val divertedStream = divertOperator.divert(diversionCell)

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = divertedStream,
                        name = divertOperator.divertedStreamName,
                        spec = EventStreamSpec(
                            expectedEvents = expectedEvents,
                        ),
                    ),
                ),
            )
        }
    }
}
