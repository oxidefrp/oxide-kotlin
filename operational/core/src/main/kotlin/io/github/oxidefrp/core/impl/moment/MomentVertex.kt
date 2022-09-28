package io.github.oxidefrp.core.impl.moment

import io.github.oxidefrp.core.impl.Transaction

internal abstract class MomentVertex<out A> {
    abstract fun computeCurrentValue(transaction: Transaction): A
}

internal fun <A, B> MomentVertex<A>.map(transform: (A) -> B): MomentVertex<B> =
    object : MomentVertex<B>() {
        override fun computeCurrentValue(transaction: Transaction): B =
            transform(this@map.computeCurrentValue(transaction = transaction))
    }
