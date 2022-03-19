package io.github.oxidefrp.oxide

sealed class Option<out A> {
    companion object {
        fun <A, B, C> map2(
            oa: Option<A>,
            ob: Option<B>,
            combine: (A, B) -> C,
        ): Option<C> {
            val sa = oa as? Some<A> ?: return None()
            val sb = ob as? Some<B> ?: return None()

            return Some(combine(sa.value, sb.value))
        }
    }

    abstract fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B

    fun isNone(): Boolean = fold(
        ifNone = { true },
        ifSome = { false },
    )

    fun isSome(): Boolean = fold(
        ifNone = { false },
        ifSome = { true },
    )

    fun ifSome(listener: (A) -> Unit): Unit = fold(
        ifNone = { },
        ifSome = listener,
    )

    fun <B> map(transform: (A) -> B): Option<B> = fold(
        ifNone = { None() },
        ifSome = { Some(transform(it)) },
    )

    fun filter(predicate: (A) -> Boolean): Option<A> = fold(
        ifNone = { None() },
        ifSome = { if (predicate(it)) Some(it) else None() },
    )
}

fun <A> Option<A>.orElse(defaultValue: () -> Option<A>): Option<A> = fold(
    ifNone = { defaultValue() },
    ifSome = { Some(it) },
)

data class Some<A>(val value: A) : Option<A>() {
    override fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B =
        ifSome(value)
}

class None<A> : Option<A>() {
    override fun <B> fold(ifNone: () -> B, ifSome: (A) -> B): B =
        ifNone()
}
