package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.impl.None
import io.github.oxidefrp.oxide.core.impl.Option
import io.github.oxidefrp.oxide.core.impl.Some
import io.github.oxidefrp.oxide.core.impl.getOrElse
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

class SignalOperatorsUnitTests {
    @Test
    fun testConstant() {
        val streamInput = EventEmitter<Unit>()

        val signal = Signal.constant(8)

        val verifier = EventStreamVerifier(
            stream = streamInput.probe(signal),
        )

        streamInput.emitExternally(Unit)

        verifier.verifyReceivedEvent(expected = 8)

        streamInput.emitExternally(Unit)

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

        streamInput.emitExternally(Unit)

        assertEquals(
            expected = 1,
            actual = sampleCount,
        )

        sampleCount = 0

        verifier.verifyReceivedEvent(expected = "A")

        preparedValue = "B"

        streamInput.emitExternally(Unit)

        assertEquals(
            expected = 1,
            actual = sampleCount,
        )

        sampleCount = 0

        verifier.verifyReceivedEvent(expected = "B")
    }

    @Test
    fun testSampleInstanceMethod() {
        val signalVerifier = SignalVerifier<Int>()

        val signal = signalVerifier.signal

        signalVerifier.prepareValue(2)

        assertEquals(
            expected = 2,
            actual = signal.sample().pullExternally(),
        )

        signalVerifier.verifyValueWasSampled()

        signalVerifier.prepareValue(3)

        assertEquals(
            expected = 3,
            actual = signal.sample().pullExternally(),
        )

        signalVerifier.verifyValueWasSampled()
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

        streamInput.emitExternally(Unit)

        signalVerifier1.verifyValueWasSampled()

        verifier.verifyReceivedEvent(expected = "6")

        signalVerifier1.prepareValue(4)

        streamInput.emitExternally(Unit)

        signalVerifier1.verifyValueWasSampled()

        verifier.verifyReceivedEvent(expected = "12")
    }

    @Test
    fun testApply() {
        val functionSignal = SignalVerifier<(Int) -> String>()

        val argumentSignal = SignalVerifier<Int>()

        val result = Signal.apply(
            function = functionSignal.signal,
            argument = argumentSignal.signal,
        )

        functionSignal.prepareValue(
            fun(i: Int): String = "#$i"
        )

        argumentSignal.prepareValue(1)

        assertEquals(
            expected = "#1",
            actual = result.sampleExternally(),
        )

        functionSignal.prepareValue(
            fun(i: Int): String = "@$i"
        )

        argumentSignal.prepareValue(2)

        assertEquals(
            expected = "@2",
            actual = result.sampleExternally(),
        )
    }

    @Test
    fun testSample() {
        val innerSignal1 = SignalVerifier<Int>()
        val innerSignal2 = SignalVerifier<Int>()

        val outerSignal = SignalVerifier<Signal<Int>>()

        outerSignal.prepareValue(innerSignal1.signal)

        val result = Signal.sample(
            signal = outerSignal.signal,
        )

        innerSignal1.prepareValue(1)
        innerSignal2.prepareValue(-1)

        assertEquals(
            expected = 1,
            actual = result.sampleExternally(),
        )

        outerSignal.prepareValue(innerSignal2.signal)

        assertEquals(
            expected = -1,
            actual = result.sampleExternally(),
        )

        innerSignal2.prepareValue(-2)

        assertEquals(
            expected = -2,
            actual = result.sampleExternally(),
        )
    }
}
