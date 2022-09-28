package io.github.oxidefrp.oxide.core.test_framework.shared

interface Printer {
    fun println(line: String)
}

fun Printer.withIncreasedIndentation(): Printer = IndentedPrinter(
    printer = this,
)

class StringPrinter : Printer {
    private var buffer = ""

    override fun println(line: String) {
        buffer += line + "\n"
    }

    fun getBuffer() = buffer
}

class IndentedPrinter(
    private val printer: Printer,
) : Printer {
    override fun println(line: String) {
        printer.println("  $line")
    }
}

internal abstract class Issue {
    companion object {
        fun assertNoIssue(
            name: String,
            issue: Issue?,
        ) {
            if (issue != null) {
                val printer = StringPrinter()

                printer.println("Issue with `$name`:")

                issue.print(printer = printer.withIncreasedIndentation())

                throw AssertionError(printer.getBuffer())
            }
        }
    }

    abstract fun print(printer: Printer)
}

internal abstract class EventIssue : Issue() {
    abstract val tick: Tick

    abstract val message: String

    final override fun print(printer: Printer) {
        printer.println("At tick ${tick.t}: $message")
    }
}

internal data class IncorrectEventIssue<out A>(
    override val tick: Tick,
    val expected: A,
    val actual: A,
) : EventIssue() {
    override val message: String = "Expected event `$expected`, received `$actual`"
}

internal data class UnexpectedEventIssue<out A>(
    override val tick: Tick,
    val actual: A,
) : EventIssue() {
    override val message: String = "Didn't expect any event, received `$actual`"

}

internal data class MissingEventIssue<out A>(
    override val tick: Tick,
    val expected: A,
) : EventIssue() {
    override val message: String = "Expected event `$expected`, didn't receive any"
}

internal class EventStreamIssue private constructor(
    private val eventIssues: List<EventIssue>,
) : Issue() {
    companion object {
        fun <A> validate(
            streamSpec: EventStreamSpec<A>,
            events: List<EventOccurrenceDesc<A>>,
        ): EventStreamIssue? {
            val consideredIncidents = if (streamSpec.matchFrontEventsOnly) {
                events.takeWhile { it.tick <= streamSpec.lastTick }
            } else {
                events.takeWhile { it.tick.t <= Tick.maxT }
            }.toList()

            val consideredTicks =
                consideredIncidents.map { it.tick }.toSet()

            val wrongEventIssues = consideredIncidents.mapNotNull { eventDesc ->
                val tick = eventDesc.tick
                val actualEvent = eventDesc.event

                val expectedEventDesc = streamSpec.getExpectedEvent(tick = tick)

                if (expectedEventDesc != null) {
                    val expectedEvent = expectedEventDesc.event

                    when {
                        eventDesc != expectedEventDesc -> IncorrectEventIssue(
                            tick = tick,
                            expected = expectedEvent,
                            actual = actualEvent,
                        )

                        else -> null
                    }
                } else UnexpectedEventIssue(
                    tick = tick,
                    actual = actualEvent,
                )
            }

            val missingEventIssues = streamSpec.expectedEvents.mapNotNull { eventDesc ->
                val tick = eventDesc.tick
                val expectedEvent = eventDesc.event

                if (!consideredTicks.contains(tick)) {
                    MissingEventIssue(
                        tick = tick,
                        expected = expectedEvent,
                    )
                } else null
            }

            val allEventIssues = wrongEventIssues + missingEventIssues

            return if (allEventIssues.isNotEmpty()) {
                EventStreamIssue(
                    eventIssues = allEventIssues.sortedBy { it.tick },
                )
            } else null
        }

    }

    override fun print(printer: Printer) {
        eventIssues.forEach {
            it.print(printer)
        }
    }
}

internal class IncorrectValueIssue<out A> private constructor(
    val expected: A,
    val actual: A,
) : Issue() {
    companion object {
        fun <A> validate(
            valueSpec: ValueSpec<A>,
            value: A,
        ): IncorrectValueIssue<A>? {
            val expected = valueSpec.expected

            return if (value != expected) IncorrectValueIssue(
                expected = expected,
                actual = value,
            ) else null
        }
    }

    override fun print(printer: Printer) {
        printer.println("Expected value `$expected`, got `$actual`")
    }
}

internal class MomentIssue private constructor(
    private val valueIssues: Map<Tick, Issue>,
) : Issue() {
    companion object {
        fun <A> validate(
            momentSpec: MomentSpec<A>,
            valueIssues: Map<Tick, Issue?>,
        ): MomentIssue? {
            val valueKeys = valueIssues.keys
            val expectedKeys = momentSpec.expectedValues.keys

            if (valueKeys != expectedKeys) {
                throw IllegalStateException("Value keys: $valueKeys\nExpected keys: $expectedKeys")
            }

            val actualValueIssues = valueIssues.mapNotNull { (tick, issue) ->
                issue?.let { tick to it }
            }.toMap()

            return if (actualValueIssues.isNotEmpty()) {
                MomentIssue(valueIssues = actualValueIssues)
            } else null
        }
    }

    override fun print(printer: Printer) {
        valueIssues.entries.sortedBy { (tick, _) -> tick }.forEach { (tick, issue) ->
            printer.println("Sampled at tick ${tick.t}:")
            issue.print(printer.withIncreasedIndentation())
        }
    }
}

/**
 * Either [sampleIssue] or [newValuesIssue] should not be null
 */
internal class CellIssue private constructor(
    private val sampleIssue: MomentIssue?,
    private val newValuesIssue: EventStreamIssue?,
) : Issue() {
    init {
        if (sampleIssue == null && newValuesIssue == null) throw IllegalArgumentException()
    }

    override fun print(printer: Printer) {
        printer.println("Cell issue:")

        val indentedPrinter = printer.withIncreasedIndentation()

        if (sampleIssue != null) {
            indentedPrinter.println("sample() issues:")
            sampleIssue.print(indentedPrinter.withIncreasedIndentation())
        }

        if (newValuesIssue != null) {
            indentedPrinter.println("newValues issues:")
            newValuesIssue.print(indentedPrinter.withIncreasedIndentation())
        }
    }

    companion object {
        fun <A> validate(
            cellSpec: EffectiveCellSpec<A>,
            sampleIssues: Map<Tick, Issue?>,
            newValues: List<EventOccurrenceDesc<A>>,
        ): CellIssue? {
            val sampleIssue = MomentIssue.validate(
                momentSpec = cellSpec.sampleSpec,
                valueIssues = sampleIssues
            )

            val newValuesIssue = EventStreamIssue.validate(
                streamSpec = cellSpec.newValuesSpec,
                events = newValues,
            )

            return if (newValuesIssue != null || sampleIssue != null) {
                CellIssue(
                    sampleIssue = sampleIssue,
                    newValuesIssue = newValuesIssue,
                )
            } else null
        }
    }
}
