package io.github.oxidefrp.semantics

import io.github.oxidefrp.semantics.test_utils.tableSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class MomentStateUnitTests {
    object AsStateStructure {
        private val stateSignal = tableSignal(
            table = mapOf(
                Time(2.0) to S(sum = 20),
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

        @Test
        fun testFromStateSignal() {
            // The old state comes from the [stateSignal]

            val (outputLayer, result) = momentState(n = 2).asStateStructure()
                .constructDirectly(
                    stateSignal = stateSignal,
                ).pullEnterDirectly(
                    t = Time(2.0),
                    oldState = inputLayer,
                )

            assertEquals<List<Instant<S>>>(
                expected = listOf(
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                    Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 2)),
                    Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40)),
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
                ),
                actual = outputLayer.stateStream.occurrences.instants.toList(),
            )

            assertEquals(
                expected = "20@2.0/2",
                actual = result,
            )
        }

        @Test
        fun testFromInputLayer() {
            // The old state comes from the [inputLayer]

            val (outputLayer, result) = momentState(n = 4).asStateStructure()
                .constructDirectly(
                    stateSignal = stateSignal,
                ).pullEnterDirectly(
                    t = Time(4.0),
                    oldState = inputLayer,
                )

            assertEquals<List<Instant<S>>>(
                expected = listOf(
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                    Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 4)),
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
                ),
                actual = outputLayer.stateStream.occurrences.instants.toList(),
            )

            assertEquals(
                expected = "40@4.0/4",
                actual = result,
            )
        }
    }
}
