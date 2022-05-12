package io.github.oxidefrp.oxide.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MomentOperatorsUnitTests {
    @Test
    fun testPure() {
        val moment = Moment.pure(8)

        assertEquals(
            expected = 8,
            actual = moment.pullExternally(),
        )
    }

    @Test
    fun testMap() {
        val signalVerifier = SignalVerifier<Int>()

        val signal = signalVerifier.signal

        val result = signal.sample().map { "&$it" }

        signalVerifier.prepareValue(2)

        assertEquals(
            expected = "&2",
            actual = result.pullExternally(),
        )

        signalVerifier.verifyValueWasSampled()


        signalVerifier.prepareValue(3)

        assertEquals(
            expected = "&3",
            actual = result.pullExternally(),
        )

        signalVerifier.verifyValueWasSampled()
    }

    @Test
    fun testApply() {
        val functionSignalVerifier = SignalVerifier<(Int) -> String>()

        val argumentSignalVerifier = SignalVerifier<Int>()

        val result = Moment.apply(
            function = functionSignalVerifier.signal.sample(),
            argument = argumentSignalVerifier.signal.sample(),
        )

        functionSignalVerifier.prepareValue(
            fun(i: Int): String = "#$i"
        )

        argumentSignalVerifier.prepareValue(1)

        assertEquals(
            expected = "#1",
            actual = result.pullExternally(),
        )

        functionSignalVerifier.prepareValue(
            fun(i: Int): String = "@$i"
        )

        argumentSignalVerifier.prepareValue(2)

        assertEquals(
            expected = "@2",
            actual = result.pullExternally(),
        )
    }

    @Test
    fun testPull() {
        val signal1Verifier = SignalVerifier<Boolean>()
        val signal2Verifier = SignalVerifier<Int>()
        val signal3Verifier = SignalVerifier<Int>()

        val result = signal1Verifier.signal.sample().pullOf {
            if (it) signal2Verifier.signal.sample()
            else signal3Verifier.signal.sample()
        }

        signal1Verifier.prepareValue(true)
        signal2Verifier.prepareValue(-1)
        signal3Verifier.prepareValue(1)

        assertEquals(
            expected = -1,
            actual = result.pullExternally(),
        )

        signal1Verifier.verifyValueWasSampled()
        signal2Verifier.verifyValueWasSampled()
        signal3Verifier.verifyValueWasNotSampled()

        signal1Verifier.prepareValue(false)
        signal2Verifier.prepareValue(-2)
        signal3Verifier.prepareValue(2)

        assertEquals(
            expected = 2,
            actual = result.pullExternally(),
        )

        signal1Verifier.verifyValueWasSampled()
        signal2Verifier.verifyValueWasNotSampled()
        signal3Verifier.verifyValueWasSampled()
    }
}
