package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.mergeWith

fun <A, B> EventStream.Companion.unzip2(
    stream: EventStream<Pair<A, B>>,
): Pair<EventStream<A>, EventStream<B>> = Pair(
    stream.map { it.first },
    stream.map { it.second },
)

fun <A> EventStream<A>.orElse(
    other: EventStream<A>,
): EventStream<A> = mergeWith(other) { l, _ -> l }
