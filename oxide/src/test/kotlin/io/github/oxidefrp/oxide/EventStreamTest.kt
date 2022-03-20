package io.github.oxidefrp.oxide

import kotlin.test.Test
import kotlin.test.assertEquals
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
            expected = expected,
            actual = singleStoredEvent,
            message = "Unexpected event received"
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
            actual = receivedEvents.isEmpty(),
            message = "Expected no received events"
        )

        receivedEvents.clear()
    }

    fun dispose() {
        subscription.cancel()
    }
}

class EventStreamTest {
    @Test
    fun testNever() {
        val stream = EventStream.never<Int>()

        stream.subscribe { }.cancel()
    }

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

        source.emit(1)

        verifier.verifyReceivedEvent(expected = "2")

        source.emit(2)

        verifier.verifyReceivedEvent(expected = "4")

        source.emit(3)

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

        source.emit(1)

        verifier.verifyNoReceivedEvents()

        source.emit(2)

        verifier.verifyReceivedEvent(2)

        source.emit(3)

        verifier.verifyNoReceivedEvents()

        source.emit(4)

        verifier.verifyReceivedEvent(4)

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source.vertex.referenceCount,
        )
    }

    @Test
    fun testMergeWithNonInstantaneous() {
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

        source1.emit("X")

        verifier.verifyReceivedEvent("X")

        source2.emit("Y")

        verifier.verifyReceivedEvent("Y")

        verifier.dispose()

        assertEquals(
            expected = 0,
            actual = source1.vertex.referenceCount,
        )
    }

    @Test
    fun testMergeWithInstantaneous() {
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

        source.emit(2)

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

        streamInput.emit('a')

        signalInput.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = "a-2")

        signalInput.prepareValue(4)

        streamInput.emit('b')

        signalInput.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = "b-4")

        outputVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = streamInput.vertex.referenceCount,
        )
    }

    @Test
    fun testProbeEach() {
        val signalInput1 = SignalVerifier<Int>()

        val signalInput2 = SignalVerifier<Int>()

        val streamInput = EventEmitter<Signal<Int>>()

        val outputVerifier = EventStreamVerifier(
            stream = EventStream.probeEach(streamInput),
        )

        assertEquals(
            expected = 1,
            actual = streamInput.vertex.referenceCount,
        )

        signalInput1.prepareValue(2)
        signalInput2.prepareValue(-2)

        streamInput.emit(signalInput1.signal)

        signalInput1.verifyValueWasSampled()
        signalInput2.verifyValueWasNotSampled()

        outputVerifier.verifyReceivedEvent(expected = 2)

        signalInput1.prepareValue(3)
        signalInput2.prepareValue(-3)

        streamInput.emit(signalInput2.signal)

        signalInput1.verifyValueWasNotSampled()
        signalInput2.verifyValueWasSampled()

        outputVerifier.verifyReceivedEvent(expected = -3)

        outputVerifier.dispose()

        assertEquals(
            expected = 0,
            actual = streamInput.vertex.referenceCount,
        )
    }
}
