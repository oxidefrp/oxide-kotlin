package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

class StateSchedulerSharedUnitTests {
    object EventStreamPullEnter {
        @Test
        fun test() = testSystem {
            val stateSignal = buildInputSignal {
                when (it) {
                    Tick(t = 20) -> S(sum = 20)
                    Tick(t = 30) -> S(sum = 30)
                    else -> throw UnsupportedOperationException()
                }
            }

            val inputStateStream = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
            )

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val sourceStream2 = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 10), event = momentState(n = 1)),
                EventOccurrenceDesc(tick = Tick(t = 20), event = momentState(n = 2)),
                EventOccurrenceDesc(tick = Tick(t = 30), event = momentState(n = 3)),
                EventOccurrenceDesc(tick = Tick(t = 40), event = momentState(n = 4)),
            )

            val (outputLayer, resultStream) = EventStream.pullEnter(sourceStream2).scheduleDirectly(
                stateSignal = stateSignal,
            ).enterDirectly(
                oldState = inputLayer,
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = outputLayer.stateStream,
                        name = "Output state stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10 + 1)),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                                EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30 + 3)),
                                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40 + 4)),
                                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = resultStream,
                        name = "Result stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(t = 10), event = "10@10.0/1"),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = "20@20.0/2"),
                                EventOccurrenceDesc(tick = Tick(t = 30), event = "30@30.0/3"),
                                EventOccurrenceDesc(tick = Tick(t = 40), event = "40@40.0/4"),
                            )
                        ),
                    ),
                ),
            )
        }
    }
}
