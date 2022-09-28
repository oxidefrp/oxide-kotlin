package io.github.oxidefrp.core.test_framework.shared

inline fun <E, R : Comparable<R>> List<E>.isMonotonicallyIncreasingBy(crossinline selector: (E) -> R): Boolean =
    zipWithNext { a, b -> selector(a) < selector(b) }.none { isIncreasing -> !isIncreasing }

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
