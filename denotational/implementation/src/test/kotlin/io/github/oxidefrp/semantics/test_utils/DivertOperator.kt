package io.github.oxidefrp.semantics.test_utils

import io.github.oxidefrp.semantics.Cell
import io.github.oxidefrp.semantics.EventStream

internal sealed interface DivertOperator {
    val divertedStreamName: String

    fun <A> divert(diversionCell: Cell<EventStream<A>>): EventStream<A>

    object Divert : DivertOperator {
        override val divertedStreamName: String = "Diverted stream"

        override fun <A> divert(
            diversionCell: Cell<EventStream<A>>,
        ): EventStream<A> = Cell.divert(diversionCell)
    }

    object DivertEarly : DivertOperator {
        override val divertedStreamName: String = "Early-diverted stream"

        override fun <A> divert(
            diversionCell: Cell<EventStream<A>>,
        ): EventStream<A> = Cell.divertEarly(diversionCell)
    }
}
