package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_utils.generateOccurrencesSequence
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals

class EventStreamOperatorsOperationalTests {
    @Test
    fun testSample() {
        // TODO: Nuke in favor of `pull`?

        val signalInput1 = object : Signal<Double>() {
            override fun at(t: Time): Double = t.t * t.t
        }

        val signalInput2 = object : Signal<Double>() {
            override fun at(t: Time): Double = t.t / 4.0
        }

        val signalInput3 = object : Signal<Double>() {
            override fun at(t: Time): Double = sin(t.t)
        }

        val streamInput = EventStream.ofSequence(
            PureSequence.of(
                Incident(Time(t = 3.0), event = signalInput1),
                Incident(Time(t = 5.0), event = signalInput2),
                Incident(Time(t = 10.0), event = signalInput3),
            ) + generateOccurrencesSequence(
                t0 = 11.0,
                seed = signalInput1,
                nextFunction = { it },
            ),
        )

        val result = EventStream.sample(streamInput)

        val actualOccurrences = result.occurrences.take(3).toList()

        assertEquals(
            expected = 3.0,
            actual = actualOccurrences[0].time.t,
        )

        assertEquals(
            expected = 9.0,
            actual = actualOccurrences[0].event,
            absoluteTolerance = epsilon,
        )

        assertEquals(
            expected = 5.0,
            actual = actualOccurrences[1].time.t,
        )

        assertEquals(
            expected = 1.25,
            actual = actualOccurrences[1].event,
            absoluteTolerance = epsilon,
        )

        assertEquals(
            expected = 10.0,
            actual = actualOccurrences[2].time.t,
        )

        assertEquals(
            expected = -0.5440211109,
            actual = actualOccurrences[2].event,
            absoluteTolerance = epsilon,
        )
    }

    @Test
    fun testHoldNever() {
        // TODO: Integration?
        val stepsInput = EventStream.never<Int>()

        val result = stepsInput.hold(0)

        val resultAt1 = result.pullDirectly(Time(t = 1.1))

        assertEquals(
            expected = 0,
            actual = resultAt1.initialValue,
        )

        assertEquals(
            expected = emptyList(),
            actual = resultAt1.newValues.occurrences.toList(),
        )
    }

    @Test
    fun testFilterHold() {
        // TODO: Integration?
        // This is a system that, with a naive sequence-based event stream
        // representation, resulted in diverged computation (stack overflows).

        val stepsInput = EventStream.ofSequence(
            PureSequence.of(
                Incident(Time(t = 1.0), event = 10),
                Incident(Time(t = 2.0), event = 20),
                Incident(Time(t = 3.0), event = 30),
            ) + generateOccurrencesSequence(
                t0 = 11.0,
                seed = 40,
                nextFunction = { it },
            ),
        )

        val result = Cell.pullLooped1 { accumulatorLoop: Cell<Int> ->
            stepsInput.pullOf { a ->
                accumulatorLoop.sample().map { acc -> a + acc }
            }.filter { it > 0 }.hold(5).map { accumulator ->
                Cell.Loop1(
                    cellA = accumulator,
                    result = accumulator,
                )
            }
        }

        val resultAt1 = result.pullDirectly(Time(t = 1.1))

        assertEquals(
            expected = 5,
            actual = resultAt1.initialValue,
        )

        assertEquals(
            expected = listOf(
                Incident(Time(t = 2.0), event = 25),
                Incident(Time(t = 3.0), event = 55),
            ),
            actual = resultAt1.newValues.occurrences.take(2).toList(),
        )
    }

    @Test
    fun testPullLooped1() {
        // TODO: Integration?

        val result = EventStream.pullLooped1 { stepsLoop: EventStream<Int> ->
            stepsLoop.hold(0).pullOf { valueCell ->
                // Looping needs to be correctly implemented, so [initialValue]
                // does not depend on [stepsLoop]'s occurrences
                valueCell.sample().map { initialValue ->
                    EventStream.Loop1(
                        streamA = EventStream.ofSequence(
                            PureSequence.of(
                                Incident(Time(t = 1.0), event = 10),
                                Incident(Time(t = 2.0), event = 20),
                                Incident(Time(t = 3.0), event = 30),
                            ) + generateOccurrencesSequence(
                                t0 = 4.0,
                                seed = 40,
                                nextFunction = { it },
                            ),
                        ),
                        result = initialValue,
                    )
                }
            }
        }.pullDirectly(t = Time.zero)

        assertEquals(
            expected = 0,
            actual = result,
        )
    }
}
