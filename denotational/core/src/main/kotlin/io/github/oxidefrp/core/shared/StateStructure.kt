package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.hold
import io.github.oxidefrp.core.pullOf

abstract class StateStructure<S, out A> {
    companion object {
        fun <S, A> construct(
            runner: StateStructure<S, StateStructure<S, A>>,
        ): StateStructure<S, A> = object : StateStructure<S, A>() {
            override fun constructDirectly(
                stateSignal: Signal<S>,
            ): MomentState<StateSchedulerLayer<S>, A> =
                runner.constructDirectly(stateSignal = stateSignal).pullEnterOf { innerRunner ->
                    innerRunner.constructDirectly(stateSignal = stateSignal)
                }
        }

        fun <S, A> pull(
            structure: StateStructure<S, Moment<A>>,
        ): StateStructure<S, A> = object : StateStructure<S, A>() {
            override fun constructDirectly(
                stateSignal: Signal<S>,
            ): MomentState<StateSchedulerLayer<S>, A> =
                MomentState.pull(structure.constructDirectly(stateSignal = stateSignal))
        }
    }

    abstract fun constructDirectly(
        stateSignal: Signal<S>,
    ): MomentState<StateSchedulerLayer<S>, A>

    fun seed(initState: S): Moment<Pair<Cell<S>, A>> =
        EventStream.pullLooped1 { newStatesLoop: EventStream<S> ->
            val firstLayer = StateSchedulerLayer<S>(EventStream.never())

            newStatesLoop.hold(initState).pullOf { stateCell ->
                constructDirectly(
                    stateSignal = stateCell.value,
                ).enterDirectly(firstLayer).map { (finalLayer, result) ->
                    val newStates = finalLayer.stateStream

                    EventStream.Loop1(
                        streamA = newStates,
                        result = Pair(stateCell, result),
                    )
                }
            }
        }

    fun <B> map(
        transform: (A) -> B,
    ): StateStructure<S, B> = object : StateStructure<S, B>() {
        override fun constructDirectly(
            stateSignal: Signal<S>,
        ): MomentState<StateSchedulerLayer<S>, B> =
            this@StateStructure.constructDirectly(stateSignal = stateSignal)
                .map(transform)
    }

    fun <B> constructOf(
        transform: (A) -> StateStructure<S, B>,
    ): StateStructure<S, B> = construct(map(transform))

    fun <B> pullOf(
        transform: (A) -> Moment<B>,
    ): StateStructure<S, B> = pull(map(transform))
}
