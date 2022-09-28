package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.event_stream.Subscription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EventStreamVerifier<A>(
    stream: EventStream<A>,
) {
    private val receivedEvents = mutableListOf<A>()

    private val subscription = stream.subscribe { event ->
        receivedEvents.add(event)
    }

    fun verifyReceivedEvent(expected: A) {
        val singleStoredEvent = receivedEvents.singleOrNull()
            ?: throw AssertionError("Received more than one event or no events ($receivedEvents)")

        assertEquals(
            expected = expected, actual = singleStoredEvent, message = "Unexpected event received"
        )

        receivedEvents.clear()
    }

    fun verifyReceivedEventsUnordered(expected: Set<A>) {
        val receivedEventsSet = receivedEvents.toSet()

        assertEquals(
            expected = expected,
            actual = receivedEventsSet,
            message = "Unexpected events received",
        )

        receivedEvents.clear()
    }

    fun verifyNoReceivedEvents() {
        assertTrue(
            actual = receivedEvents.isEmpty(), message = "Expected no received events"
        )

        receivedEvents.clear()
    }

    fun dispose() {
        subscription.cancel()
    }
}

class EventStreamOperatorsOperationalTests {
    @Test
    fun testSource() {
        var emitOrNull: ((Int) -> Unit)? = null

        var isCancelled = false

        val stream = EventStream.source<Int> {
            if (emitOrNull != null) throw IllegalStateException("Emit function is already remembered")

            emitOrNull = it

            object : Subscription {
                override fun cancel() {
                    if (isCancelled) throw IllegalStateException("Subscription is already cancelled")
                    isCancelled = true
                }
            }
        }

        assertEquals(
            expected = null,
            actual = emitOrNull,
        )

        val verifier = EventStreamVerifier(
            stream = stream,
        )

        val emit = assertNotNull(actual = emitOrNull)

        verifier.verifyNoReceivedEvents()

        emit(10)

        verifier.verifyReceivedEvent(10)

        emit(20)

        verifier.verifyReceivedEvent(20)

        emit(30)

        verifier.verifyReceivedEvent(30)

        assertEquals(
            expected = false,
            actual = isCancelled,
        )

        verifier.dispose()

        assertEquals(
            expected = true,
            actual = isCancelled,
        )
    }

    @Test
    fun testNever() {
        val stream = EventStream.never<Int>()

        stream.subscribe { }.cancel()
    }

