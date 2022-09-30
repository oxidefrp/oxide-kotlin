package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.S
import io.github.oxidefrp.core.StateSchedulerLayer
import io.github.oxidefrp.core.Time
import io.github.oxidefrp.core.momentState
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputSignalSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

class MomentStateSharedUnitTests {
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

            val stateSignal = buildInputSignal(stateSignalSpec)

            val inputStateStream = buildInputStream(inputStateStreamSpec)

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val resultMoment = momentState(n = 2).asStateStructure().constructEnterDirectly(
                    stateSignal = stateSignal,
                    inputLayer = inputLayer,
                )

            val (outputLayer, result) = resultMoment.pullDirectly(t = Time(20.0))

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
                                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
                                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = result,
                        name = "Result",
                        spec = ValueSpec(
                            expected = "20@20.0/2",
                        ),
                    ),
                ),
            )
        }
    }
}
