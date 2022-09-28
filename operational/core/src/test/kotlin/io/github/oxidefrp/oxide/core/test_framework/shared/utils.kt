package io.github.oxidefrp.oxide.core.test_framework.shared

inline fun <E, R : Comparable<R>> List<E>.isMonotonicallyIncreasingBy(crossinline selector: (E) -> R): Boolean =
    zipWithNext { a, b -> selector(a) < selector(b) }.none { isIncreasing -> !isIncreasing }
