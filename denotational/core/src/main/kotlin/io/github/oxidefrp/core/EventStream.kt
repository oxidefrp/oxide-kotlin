package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState
import io.github.oxidefrp.core.shared.State
import io.github.oxidefrp.core.shared.StateScheduler
import io.github.oxidefrp.core.shared.StateSchedulerLayer
import io.github.oxidefrp.core.shared.pullEnter

abstract class EventStream<out A> {
    data class Loop1<A, R>(
        val streamA: EventStream<A>,
        val result: R,
    )

    companion object {
        fun <A> strict(
            occurrences: TimelineSequence<A>,
        ): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> = occurrences
        }

        fun <A> ofOccurrences(
            vararg occurrences: Incident<A>,
        ): EventStream<A> {
            val occurrences1 = occurrences.asPureSequence()
            return ofSequence(occurrences1)
        }

        fun <A> ofInstants(
            vararg occurrences: Instant<A>,
        ): EventStream<A> = strict(
            occurrences = TimelineSequence.ofSequence(occurrences.asPureSequence()),
        )

        fun <A> ofSequence(
            occurrences: PureSequence<Incident<A>>,
        ): EventStream<A> = strict(
            occurrences = TimelineSequence.ofSequence(occurrences.map { it.asInstant }),
        )

        fun <A> never(): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> = TimelineSequence.empty()
        }

        fun <A> spark(value: A): Moment<EventStream<A>> = object : Moment<EventStream<A>>() {
            override fun pullDirectly(t: Time): EventStream<A> = strict(
                occurrences = TimelineSequence.ofSingle(
                    EventOccurrence.strict(
                        time = t,
                        element = value,
                    ),
                ),
            )
        }

        fun <A> sample(stream: EventStream<Signal<A>>): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> by lazy {
                stream.occurrences.map { time, signal ->
                    signal.at(time)
                }
            }
        }

        fun <A> pull(stream: EventStream<Moment<A>>): EventStream<A> = object : EventStream<A>() {
            override val occurrences: TimelineSequence<A> by lazy {
                stream.occurrences.map { time, moment ->
                    moment.pullDirectly(time)
                }
            }
        }

        fun <S, A> enter(
            stream: EventStream<State<S, A>>,
        ): StateScheduler<S, EventStream<A>> = pullEnter(stream.map { it.asMomentState() })

        fun <S> enterUnit(
            stream: EventStream<State<S, Unit>>,
        ): StateScheduler<S, Unit> = pullEnter(stream.map { it.asMomentState() }).map { }

        fun <A, R> pullLooped1(
            f: (streamA: EventStream<A>) -> Moment<EventStream.Loop1<A, R>>,
        ): Moment<R> = object : Moment<R>() {
            override fun pullDirectly(t: Time): R = object {
                val streamALoop: EventStream<A> = object : EventStream<A>() {
                    override val occurrences: TimelineSequence<A> by lazy {
                        loop.streamA.occurrences
                    }
                }

                val loop by lazy { f(streamALoop).pullDirectly(t) }
            }.loop.result
        }
    }

//    val occurrences = regenerateOccurrencesWithMonotonicityChecks(occurrences)

    abstract val occurrences: TimelineSequence<A>

    fun <B> map(transform: (A) -> B): EventStream<B> = object : EventStream<B>() {
        override val occurrences: TimelineSequence<B> by lazy {
            this@EventStream.occurrences.map { _, a -> transform(a) }
        }
    }

    fun filter(predicate: (A) -> Boolean): EventStream<A> = object : EventStream<A>() {
        override val occurrences: TimelineSequence<A> by lazy {
            this@EventStream.occurrences.filter(predicate)
        }
    }

    fun <B, C> probe(
        signal: Signal<B>,
        combine: (A, B) -> C,
    ): EventStream<C> = object : EventStream<C>() {
        override val occurrences: TimelineSequence<C> by lazy {
            this@EventStream.occurrences.map { time, a ->
                combine(a, signal.at(time))
            }
        }
    }

    val currentOccurrence: Moment<EventOccurrence<A>?> = object : Moment<EventOccurrence<A>?>() {
        override fun pullDirectly(t: Time): EventOccurrence<A>? =
            occurrences.takeNotAfter(t).incidents.lastOrNull()?.let { incident ->
                if (incident.time == t) EventOccurrence(incident.event) else null
            }
    }

    fun <B> pullOf(selector: (A) -> Moment<B>): EventStream<B> = pull(map(selector))

    fun <B> sampleOf(selector: (A) -> Signal<B>): EventStream<B> = sample(map(selector))

    fun <S, B> enterOf(selector: (A) -> State<S, B>): StateScheduler<S, EventStream<B>> = enter(map(selector))
}

fun <A, B : Any> EventStream<A>.mapNotNull(transform: (A) -> B?): EventStream<B> = object : EventStream<B>() {
    override val occurrences: TimelineSequence<B> by lazy {
        this@mapNotNull.occurrences.mapNotNull { _, a -> transform(a) }
    }
}