    // TODO: Remove unnecessary operational tests when all semantics unit tests
    //       are ported
    @Test
    fun testMap() {
        val source = EventEmitter<Int>()

        val verifier = EventStreamVerifier(
            stream = source.map { "${it * 2}" },
        )

        assertEquals(
            expected = 1,
            actual = source.vertex.referenceCount,
        )

        source.emitExternally(1)

        verifier.verifyReceivedEvent(expected = "2")

        source.emitExternally(2)

        verifier.verifyReceivedEvent(expected = "4")

        source.emitExternally(3)

        verifier.verifyReceivedEvent(expected = "6")

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source.vertex.referenceCount,
        )
    }

    @Test
    fun testFilter() {
        val source = EventEmitter<Int>()

        val verifier = EventStreamVerifier(
            stream = source.filter { it % 2 == 0 },
        )

        assertEquals(
            expected = 1,
            actual = source.vertex.referenceCount,
        )

        source.emitExternally(1)

        verifier.verifyNoReceivedEvents()

        source.emitExternally(2)

        verifier.verifyReceivedEvent(2)

        source.emitExternally(3)

        verifier.verifyNoReceivedEvents()

        source.emitExternally(4)

        verifier.verifyReceivedEvent(4)

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source.vertex.referenceCount,
        )
    }

    @Test
    fun testMergeWithNonSimultaneous() {
        val source1 = EventEmitter<String>()

        val source2 = EventEmitter<String>()

        val verifier = EventStreamVerifier(
            stream = source1.mergeWith(source2) { a, b -> "$a+$b" },
        )

        assertEquals(
            expected = 1,
            actual = source1.vertex.referenceCount,
        )

        assertEquals(
            expected = 1,
            actual = source2.vertex.referenceCount,
        )

        source1.emitExternally("X")

        verifier.verifyReceivedEvent("X")

        source2.emitExternally("Y")

        verifier.verifyReceivedEvent("Y")

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source1.vertex.referenceCount,
        )
    }

    @Test
    fun testMergeWithSimultaneous() {
        val source = EventEmitter<Int>()

        val branch1 = source.map { "$it" }

        val branch2 = source.map { "${it * 2}" }

        val verifier = EventStreamVerifier(
            stream = branch1.mergeWith(branch2) { a, b -> "$a+$b" },
        )

        assertEquals(
            expected = 2,
            actual = source.vertex.referenceCount,
        )

        source.emitExternally(2)

        verifier.verifyReceivedEvent("2+4")

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source.vertex.referenceCount,
        )
    }

    @Test
    fun testProbe() {
        val streamInput = EventEmitter<Char>()

        val signalInput = SignalVerifier<Int>()

        val outputVerifier = EventStreamVerifier(
            stream = streamInput.probe(
                signalInput.signal,
            ) { i, c -> "$i-$c" },
        )

        assertEquals(
            expected = 1,
            actual = streamInput.vertex.referenceCount,
        )

        signalInput.prepareValue(2)

        streamInput.emitExternally('a')

        signalInput.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = "a-2")

        signalInput.prepareValue(4)

        streamInput.emitExternally('b')

        signalInput.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = "b-4")

        outputVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = streamInput.vertex.referenceCount,
        )
    }

    @Test
    fun testSample() {
        val signalInput1 = SignalVerifier<Int>()

        val signalInput2 = SignalVerifier<Int>()

        val streamInput = EventEmitter<Signal<Int>>()

        val outputVerifier = EventStreamVerifier(
            stream = EventStream.sample(streamInput),
        )

        assertEquals(
            expected = 1,
            actual = streamInput.vertex.referenceCount,
        )

        signalInput1.prepareValue(2)
        signalInput2.prepareValue(-2)

        streamInput.emitExternally(signalInput1.signal)

        signalInput1.verifyValueWasSampled()
        signalInput2.verifyValueWasNotSampled()

        outputVerifier.verifyReceivedEvent(expected = 2)

        signalInput1.prepareValue(3)
        signalInput2.prepareValue(-3)

        streamInput.emitExternally(signalInput2.signal)

        signalInput1.verifyValueWasNotSampled()
        signalInput2.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = -3)

        outputVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = streamInput.vertex.referenceCount,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testHold() = runTest {
        val steps = EventEmitter<Int>()

        assertEquals(
            expected = 0,
            actual = steps.vertex.referenceCount,
        )

        val result: Cell<Int> = steps.hold(1).pullExternally()

        assertEquals(
            expected = 1,
            actual = steps.vertex.referenceCount,
        )

        assertEquals(
            expected = 1,
            actual = result.value.sampleExternally(),
        )

        val outputVerifier = EventStreamVerifier(
            stream = result.changes,
        )

        assertEquals(
            expected = 1,
            actual = steps.vertex.referenceCount,
        )

        outputVerifier.verifyNoReceivedEvents()

        steps.emitExternally(2)

        assertEquals(
            expected = 2,
            actual = result.value.sampleExternally(),
        )

        outputVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 1,
                newValue = 2,
            ),
        )

        steps.emitExternally(3)

        assertEquals(
            expected = 3,
            actual = result.value.sampleExternally(),
        )

        outputVerifier.verifyReceivedEvent(
            expected = ValueChange(
                oldValue = 2,
                newValue = 3,
            ),
        )

        outputVerifier.dispose()

        assertEquals(
            expected = 3,
            actual = result.value.sampleExternally(),
        )

        assertEquals(
            expected = 1,
            actual = steps.vertex.referenceCount,
        )
    }

    object Divert {
        @Test
        fun testOccurrenceInner() {
            val sourceStream = EventEmitter<Int>()

            val source = Cell.constant(sourceStream)

            val result = Cell.divert(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = 1)
        }

        @Test
        fun testChangeOuter() {
            val oldStream = EventStream.never<Int>()
            val newStream = EventEmitter<Int>()

            val source = MutableCell(initialValue = oldStream)

            val result = Cell.divert(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            assertEquals(
                expected = 1,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 0,
                actual = newStream.vertex.referenceCount,
            )

            source.setValueExternally(newStream)

            assertEquals(
                expected = 0,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 1,
                actual = newStream.vertex.referenceCount,
            )

            newStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = 1)

            changesVerifier.dispose()

            assertEquals(
                expected = 0,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 0,
                actual = newStream.vertex.referenceCount,
            )
        }

        @Test
        fun testChangeOldStreamOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Int>()

            val source =
                sourceStream.map { EventStream.never<String>() }.hold(initialValue = sourceStream.map { "$it" })
                    .pullExternally()

            val result = Cell.divert(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = "1")
        }

        @Test
        fun testChangeNewStreamOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Int>()

            val source = sourceStream.map { sourceStream.map { "$it" } }.hold(initialValue = EventStream.never())
                .pullExternally()

            val result = Cell.divert(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyNoReceivedEvents()
        }

        @Test
        fun testChangeBothStreamsOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Char>()

            val source = sourceStream.map { sourceStream.map { it.toString() } }
                .hold(initialValue = sourceStream.map { it.uppercase() }).pullExternally()

            val result = Cell.divert(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally('a')

            changesVerifier.verifyReceivedEvent(expected = "A")
        }
    }

    object DivertEarly {
        @Test
        fun testOccurrenceInner() {
            val sourceStream = EventEmitter<Int>()

            val source = Cell.constant(sourceStream)

            val result = Cell.divertEarly(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = 1)
        }

        @Test
        fun testChangeOuter() {
            val oldStream = EventStream.never<Int>()
            val newStream = EventEmitter<Int>()

            val source = MutableCell(initialValue = oldStream)

            val result = Cell.divertEarly(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            assertEquals(
                expected = 1,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 0,
                actual = newStream.vertex.referenceCount,
            )

            source.setValueExternally(newStream)

            assertEquals(
                expected = 0,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 1,
                actual = newStream.vertex.referenceCount,
            )

            newStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = 1)

            changesVerifier.dispose()

            assertEquals(
                expected = 0,
                actual = oldStream.vertex.referenceCount,
            )

            assertEquals(
                expected = 0,
                actual = newStream.vertex.referenceCount,
            )
        }

        @Test
        fun testChangeOldStreamOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Int>()

            val source = sourceStream.map { EventStream.never<String>() }
                .hold(initialValue = sourceStream.map { "$it" })
                .pullExternally()

            val result = Cell.divertEarly(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyNoReceivedEvents()
        }

        @Test
        fun testChangeNewStreamOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Int>()

            val source = sourceStream.map { sourceStream.map { "$it" } }
                .hold(initialValue = EventStream.never())
                .pullExternally()

            val result = Cell.divertEarly(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally(1)

            changesVerifier.verifyReceivedEvent(expected = "1")
        }

        @Test
        fun testChangeBothStreamsOccurrenceSimultaneous() {
            val sourceStream = EventEmitter<Char>()

            val source = sourceStream.map { sourceStream.map { it.toString() } }
                .hold(initialValue = sourceStream.map { it.uppercase() }).pullExternally()

            val result = Cell.divertEarly(source)

            val changesVerifier = EventStreamVerifier(
                stream = result,
            )

            sourceStream.emitExternally('a')

            changesVerifier.verifyReceivedEvent(expected = "a")
        }
    }
}
