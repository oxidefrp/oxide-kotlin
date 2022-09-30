package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState
import io.github.oxidefrp.core.shared.StateSchedulerLayer
import io.github.oxidefrp.core.shared.StateStructure
import io.github.oxidefrp.core.shared.construct
import io.github.oxidefrp.core.shared.orElse
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.shared.ValueSpec
import io.github.oxidefrp.core.test_framework.testSystem
import java.lang.UnsupportedOperationException
import kotlin.test.Test

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

    object CellConstruct {
        @Test
        fun test() = testSystem {
            val stateSignal = buildInputSignal {
                when (it) {
                    Tick(t = 15) -> S(sum = 15)
                    Tick(t = 35) -> S(sum = 35)
                    else -> throw UnsupportedOperationException()
                }
            }

            val inputStateStream = buildInputStream(
                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10)),
                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20)),
                EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30)),
                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40)),
                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50)),
                EventOccurrenceDesc(tick = Tick(t = 60), event = S(sum = 60)),
                EventOccurrenceDesc(tick = Tick(t = 70), event = S(sum = 70)),
                EventOccurrenceDesc(tick = Tick(t = 80), event = S(sum = 80)),
            )

            val inputLayer = StateSchedulerLayer(stateStream = inputStateStream)

            val firstSourceStructure = stateStructure(
                n = 1,
                extra = EventStream.ofInstants(
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = -1)),
                    Instant.strictNonNull(time = Time(15.0), element = S(sum = -1)),
                    Instant.strictNonNull(time = Time(25.0), element = S(sum = -1)),
                    Instant.strictNonNull(time = Time(35.0), element = S(sum = -1)),
                ),
                result = { "X" },
            )

            val secondSourceStructure = stateStructure(
                n = 2,
                extra = EventStream.ofInstants(
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 5)),
                    Instant.strictNonNull(time = Time(25.0), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(45.0), element = S(sum = -1)),
                ),
                result = { "${it.sum}/A" },
            )

            val thirdSourceStructure = stateStructure(
                n = 3,
                extra = EventStream.ofInstants(
                    Instant.strictNonNull(time = Time(30.0), element = S(sum = -1)),
                    Instant.strictNonNull(time = Time(55.0), element = S(sum = 55)),
                    Instant.strictNonNull(time = Time(65.0), element = S(sum = -1)),
                ),
                result = { "${it.sum}/B" },
            )

            val fourthSourceStructure = stateStructure(
                n = 4,
                extra = EventStream.ofInstants(
                    Instant.strictNonNull(time = Time(55.0), element = S(sum = -1)),
                    Instant.strictNonNull(time = Time(66.0), element = S(sum = 66)),
                    Instant.strictNonNull(time = Time(75.0), element = S(sum = 75)),
                ),
                result = { "${it.sum}/C" },
            )

            val structureCell = buildInputCell(
                // Thought: Maybe this test should not use a fake state structure, but rather be moved to the cell unit
                // tests file and use the (assumed to be tested) `Cell.pullEnter`-returned structure
                initialValue = firstSourceStructure,
                CellValueSpec(tick = Tick(t = 10), newValue = secondSourceStructure),
                CellValueSpec(tick = Tick(t = 35), newValue = thirdSourceStructure),
                CellValueSpec(tick = Tick(t = 60), newValue = fourthSourceStructure),
            )

            val (outputLayer, resultCell) = Cell.construct(structureCell).constructDirectly(
                stateSignal,
            ).pullEnterDirectly(
                t = Time(15.0),
                oldState = inputLayer,
            )

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = outputLayer.stateStream,
                        name = "Output state stream",
                        spec = EventStreamSpec(
                            expectedEvents = listOf(
                                // The extra states from the structure being current at the time of construction/entering _are_
                                // forwarded (to the point of first structure cell change), but the ones from earlier structures
                                // are not forwarded at all (thought: but is it good?)
                                EventOccurrenceDesc(tick = Tick(t = 5), event = S(sum = 5)),
                                EventOccurrenceDesc(tick = Tick(t = 10), event = S(sum = 10 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 20), event = S(sum = 20 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                                EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30 + 2)),
                                EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40 + 3)),
                                EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50 + 3)),
                                EventOccurrenceDesc(tick = Tick(t = 55), event = S(sum = 55)),
                                EventOccurrenceDesc(tick = Tick(t = 60), event = S(sum = 60 + 4)),
                                EventOccurrenceDesc(tick = Tick(t = 66), event = S(sum = 66)),
                                EventOccurrenceDesc(tick = Tick(t = 70), event = S(sum = 70 + 4)),
                                EventOccurrenceDesc(tick = Tick(t = 75), event = S(sum = 75)),
                                EventOccurrenceDesc(tick = Tick(t = 80), event = S(sum = 80 + 4)),
                            )
                        ),
                    ),
                    TestCheck(
                        subject = resultCell,
                        name = "Result cell",
                        spec = CellSpec(
                            expectedInitialValue = "15/A",
                            matchFrontValuesOnly = true,
                            expectedInnerValues = listOf(
                                CellValueDesc(tick = Tick(t = 35), value = "35/B"),
                                CellValueDesc(tick = Tick(t = 60), value = "60/C"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
