package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Moment

abstract class State<S, out A> {
    companion object {
        fun <S, A> pure(a: A): State<S, A> =
            object : State<S, A>() {
                override fun enterDirectly(oldState: S): Pair<S, A> = oldState to a
            }

        fun <S> read(): State<S, S> =
            object : State<S, S>() {
                override fun enterDirectly(oldState: S): Pair<S, S> = oldState to oldState
            }

        fun <S> write(s: S): State<S, Unit> =
            object : State<S, Unit>() {
                override fun enterDirectly(oldState: S): Pair<S, Unit> = s to Unit
            }

        fun <S, A, B> apply(
            function: State<S, (A) -> B>,
            argument: State<S, A>,
        ): State<S, B> = object : State<S, B>() {
            override fun enterDirectly(oldState: S): Pair<S, B> {
                val (s1, a) = argument.enterDirectly(oldState)
                val (s2, f) = function.enterDirectly(s1)
                val b = f(a)
                return s2 to b
            }
        }

        fun <S, A> construct(
            state: State<S, StateStructure<S, A>>,
        ): StateStructure<S, A> =
            StateStructure.construct(state.asStateStructure())
    }

    abstract fun enterDirectly(oldState: S): Pair<S, A>

    fun <B> map(transform: (A) -> B): State<S, B> =
        object : State<S, B>() {
            override fun enterDirectly(oldState: S): Pair<S, B> {
                val (s, a) = this@State.enterDirectly(oldState)
                return s to transform(a)
            }
        }

    fun <B> enterOf(transform: (A) -> State<S, B>): State<S, B> =
        object : State<S, B>() {
            override fun enterDirectly(oldState: S): Pair<S, B> {
                val (s, a) = this@State.enterDirectly(oldState)
                return transform(a).enterDirectly(s)
            }
        }

    fun <B> constructOf(
        transform: (A) -> StateStructure<S, B>,
    ): StateStructure<S, B> = construct(map(transform))

    fun asMomentState() = object : MomentState<S, A>() {
        override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
            Moment.pure(this@State.enterDirectly(oldState))
    }

    fun asStateStructure(): StateStructure<S, A> =
        asMomentState().asStateStructure()
}
