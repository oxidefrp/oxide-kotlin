package io.github.oxidefrp.oxide.core.test_utils

import io.github.oxidefrp.oxide.core.Cell
import io.github.oxidefrp.oxide.core.EventStream

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
