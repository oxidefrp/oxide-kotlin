package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.signal.ApplySignalVertex
import io.github.oxidefrp.core.impl.signal.ConstantSignalVertex
import io.github.oxidefrp.core.impl.signal.MapSignalVertex
import io.github.oxidefrp.core.impl.signal.MomentSourceSignalVertex
import io.github.oxidefrp.core.impl.signal.SampleSignalVertex
import io.github.oxidefrp.core.impl.signal.SignalVertex
import io.github.oxidefrp.core.impl.signal.SourceSignalVertex
import io.github.oxidefrp.core.shared.Io

abstract class Signal<out A> {
    companion object {
        fun <A> constant(value: A): Signal<A> =
            object : Signal<A>() {
                override val vertex: SignalVertex<A> =
                    ConstantSignalVertex(value = value)
            }

        fun <A> sample(signal: Signal<Signal<A>>): Signal<A> =
            object : Signal<A>() {
                override val vertex: SignalVertex<A> =
                    SampleSignalVertex(signal = signal.vertex)
            }

        fun <A> source(sampleExternal: () -> A): Signal<A> =
            object : Signal<A>() {
                override val vertex: SignalVertex<A> =
                    SourceSignalVertex(sampleExternal = sampleExternal)
            }

        fun <A> source(moment: Moment<A>): Signal<A> =
            object : Signal<A>() {
                override val vertex: SignalVertex<A> =
                    MomentSourceSignalVertex(moment = moment)
            }

        fun <A, B> apply(
            function: Signal<(A) -> B>,
            argument: Signal<A>,
        ): Signal<B> =
            object : Signal<B>() {
                override val vertex: SignalVertex<B> =
                    ApplySignalVertex(
                        function = function.vertex,
                        argument = argument.vertex,
                    )
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

        fun <A, B, C, D, E> map4(
            sa: Signal<A>,
            sb: Signal<B>,
            sc: Signal<C>,
            sd: Signal<D>,
            f: (a: A, b: B, c: C, d: D) -> E,
        ): Signal<E> {
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

    internal abstract val vertex: SignalVertex<A>

    fun <B> map(transform: (A) -> B): Signal<B> =
        object : Signal<B>() {
            override val vertex: SignalVertex<B> =
                MapSignalVertex(
                    source = this@Signal.vertex,
                    transform = transform,
                )
        }

    fun <B> sampleOf(transform: (A) -> Signal<B>): Signal<B> =
        sample(map(transform))

    fun discretize(ticks: EventStream<Unit>): Moment<Cell<A>> =
        this.sample().pullOf { initialValue ->
            ticks.probe(this).hold(initialValue)
        }

    // TODO: Nuke?
    fun sample(): Moment<A> = object : Moment<A>() {
        override fun pullCurrentValue(transaction: Transaction): A =
            vertex.pullCurrentValue(transaction = transaction)
    }

    fun sampleExternally(): A = Transaction.wrap {
        vertex.pullCurrentValue(transaction = it)
    }
}

fun <A, B> Signal<Io<A>>.mapNested(
    transform: (A) -> B,
): Signal<Io<B>> =
    this.map { it.map(transform) }
