package io.github.oxidefrp.semantics

abstract class Instant<out E>(
    val time: Time,
) {
    abstract val occurrence: EventOccurrence<E>?

    companion object {
        fun <A> strict(
            time: Time,
            occurrence: EventOccurrence<A>?,
        ): Instant<A> = object : Instant<A>(time = time) {
            override val occurrence: EventOccurrence<A>? = occurrence
        }

        fun <A : Any> strictNonNull(
            time: Time,
            element: A?,
        ): Instant<A> = object : Instant<A>(time = time) {
            override val occurrence: EventOccurrence<A>? = element?.let(::EventOccurrence)
        }
    }

    val asIncident: Incident<E>?
        get() = occurrence?.let {
            Incident(
                time = time,
                event = it.event,
            )
        }

    fun <R> map(transform: (Time, E) -> R): Instant<R> = object : Instant<R>(time = time) {
        override val occurrence: EventOccurrence<R>? by lazy {
            this@Instant.occurrence?.let { occurrence ->
                EventOccurrence(
                    event = transform(time, occurrence.event),
                )
            }
        }
    }

    fun <R : Any> mapNotNull(transform: (Time, E) -> R?): Instant<R> = object : Instant<R>(time = time) {
        override val occurrence: EventOccurrence<R>? by lazy {
            this@Instant.occurrence?.let { occurrence ->
                transform(time, occurrence.event)?.let {
                    EventOccurrence(event = it)
                }
            }
        }
    }

    fun filter(predicate: (E) -> Boolean): Instant<E> = object : Instant<E>(time = time) {
        override val occurrence: EventOccurrence<E>? by lazy {
            this@Instant.occurrence?.let { occurrence ->
                occurrence.takeIf { predicate(it.event) }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        other as Instant<*>

        if (time != other.time) return false
        if (occurrence != other.occurrence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + (occurrence?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Instant(time=$time, occurrence=$occurrence)"
}

data class Incident<out E>(
    val time: Time,
    val event: E,
) {
    val asInstant: Instant<E>
        get() = object : Instant<E>(time = time) {
            override val occurrence: EventOccurrence<E> = EventOccurrence(event = event)
        }
}

data class EventOccurrence<out E>(
    val event: E,
) {
    companion object {
        fun <E> strict(
            time: Time,
            element: E,
        ): Instant<E> = object : Instant<E>(time = time) {
            override val occurrence: EventOccurrence<E> = EventOccurrence(event = element)
        }
    }
}

abstract class TimelineSequence<out E> {
    companion object {
        fun <E> cons(
            head: Instant<E>,
            tail: () -> TimelineSequence<E>,
        ): TimelineSequence<E> = object : TimelineSequence<E>() {
            override val instants: PureSequence<Instant<E>> by lazy {
                PureSequence.cons(
                    head = head,
                    tail = { tail().instants },
                )
            }
        }

        fun <A, B, C : Any> wait(
            sequenceA: TimelineSequence<A>,
            sequenceB: TimelineSequence<B>,
            onFirst: (
                instant: Instant<A>, tail: TimelineSequence<A>,
            ) -> C,
            onSecond: (
                instant: Instant<B>, tail: TimelineSequence<B>,
            ) -> C,
            onBoth: (
                instantA: Instant<A>, tailA: TimelineSequence<A>,
                instantB: Instant<B>, tailB: TimelineSequence<B>,
            ) -> C,
        ): C? {
            val cutA = sequenceA.cutOff()
            val cutB = sequenceB.cutOff()

            return if (cutA != null && cutB != null) {
                val (headA, tailA) = cutA
                val (headB, tailB) = cutB

                when {
                    headA.time == headB.time -> onBoth(
                        headA, tailA,
                        headB, tailB,
                    )

                    headA.time < headB.time -> onFirst(headA, tailA)

                    else -> {
                        assert(headA.time > headB.time)
                        onSecond(headB, tailB)
                    }
                }
            } else if (cutA != null) {
                assert(cutB == null)

                val (headA, tailA) = cutA

                onFirst(headA, tailA)
            } else if (cutB != null) {
                val (headB, tailB) = cutB

                onSecond(headB, tailB)
            } else null
        }

        fun <E> empty(): TimelineSequence<E> = object : TimelineSequence<E>() {
            override val instants: PureSequence<Instant<E>> = PureSequence.empty()
        }

        fun <A> ofSingle(occurrence: Instant<A>) = object : TimelineSequence<A>() {
            override val instants: PureSequence<Instant<A>> = PureSequence.ofSingle(occurrence)
        }

        fun <A> ofInstants(vararg occurrences: Instant<A>): TimelineSequence<A> = ofSequence(occurrences.asPureSequence())

        fun <A> ofSequence(occurrences: PureSequence<Instant<A>>) = object : TimelineSequence<A>() {
            override val instants: PureSequence<Instant<A>> = occurrences
        }
    }

    // This laziness might be unnecessary
    // Or maybe it is, wait
    // I'm not sure anymore
    abstract val instants: PureSequence<Instant<E>>

    val occurrences: PureSequence<EventOccurrence<E>> by lazy {
        instants.mapNotNull { it.occurrence }
    }

    val incidents: PureSequence<Incident<E>> by lazy {
        instants.mapNotNull { it.asIncident }
    }

    fun cutOff(): Pair<Instant<E>, TimelineSequence<E>>? = instants.asNonEmpty()?.let {
        Pair(
            it.head,
            ofSequence(it.tail),
        )
    }

    fun <S, R> runningStatefulFold(
        initialState: S,
        operation: (
            acc: S,
            time: Time,
            element: E,
        ) -> Pair<S, R>,
    ): TimelineSequence<R> = object : TimelineSequence<R>() {
        override val instants: PureSequence<Instant<R>> by lazy {
            this@TimelineSequence.instants.runningStatefulFold(
                initialState = initialState,
                operation = { acc, instant ->
                    val time = instant.time

                    instant.occurrence?.let {
                        val (newAcc, r) = operation(acc, time, it.event)

                        Pair(
                            newAcc,
                            Instant.strict(
                                time = time,
                                occurrence = EventOccurrence(r),
                            ),
                        )
                    } ?: Pair(
                        acc,
                        Instant.strict(
                            time = time,
                            occurrence = null,
                        ),
                    )
                },
            )
        }
    }

    fun <R> map(transform: (Time, E) -> R): TimelineSequence<R> = object : TimelineSequence<R>() {
        override val instants: PureSequence<Instant<R>> = this@TimelineSequence.instants.map { it.map(transform) }
    }

    fun <R : Any> mapNotNull(transform: (Time, E) -> R?): TimelineSequence<R> = object : TimelineSequence<R>() {
        override val instants: PureSequence<Instant<R>> = this@TimelineSequence.instants.map { it.mapNotNull(transform) }
    }

    fun filter(predicate: (E) -> Boolean): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> = this@TimelineSequence.instants.map {
            it.filter(predicate)
        }
    }

    fun takeWithin(end: TimeEndpoint): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> = this@TimelineSequence.instants.takeWhile { instant ->
            end.isIncludedBackSide(time = instant.time)
        }
    }

    fun takeBefore(time: Time): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> = this@TimelineSequence.instants.takeWhile { instant ->
            instant.time < time
        }
    }

    fun takeNotAfter(time: Time): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> = this@TimelineSequence.instants.takeWhile { instant ->
            instant.time <= time
        }
    }

    fun toList(): List<Incident<E>> = incidents.toList()

    fun getOccurrencesUntil(end: ClosedTimeEndpoint): List<Incident<E>> = takeWithin(end = end).toList()

    fun take(n: Int): List<Incident<E>> = incidents.take(n).toList()

    fun dropBefore(time: Time): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> by lazy {
            this@TimelineSequence.instants.dropWhile { it.time < time }
        }
    }

    fun dropNotAfter(time: Time): TimelineSequence<E> = object : TimelineSequence<E>() {
        override val instants: PureSequence<Instant<E>> by lazy {
            this@TimelineSequence.instants.dropWhile { it.time <= time }
        }
    }

    fun getOccurrenceAt(time: Time): EventOccurrence<E>? =
        takeWithin(end = ClosedTimeEndpoint(time)).instants.singleOrNull { it.time == time }?.occurrence

    override fun equals(other: Any?): Boolean {
        other as TimelineSequence<*>

        if (instants.toList() != other.instants.toList()) return false

        return true
    }

    override fun hashCode(): Int {
        return instants.hashCode()
    }

    override fun toString(): String = "TimelineSequence(instants=${instants.toList()})"
}

