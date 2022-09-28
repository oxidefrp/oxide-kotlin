package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_utils.generateIntegerEventStream
import kotlin.test.Test
import kotlin.test.assertEquals

class OperatorsOperationalIntegrationTests {
    @Test
    fun testDivertHoldLoop() {
        // This is a divert-hold system that demonstrates a subtle but crucial
        // causality requirement on the divert operator.

        val initialStream: EventStream<Int> = EventStream.strict(
            TimelineSequence.cons(
                head = Instant.strictNonNull(
                    time = Time(0.5),
                    element = 10,
                ),
                tail = {
                    TimelineSequence.ofSingle(
                        object : Instant<Int>(time = Time(2.0)) {
                            override val occurrence: EventOccurrence<Int>
                                get() = throw UnsupportedOperationException()
                        }
                    )
                },
            )
        )

        val ticks = EventStream.ofOccurrences(
            Incident(Time(1.0), Unit),
        )

        val divertedStream = EventStream.pullLooped1 { divertedStreamLoop: EventStream<Int> ->
            divertedStreamLoop.hold(0).pullOf { memory ->
                val newStreams: EventStream<EventStream<Int>> =
                    ticks.pullOf { memory.sample() }.mapNotNull {
                        // Choosing the new stream we'll divert to depends on
                        // the [memory] cell, which in turn depends on previous
                        // values of the result stream. That means that finding
                        // the current value of the [memory] cell at the time
                        // of diversion can't be strict on any information from
                        // the result stream that comes from the time of
                        // diversion or later. That's trickier than it sounds.
                        if (it == 10) null else EventStream.never()
                    }

                newStreams.hold(initialStream).map { streamCell ->
                    val divertedStream = Cell.divert(streamCell)

                    EventStream.Loop1(
                        streamA = divertedStream,
                        result = divertedStream,
                    )
                }
            }
        }.pullDirectly(Time.zero)

        val front1 = divertedStream.occurrences.take(1)

        assertEquals(
            expected = listOf(
                Incident(time = Time(0.5), event = 10),
            ),
            actual = front1,
        )

        assertEquals(
            expected = listOf(
                Incident(time = Time(0.5), event = 10),
            ),
            actual = divertedStream.occurrences.getOccurrencesUntil(
                end = ClosedTimeEndpoint(Time(0.5)),
            ).toList(),
        )
    }

    @Test
    fun testHoldFilterLoop() {
        val ticks = generateIntegerEventStream()

        val cell = Cell.pullLooped1 { cellLoop: Cell<Int> ->
            ticks.pullOf {
                cellLoop.sample().map { it.takeIf { it > 0 } }
            }.filterNotNull().hold(0).map {
                Cell.Loop1(
                    cellA = it,
                    result = it,
                )
            }
        }.pullDirectly(Time(1.5))

        assertEquals(
            expected = 0,
            actual = cell.initialValue,
        )

        assertEquals(
            expected = listOf(
                Instant.strictNonNull(Time(2.0), null),
                Instant.strictNonNull(Time(3.0), null),
                Instant.strictNonNull(Time(4.0), null),
            ),
            actual = cell.innerValues.instants.take(3).toList(),
        )
    }
}
