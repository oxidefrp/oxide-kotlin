package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.EventOccurrence
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.accum
import io.github.oxidefrp.core.hold
import io.github.oxidefrp.core.mergeWith
import io.github.oxidefrp.core.squashWith
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InfiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec
import io.github.oxidefrp.core.test_framework.shared.withTail
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

class EventStreamSharedUnitTests {
    object Never {
        @Test
        fun test() = testSystem {
            val inputStream = EventStream.never<Int>()

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = inputStream,
                        name = "Never stream",
                        spec = EventStreamSpec(
                            expectedEvents = emptyList(),
                        ),
                    ),
                ),
            )
        }
    }

    object CurrentOccurrence {
        @Test
        fun test() = testSystem {
            val inputStream = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(
                        tick = Tick(t = 1),
                        event = 10,
                    ),
                    EventOccurrenceDesc(
                        tick = Tick(t = 3),
                        event = 30,
                    ),
                    EventOccurrenceDesc(
                        tick = Tick(t = 5),
                        event = 50,
                    ),
                )
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = inputStream.currentOccurrence,
                        name = "Current occurrence",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                Tick(t = 1) to ValueSpec(expected = EventOccurrence(10)),
                                Tick(t = 2) to ValueSpec(expected = null),
                                Tick(t = 3) to ValueSpec(expected = EventOccurrence(30)),
                                Tick(t = 4) to ValueSpec(expected = null),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Spark {
        @Test
        fun test() = testSystem {
            val sparkStream = EventStream.spark(13)

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = sparkStream,
                        name = "Spark stream",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                Tick(1) to EventStreamSpec(
                                    expectedEvents = listOf(
                                        EventOccurrenceDesc(tick = Tick(t = 1), event = 13),
                                    ),
                                ),
                                Tick(3) to EventStreamSpec(
                                    expectedEvents = listOf(
                                        EventOccurrenceDesc(tick = Tick(t = 3), event = 13),
                                    ),
                                ),
                                Tick(7) to EventStreamSpec(
                                    expectedEvents = listOf(
                                        EventOccurrenceDesc(tick = Tick(t = 7), event = 13),
                                    ),
                                ),
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
            val sourceStream = buildInputStream(
                EventOccurrenceDesc(
                    tick = Tick(t = 12),
                    event = 10,
                ),
                EventOccurrenceDesc(
                    tick = Tick(t = 31),
                    event = 20,
                ),
                EventOccurrenceDesc(
                    tick = Tick(t = 58),
                    event = 30,
                ),
            )

            val mappedStream = sourceStream.map {
                "0x${it.toString(radix = 16).uppercase()}"
            }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mappedStream,
                        name = "Mapped stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(
                                    tick = Tick(t = 12),
                                    event = "0xA",
                                ),
                                EventOccurrenceDesc(
                                    tick = Tick(t = 31),
                                    event = "0x14",
                                ),
                                EventOccurrenceDesc(
                                    tick = Tick(t = 58),
                                    event = "0x1E",
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun testInfinite() = testSystem {
            val mappedStream = buildInputStream(
                InfiniteInputStreamSpec { tick: Tick ->
                    val t = tick.t

                    when {
                        t % 3 == 0 && t % 5 == 0 -> "Fizz Buzz"
                        t % 3 == 0 -> "Fizz"
                        t % 5 == 0 -> "Buzz"
                        else -> t.toString()
                    }
                }
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mappedStream,
                        name = "Mapped stream",
                        spec = EventStreamSpec(
                            matchFrontEventsOnly = true,
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 0), event = "Fizz Buzz"),
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "1"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "2"),
                                EventOccurrenceDesc(tick = Tick(t = 3), event = "Fizz"),
                                EventOccurrenceDesc(tick = Tick(t = 4), event = "4"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "Buzz"),
                                EventOccurrenceDesc(tick = Tick(t = 6), event = "Fizz"),
                                EventOccurrenceDesc(tick = Tick(t = 7), event = "7"),
                                EventOccurrenceDesc(tick = Tick(t = 8), event = "8"),
                                EventOccurrenceDesc(tick = Tick(t = 9), event = "Fizz"),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = "Buzz"),
                                EventOccurrenceDesc(tick = Tick(t = 11), event = "11"),
                                EventOccurrenceDesc(tick = Tick(t = 12), event = "Fizz"),
                                EventOccurrenceDesc(tick = Tick(t = 13), event = "13"),
                                EventOccurrenceDesc(tick = Tick(t = 14), event = "14"),
                                EventOccurrenceDesc(tick = Tick(t = 15), event = "Fizz Buzz"),
                                EventOccurrenceDesc(tick = Tick(t = 16), event = "16"),
                                EventOccurrenceDesc(tick = Tick(t = 17), event = "17"),
                                EventOccurrenceDesc(tick = Tick(t = 18), event = "Fizz"),
                                EventOccurrenceDesc(tick = Tick(t = 19), event = "19"),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = "Buzz"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Filter {
        @Test
        fun test() = testSystem {
            val inputStream = buildInputStream(
                InfiniteInputStreamSpec { it.t },
            )

            val filteredStream = inputStream.filter { it % 2 == 0 }


            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = filteredStream,
                        name = "Filtered stream",
                        spec = EventStreamSpec(
                            matchFrontEventsOnly = true,
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 0), event = 0),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = 2),
                                EventOccurrenceDesc(tick = Tick(t = 4), event = 4),
                                EventOccurrenceDesc(tick = Tick(t = 6), event = 6),
                                EventOccurrenceDesc(tick = Tick(t = 8), event = 8),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = 10),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Hold {
        @Test
        fun test() = testSystem {
            val stepsStream = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 1), event = 1),
                    EventOccurrenceDesc(tick = Tick(t = 10), event = 10),
                    EventOccurrenceDesc(tick = Tick(t = 20), event = 20),
                    EventOccurrenceDesc(tick = Tick(t = 30), event = 30),
                ).withTail { tick ->
                    tick.t.takeIf { it % 10 == 0 }
                }
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        name = "Hold 0",
                        subject = stepsStream.hold(0),
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                Tick(1) to CellSpec(
                                    expectedInitialValue = 0,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 1), value = 1),
                                        CellValueDesc(tick = Tick(t = 10), value = 10),
                                        CellValueDesc(tick = Tick(t = 20), value = 20),
                                        CellValueDesc(tick = Tick(t = 30), value = 30),
                                    ),
                                ),
                                Tick(2) to CellSpec(
                                    expectedInitialValue = 0,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 10), value = 10),
                                        CellValueDesc(tick = Tick(t = 20), value = 20),
                                        CellValueDesc(tick = Tick(t = 30), value = 30),
                                        CellValueDesc(tick = Tick(t = 40), value = 40),
                                    ),
                                ),
                                Tick(10) to CellSpec(
                                    expectedInitialValue = 0,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 10), value = 10),
                                        CellValueDesc(tick = Tick(t = 20), value = 20),
                                        CellValueDesc(tick = Tick(t = 30), value = 30),
                                        CellValueDesc(tick = Tick(t = 40), value = 40),
                                    ),
                                ),
                                Tick(11) to CellSpec(
                                    expectedInitialValue = 0,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 20), value = 20),
                                        CellValueDesc(tick = Tick(t = 30), value = 30),
                                        CellValueDesc(tick = Tick(t = 40), value = 40),
                                        CellValueDesc(tick = Tick(t = 50), value = 50),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    // While semantically this should be redundant with the `CellSpec` check, optimizations might cause
                    // a situation, when observing `newValues` affects values returned by `sample()`
                    TestCheck(
                        name = "Hold 0 [sample() isolated]",
                        subject = stepsStream.hold(0).map {
                            it.sample()
                        },
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                Tick(1) to MomentSpec(
                                    expectedValues = mapOf(
                                        Tick(1) to ValueSpec(expected = 0),
                                        Tick(2) to ValueSpec(expected = 1),
                                        Tick(10) to ValueSpec(expected = 1),
                                        Tick(11) to ValueSpec(expected = 10),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Accum {
        @Test
        fun test() = testSystem {
            val inputSteps = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 1), event = 10),
                    EventOccurrenceDesc(tick = Tick(t = 2), event = 20),
                    EventOccurrenceDesc(tick = Tick(t = 3), event = 30),
                    EventOccurrenceDesc(tick = Tick(t = 4), event = 40),
                ).withTail { tick ->
                    tick.t * 10
                },
            )

            val accumCell = inputSteps.accum(5) { acc, step ->
                acc + step
            }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = accumCell,
                        name = "Accum cell",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                Tick(1) to CellSpec(
                                    expectedInitialValue = 5,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 1), value = 5 + 10),
                                        CellValueDesc(tick = Tick(t = 2), value = 5 + 10 + 20),
                                        CellValueDesc(tick = Tick(t = 3), value = 5 + 10 + 20 + 30),
                                    ),
                                ),
                                Tick(2) to CellSpec(
                                    expectedInitialValue = 5,
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 2), value = 5 + 20),
                                        CellValueDesc(tick = Tick(t = 3), value = 5 + 20 + 30),
                                        CellValueDesc(tick = Tick(t = 4), value = 5 + 20 + 30 + 40),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Merge {
        @Test
        fun testWithFirstFinite() = testSystem {
            val inputStream1 = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 2), event = "a"),
                ),
            )

            val inputStream2 = buildInputStream(
                InfiniteInputStreamSpec { tick ->
                    "x".takeIf { tick.t % 2 == 1 }
                }
            )

            val mergedStream = inputStream1.mergeWith(inputStream2) { a, b -> a + b }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mergedStream,
                        name = "Merged stream",
                        spec = EventStreamSpec(
                            matchFrontEventsOnly = true,
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "x"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "a"),
                                EventOccurrenceDesc(tick = Tick(t = 3), event = "x"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "x"),
                            ),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun testWithSecondFinite() = testSystem {
            val inputStream1 = buildInputStream(
                InfiniteInputStreamSpec { tick ->
                    "y".takeIf { tick.t % 2 == 1 }
                }
            )

            val inputStream2 = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 2), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 4), event = "b"),
                ),
            )

            val mergedStream = inputStream1.mergeWith(inputStream2) { a, b -> a + b }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mergedStream,
                        name = "Merged stream",
                        spec = EventStreamSpec(
                            matchFrontEventsOnly = true,
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "y"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "a"),
                                EventOccurrenceDesc(tick = Tick(t = 3), event = "y"),
                                EventOccurrenceDesc(tick = Tick(t = 4), event = "b"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "y"),
                                EventOccurrenceDesc(tick = Tick(t = 7), event = "y"),
                            ),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun testWithNonSimultaneous() = testSystem {
            val sourceStream1 = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 1), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 5), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 10), event = "c"),
            )

            val sourceStream2 = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 2), event = "d"),
                EventOccurrenceDesc(tick = Tick(t = 7), event = "e"),
                EventOccurrenceDesc(tick = Tick(t = 11), event = "f"),
            )

            val mergedStream = sourceStream1.mergeWith(sourceStream2) { a, b -> a + b }


            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mergedStream,
                        name = "Merged stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "a"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "d"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "b"),
                                EventOccurrenceDesc(tick = Tick(t = 7), event = "e"),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = "c"),
                                EventOccurrenceDesc(tick = Tick(t = 11), event = "f"),
                            ),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun testWithSimultaneous() = testSystem {
            val sourceStream1 = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 1), event = "a"),
                    EventOccurrenceDesc(tick = Tick(t = 5), event = "b"),
                    EventOccurrenceDesc(tick = Tick(t = 11), event = "c"),
                    EventOccurrenceDesc(tick = Tick(t = 16), event = "d"),
                ).withTail { tick ->
                    "x".takeIf { tick.t % 2 == 1 }
                },
            )

            val sourceStream2 = buildInputStream(
                FiniteInputStreamSpec(
                    EventOccurrenceDesc(tick = Tick(t = 2), event = "d"),
                    EventOccurrenceDesc(tick = Tick(t = 5), event = "e"),
                    EventOccurrenceDesc(tick = Tick(t = 10), event = "f"),
                    EventOccurrenceDesc(tick = Tick(t = 16), event = "g"),
                ).withTail { tick ->
                    "y".takeIf { tick.t % 2 == 0 }
                },
            )

            val mergedStream = sourceStream1.mergeWith(sourceStream2) { a, b -> a + b }

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = mergedStream,
                        name = "Merged stream",
                        spec = EventStreamSpec(
                            matchFrontEventsOnly = true,
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "a"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "d"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "be"),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = "f"),
                                EventOccurrenceDesc(tick = Tick(t = 11), event = "c"),
                                EventOccurrenceDesc(tick = Tick(t = 16), event = "dg"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object SquashWith {
        @Test
        fun test() = testSystem {
            val sourceStream1 = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 1), event = 10),
                EventOccurrenceDesc(tick = Tick(t = 5), event = 50),
                EventOccurrenceDesc(tick = Tick(t = 10), event = 100),
            )

            val sourceStream2 = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 2), event = 'd'),
                EventOccurrenceDesc(tick = Tick(t = 5), event = 'e'),
                EventOccurrenceDesc(tick = Tick(t = 11), event = 'f'),
            )

            val squashedStream = sourceStream1.squashWith(
                sourceStream2,
                ifFirst = { "number: $it" },
                ifSecond = { "character: $it" },
                ifBoth = { n, c -> "both: $n/$c" },
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = squashedStream,
                        name = "Squashed stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 1), event = "number: 10"),
                                EventOccurrenceDesc(tick = Tick(t = 2), event = "character: d"),
                                EventOccurrenceDesc(tick = Tick(t = 5), event = "both: 50/e"),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = "number: 100"),
                                EventOccurrenceDesc(tick = Tick(t = 11), event = "character: f"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