fun <A> TimelineSequence<A>.divertTo(
    newStreams: TimelineSequence<EventStream<A>>,
): TimelineSequence<A> {
    fun TimelineSequence<A>.divertIfNecessary(
        newStreamInstant: Instant<EventStream<A>>,
        newStreamsTail: TimelineSequence<EventStream<A>>,
    ): TimelineSequence<A> {
        val newStream = newStreamInstant.occurrence?.event

        return when {
            // If this is actually a diversion, keep only the future
            // events from the new stream and divert to it
            newStream != null -> newStream.occurrences.dropNotAfter(newStreamInstant.time)
                .divertTo(newStreams = newStreamsTail)
            // Otherwise, let's stick to this stream
            else -> this.divertTo(
                newStreams = newStreamsTail,
            )
        }
    }

    return TimelineSequence.wait(
        sequenceA = this,
        sequenceB = newStreams,
        onFirst = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
        ): TimelineSequence<A> = TimelineSequence.cons(
            // Forward the instant (no matter if a real one or an imaginary one)
            // from this stream, as the diversion hasn't happened yet
            head = instant,
            tail = {
                tail.divertTo(
                    newStreams = newStreams,
                )
            },
        ),
        onSecond = fun(
            newStreamInstant: Instant<EventStream<A>>,
            newStreamsTail: TimelineSequence<EventStream<A>>,
        ): TimelineSequence<A> {
            // The diversion is potentially happening, but this stream is
            // definitely not emitting

            return TimelineSequence.cons(
                // To ensure causality, we need to propagate the possible diversion
                // instant _without_ being strict on the actual new stream
                // occurrence. When deciding what stream to divert to, one might
                // depend (in any way) on past events from the resulting stream. The
                // search for the last event in the resulting stream from before the
                // diversion has to stop when reaching the first instant with time
                // equal or later than the diversion time. If we didn't emit a
                // protective instant here, we wouldn't know when to stop the
                // search, and we'd pass the diversion time, while designation of
                // the instants (not even their occurrences, but the instants
                // themselves) from after the diversion is strict on the new stream
                // we're (possibly) diverting to.
                head = Instant.strict(
                    time = newStreamInstant.time,
                    // As this is a classic (non-early) divert, we know we won't
                    // be propagating a potential instant from the new stream
                    // that would happen at the time of diversion. We also know
                    // that this is an exclusive diversion (the stream we're
                    // diverting from has no instant), so the protective
                    // diversion instant is imaginary.
                    occurrence = null,
                ),
                tail = {
                    this.divertIfNecessary(
                        newStreamInstant = newStreamInstant,
                        newStreamsTail = newStreamsTail,
                    )
                },
            )
        },
        onBoth = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
            newStreamInstant: Instant<EventStream<A>>,
            newStreamsTail: TimelineSequence<EventStream<A>>,
        ): TimelineSequence<A> {
            // This event is potentially emitting and a diversion is potentially
            // happening

            return TimelineSequence.cons(
                // As this is a classic (non-early) divert, we do propagate
                // the (potential) last event from the stream we're diverting
                // from, that's happening at the time of diversion itself.
                head = instant,
                tail = {
                    tail.divertIfNecessary(
                        newStreamInstant = newStreamInstant,
                        newStreamsTail = newStreamsTail,
                    )
                },
            )
        },
    ) ?: TimelineSequence.empty()
}

