package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec
import io.github.oxidefrp.core.test_framework.testSystem
import io.github.oxidefrp.core.test_utils.tableSignal
import java.lang.UnsupportedOperationException
import kotlin.test.Test
import kotlin.test.assertEquals

object StateStructureSharedUnitTests {
    /// Build a [StateStructure] object that adds [n] to every sum in the input
    /// layer [S] state, injects all the states from the [extra] stream, and
    /// returns [value] as the result.
    /// This is not a practical [StateStructure], but rather a fake instance
    /// that implements the contract directly and trivially.
    private fun <A> stateStructure(
        n: Int,
        extra: EventStream<S>,
        result: (initialState: S) -> A,
    ): StateStructure<S, A> = object : StateStructure<S, A>() {
        override fun constructDirectly(
            stateSignal: Signal<S>,
        ): MomentState<StateSchedulerLayer<S>, A> = object : MomentState<StateSchedulerLayer<S>, A>() {
            override fun enterDirectly(
                oldState: StateSchedulerLayer<S>,
            ): Moment<Pair<StateSchedulerLayer<S>, A>> {
                val inputLayer = oldState

                val newStateStream = extra.orElse(
                    inputLayer.stateStream.map { S(sum = it.sum + n) },
                )

                return inputLayer
                    .getOldState(stateSignal = stateSignal)
                    .map { initialState ->
                        Pair(
                            StateSchedulerLayer(stateStream = newStateStream),
                            result(initialState),
                        )
                    }
            }
        }
    }

    object Pull {
        @Test
        fun test() = testSystem {
            val stateSignal = buildInputSignal {
                when (it) {
                    Tick(t = 30) -> S(sum = 1)
                    else -> throw UnsupportedOperationException("Unexpected tick: $it")
                }
            }

            val inputStateStream = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
            )

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val extraStream = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20)),
                EventOccurrenceDesc(tick = Tick(t = 45), event = S(sum = 45)),
            )

            val momentStateStructure = stateStructure(
                n = 2,
                extra = extraStream,
                result = {
                    object : Moment<String>() {
                        override fun pullDirectly(t: Time): String = "${it.sum}@${t.t}"
                    }
                },
            )

            val (outputLayer, resultValue) = StateStructure.pull(momentStateStructure).constructDirectly(
                stateSignal = stateSignal,
            ).pullEnterDirectly(
                t = Time(t = 30.0),
                oldState = inputLayer,
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = outputLayer.stateStream,
                        name = "Output state stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                EventOccurrenceDesc(tick = Tick(10), event = S(sum = 12)),
                                EventOccurrenceDesc(tick = Tick(20), event = S(sum = 20)),
                                EventOccurrenceDesc(tick = Tick(25), event = S(sum = 27)),
                                EventOccurrenceDesc(tick = Tick(40), event = S(sum = 42)),
                                EventOccurrenceDesc(tick = Tick(45), event = S(sum = 45)),
                                EventOccurrenceDesc(tick = Tick(50), event = S(sum = 52)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = resultValue,
                        name = "Result value",
                        spec = ValueSpec(
                            expected = "1@30.0",
                        ),
                    ),
                ),
            )
        }
    }
}
