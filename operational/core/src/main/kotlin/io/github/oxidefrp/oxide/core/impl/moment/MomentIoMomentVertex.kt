package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.Io
import io.github.oxidefrp.oxide.core.Moment
import io.github.oxidefrp.oxide.core.impl.Transaction

internal class MomentIoMomentVertex<A>(
    private val moment: MomentVertex<Io<A>>,
) : MomentVertex<A>() {
    override fun computeCurrentValue(transaction: Transaction): A =
        moment.computeCurrentValue(transaction = transaction).performExternally()
}
