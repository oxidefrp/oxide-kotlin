package io.github.oxidefrp.core

object IterableUtils {
    fun <A> pull(iterable: Iterable<Moment<A>>): Moment<List<A>> {
        val (headMoment, tailMoments) = iterable.cutOff() ?: return Moment.pure(emptyList())

        return headMoment.pullOf { head ->
            pull(tailMoments).map { tail ->
                listOf(head) + tail
            }
        }
    }
}

fun <T, R> Iterable<T>.pullOf(transform: (T) -> Moment<R>): Moment<List<R>> =
    IterableUtils.pull(map(transform))

data class CutList<out A>(
    val head: A,
    val tail: List<A>,
)

fun <T> Iterable<T>.cutOff(): CutList<T>? =
    firstOrNull()?.let {
        CutList(
            head = it,
            tail = drop(1),
        )
    }
