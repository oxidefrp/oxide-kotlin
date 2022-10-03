package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.test_framework.shared.CellValueSpec
import io.github.oxidefrp.core.test_framework.shared.EventOccurrenceDesc
import io.github.oxidefrp.core.test_framework.shared.EventStreamSpec
import io.github.oxidefrp.core.test_framework.shared.FiniteInputStreamSpec
import io.github.oxidefrp.core.test_framework.shared.TestCheck
import io.github.oxidefrp.core.test_framework.shared.TestSpec
import io.github.oxidefrp.core.test_framework.shared.Tick
import io.github.oxidefrp.core.test_framework.testSystem
import io.github.oxidefrp.core.test_utils.shared.DivertOperator
import kotlin.test.Test

object DivertOperatorUnitTests {
    private val collidingNoneDiversionTick = Tick(t = 25)

    private val collidingFirstDiversionTick = Tick(t = 20)

    private val collidingSecondDiversionTick = Tick(t = 21)

    private val collidingBothDiversionTick = Tick(t = 30)

    private val firstSourceStreamSpec = buildSourceStreamSpec(
        events = listOf(
            EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
            EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
            EventOccurrenceDesc(tick = Tick(t = 30), event = "c"),
            EventOccurrenceDesc(tick = Tick(t = 40), event = "d"),
        ),
        collidingTick = collidingFirstDiversionTick,
        nonCollidingTick = collidingSecondDiversionTick,
    )

    private val secondSourceStreamSpec = buildSourceStreamSpec(
        events = listOf(
            EventOccurrenceDesc(tick = Tick(t = 11), event = "A"),
            EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
            EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
            EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
        ),
        collidingTick = collidingSecondDiversionTick,
        nonCollidingTick = collidingFirstDiversionTick,
    )

    object Divert {
        @Test
        fun testDiversionCollidingNone() = testCase(
            diversionTick = collidingNoneDiversionTick,
            divertOperator = DivertOperator.Divert,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingFirst() = testCase(
            diversionTick = collidingFirstDiversionTick,
            divertOperator = DivertOperator.Divert,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingSecond() = testCase(
            diversionTick = collidingSecondDiversionTick,
            divertOperator = DivertOperator.Divert,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingBoth() = testCase(
            diversionTick = collidingBothDiversionTick,
            divertOperator = DivertOperator.Divert,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "c"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )
    }

    object DivertEarly {
        @Test
        fun testDiversionCollidingNone() = testCase(
            diversionTick = collidingNoneDiversionTick,
            divertOperator = DivertOperator.DivertEarly,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingFirst() = testCase(
            diversionTick = collidingFirstDiversionTick,
            divertOperator = DivertOperator.DivertEarly,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingSecond() = testCase(
            diversionTick = collidingSecondDiversionTick,
            divertOperator = DivertOperator.DivertEarly,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 21), event = "B"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )

        @Test
        fun testDiversionCollidingBoth() = testCase(
            diversionTick = collidingBothDiversionTick,
            divertOperator = DivertOperator.DivertEarly,
            expectedEvents = listOf(
                EventOccurrenceDesc(tick = Tick(t = 10), event = "a"),
                EventOccurrenceDesc(tick = Tick(t = 20), event = "b"),
                EventOccurrenceDesc(tick = Tick(t = 30), event = "C"),
                EventOccurrenceDesc(tick = Tick(t = 41), event = "D"),
            )
        )
    }

    private fun buildSourceStreamSpec(
        events: List<EventOccurrenceDesc<String>>,
        collidingTick: Tick,
        nonCollidingTick: Tick,
    ): FiniteInputStreamSpec<String> {
        val spec = FiniteInputStreamSpec(events = events)

        if (
            spec.hasOccurrence(collidingNoneDiversionTick) ||
            spec.hasOccurrence(nonCollidingTick) ||
            !spec.hasOccurrence(collidingBothDiversionTick) ||
            !spec.hasOccurrence(collidingTick)
        ) {
            throw IllegalArgumentException()
        }

        return spec
    }

    private fun testCase(
        diversionTick: Tick,
        divertOperator: DivertOperator,
        expectedEvents: List<EventOccurrenceDesc<String>>,
    ) = testSystem {
        val firstSourceStream = buildInputStream(firstSourceStreamSpec)
        val secondSourceStream = buildInputStream(secondSourceStreamSpec)

        val diversionCell = buildInputCell(
            initialValue = firstSourceStream,
            CellValueSpec(tick = diversionTick, newValue = secondSourceStream),
        )

        val divertedStream = divertOperator.divert(diversionCell)

        TestSpec(
            checks = listOf(
                TestCheck(
                    subject = divertedStream,
                    name = divertOperator.divertedStreamName,
                    spec = EventStreamSpec(
                        expectedEvents = expectedEvents,
                    ),
                ),
            ),
        )
    }
}
