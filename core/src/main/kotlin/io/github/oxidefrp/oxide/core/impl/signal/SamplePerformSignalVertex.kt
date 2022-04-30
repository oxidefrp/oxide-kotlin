package io.github.oxidefrp.oxide.core.impl.signal

import io.github.oxidefrp.oxide.core.Io
import io.github.oxidefrp.oxide.core.Signal
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class SamplePerformSignalVertex<A>(
    private val signal: SignalVertex<Io<Signal<Io<A>>>>,
) : SignalVertex<Io<A>>() {
    override fun pullCurrentValue(transaction: Transaction): Io<A> {
        val outerIo = signal.pullCurrentValue(transaction = transaction)

        // FIXME: The outer I/O is externally performed, although there's no
        //        guarantee that the inner I/O will ever be performed
        val innerSignal = outerIo.performExternally()

        val innerIo = innerSignal.vertex.pullCurrentValue(transaction = transaction)

        return  innerIo
    }
}
