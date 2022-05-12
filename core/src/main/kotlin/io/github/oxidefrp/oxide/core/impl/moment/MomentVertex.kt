package io.github.oxidefrp.oxide.core.impl.moment

import io.github.oxidefrp.oxide.core.impl.Transaction

internal abstract class MomentVertex<out A> {
    abstract fun computeCurrentValue(transaction: Transaction): A
}
