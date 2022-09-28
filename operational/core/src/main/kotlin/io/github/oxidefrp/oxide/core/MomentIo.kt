package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.moment.ApplyMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.IoMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.MapMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.MomentIoMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.MomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.PullMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.PureMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.map

abstract class MomentIo<out A> {
    companion object {
        fun <A> pure(value: A): MomentIo<A> =
            object : MomentIo<A>() {
                override val vertex: MomentVertex<A> =
                    PureMomentVertex(value = value)
            }

        fun <A> lift(moment: Moment<Io<A>>): MomentIo<A> = object : MomentIo<A>() {
            override val vertex: MomentVertex<A> = MomentIoMomentVertex(
                moment = moment.vertex,
            )
        }

        fun <A> lift(moment: Moment<A>): MomentIo<A> = object : MomentIo<A>() {
            override val vertex: MomentVertex<A> = moment.vertex
        }

        fun <A> lift(io: Io<A>): MomentIo<A> = object : MomentIo<A>() {
            override val vertex: MomentVertex<A> = IoMomentVertex(io = io)
        }

        fun <A> perform(
            moment: MomentIo<MomentIo<A>>,
        ): MomentIo<A> = object : MomentIo<A>() {
            override val vertex: MomentVertex<A> = PullMomentVertex(
                source = moment.vertex.map { it.vertex },
            )
        }

        fun <A, B> apply(
            function: MomentIo<(A) -> B>,
            argument: MomentIo<A>,
        ): MomentIo<B> = object : MomentIo<B>() {
            override val vertex: MomentVertex<B> = ApplyMomentVertex(
                function = function.vertex,
                argument = argument.vertex,
            )
        }

        fun <A, B> map1(
            sa: MomentIo<A>,
            f: (a: A) -> B,
        ): MomentIo<B> {
            fun g(a: A) = f(a)

            return sa.map(::g)
        }

        fun <A, B, C> map2(
            sa: MomentIo<A>,
            sb: MomentIo<B>,
            f: (a: A, b: B) -> C,
        ): MomentIo<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                sa.map(::g),
                sb,
            )
        }

        fun <A, B, C, D> map3(
            sa: MomentIo<A>,
            sb: MomentIo<B>,
            sc: MomentIo<C>,
            f: (a: A, b: B, c: C) -> D,
        ): MomentIo<D> {
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
            sa: MomentIo<A>,
            sb: MomentIo<B>,
            sc: MomentIo<C>,
            sd: MomentIo<D>,
            f: (a: A, b: B, c: C, d: D) -> E,
        ): MomentIo<E> {
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

    fun <B> map(transform: (A) -> B): MomentIo<B> = object : MomentIo<B>() {
        override val vertex: MomentVertex<B> = MapMomentVertex(
            source = this@MomentIo.vertex,
            transform = transform,
        )
    }

    fun performExternally(): A = Transaction.wrap {
        vertex.computeCurrentValue(transaction = it)
    }
}

fun <A, B> MomentIo<A>.performOf(
    transform: (A) -> MomentIo<B>,
): MomentIo<B> =
    MomentIo.perform(map(transform))