fun <A> TimelineSequence<A>.divertEarlyTo(
    newStreams: TimelineSequence<EventStream<A>>,
): TimelineSequence<A> {
    // A diversion happens if a new stream instant contains an occurrence.
    // An "early diversion" is a diversion to a stream that has an instant at
    // the time of diversion. Such instant will be called an "early instant" and
    // its tail an "early tail".

    fun TimelineSequence<A>.divertEarlyIfNecessary(
        instant: Instant<A>?,
        newStreamInstant: Instant<EventStream<A>>,
        newStreamsTail: TimelineSequence<EventStream<A>>,
    ): TimelineSequence<A> = object {
        val time = newStreamInstant.time

        // The new sequence we're diverting to (if a diversion takes place)
        val newSequence: TimelineSequence<A>? by lazy {
            val newStream = newStreamInstant.occurrence?.event
            newStream?.occurrences?.dropBefore(time)
        }

        // The early instant and its tail (if an early diversion takes place)
        val earlyCut: Pair<Instant<A>, TimelineSequence<A>>? by lazy {
            newSequence?.cutOff()?.takeIf { (firstInstant, _) ->
                firstInstant.time == time
            }
        }

        // The early instant (if an early diversion takes place)
        val earlyInstant: Instant<A>?
            get() = earlyCut?.first

        // The early tail (if an early diversion takes place)
        val earlyTail: TimelineSequence<A>?
            get() = earlyCut?.second

        val result = TimelineSequence.cons(
            // No matter if a diversion happens or not, we need to
            // emit an instant with time not strict on the [newStream]

            head = object : Instant<A>(time = time) {
                override val occurrence: EventOccurrence<A>? by lazy {
                    // Only now we can become strict on the (potential) new
                    // stream. Let's propagate the early diversion if the
                    // diversion takes place, or the (potential) simultaneous
                    // instant occurrence from the stream we were supposed to
                    // divert from otherwise.
                    when (newSequence) {
                        null -> instant?.occurrence
                        else -> earlyInstant?.occurrence
                    }
                }
            },
            tail = {
                // If the new stream emitted an instant at the time of
                // diversion, we handled it above. In that case, the tail will
                // need to not include that early instant. Otherwise, the tail
                // will be the whole new stream (if there is any at all).
                val effectiveNewStream = earlyTail ?: newSequence

                when {
                    // Divert to the effective new stream if there is one
                    effectiveNewStream != null -> effectiveNewStream.divertEarlyTo(
                        newStreams = newStreamsTail,
                    )
                    // Otherwise, let's stick to this one
                    else -> this@divertEarlyIfNecessary.divertEarlyTo(
                        newStreams = newStreamsTail,
                    )
                }
            },
        )
    }.result

    return TimelineSequence.wait(
        sequenceA = this,
        sequenceB = newStreams,
        onFirst = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
        ): TimelineSequence<A> = TimelineSequence.cons(
            // Forward the instant (no matter if a real one or an imaginary one)
            // from this stream, as a diversion hasn't happened yet
            head = instant,
            tail = {
                tail.divertEarlyTo(
                    newStreams = newStreams,
                )
            },
        ),
        onSecond = fun(
            newStreamInstant: Instant<EventStream<A>>,
            newStreamsTail: TimelineSequence<EventStream<A>>,
        ): TimelineSequence<A> {
            // A diversion is potentially happening, but this stream is
            // definitely not emitting (in case of the early diversion we don't
            // care anyway).

            return this.divertEarlyIfNecessary(
                instant = null,
                newStreamInstant = newStreamInstant,
                newStreamsTail = newStreamsTail,
            )
        },
        onBoth = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
            newStreamInstant: Instant<EventStream<A>>,
            newStreamsTail: TimelineSequence<EventStream<A>>,
        ): TimelineSequence<A> {
            // This event is potentially emitting and a diversion is potentially
            // happening

            return tail.divertEarlyIfNecessary(
                instant = instant,
                newStreamInstant = newStreamInstant,
                newStreamsTail = newStreamsTail,
            )
        },
    ) ?: TimelineSequence.empty()
}

