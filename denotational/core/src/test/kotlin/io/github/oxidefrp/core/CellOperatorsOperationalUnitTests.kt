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
    }
}
