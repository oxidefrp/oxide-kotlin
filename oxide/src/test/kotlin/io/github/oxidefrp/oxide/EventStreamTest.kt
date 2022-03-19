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
            ?: throw AssertionError("Expected a single received event <$expected>, actual received events <$receivedEvents>")

        assertEquals(
            expected = expected,
            actual = singleStoredEvent,
            message = "Expected a single received event <$expected>, actual received event <$singleStoredEvent>."
        )

        receivedEvents.clear()
    }

    fun verifyNoReceivedEvents() {
        assertTrue(
            actual = receivedEvents.isEmpty(),
            message = "Expected no received events, actual received events <$receivedEvents>."
        )

        receivedEvents.clear()
    }

    fun dispose() {
        subscription.cancel()
    }
}

class EventStreamTest {
    @Test
    fun testMap() {
        val source = EventEmitter<Int>()

        val verifier = EventStreamVerifier(
            stream = source.map { "${it * 2}" },
        )

        assertEquals(
            expected = source.referenceCount,
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
            expected = source.referenceCount,
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
            expected = source.referenceCount,
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
            expected = source.referenceCount,
            actual = 0,
        )
    }
}
