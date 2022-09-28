package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.impl.Transaction
import io.github.oxidefrp.oxide.core.impl.event_stream.Subscription
import io.github.oxidefrp.oxide.core.impl.moment.MomentIoMomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.MomentVertex
import io.github.oxidefrp.oxide.core.impl.moment.SampleMomentVertex
import io.github.oxidefrp.oxide.core.impl.signal.ApplySignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.ConstantSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.MapSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.MomentSourceSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.SamplePerformSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.SampleSignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.SignalVertex
import io.github.oxidefrp.oxide.core.impl.signal.SourceSignalVertex

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

        fun <A> samplePerform(
            signal: Signal<Io<Signal<Io<A>>>>,
        ): Signal<Io<A>> = object : Signal<Io<A>>() {
            override val vertex: SignalVertex<Io<A>> =
                SamplePerformSignalVertex(signal = signal.vertex)
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
        override val vertex: MomentVertex<A> by lazy {
            SampleMomentVertex(
                signal = this@Signal.vertex,
            )
        }
    }

    fun sampleIo(): MomentIo<A> = MomentIo.lift(sample())

    fun sampleExternally(): A = Transaction.wrap {
        vertex.pullCurrentValue(transaction = it)
    }

    fun reactExternally(
        action: (A) -> Unit,
        ticks: EventStream<Unit>,
    ): Subscription {
        val currentValue = sampleExternally()

        action(currentValue)

        return ticks.probe(this).subscribe(action)
    }
}

fun <A, B> Signal<Io<A>>.mapNested(
    transform: (A) -> B,
): Signal<Io<B>> =
    this.map { it.map(transform) }

fun <A, B> Signal<Io<A>>.sampleOfPure(
    transform: (A) -> Signal<B>,
): Signal<Io<B>> =
    samplePerformOf {
        transform(it).map(Io.Companion::pure)
    }

fun <A, B> Signal<A>.samplePerformOfPure(
    transform: (A) -> Signal<Io<B>>,
): Signal<Io<B>> =
    Signal.samplePerform(this.map { Io.pure(transform(it)) })

fun <A, B> Signal<Io<A>>.samplePerformOf(
    transform: (A) -> Signal<Io<B>>,
): Signal<Io<B>> =
    Signal.samplePerform(this.mapNested(transform))
