package io.github.oxidefrp.oxide

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class SignalVerifier<A> {
    private var preparedValue: Option<A> = None()

    private var sampleCount = 0

    fun prepareValue(value: A) {
        preparedValue = Some(value)
        sampleCount = 0
    }

    fun verifyValueWasSampled() {
        assertEquals(
            expected = 1,
            actual = sampleCount,
            message = "Value was not sampled",
        )

        preparedValue = None()
        sampleCount = 0
    }

    fun verifyValueWasNotSampled() {
        assertEquals(
            expected = 0,
            actual = sampleCount,
            message = "Value was not supposed to be sampled",
        )

        preparedValue = None()
        sampleCount = 0
    }

    val signal: Signal<A> = Signal.source {
        sampleCount += 1
        preparedValue.getOrElse { throw IllegalStateException("There's no prepared value") }
    }
}

class SignalTest {
    @Test
    fun testConstant() {
        val streamInput = EventEmitter<Unit>()

        val signal = Signal.constant(8)

        val verifier = EventStreamVerifier(
            stream = streamInput.probe(signal),
        )

        streamInput.emit(Unit)

        verifier.verifyReceivedEvent(expected = 8)

        streamInput.emit(Unit)

        verifier.verifyReceivedEvent(expected = 8)
    }

    @Test
    fun testSource() {
        val streamInput = EventEmitter<Unit>()

        var preparedValue = "A"

        var sampleCount = 0

        val sourceSignal = Signal.source {
            sampleCount += 1
            preparedValue
        }

        val verifier = EventStreamVerifier(
            stream = streamInput.probe(sourceSignal),
        )

        streamInput.emit(Unit)

        assertEquals(
            expected = 1,
            actual = sampleCount,
        )

        sampleCount = 0

        verifier.verifyReceivedEvent(expected = "A")

        preparedValue = "B"

        streamInput.emit(Unit)

        assertEquals(
            expected = 1,
            actual = sampleCount,
        )

        sampleCount = 0

        verifier.verifyReceivedEvent(expected = "B")
    }

    @Test
    fun testMap() {
        val signalVerifier1 = SignalVerifier<Int>()

        val streamInput = EventEmitter<Unit>()

        val verifier = EventStreamVerifier(
            stream = streamInput.probe(
                signalVerifier1.signal.map { "${it * 3}" },
            ),
        )

        signalVerifier1.prepareValue(2)

        streamInput.emit(Unit)

        signalVerifier1.verifyValueWasSampled()

        verifier.verifyReceivedEvent(expected = "6")

        signalVerifier1.prepareValue(4)

        streamInput.emit(Unit)

        signalVerifier1.verifyValueWasSampled()

        verifier.verifyReceivedEvent(expected = "12")
    }
}
