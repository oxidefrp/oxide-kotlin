package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.moment.ApplyMomentVertex
import io.github.oxidefrp.core.impl.moment.MapMomentVertex
import io.github.oxidefrp.core.impl.moment.MomentVertex
import io.github.oxidefrp.core.impl.moment.PullMomentVertex
import io.github.oxidefrp.core.impl.moment.PureMomentVertex
import io.github.oxidefrp.core.impl.moment.map

// Idea: Create a Moment -> LazyMoment hierarchy with a `buildVertex(transaction)` method?
abstract class Moment<out A> {
    companion object {
        fun <A> pure(value: A): Moment<A> =
            object : Moment<A>() {
                override val vertex: MomentVertex<A> =
                    PureMomentVertex(value = value)
            }

        fun <A, B> apply(
            function: Moment<(A) -> B>,
            argument: Moment<A>,
        ): Moment<B> =
            object : Moment<B>() {
                override val vertex: MomentVertex<B> by lazy {
                    ApplyMomentVertex(
                        function = function.vertex,
                        argument = argument.vertex,
                    )
                }
            }

        fun <A> pull(
            moment: Moment<Moment<A>>,
        ): Moment<A> = object : Moment<A>() {
            override val vertex: MomentVertex<A> by lazy {
                PullMomentVertex(
                    source = moment.vertex.map { it.vertex },
                )
            }
        }

        fun <A, B> map1(
            sa: Moment<A>,
            f: (a: A) -> B,
        ): Moment<B> {
            fun g(a: A) = f(a)

            return sa.map(::g)
        }

        fun <A, B, C> map2(
            sa: Moment<A>,
            sb: Moment<B>,
            f: (a: A, b: B) -> C,
        ): Moment<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                sa.map(::g),
                sb,
            )
        }

        fun <A, B, C, D> map3(
            sa: Moment<A>,
            sb: Moment<B>,
            sc: Moment<C>,
            f: (a: A, b: B, c: C) -> D,
        ): Moment<D> {
            fun g(a: A) = fun(b: B) = fun(c: C) = f(a, b, c)

            return apply(
                apply(
                    sa.map(::g),
                    sb,
                ),
                sc,
            )
        }

        fun <A, B, C, D, E> map4(
            sa: Moment<A>,
            sb: Moment<B>,
            sc: Moment<C>,
            sd: Moment<D>,
            f: (a: A, b: B, c: C, d: D) -> E,
        ): Moment<E> {
            fun g(a: A) = fun(b: B) = fun(c: C) = fun(d: D) = f(a, b, c, d)

            return apply(
                apply(
                    apply(
                        sa.map(::g),
                        sb,
                    ),
                    sc,
                ),
                sd,
            )
        }
    }

    internal abstract val vertex: MomentVertex<A>

    fun <B> map(transform: (A) -> B): Moment<B> =
        object : Moment<B>() {
            override val vertex: MomentVertex<B> by lazy {
                MapMomentVertex(
                    source = this@Moment.vertex,
                    transform = transform,
                )
            }
        }

    fun pullExternally(): A = Transaction.wrap {
        vertex.computeCurrentValue(transaction = it)
    }

    internal fun pullDirectly(transaction: Transaction): A =
        vertex.computeCurrentValue(transaction = transaction)
}

fun <A, B> Moment<A>.pullOf(
    transform: (A) -> Moment<B>,
): Moment<B> =
    Moment.pull(map(transform))
