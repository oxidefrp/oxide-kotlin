package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.mergeWith

fun <S, A> EventStream.Companion.pullEnter(
    stream: EventStream<MomentState<S, A>>,
): StateScheduler<S, EventStream<A>> = object : StateScheduler<S, EventStream<A>>() {
    override fun scheduleDirectly(
        stateSignal: Signal<S>,
    ) = object : State<StateSchedulerLayer<S>, EventStream<A>>() {
        override fun enterDirectly(
            oldState: StateSchedulerLayer<S>,
        ): Pair<StateSchedulerLayer<S>, EventStream<A>> {
            val previousSchedulerLayer = oldState

            return previousSchedulerLayer.squashWith(
                stateSignal = stateSignal,
                stream = stream
            )
        }
    }
}

fun <A, B> EventStream.Companion.unzip2(
    stream: EventStream<Pair<A, B>>,
): Pair<EventStream<A>, EventStream<B>> = Pair(
    stream.map { it.first },
    stream.map { it.second },
)

fun <A> EventStream<A>.orElse(
    other: EventStream<A>,
): EventStream<A> = mergeWith(other) { l, _ -> l }
