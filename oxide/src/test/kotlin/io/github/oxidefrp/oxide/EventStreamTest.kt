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
            ?: throw AssertionError("Received more than one event")

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
            expected = source.vertex.referenceCount,
            actual = 1,
        )

        source.emit(1)

        verifier.verifyReceivedEvent(expected = "2")

        source.emit(2)

        verifier.verifyReceivedEvent(expected = "4")

        source.emit(3)

        verifier.verifyReceivedEvent(expected = "6")

        verifier.dispose()

        assertEquals(
            expected = source.vertex.referenceCount,
            actual = 0,
        )
    }

    @Test
    fun testFilter() {
        val source = EventEmitter<Int>()

        val verifier = EventStreamVerifier(
            stream = source.filter { it % 2 == 0 },
        )

        assertEquals(
            expected = source.vertex.referenceCount,
            actual = 1,
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
            expected = source.vertex.referenceCount,
            actual = 0,
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
            expected = source1.vertex.referenceCount,
            actual = 1,
        )

        assertEquals(
            expected = source2.vertex.referenceCount,
            actual = 1,
        )

        source1.emit("X")

        verifier.verifyReceivedEvent("X")

        source2.emit("Y")

        verifier.verifyReceivedEvent("Y")

        verifier.dispose()

        assertEquals(
            expected = source1.vertex.referenceCount,
            actual = 0,
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
            expected = source.vertex.referenceCount,
            actual = 2,
        )

        source.emit(2)

        verifier.verifyReceivedEvent("2+4")

        verifier.dispose()

        assertEquals(
            expected = source.vertex.referenceCount,
            actual = 0,
        )
    }
}
