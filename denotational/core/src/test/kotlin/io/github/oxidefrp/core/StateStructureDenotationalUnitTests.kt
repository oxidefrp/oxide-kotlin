package io.github.oxidefrp.core

import io.github.oxidefrp.core.test_utils.tableSignal
import kotlin.test.Test
import kotlin.test.assertEquals

object StateStructureDenotationalUnitTests {
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

    object TestCell {
        @Test
        fun testConstruct() {
            val stateSignal = tableSignal(
                table = mapOf(
                    Time(1.5) to S(sum = 15),
                    Time(3.5) to S(sum = 35),
                ),
            )

            val inputLayer = StateSchedulerLayer(
                stateStream = EventStream.ofInstants(
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10)),
                    Instant.strictNonNull(time = Time(2.0), element = S(sum = 20)),
                    Instant.strictNonNull(time = Time(3.0), element = S(sum = 30)),
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40)),
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50)),
                    Instant.strictNonNull(time = Time(6.0), element = S(sum = 60)),
                    Instant.strictNonNull(time = Time(7.0), element = S(sum = 70)),
                    Instant.strictNonNull(time = Time(8.0), element = S(sum = 80)),
                ),
            )

            val cell = Cell.ofInstants(
                // Thought: Maybe this test should not use a fake state structure, but rather be moved to the cell unit
                // tests file and use the (assumed to be tested) `Cell.pullEnter`-returned structure
                initialValue = stateStructure(
                    n = 1,
                    extra = EventStream.ofInstants(
                        Instant.strictNonNull(time = Time(0.5), element = S(sum = -1)),
                        Instant.strictNonNull(time = Time(1.5), element = S(sum = -1)),
                        Instant.strictNonNull(time = Time(2.5), element = S(sum = -1)),
                        Instant.strictNonNull(time = Time(3.5), element = S(sum = -1)),
                    ),
                    result = { "X" },
                ),
                Instant.strictNonNull(
                    time = Time(1.0),
                    element = stateStructure(
                        n = 2,
                        extra = EventStream.ofInstants(
                            Instant.strictNonNull(time = Time(0.5), element = S(sum = 5)),
                            Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                            Instant.strictNonNull(time = Time(4.5), element = S(sum = -1)),
                        ),
                        result = { "${it.sum}/A" },
                    ),
                ),
                Instant.strictNonNull(
                    time = Time(3.5),
                    element = stateStructure(
                        n = 3,
                        extra = EventStream.ofInstants(
                            Instant.strictNonNull(time = Time(3.0), element = S(sum = -1)),
                            Instant.strictNonNull(time = Time(5.5), element = S(sum = 55)),
                            Instant.strictNonNull(time = Time(6.5), element = S(sum = -1)),
                        ),
                        result = { "${it.sum}/B" },
                    ),
                ),
                Instant.strictNonNull(
                    time = Time(6.0),
                    element = stateStructure(
                        n = 4,
                        extra = EventStream.ofInstants(
                            Instant.strictNonNull(time = Time(5.5), element = S(sum = -1)),
                            Instant.strictNonNull(time = Time(6.6), element = S(sum = 66)),
                            Instant.strictNonNull(time = Time(7.5), element = S(sum = 75)),
                        ),
                        result = { "${it.sum}/C" },
                    ),
                ),
            )

            val (outputLayer, outputCell) = Cell.construct(cell).constructDirectly(
                stateSignal,
            ).pullEnterDirectly(
                t = Time(1.5),
                oldState = inputLayer,
            )

            assertEquals(
                expected = listOf(
                    // The extra states from the structure being current at the time of construction/entering _are_
                    // forwarded (to the point of first structure cell change), but the ones from earlier structures
                    // are not forwarded at all (thought: but is it good?)
                    Instant.strictNonNull(time = Time(0.5), element = S(sum = 5)),
                    Instant.strictNonNull(time = Time(1.0), element = S(sum = 10 + 2)),
                    Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 2)),
                    Instant.strictNonNull(time = Time(2.5), element = S(sum = 25)),
                    Instant.strictNonNull(time = Time(3.0), element = S(sum = 30 + 2)),
                    Instant.strictNonNull(time = Time(3.5), element = null),
                    Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 3)),
                    Instant.strictNonNull(time = Time(5.0), element = S(sum = 50 + 3)),
                    Instant.strictNonNull(time = Time(5.5), element = S(sum = 55)),
                    Instant.strictNonNull(time = Time(6.0), element = S(sum = 60 + 4)),
                    Instant.strictNonNull(time = Time(6.6), element = S(sum = 66)),
                    Instant.strictNonNull(time = Time(7.0), element = S(sum = 70 + 4)),
                    Instant.strictNonNull(time = Time(7.5), element = S(sum = 75)),
                    Instant.strictNonNull(time = Time(8.0), element = S(sum = 80 + 4)),
                ),
                actual = outputLayer.stateStream.occurrences.instants.toList(),
            )

            val outputSegmentSequence = outputCell.segmentSequence

            assertEquals(
                // The output cell initial value should be based on the state from the entering (t = 1.5)
                expected = "15/A",
                actual = outputSegmentSequence.initialValue,
            )

            assertEquals(
                expected = listOf(
                    // The output cell inner values should be based on the state from time of their respective structure
                    // cell inner values
                    Instant.strictNonNull(time = Time(3.5), element = "35/B"),
                    Instant.strictNonNull(time = Time(6.0), element = "60/C"),
                ),
                actual = outputSegmentSequence.innerValues.instants.toList(),
            )
        }
    }

    @Test
    fun testConstruct() {
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

        val nestedStateStructure = stateStructure(
            n = 2,
            extra = EventStream.ofInstants(
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 20)),
                Instant.strictNonNull(time = Time(4.5), element = S(sum = 45)),
            ),
            result = {
                stateStructure(
                    n = 3,
                    extra = EventStream.ofInstants(
                        Instant.strictNonNull(time = Time(1.5), element = S(sum = 15)),
                        Instant.strictNonNull(time = Time(5.5), element = S(sum = 55)),
                    ),
                    result = { "foo" },
                )
            },
        )

        val (outputLayer, outputValue) = StateStructure.construct(nestedStateStructure).constructDirectly(
            stateSignal = stateSignal,
        ).pullEnterDirectly(
            t = Time(2.0),
            oldState = inputLayer,
        )

        assertEquals(
            expected = listOf(
                // All the states from the input layer have both transformations applied (+ 2 + 3)
                Instant.strictNonNull(time = Time(1.0), element = S(sum = 10 + 2 + 3)),
                // A state injected from the inner structure has no transformations applied
                Instant.strictNonNull(time = Time(1.5), element = S(sum = 15)),
                // A state injected from the outer structure has only the transformation from the inner structure
                // applied (+ 3)
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 20 + 3)),
                Instant.strictNonNull(time = Time(2.5), element = S(sum = 25 + 2 + 3)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 40 + 2 + 3)),
                Instant.strictNonNull(time = Time(4.5), element = S(sum = 45 + 3)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 50 + 2 + 3)),
                Instant.strictNonNull(time = Time(5.5), element = S(sum = 55)),
            ),
            actual = outputLayer.stateStream.occurrences.instants.toList(),
        )

        // The result of the constructed structure should be the one from the inner nested structure
        assertEquals(
            expected = "foo",
            actual = outputValue,
        )
    }

    @Test
    fun testPull() {
        val stateSignal = tableSignal(
            table = mapOf(
                Time(3.0) to S(sum = 1),
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

        val momentStateStructure = stateStructure(
            n = 2,
            extra = EventStream.ofInstants(
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 20)),
                Instant.strictNonNull(time = Time(4.5), element = S(sum = 45)),
            ),
            result = {
                object : Moment<String>() {
                    override fun pullDirectly(t: Time): String = "${it.sum}@${t.t}"
                }
            },
        )

        val (outputLayer, outputValue) = StateStructure.pull(momentStateStructure).constructDirectly(
            stateSignal = stateSignal,
        ).pullEnterDirectly(
            t = Time(3.0),
            oldState = inputLayer,
        )

        assertEquals(
            expected = listOf(
                Instant.strictNonNull(time = Time(1.0), element = S(sum = 12)),
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 20)),
                Instant.strictNonNull(time = Time(2.5), element = S(sum = 27)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 42)),
                Instant.strictNonNull(time = Time(4.5), element = S(sum = 45)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 52)),
            ),
            actual = outputLayer.stateStream.occurrences.instants.toList(),
        )

        assertEquals(
            expected = "1@3.0",
            actual = outputValue,
        )
    }

    @Test
    fun testSeed() {
        val rootStateStructure = Cell.ofInstants(
            initialValue = 10,
            Instant.strictNonNull(time = Time(1.5), element = 15),
            Instant.strictNonNull(time = Time(3.0), element = 30),
            Instant.strictNonNull(time = Time(4.0), element = 40),
            Instant.strictNonNull(time = Time(5.0), element = 50),
        ).enterOf { n ->
            object : State<S, String>() {
                override fun enterDirectly(oldState: S): Pair<S, String> {
                    val newState = S(sum = oldState.sum + n)
                    val result = "${oldState.sum}/+$n"
                    return Pair(newState, result)
                }
            }
        }

        val (stateCell, resultCell) = rootStateStructure.seed(
            initState = S(sum = 1),
        ).pullDirectly(
            t = Time(2.0),
        )

        val stateSegmentSequence = stateCell.segmentSequence

        assertEquals(
            // The state cell initial value should be the seed init state
            expected = S(sum = 1),
            actual = stateSegmentSequence.initialValue,
        )

        assertEquals(
            expected = listOf(
                // The state cell inner values should represent the state accumulated over time and should not include
                // the states from before the moment of seeding
                Instant.strictNonNull(time = Time(2.0), element = S(sum = 1 + 15)),
                Instant.strictNonNull(time = Time(3.0), element = S(sum = 1 + 15 + 30)),
                Instant.strictNonNull(time = Time(4.0), element = S(sum = 1 + 15 + 30 + 40)),
                Instant.strictNonNull(time = Time(5.0), element = S(sum = 1 + 15 + 30 + 40 + 50)),
            ),
            actual = stateSegmentSequence.innerValues.instants.toList(),
        )

        val resultSegmentSequence = resultCell.segmentSequence

        assertEquals(
            // The result cell initial value should be the seed init state and the current input cell state at the time
            // of seeding (t = 2.0)
            expected = "1/+15",
            actual = resultSegmentSequence.initialValue,
        )

        assertEquals(
            expected = listOf(
                // The result cell inner values should be based on the state accumulated over time and do not include
                // results at times from before the moment of seeding
                Instant.strictNonNull(time = Time(2.0), element = null),
                Instant.strictNonNull(time = Time(3.0), element = "16/+30"),
                Instant.strictNonNull(time = Time(4.0), element = "46/+40"),
                Instant.strictNonNull(time = Time(5.0), element = "86/+50"),
            ),
            actual = resultSegmentSequence.innerValues.instants.toList(),
        )
    }
}
