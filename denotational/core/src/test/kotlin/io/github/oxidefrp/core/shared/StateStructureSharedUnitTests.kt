package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.test_framework.shared.CellSpec
import io.github.oxidefrp.core.test_framework.shared.CellValueDesc
import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import kotlin.test.Test

object StateStructureSharedUnitTests {
    private data class S(
        val sum: Int,
    )

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
                extra = buildInputStream(
                    EventOccurrenceDesc(tick = Tick(t = 5), event = S(sum = -1)),
                    EventOccurrenceDesc(tick = Tick(t = 15), event = S(sum = -1)),
                    EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = -1)),
                    EventOccurrenceDesc(tick = Tick(t = 35), event = S(sum = -1)),
                ),
                result = { "X" },
            )

            val secondSourceStructure = stateStructure(
                n = 2,
                extra = buildInputStream(
                    EventOccurrenceDesc(tick = Tick(t = 5), event = S(sum = 5)),
                    EventOccurrenceDesc(tick = Tick(t = 25), event = S(sum = 25)),
                    EventOccurrenceDesc(tick = Tick(t = 45), event = S(sum = -1)),
                ),
                result = { "${it.sum}/A" },
            )

            val thirdSourceStructure = stateStructure(
                n = 3,
                extra = buildInputStream(
                    EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = -1)),
                    EventOccurrenceDesc(tick = Tick(t = 55), event = S(sum = 55)),
                    EventOccurrenceDesc(tick = Tick(t = 65), event = S(sum = -1)),
                ),
                result = { "${it.sum}/B" },
            )

            val fourthSourceStructure = stateStructure(
                n = 4,
                extra = buildInputStream(
                    EventOccurrenceDesc(tick = Tick(t = 55), event = S(sum = -1)),
                    EventOccurrenceDesc(tick = Tick(t = 66), event = S(sum = 66)),
                    EventOccurrenceDesc(tick = Tick(t = 75), event = S(sum = 75)),
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

            val resultMoment = Cell.construct(structureCell).constructDirectly(
                stateSignal = stateSignal,
            ).enterDirectly(
                oldState = inputLayer,
            )

            val tick = Tick(t = 15)

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
                                        EventOccurrenceDesc(tick = Tick(t = 30), event = S(sum = 30 + 2)),
                                        EventOccurrenceDesc(tick = Tick(t = 40), event = S(sum = 40 + 3)),
                                        EventOccurrenceDesc(tick = Tick(t = 50), event = S(sum = 50 + 3)),
                                        EventOccurrenceDesc(tick = Tick(t = 55), event = S(sum = 55)),
                                        EventOccurrenceDesc(tick = Tick(t = 60), event = S(sum = 60 + 4)),
                                        EventOccurrenceDesc(tick = Tick(t = 66), event = S(sum = 66)),
                                        EventOccurrenceDesc(tick = Tick(t = 70), event = S(sum = 70 + 4)),
                                        EventOccurrenceDesc(tick = Tick(t = 75), event = S(sum = 75)),
                                        EventOccurrenceDesc(tick = Tick(t = 80), event = S(sum = 80 + 4)),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    TestCheck(
                        subject = resultMoment.map { (_, resultCell) -> resultCell },
                        name = "Result cell",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                tick to CellSpec(
                                    expectedInitialValue = "15/A",
                                    matchFrontValuesOnly = true,
                                    expectedInnerValues = listOf(
                                        CellValueDesc(tick = Tick(t = 35), value = "35/B"),
                                        CellValueDesc(tick = Tick(t = 60), value = "60/C"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    object Seed {
        @Test
        fun test() = testSystem {
            val sourceCell = buildInputCell(
                initialValue = 10,
                CellValueSpec(tick = Tick(t = 15), newValue = 15),
                CellValueSpec(tick = Tick(t = 30), newValue = 30),
                CellValueSpec(tick = Tick(t = 40), newValue = 40),
                CellValueSpec(tick = Tick(t = 50), newValue = 50),
            )

            val rootStateStructure = sourceCell.enterOf { n ->
                object : State<S, String>() {
                    override fun enterDirectly(oldState: S): Pair<S, String> {
                        val newState = S(sum = oldState.sum + n)
                        val result = "${oldState.sum}/+$n"
                        return Pair(newState, result)
                    }
                }
            }

            val resultMoment = rootStateStructure.seed(
                initState = S(sum = 1),
            )

            val tick = Tick(t = 20)

            TestSpec(
                checks = listOf(
                    TestCheck(
                        subject = resultMoment.map { (stateCell, _) -> stateCell },
                        name = "State cell",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                tick to CellSpec(
                                    expectedInitialValue = S(sum = 1),
                                    expectedInnerValues = listOf(
                                        // The state cell inner values should represent the state accumulated over time
                                        // and should not include the states from before the moment of seeding
                                        CellValueDesc(tick = Tick(t = 20), value = S(sum = 1 + 15)),
                                        CellValueDesc(tick = Tick(t = 30), value = S(sum = 1 + 15 + 30)),
                                        CellValueDesc(tick = Tick(t = 40), value = S(sum = 1 + 15 + 30 + 40)),
                                        CellValueDesc(tick = Tick(t = 50), value = S(sum = 1 + 15 + 30 + 40 + 50)),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    TestCheck(
                        subject = resultMoment.map { (_, resultCell) -> resultCell },
                        name = "Result cell",
                        spec = MomentSpec(
                            expectedValues = mapOf(
                                tick to CellSpec(
                                    // The result cell initial value should be the seed init state and the current input
                                    // cell state at the time of seeding (t = 20)
                                    expectedInitialValue = "1/+15",
                                    expectedInnerValues = listOf(
                                        // The result cell inner values should be based on the state accumulated over
                                        // time and do not include results at times from before the moment of seeding
                                        CellValueDesc(tick = Tick(t = 30), value = "16/+30"),
                                        CellValueDesc(tick = Tick(t = 40), value = "46/+40"),
                                        CellValueDesc(tick = Tick(t = 50), value = "86/+50"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
