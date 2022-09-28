package io.github.oxidefrp.oxide.core.test_framework

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
