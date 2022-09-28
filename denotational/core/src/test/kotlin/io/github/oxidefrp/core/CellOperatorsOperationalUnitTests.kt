package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_utils.tableSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class CellOperatorsOperationalUnitTests {
    object DivertFamily {
        object Divert {
            @Test
            fun testCausality() {
                val stream = Cell.divert(
                    Cell.strict(
                        initialValue = EventStream.ofInstants(
                            Instant.strictNonNull(
                                time = Time(1.0),
                                element = 10,
                            ),
                            object : Instant<EventStream<Int>>(time = Time(2.0)) {
                                override val occurrence: EventOccurrence<EventStream<Int>>
                                    get() = throw UnsupportedOperationException()
                            },
                        ),
                        newValues = EventStream.ofInstants(
                            object : Instant<EventStream<Int>>(time = Time(1.0)) {
                                override val occurrence: EventOccurrence<EventStream<Int>>
                                    get() = throw UnsupportedOperationException()
                            },
                        ),
                    ),
                )

                assertEquals(
                    expected = Instant.strictNonNull(time = Time(1.0), element = 10),
                    actual = stream.occurrences.instants.first(),
                )
            }
        }

        object DivertEarly {
            @Test
            fun testCausality() {
                val stream = Cell.divertEarly(
                    Cell.strict(
                        initialValue = EventStream.ofInstants(
                            Instant.strictNonNull(
                                time = Time(0.5),
                                element = 10,
                            ),
                            object : Instant<EventStream<Int>>(time = Time(2.0)) {
                                override val occurrence: EventOccurrence<EventStream<Int>>
                                    get() = throw UnsupportedOperationException()
                            },
                        ),
                        newValues = EventStream.ofInstants(
                            object : Instant<EventStream<Int>>(time = Time(1.0)) {
                                override val occurrence: EventOccurrence<EventStream<Int>>
                                    get() = throw UnsupportedOperationException()
                            },
                        ),
                    ),
                )

                assertEquals(
                    expected = Instant.strictNonNull(time = Time(0.5), element = 10),
                    actual = stream.occurrences.instants.first(),
                )
            }
        }
    }

    object Switch {
        @Test
        fun testExclusiveSourceChange() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = null),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = null),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testExclusiveSwitch() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = Cell.ofInstants(
                            initialValue = 20,
                            Instant.strictNonNull(time = Time(4.0), element = 22),
                        ),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 20),
                        Instant.strictNonNull(time = Time(4.0), element = 22),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testSwitchWithSimultaneousSourceChange() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 12),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = Cell.ofInstants(
                            initialValue = 20,
                            Instant.strictNonNull(time = Time(1.0), element = 29),
                            Instant.strictNonNull(time = Time(3.0), element = 21),
                            Instant.strictNonNull(time = Time(4.0), element = 22),
                            Instant.strictNonNull(time = Time(5.0), element = 23),
                        ),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 29),
                        Instant.strictNonNull(time = Time(3.0), element = 21),
                        Instant.strictNonNull(time = Time(4.0), element = 22),
                        Instant.strictNonNull(time = Time(5.0), element = 23),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testSwitchWithSimultaneousImaginarySourceChange() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = null),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = Cell.ofInstants(
                            initialValue = 20,
                            Instant.strictNonNull(time = Time(1.0), element = 29),
                            Instant.strictNonNull(time = Time(3.0), element = 21),
                            Instant.strictNonNull(time = Time(4.0), element = 22),
                            Instant.strictNonNull(time = Time(5.0), element = 23),
                        ),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 29),
                        Instant.strictNonNull(time = Time(3.0), element = 21),
                        Instant.strictNonNull(time = Time(4.0), element = 22),
                        Instant.strictNonNull(time = Time(5.0), element = 23),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testImaginarySwitchWithSimultaneousSourceChange() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 12),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = null,
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 12),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testImaginarySwitchWithSimultaneousImaginarySourceChange() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = null),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = null,
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = null),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testCausality() {
            val cell = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(
                            time = Time(0.5),
                            element = 20,
                        ),
                    ),
                    object : Instant<Cell<Int>>(time = Time(1.0)) {
                        override val occurrence: EventOccurrence<Cell<Int>>
                            get() = throw UnsupportedOperationException()
                    },
                ),
            )

            assertEquals(
                expected = Instant.strictNonNull(time = Time(0.5), element = 20),
                actual = cell.segmentSequence.innerValues.instants.first(),
            )
        }

        @Test
        fun testSwitchToOldValue() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = Cell.ofInstants(
                            initialValue = 20,
                            Instant.strictNonNull(time = Time(1.0), element = 21),
                            Instant.strictNonNull(time = Time(4.0), element = 22),
                        ),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 21),
                        Instant.strictNonNull(time = Time(4.0), element = 22),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }

        @Test
        fun testSwitchToNewValue() {
            val result = Cell.switch(
                Cell.ofInstants(
                    initialValue = Cell.ofInstants(
                        initialValue = 10,
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(3.0), element = 13),
                    ),
                    Instant.strictNonNull(
                        time = Time(2.0),
                        element = Cell.ofInstants(
                            initialValue = 20,
                            Instant.strictNonNull(time = Time(1.0), element = 21),
                            Instant.strictNonNull(time = Time(2.0), element = 22),
                            Instant.strictNonNull(time = Time(4.0), element = 23),
                        ),
                    ),
                ),
            )

            assertEquals(
                expected = Cell.FullSegmentSequence(
                    initialValue = 10,
                    innerValues = TimelineSequence.ofInstants(
                        Instant.strictNonNull(time = Time(1.0), element = 11),
                        Instant.strictNonNull(time = Time(2.0), element = 22),
                        Instant.strictNonNull(time = Time(4.0), element = 23),
                    ),
                ),
                actual = result.segmentSequence,
            )
        }
    }

    object PullEnter {
        private val stateSignal = tableSignal(
            table = mapOf(
                Time(2.0) to S(sum = 20),
                Time(3.0) to S(sum = 30),
                Time(3.5) to S(sum = 35),
            ),
        )

        private val inputLayer = StateSchedulerLayer(
            stateStream = EventStream.ofInstants(
                Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 40)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
            ),
        )

        private val cell = Cell.ofInstants(
            initialValue = momentState(n = 1),
            Instant.strictNonNull(time = Time(2.0), element = momentState(n = 2)),
            Instant.strictNonNull(time = Time(3.0), element = momentState(n = 3)),
            Instant.strictNonNull(time = Time(4.0), element = momentState(n = 4)),
            Instant.strictNonNull(time = Time(5.0), element = momentState(n = 5)),
        )

        @Test
        fun testNoCollision() {
            // The time of entering does not "collide" with a cell's new value

            val (outputLayer, outputCell) = Cell.pullEnter(cell).constructDirectly(
                stateSignal = stateSignal,
            ).pullEnterDirectly(
                t = Time(3.5),
                oldState = inputLayer,
            )

            assertEquals(
                expected = listOf(
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                    Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 2)),
                    Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(3.0), element = S(sum = 30 + 3)),
                    Instant.strictNonNull(time = Time(3.5), element = S(sum = 35 + 3)), // moment of entering
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 4)),
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50 + 5)),
                ),
                actual = outputLayer.stateStream.occurrences.instants.toList(),
            )

            val outputSegmentSequence = outputCell.segmentSequence

            assertEquals(
                expected = "35@3.5/3",
                actual = outputSegmentSequence.initialValue,
            )

            assertEquals(
                expected = listOf(
                    // Thought: shouldn't hold emit imaginary instants from before the time holding begins?
                    Instant.strictNonNull(time = Time(3.5), element = null),
                    Instant.strictNonNull(time = Time(4.0), element = "40@4.0/4"),
                    Instant.strictNonNull(time = Time(5.0), element = "50@5.0/5"),
                ),
                actual = outputSegmentSequence.innerValues.instants.toList(),
            )
        }

        @Test
        fun testCollision() {
            // The time of entering does "collide" with a cell's new value

            val (outputLayer, outputCell) = Cell.pullEnter(cell).constructDirectly(
                stateSignal = stateSignal,
            ).pullEnterDirectly(
                t = Time(4.0),
                oldState = inputLayer,
            )

            assertEquals(
                expected = listOf(
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                    Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 2)),
                    Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(3.0), element = S(sum = 30 + 3)),
                    // Thought: Maybe just the new state should be entered? (the +4)
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 3 + 4)), // moment of entering
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50 + 5)),
                ),
                actual = outputLayer.stateStream.occurrences.instants.toList(),
            )

            val outputSegmentSequence = outputCell.segmentSequence

            assertEquals(
                expected = "40@4.0/3",
                actual = outputSegmentSequence.initialValue,
            )

            assertEquals(
                expected = listOf(
                    Instant.strictNonNull(time = Time(4.0), element = "43@4.0/4"),
                    Instant.strictNonNull(time = Time(5.0), element = "50@5.0/5"),
                ),
                actual = outputSegmentSequence.innerValues.instants.toList(),
            )
        }
    }
}
