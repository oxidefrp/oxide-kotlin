package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState
import io.github.oxidefrp.core.shared.State
import io.github.oxidefrp.core.shared.enter
import io.github.oxidefrp.core.shared.pullEnter

abstract class Moment<out A> {
    companion object {
        fun <A> pure(value: A): Moment<A> =
            object : Moment<A>() {
                override fun pullDirectly(t: Time): A = value
            }

        fun <A> pull(moment: Moment<Moment<A>>): Moment<A> =
            object : Moment<A>() {
                override fun pullDirectly(t: Time): A = moment.pullDirectly(t).pullDirectly(t)
            }

        fun <A, B> apply(
            function: Moment<(A) -> B>,
            argument: Moment<A>,
        ): Moment<B> =
            object : Moment<B>() {
                override fun pullDirectly(t: Time): B =
                    function.pullDirectly(t)(argument.pullDirectly(t))
            }

        fun <A, B> map1(
            ma: Moment<A>,
            f: (a: A) -> B,
        ): Moment<B> {
            fun g(a: A) = f(a)

            return ma.map(::g)
        }

        fun <A, B, C> map2(
            ma: Moment<A>,
            mb: Moment<B>,
            f: (a: A, b: B) -> C,
        ): Moment<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                ma.map(::g),
                mb,
            )
        }

        fun <A, B, C, D> map3(
            ma: Moment<A>,
            mb: Moment<B>,
            mc: Moment<C>,
            f: (a: A, b: B, c: C) -> D,
        ): Moment<D> {
            fun g(a: A) = fun(b: B) = fun(c: C) = f(a, b, c)

            return apply(
                apply(
                    ma.map(::g),
                    mb,
                ),
                mc,
            )
        }
    }

    private fun asSignal(): Signal<A> = object : Signal<A>() {
        override fun at(t: Time): A = pullDirectly(t)
    }

    abstract fun pullDirectly(t: Time): A

    fun <B> map(transform: (A) -> B): Moment<B> =
        object : Moment<B>() {
            override fun pullDirectly(t: Time): B {
                return transform(this@Moment.pullDirectly(t))
            }
        }

    fun <S, B> pullEnterOf(transform: (A) -> MomentState<S, B>): MomentState<S, B> =
        pullEnter(map(transform))

    fun pullExternally(): A =
        throw NotImplementedError("Operational operator has no semantic implementation")
}
