package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.filterNotNull
import io.github.oxidefrp.core.hold
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.InputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.MomentSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import io.github.oxidefrp.core.test_utils.shared.DivertOperator
import kotlin.test.Test

object OperatorsIntegrationTests {
    /**
     * This is a matrix of test cases for a specific `filter` + `hold` +
     * (`divert` / `divertEarly`) system.
     *
     * The timeline consists of three crucial moments: t = 1, t = 2
     * (the "middle" moment), and t = 3.
     *
     * The system contains of...
     * - two source streams of numbers (events at t = 1, t = 2, and t = 3)
     * - a diversion cell which first exposes the first stream, but at t = 2 it
     *   diverts to the second one.
     *
     * The catch is that both the source stream number middle events and the
     * diversion event itself can be _filtered-out_.
     *
     * This system can be tricky to handle especially at the executable
     * semantics level.
     */
    object FilterHoldDiverts {

        private sealed interface SourceStreamSpec {
            val description: String

            val spec: InputStreamSpec<Double?>
        }

        private data class SourceCaseSpec(
            val firstStream: SourceStreamSpec,
            val secondStream: SourceStreamSpec,
        )

        private sealed interface DiversionStreamSpec {
            val description: String

            fun buildSpec(secondStream: EventStream<Double>): FiniteInputStreamSpec<EventStream<Double>?>
        }

        private object FirstStreamWithFilteredOutMiddleEvent : SourceStreamSpec {
            override val description: String = "with filtered-out middle event"