fun <A : Any> EventStream<A?>.filterNotNull(): EventStream<A> = this.mapNotNull { it }

fun <A, B, C> EventStream<A>.squashWith(
    other: EventStream<B>,
    ifFirst: (A) -> C,
    ifSecond: (B) -> C,
    ifBoth: (A, B) -> C,
): EventStream<C> = object : EventStream<C>() {
    override val occurrences: TimelineSequence<C> by lazy {
        mergeTimelineSequences(
            eventSequenceA = this@squashWith.occurrences,
            eventSequenceB = other.occurrences,
            transformA = { _, a -> ifFirst(a) },
            transformB = { _, b -> ifSecond(b) },
            combine = { _, a, b -> ifBoth(a, b) },
        )
    }
}

fun <A> EventStream<A>.mergeWith(
    other: EventStream<A>,
    combine: (A, A) -> A,
): EventStream<A> = object : EventStream<A>() {
    override val occurrences: TimelineSequence<A> by lazy {
        mergeTimelineSequences(
            eventSequenceA = this@mergeWith.occurrences,
            eventSequenceB = other.occurrences,
            transformA = { _, a -> a },
            transformB = { _, b -> b },
            combine = { _, a, b -> combine(a, b) },
        )
    }
}

fun <A, B, C> mergeTimelineSequences(
    eventSequenceA: TimelineSequence<A>,
    eventSequenceB: TimelineSequence<B>,
    transformA: (time: Time, a: A) -> C,
    transformB: (time: Time, b: B) -> C,
    combine: (time: Time, A, B) -> C,
): TimelineSequence<C> = TimelineSequence.wait(
    sequenceA = eventSequenceA,
    sequenceB = eventSequenceB,
    onFirst = fun(
        instant: Instant<A>,
        tail: TimelineSequence<A>,
    ): TimelineSequence<C> = TimelineSequence.cons(
        head = instant.map(transformA),
        tail = {
            mergeTimelineSequences(
                eventSequenceA = tail,
                eventSequenceB = eventSequenceB,
                transformA = transformA,
                transformB = transformB,
                combine = combine,
            )
        },
    ),
    onSecond = fun(
        instant: Instant<B>,
        tail: TimelineSequence<B>,
    ): TimelineSequence<C> = TimelineSequence.cons(
        head = instant.map(transformB),
        tail = {
            mergeTimelineSequences(
                eventSequenceA = eventSequenceA,
                eventSequenceB = tail,
                transformA = transformA,
                transformB = transformB,
                combine = combine,
            )
        },
    ),
    onBoth = fun(
        instantA: Instant<A>,
        tailA: TimelineSequence<A>,
        instantB: Instant<B>,
        tailB: TimelineSequence<B>,
    ): TimelineSequence<C> {
        val time = instantA.time

        return TimelineSequence.cons(
            head = object : Instant<C>(time = time) {
                override val occurrence: EventOccurrence<C>? by lazy {
                    val occurrenceA = instantA.occurrence
                    val occurrenceB = instantB.occurrence

                    when {
                        occurrenceA != null && occurrenceB != null -> EventOccurrence(
                            event = combine(time, occurrenceA.event, occurrenceB.event),
                        )

                        occurrenceA != null -> {
                            assert(occurrenceB == null)

                            EventOccurrence(
                                event = transformA(time, occurrenceA.event),
                            )
                        }

                        occurrenceB != null -> {
                            EventOccurrence(
                                event = transformB(time, occurrenceB.event),
                            )
                        }

                        else -> null
                    }
                }
            },
            tail = {
                mergeTimelineSequences(
                    eventSequenceA = tailA,
                    eventSequenceB = tailB,
                    transformA = transformA,
                    transformB = transformB,
                    combine = combine,
                )
            },
        )
    },
) ?: TimelineSequence.empty()

fun <A> EventStream<A>.hold(initialValue: A): Moment<Cell<A>> = object : Moment<Cell<A>>() {
    override fun pullDirectly(t: Time): Cell<A> {
        val t0 = t

        return object : Cell<A>() {
            private fun sampleNowReally() = super.sample()

            override val segmentSequence: FullSegmentSequence<A> by lazy {
                FullSegmentSequence(
                    initialValue = initialValue,
                    innerValues = this@hold.occurrences.dropBefore(t0),
                )
            }

            override fun sample(): Moment<A> = object : Moment<A>() {
                override fun pullDirectly(t: Time): A = when {
                    t <= t0 -> initialValue
                    else -> sampleNowReally().pullDirectly(t)
                }
            }
        }
    }
}

fun <A, R> EventStream<A>.accum(
    initialValue: R,
    combine: (R, A) -> R,
): Moment<Cell<R>> = EventStream.pullLooped1 { stepsLoop: EventStream<R> ->
    stepsLoop.hold(initialValue).map { accumulator ->
        val steps = pullOf { a ->
            accumulator.sample().map { acc -> combine(acc, a) }
        }

        EventStream.Loop1(
            streamA = steps,
            result = accumulator,
        )
    }
}
