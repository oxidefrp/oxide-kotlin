package io.github.oxidefrp.core.shared

import io.github.oxidefrp.core.Cell
import io.github.oxidefrp.core.EventStream
import io.github.oxidefrp.core.Moment
import io.github.oxidefrp.core.Signal
import io.github.oxidefrp.core.hold
import io.github.oxidefrp.core.shared.pullOf

fun <A> Cell.Companion.pull(
    cell: Cell<Moment<A>>,
): Moment<Cell<A>> = cell.sample().pullOf { initialMoment ->
    initialMoment.pullOf { initialValue ->
        EventStream.pull(cell.newValues).hold(initialValue)
    }
}

fun <S, A> Cell.Companion.enter(
    cell: Cell<State<S, A>>,
): StateStructure<S, Cell<A>> = Moment.enter(cell.sample()).constructOf { initialValue ->
    EventStream.enter(cell.newValues).pullOf { newValues ->
        newValues.hold(initialValue)
    }
}

fun <S, A> Cell.Companion.pullEnter(
    cell: Cell<MomentState<S, A>>,
): StateStructure<S, Cell<A>> = Moment.pullEnter(cell.sample()).constructOf { initialValue ->
    EventStream.pullEnter(cell.newValues).pullOf { newValues ->
        newValues.hold(initialValue)
    }
}

fun <S, A> Cell.Companion.construct(
    cell: Cell<StateStructure<S, A>>,
): StateStructure<S, Cell<A>> = object : StateStructure<S, Cell<A>>() {
    override fun constructDirectly(
        stateSignal: Signal<S>,
    ): MomentState<StateSchedulerLayer<S>, Cell<A>> = MomentState.enterDirectly { inputLayer ->
        cell.pullOf { structure ->
            structure.constructDirectly(stateSignal = stateSignal).enterDirectly(inputLayer)
        }.map(Cell.Companion::unzip2).map { (layerCell, valueCell) ->
            val outputLayer = StateSchedulerLayer.divert(layerCell)
            return@map Pair(outputLayer, valueCell)
        }
    }
}

fun <A, B> Cell.Companion.unzip2(
    cell: Cell<Pair<A, B>>,
): Pair<Cell<A>, Cell<B>> = Pair(
    cell.map { it.first },
    cell.map { it.second },
)

fun <A, B> Cell<A>.pullOf(
    transform: (A) -> Moment<B>,
): Moment<Cell<B>> = Cell.pull(map(transform))

fun <A, B> Cell<A>.divertOf(
    transform: (A) -> EventStream<B>,
): EventStream<B> = Cell.divert(map(transform))

fun <A, B> Cell<A>.divertEarlyOf(
    transform: (A) -> EventStream<B>,
): EventStream<B> = Cell.divertEarly(map(transform))

fun <A, B> Cell<A>.switchOf(
    transform: (A) -> Cell<B>,
): Cell<B> = Cell.switch(map(transform))

fun <A, S, B> Cell<A>.constructOf(
    transform: (A) -> StateStructure<S, B>,
): StateStructure<S, Cell<B>> = Cell.construct(map(transform))

fun <A, S, B> Cell<A>.enterOf(
    transform: (A) -> State<S, B>,
): StateStructure<S, Cell<B>> = Cell.enter(map(transform))

fun <A, S, B> Cell<A>.pullEnterOf(
    transform: (A) -> MomentState<S, B>,
): StateStructure<S, Cell<B>> = Cell.pullEnter(map(transform))
