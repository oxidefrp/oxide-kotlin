package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.mapNotNull
import io.github.oxidefrp.core.shared.pullOf
import io.github.oxidefrp.core.squashWith

data class StateSchedulerLayer<S>(
    val stateStream: EventStream<S>,
) {
    companion object {
        fun <S> divert(cell: Cell<StateSchedulerLayer<S>>): StateSchedulerLayer<S> =
            StateSchedulerLayer(
                stateStream = cell.divertEarlyOf { it.stateStream },
            )
    }

    fun getOldState(stateSignal: Signal<S>): Moment<S> =
        stateStream.currentOccurrence.pullOf { optionalOccurrence ->
            when (optionalOccurrence) {
                null -> stateSignal.sample()
                else -> Moment.pure(optionalOccurrence.event)
            }
        }

    fun mergeWithNewState(newState: S): Moment<StateSchedulerLayer<S>> =
        EventStream.spark(newState).map { newStateStream ->
            val outputStateStream = newStateStream.orElse(stateStream)

            StateSchedulerLayer(
                stateStream = outputStateStream,
            )
        }

    fun <A> squashWith(
        stateSignal: Signal<S>,
        stream: EventStream<MomentState<S, A>>,
    ): Pair<StateSchedulerLayer<S>, EventStream<A>> {
        val (outputStateStream, valueOrNullStream) = EventStream.unzip2(
            EventStream.pull(
                stateStream.squashWith(
                    stream,
                    ifFirst = {
                        Moment.pure(Pair(it, null))
                    },
                    ifSecond = {
                        stateSignal.sample().pullOf { oldState: S ->
                            it.enterDirectly(oldState = oldState)
                        }
                    },
                    ifBoth = { oldState: S, momentState: MomentState<S, A> ->
                        momentState.enterDirectly(oldState = oldState)
                    },
                ),
            )
        )

        val valueStream = valueOrNullStream.mapNotNull { it }

        return Pair(
            StateSchedulerLayer(
                stateStream = outputStateStream,
            ),
            valueStream,
        )
    }
}

// Thought: With the current prototype of [scheduleDirectly], keeping [StateScheduler] and [StateStructure] separate
// might be not worth it (we could just ignore the time in [EventStream.pullEnter])
abstract class StateScheduler<S, A> {
    companion object {
        // Thought: Maybe this should be `pullEnter`, if we return [StateStructure] anyway
        fun <S, A> pull(
            scheduler: StateScheduler<S, Moment<A>>,
        ): StateStructure<S, A> =
            StateStructure.pull(scheduler.asStateStructure())
    }

    abstract fun scheduleDirectly(
        stateSignal: Signal<S>,
    ): State<StateSchedulerLayer<S>, A>

    fun <B> map(
        transform: (A) -> B,
    ): StateScheduler<S, B> = object : StateScheduler<S, B>() {
        override fun scheduleDirectly(
            stateSignal: Signal<S>,
        ): State<StateSchedulerLayer<S>, B> =
            this@StateScheduler.scheduleDirectly(stateSignal = stateSignal)
                .map(transform)
    }

    fun <B> pullOf(
        transform: (A) -> Moment<B>,
    ): StateStructure<S, B> = pull(map(transform))

    fun asStateStructure(): StateStructure<S, A> = object : StateStructure<S, A>() {
        override fun constructDirectly(
            stateSignal: Signal<S>,
        ): MomentState<StateSchedulerLayer<S>, A> =
            scheduleDirectly(stateSignal = stateSignal).asMomentState()
    }
}
