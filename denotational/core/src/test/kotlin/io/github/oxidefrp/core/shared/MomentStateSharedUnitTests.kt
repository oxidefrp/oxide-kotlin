package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

class MomentStateSharedUnitTests {
    private data class S(
        val sum: Int,
    )

    object AsStateStructure {
        private val stateSignalSpec = InputSignalSpec {
            when (it) {
                Tick(t = 20) -> S(sum = 20)
                else -> throw UnsupportedOperationException("Unexpected tick: $it")
            }
        }

        private val inputStateStreamSpec = FiniteInputStreamSpec(
            EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
            EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
            EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
            EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
        )

        @Test
        fun testFromStateSignal() = testSystem {
            // The old state comes from the [stateSignal]

            fun momentState(n: Int) =
                object : MomentState<S, String>() {
                    override fun enterDirectly(oldState: S): Moment<Pair<S, String>> =
                        getCurrentTick().map {
                            val result = "${oldState.sum}@${it.t}.0/$n"
                            val newState = S(sum = oldState.sum + n)
                            Pair(newState, result)
                        }
                }

            val stateSignal = buildInputSignal(stateSignalSpec)

            val inputStateStream = buildInputStream(inputStateStreamSpec)

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val resultMoment = momentState(n = 2).asStateStructure().constructEnterDirectly(
                stateSignal = stateSignal,
                inputLayer = inputLayer,
            )

            val tick = Tick(t = 20)

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = resultMoment.map { (outputLayer, _) -> outputLayer.stateStream },
                        name = "Output state stream",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                tick to EventStreamSpec(
                                    expectedEvents = listOf(
                                        EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20 + 2)),
                                        EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                                        EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
                                        EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
                                    )
                                ),
                            ),
                        ),
                    ),
                    TestCheck(
                        subject = resultMoment.map { (_, result) -> result },
                        name = "Result",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                tick to ValueSpec(
                                    expected = "20@20.0/2",
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