fun <A> TimelineSequence<A>.switchTo(
    newCells: TimelineSequence<Cell<A>>,
): TimelineSequence<A> {
    // A switch happens if a new stream instant contains an occurrence.

    fun TimelineSequence<A>.switchIfNecessary(
        instant: Instant<A>?,
        newCellInstant: Instant<Cell<A>>,
        newCellsTail: TimelineSequence<Cell<A>>,
    ): TimelineSequence<A> = object {
        val time = newCellInstant.time

        val newCell: Cell<A>?
            get() = newCellInstant.occurrence?.event

        val result = TimelineSequence.cons(
            // No matter if a switch happens or not, we need to emit an instant
            // with time not strict on the [newCell]
            head = object : Instant<A>(time = time) {
                override val occurrence: EventOccurrence<A>? by lazy {
                    // Only now we can become strict on the (potential) new
                    // cell.
                    when (val newCell = newCell) {
                        // Propagate the change from the old cell if the switch
                        // isn't actually happening
                        null -> instant?.occurrence
                        // But if it is, always emit the new value of the new
                        // cell
                        else -> EventOccurrence(
                            event = newCell.sampleNew().pullDirectly(time),
                        )
                    }
                }
            },
            tail = {
                val newCellNewValues: TimelineSequence<A>? =
                    newCell?.innerValues?.dropNotAfter(time)

                // Switch to the new cell if there is one
                newCellNewValues?.switchTo(
                    newCells = newCellsTail,
                ) ?:
                // Otherwise, let's stick to this one
                this@switchIfNecessary.switchTo(
                    newCells = newCellsTail,
                )
            },
        )
    }.result

    return TimelineSequence.wait(
        sequenceA = this,
        sequenceB = newCells,
        onFirst = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
        ): TimelineSequence<A> = TimelineSequence.cons(
            // Forward the instant (no matter if a real one or an imaginary one)
            // from this cell, as a switch hasn't happened yet
            head = instant,
            tail = {
                tail.switchTo(
                    newCells = newCells,
                )
            },
        ),
        onSecond = fun(
            newCellInstant: Instant<Cell<A>>,
            newCellsTail: TimelineSequence<Cell<A>>,
        ): TimelineSequence<A> {
            // A switch is potentially happening, but this cell is definitely
            // not changing.

            return this.switchIfNecessary(
                instant = null,
                newCellInstant = newCellInstant,
                newCellsTail = newCellsTail,
            )
        },
        onBoth = fun(
            instant: Instant<A>,
            tail: TimelineSequence<A>,
            newCellInstant: Instant<Cell<A>>,
            newCellsTail: TimelineSequence<Cell<A>>,
        ): TimelineSequence<A> {
            // This cell is potentially changing and a switch is potentially
            // happening

            return tail.switchIfNecessary(
                instant = instant,
                newCellInstant = newCellInstant,
                newCellsTail = newCellsTail,
            )
        },
    ) ?: TimelineSequence.empty()
}
