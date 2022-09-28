package io.github.oxidefrp.semantics

abstract class MomentState<S, out A> {
    companion object {
        fun <S, A> pure(value: A): MomentState<S, A> =
            object : MomentState<S, A>() {
                override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
                    Moment.pure(Pair(oldState, value))
            }

        fun <S> read(): MomentState<S, S> =
            object : MomentState<S, S>() {
                override fun enterDirectly(oldState: S): Moment<Pair<S, S>> =
                    Moment.pure(Pair(oldState, oldState))
            }

        fun <S> write(s: S): MomentState<S, Unit> =
            object : MomentState<S, Unit>() {
                override fun enterDirectly(oldState: S): Moment<Pair<S, Unit>> =
                    Moment.pure(Pair(oldState, Unit))
            }

        // TODO: Nuke?
        fun <S, A> enterDirectly(
            enter: (S) -> Moment<Pair<S, A>>,
        ): MomentState<S, A> = object : MomentState<S, A>() {
            override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
                enter(oldState)
        }

        fun <S, A> pull(
            moment: MomentState<S, Moment<A>>,
        ): MomentState<S, A> = object : MomentState<S, A>() {
            override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
                moment.enterDirectly(oldState).pullOf { (newState, innerMoment) ->
                    innerMoment.map { a -> Pair(newState, a) }
                }
        }

        fun <S, A> enter(
            moment: MomentState<S, State<S, A>>,
        ): MomentState<S, A> = object : MomentState<S, A>() {
            override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
                moment.enterDirectly(oldState).map { (newState, innerState) ->
                    innerState.enterDirectly(newState)
                }
        }

        fun <S, A> pullEnter(
            moment: MomentState<S, MomentState<S, A>>,
        ): MomentState<S, A> = object : MomentState<S, A>() {
            override fun enterDirectly(oldState: S): Moment<Pair<S, A>> =
                moment.enterDirectly(oldState).pullOf { (newState, innerMoment) ->
                    innerMoment.enterDirectly(newState)
                }
        }

        fun <S1, S2, A> pullEnterOther(
            moment: MomentState<S1, MomentState<S2, A>>,
        ): MomentState<Pair<S1, S2>, A> = object : MomentState<Pair<S1, S2>, A>() {
            override fun enterDirectly(oldState: Pair<S1, S2>): Moment<Pair<Pair<S1, S2>, A>> {
                val (oldState1, oldState2) = oldState

                return moment.enterDirectly(oldState1).pullOf { (newState1, innerMoment) ->
                    innerMoment.enterDirectly(oldState2).map { (newState2, a) ->
                        Pair(Pair(newState1, newState2), a)
                    }
                }
            }
        }

        fun <S1, S2, A> enterOther(
            moment: MomentState<S1, State<S2, A>>,
        ): MomentState<Pair<S1, S2>, A> = pullEnterOther(moment.map { it.asMomentState() })

        fun <S, A> construct(
            moment: MomentState<S, StateStructure<S, A>>,
        ): StateStructure<S, A> = StateStructure.construct(moment.asStateStructure())

        fun <S, A, B> apply(
            function: MomentState<S, (A) -> B>,
            argument: MomentState<S, A>,
        ): MomentState<S, B> = object : MomentState<S, B>() {
            override fun enterDirectly(oldState: S): Moment<Pair<S, B>> =
                function.enterDirectly(oldState).pullOf { (newState1, f) ->
                    argument.enterDirectly(newState1).map { (newState2, a) ->
                        Pair(newState2, f(a))
                    }
                }
        }

        fun <S, A, B> map1(
            sa: MomentState<S, A>,
            f: (a: A) -> B,
        ): MomentState<S, B> {
            fun g(a: A) = f(a)

            return sa.map(::g)
        }

        fun <S, A, B, C> map2(
            sa: MomentState<S, A>,
            sb: MomentState<S, B>,
            f: (a: A, b: B) -> C,
        ): MomentState<S, C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return apply(
                sa.map(::g),
                sb,
            )
        }

        fun <S, A, B, C, D> map3(
            sa: MomentState<S, A>,
            sb: MomentState<S, B>,
            sc: MomentState<S, C>,
            f: (a: A, b: B, c: C) -> D,
        ): MomentState<S, D> {
            fun g(a: A) = fun(b: B) = fun(c: C) = f(a, b, c)

            return apply(
                apply(
                    sa.map(::g),
                    sb,
                ),
                sc,
            )
        }
    }

    abstract fun enterDirectly(oldState: S): Moment<Pair<S, A>>

    fun pullEnterDirectly(t: Time, oldState: S): Pair<S, A> =
        enterDirectly(oldState).pullDirectly(t)

    fun <B> pullEnterOf(
        transform: (A) -> MomentState<S, B>,
    ): MomentState<S, B> =
        pullEnter(map(transform))

    fun <B> pullOf(
        transform: (A) -> Moment<B>,
    ): MomentState<S, B> =
        pull(map(transform))

    fun <S2, B> pullEnterOtherOf(
        transform: (A) -> MomentState<S2, B>,
    ): MomentState<Pair<S, S2>, B> =
        pullEnterOther(map(transform))

    fun <S2, B> enterOtherOf(
        transform: (A) -> State<S2, B>,
    ): MomentState<Pair<S, S2>, B> =
        enterOther(map(transform))

    fun <B> constructOf(
        transform: (A) -> StateStructure<S, B>,
    ): StateStructure<S, B> =
        construct(map(transform))

    fun asStateStructure(): StateStructure<S, A> {
        val momentState = this

        return object : StateStructure<S, A>() {
            override fun constructDirectly(
                stateSignal: Signal<S>,
            ): MomentState<StateSchedulerLayer<S>, A> = enterDirectly { inputLayer ->
                inputLayer.getOldState(stateSignal = stateSignal).pullOf { oldState ->
                    momentState.enterDirectly(oldState)
                }.pullOf { (newState, value) ->
                    // Problem: When the `MomentState<S, A>` (the constructor action) is merged with the scheduler
                    // layer, it mixes constructor actions with the later stateful actions. They may potentially be
                    // interlaced.
                    // Idea: Maybe only the new cell value should be considered?
                    inputLayer.mergeWithNewState(newState).map { outputLayer ->
                        Pair(outputLayer, value)
                    }
                }
            }
        }
    }
}

fun <S, A, B> MomentState<S, A>.enterOf(
    transform: (A) -> State<S, B>,
): MomentState<S, B> =
    MomentState.enter(map(transform))

fun <S, A, B> MomentState<S, A>.map(transform: (A) -> B): MomentState<S, B> =
    object : MomentState<S, B>() {
        override fun enterDirectly(oldState: S): Moment<Pair<S, B>> =
            this@map.enterDirectly(oldState).map { (oldState, a) ->
                Pair(oldState, transform(a))
            }
    }
