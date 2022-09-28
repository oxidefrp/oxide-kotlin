package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_utils.tableSignal
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class StateSchedulerUnitTests {
    @Test
    fun testPullEnter() {
        val stateSignal = tableSignal(
            table = mapOf(
                Time(2.0) to S(sum = 20),
                Time(3.0) to S(sum = 30),
            ),
        )

        val inputLayer = StateSchedulerLayer(
            stateStream = EventStream.ofInstants(
                Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 40)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
            ),
        )

        val stream = EventStream.ofInstants(
            Instant.strictNonNull(time = Time(1.0), element = momentState(n = 1)),
            Instant.strictNonNull(time = Time(2.0), element = momentState(n = 2)),
            Instant.strictNonNull(time = Time(3.0), element = momentState(n = 3)),
            Instant.strictNonNull(time = Time(4.0), element = momentState(n = 4)),
        )

        val (outputLayer, outputStream) = EventStream.pullEnter(stream).scheduleDirectly(
            stateSignal = stateSignal,
        ).enterDirectly(
            oldState = inputLayer,
        )

        assertEquals(
            expected = listOf(
                Instant.strictNonNull(time = Time(1.0), element = S(sum = 10 + 1)),
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 2)),
                Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                Instant.strictNonNull(time = Time(3.0), element = S(sum = 30 + 3)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 4)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
            ),
            actual = outputLayer.stateStream.occurrences.instants.toList(),
        )

        assertEquals(
            expected = listOf(
                Instant.strictNonNull(time = Time(1.0), element = "10@1.0/1"),
                Instant.strictNonNull(time = Time(2.0), element = "20@2.0/2"),
                Instant.strictNonNull(time = Time(2.5), element = null),
                Instant.strictNonNull(time = Time(3.0), element = "30@3.0/3"),
                Instant.strictNonNull(time = Time(4.0), element = "40@4.0/4"),
                Instant.strictNonNull(time = Time(5.0), element = null),
            ),
            actual = outputStream.occurrences.instants.toList(),
        )
    }
}