            override val spec = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = null),
                EventOccurrenceDesc(tick = Tick(t = 3), event = -3.0),
            )
        }

        private object FirstStreamWithMiddleEvent : SourceStreamSpec {
            override val description: String = "with middle event"

            override val spec = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = -2.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = -3.0),
            )
        }

        private object SecondStreamWithFilteredOutMiddleEvent : SourceStreamSpec {
            override val description: String = "with filtered-out middle event"

            override val spec = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 1), event = 1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = null),
                EventOccurrenceDesc(tick = Tick(t = 3), event = 3.0),
            )
        }

        private object SecondStreamWithMiddleEvent : SourceStreamSpec {
            override val description: String = "with middle event"

            override val spec = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 1), event = 1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = 2.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = 3.0),
            )
        }

        private val sourceFirstWithFilteredOutMiddleSecondWithFilteredOutMiddle = SourceCaseSpec(
            firstStream = FirstStreamWithFilteredOutMiddleEvent,
            secondStream = SecondStreamWithFilteredOutMiddleEvent,
        )

        private val sourceFirstWithFilteredOutMiddleSecondWithMiddle = SourceCaseSpec(
            firstStream = FirstStreamWithFilteredOutMiddleEvent,
            secondStream = SecondStreamWithMiddleEvent,
        )

        private val sourceFirstWithMiddleSecondWithFilteredOutMiddle = SourceCaseSpec(
            firstStream = FirstStreamWithMiddleEvent,
            secondStream = SecondStreamWithFilteredOutMiddleEvent,
        )

        private val sourceFirstWithMiddleSecondWithMiddle = SourceCaseSpec(
            firstStream = FirstStreamWithMiddleEvent,
            secondStream = SecondStreamWithMiddleEvent,
        )

        private object DiversionStreamWithFilteredOutDiversion : DiversionStreamSpec {
            override val description: String = "with filtered-out diversion"

            override fun buildSpec(secondStream: EventStream<Double>) = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 2), event = null),
                EventOccurrenceDesc(tick = Tick(t = 5), event = EventStream.never<Double>()),
            )
        }

        private object DiversionStreamWithDiversion : DiversionStreamSpec {
            override val description: String = "with diversion"

            override fun buildSpec(secondStream: EventStream<Double>) = FiniteInputStreamSpec(
                EventOccurrenceDesc(tick = Tick(t = 2), event = secondStream),
                EventOccurrenceDesc(tick = Tick(t = 5), event = EventStream.never()),
            )
        }

        private val expectedStreamWithoutMiddleWithoutDiversion = EventStreamSpec(
            expectedEvents = listOfNotNull(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = -3.0),
            ),
        )

        private val expectedStreamWithoutMiddleWithDiversion = EventStreamSpec(
            expectedEvents = listOfNotNull(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = 3.0),
            ),
        )

        private val expectedStreamWithOldMiddleWithoutDiversion = EventStreamSpec(
            expectedEvents = listOfNotNull(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = -2.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = -3.0),
            ),
        )

        private val expectedStreamWithOldMiddleWithDiversion = EventStreamSpec(
            expectedEvents = listOfNotNull(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = -2.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = 3.0),
            ),
        )

        private val expectedStreamWithNewMiddleWithDiversion = EventStreamSpec(
            expectedEvents = listOfNotNull(
                EventOccurrenceDesc(tick = Tick(t = 1), event = -1.0),
                EventOccurrenceDesc(tick = Tick(t = 2), event = 2.0),
                EventOccurrenceDesc(tick = Tick(t = 3), event = 3.0),
            ),
        )

        private fun testCase(
            sourceCase: SourceCaseSpec,
            diversionStream: DiversionStreamSpec,
            divertOperator: DivertOperator,
            expectedStream: EventStreamSpec<Double>,
        ) = testSystem {
            fun buildDiversionCell(): Moment<Cell<EventStream<Double>>> {
                val sourceStream1 = buildInputStream(sourceCase.firstStream.spec).filterNotNull()
                val sourceStream2 = buildInputStream(sourceCase.secondStream.spec).filterNotNull()

                return buildInputStream(
                    diversionStream.buildSpec(secondStream = sourceStream2),
                ).filterNotNull().hold(sourceStream1)
            }

            val divertedStream = buildDiversionCell().map(divertOperator::divert)

            val namePrefix = divertOperator.divertedStreamName

            val nameSuffix = listOf(
                "first source stream: ${sourceCase.firstStream.description}",
                "second source stream: ${sourceCase.secondStream.description}",
                "diversion stream: ${diversionStream.description}",
            ).joinToString(separator = ", ")

            TestCheck(
                subject = divertedStream,
                name = "$namePrefix ($nameSuffix)",
                spec = MomentSpec(
                    expectedValues = mapOf(
                        Tick(t = 0) to expectedStream,
                    ),
                ),
            )
        }

        object Divert {
            @Test
            fun testSourceCase1WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithoutMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase1WithDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithoutMiddleWithDiversion,
            )

            @Test
            fun testSourceCase2WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithoutMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase2WithDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithoutMiddleWithDiversion,
            )

            @Test
            fun testSourceCase3WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithOldMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase3WithDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithOldMiddleWithDiversion,
            )

            @Test
            fun testSourceCase4WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithOldMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase4WithDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.Divert,
                expectedStream = expectedStreamWithOldMiddleWithDiversion,
            )
        }

        object DivertEarly {
            @Test
            fun testSourceCase1WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithoutMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase1WithDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithoutMiddleWithDiversion,
            )

            @Test
            fun testSourceCase2WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithoutMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase2WithDiversion() = testCase(
                sourceCase = sourceFirstWithFilteredOutMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithNewMiddleWithDiversion,
            )

            @Test
            fun testSourceCase3WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithOldMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase3WithDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithFilteredOutMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithoutMiddleWithDiversion,
            )

            @Test
            fun testSourceCase4WithFilteredOutDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithFilteredOutDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithOldMiddleWithoutDiversion,
            )

            @Test
            fun testSourceCase4WithDiversion() = testCase(
                sourceCase = sourceFirstWithMiddleSecondWithMiddle,
                diversionStream = DiversionStreamWithDiversion,
                divertOperator = DivertOperator.DivertEarly,
                expectedStream = expectedStreamWithNewMiddleWithDiversion,
            )
        }
    }
}
