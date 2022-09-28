package io.github.oxidefrp.semantics

import java.lang.IllegalArgumentException

data class Time(
    val t: Double,
) : Comparable<Time> {
    companion object {
        val zero = Time(t = 0.0)
    }

    init {
        if (!t.isFinite()) throw IllegalArgumentException("Time can't be expressed as an non-finite number (got: $t)")
        if (t < 0) throw IllegalArgumentException("Time can't be negative")
    }

    override operator fun compareTo(other: Time): Int = t.compareTo(other.t)
}

abstract class Signal<out A> {
    companion object {
        fun <A> constant(value: A): Signal<A> =
            object : Signal<A>() {
                override fun at(t: Time): A = value
            }

        fun <A> sample(signal: Signal<Signal<A>>): Signal<A> =
            object : Signal<A>() {
                override fun at(t: Time): A = signal.at(t).at(t)
            }

        fun <A> source(sampleExternal: () -> A): Signal<A> =
            throw NotImplementedError("Operational operator has no semantic implementation")

        fun <A, B> apply(
            function: Signal<(A) -> B>,
            argument: Signal<A>,
        ): Signal<B> =
            object : Signal<B>() {
                override fun at(t: Time): B = function.at(t)(argument.at(t))
            }

        fun <A, B> map1(
            sa: Signal<A>,
            f: (a: A) -> B,
        ): Signal<B> {
            fun g(a: A) = f(a)

            return sa.map(::g)
        }

        fun <A, B, C> map2(
            sa: Signal<A>,
            sb: Signal<B>,
            f: (a: A, b: B) -> C,
        ): Signal<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                sa.map(::g),
                sb,
            )
        }

        fun <A, B, C, D> map3(
            sa: Signal<A>,
            sb: Signal<B>,
            sc: Signal<C>,
            f: (a: A, b: B, c: C) -> D,
        ): Signal<D> {
            fun g(a: A) = fun(b: B) = fun(c: C) = f(a, b, c)

            return apply(
                apply(
                    sa.map(::g),
                    sb,
                ),
                sc,
            )
        }
    }

    abstract fun at(t: Time): A

    fun sample(): Moment<A> = object : Moment<A>() {
        override fun pullDirectly(t: Time): A = at(t)
    }

    fun <B> map(transform: (A) -> B): Signal<B> =
        object : Signal<B>() {
            override fun at(t: Time): B {
                return transform(this@Signal.at(t))
            }
        }
}

fun <A, B> Signal<A>.sampleOf(transform: (A) -> Signal<B>): Signal<B> =
    Signal.sample(map(transform))
