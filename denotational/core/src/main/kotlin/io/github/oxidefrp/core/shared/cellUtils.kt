package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream

fun <A, B> Cell.Companion.unzip2(
    cell: Cell<Pair<A, B>>,
): Pair<Cell<A>, Cell<B>> = Pair(
    cell.map { it.first },
    cell.map { it.second },
)

fun <A, B> Cell<A>.divertEarlyOf(
    transform: (A) -> EventStream<B>,
): EventStream<B> = Cell.divertEarly(map(transform))
