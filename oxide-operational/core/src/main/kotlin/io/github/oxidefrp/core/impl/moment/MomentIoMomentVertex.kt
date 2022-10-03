package io.github.oxidefrp.core.impl.moment

import io.github.oxidefrp.core.Io
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.impl.Transaction

internal class MomentIoMomentVertex<A>(
    private val moment: MomentVertex<Io<A>>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        moment.computeCurrentValue(transaction = transaction).performExternally()
}
