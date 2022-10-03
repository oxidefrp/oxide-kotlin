package io.github.oxidefrp.core.test_utils

import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.Time

/// Signal defined based on a time function table
fun <A> tableSignal(
    table: Map<Time, A>,
): Signal<A> = object : Signal<A>() {
    override fun at(t: Time): A = table.getOrElse(t) {
        throw UnsupportedOperationException("There's no table entry for time $t")
    }
}
