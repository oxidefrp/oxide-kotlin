package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Moment

fun <S, A> Moment.Companion.enter(moment: Moment<State<S, A>>): MomentState<S, A> =
    object : MomentState<S, A>() {
        override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
            moment.map { innerState ->
                innerState.enterDirectly(oldState)
            }
    }

fun <S, A> Moment.Companion.pullEnter(moment: Moment<MomentState<S, A>>): MomentState<S, A> =
    object : MomentState<S, A>() {
        override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
            moment.pullOf { innerMoment ->
                innerMoment.enterDirectly(oldState)
            }
    }

fun <A, B> Moment<A>.pullOf(transform: (A) -> Moment<B>): Moment<B> =
    Moment.pull(map(transform))

fun <S, A, B> Moment<A>.enterOf(transform: (A) -> State<S, B>): MomentState<S, B> =
    Moment.enter(map(transform))
