package io.github.oxidefrp.core

sealed interface TimeEndpoint {
    val asOpen: TimeEndpoint

    companion object {
        val startComparator: Comparator<TimeEndpoint> = Comparator { o1, o2 ->
            fun compareUnboundedAndBounded(o1: UnboundedTimeEndpoint, o2: BoundedTimeEndpoint) = -1

            fun compareClosedAndOpen(o1: ClosedTimeEndpoint, o2: OpenTimeEndpoint) =
                if (o1.t == o2.t) -1 else o1.t.compareTo(o2.t)

            fun compareBoundedSameKind(o1: BoundedTimeEndpoint, o2: BoundedTimeEndpoint) =
                o1.t.compareTo(o2.t)

            when {
                o1 is UnboundedTimeEndpoint && o2 is UnboundedTimeEndpoint -> 0
                o1 is UnboundedTimeEndpoint && o2 is ClosedTimeEndpoint -> compareUnboundedAndBounded(o1, o2)
                o1 is UnboundedTimeEndpoint && o2 is OpenTimeEndpoint -> compareUnboundedAndBounded(o1, o2)

                o1 is ClosedTimeEndpoint && o2 is UnboundedTimeEndpoint -> -compareUnboundedAndBounded(o2, o1)
                o1 is ClosedTimeEndpoint && o2 is ClosedTimeEndpoint -> compareBoundedSameKind(o1, o2)
                o1 is ClosedTimeEndpoint && o2 is OpenTimeEndpoint -> compareClosedAndOpen(o1, o2)

                o1 is OpenTimeEndpoint && o2 is UnboundedTimeEndpoint -> -compareUnboundedAndBounded(o2, o1)
                o1 is OpenTimeEndpoint && o2 is ClosedTimeEndpoint -> -compareClosedAndOpen(o2, o1)
                o1 is OpenTimeEndpoint && o2 is OpenTimeEndpoint -> compareBoundedSameKind(o1, o2)

                // Should be unreachable, as all combinations are checked
                else -> throw UnsupportedOperationException("Can't compare $o1 and $o2 time endpoints")
            }
        }

        val endComparator: Comparator<TimeEndpoint> =
            startComparator.reversed()

        fun coerceStartAtLeast(
            endpoint: TimeEndpoint,
            minimumValue: TimeEndpoint,
        ): TimeEndpoint = maxOf(
            endpoint,
            minimumValue,
            comparator = startComparator,
        )

        fun coerceEndAtMost(
            endpoint: TimeEndpoint,
            maximumValue: TimeEndpoint,
        ): TimeEndpoint = minOf(
            endpoint,
            maximumValue,
            comparator = endComparator,
        )
    }

    // Assuming that this is a start endpoint, determine whether [time] is
    // within the interval front-side
    fun isIncludedFrontSide(time: Time): Boolean

    // Assuming that this is an end endpoint, determine whether [time] is
    // within the interval back-side
    fun isIncludedBackSide(time: Time): Boolean
}

sealed class BoundedTimeEndpoint : TimeEndpoint {
    abstract val t: Time
}

data class ClosedTimeEndpoint(
    override val t: Time,
) : BoundedTimeEndpoint() {
    override val asOpen: TimeEndpoint
        get() = OpenTimeEndpoint(t = t)

    override fun isIncludedFrontSide(time: Time): Boolean =
        time >= t

    override fun isIncludedBackSide(time: Time): Boolean =
        time <= t
}

data class OpenTimeEndpoint(
    override val t: Time,
) : BoundedTimeEndpoint() {
    override val asOpen: TimeEndpoint = this

    override fun isIncludedFrontSide(time: Time): Boolean =
        time > t

    override fun isIncludedBackSide(time: Time): Boolean =
        time < t
}

object UnboundedTimeEndpoint : TimeEndpoint {
    override val asOpen: TimeEndpoint = this

    override fun isIncludedFrontSide(time: Time): Boolean =
        true

    override fun isIncludedBackSide(time: Time): Boolean =
        true

    override fun toString(): String = "UnboundedTimeEndpoint"
}

data class TimeInterval(
    val start: TimeEndpoint,
    val end: TimeEndpoint,
) {
    val isNotEmpty: Boolean
        get() {
            val start = this.start
            val end = this.end

            return if (
                start is BoundedTimeEndpoint && end is BoundedTimeEndpoint
            ) {
                when {
                    end.t > start.t -> true
                    start.t == end.t -> start is ClosedTimeEndpoint && end is ClosedTimeEndpoint
                    else -> false
                }
            } else {
                // At least one endpoint is unbounded, so the interval has to be non-empty
                true
            }
        }

    fun contains(time: Time): Boolean =
        start.isIncludedFrontSide(time) && end.isIncludedBackSide(time)

    fun coerceStartAtLeast(endpoint: TimeEndpoint): TimeInterval = this.copy(
        start = maxOf(
            this.start,
            endpoint,
            comparator = TimeEndpoint.startComparator,
        ),
    )

    fun coerceEndAtMost(endpoint: TimeEndpoint): TimeInterval = this.copy(
        end = minOf(
            this.end,
            endpoint,
            comparator = TimeEndpoint.endComparator,
        ),
    )

    fun intersectWith(otherInterval: TimeInterval): TimeInterval =
        coerceStartAtLeast(otherInterval.start).coerceEndAtMost(otherInterval.end)

    fun overlapsWith(otherInterval: TimeInterval): Boolean =
        intersectWith(otherInterval).isNotEmpty

    companion object {
        fun open(
            tMin: Time,
            tMax: Time,
        ) = TimeInterval(
            start = OpenTimeEndpoint(t = tMin),
            end = OpenTimeEndpoint(t = tMax),
        )

        fun closed(
            tMin: Time,
            tMax: Time,
        ) = TimeInterval(
            start = ClosedTimeEndpoint(t = tMin),
            end = ClosedTimeEndpoint(t = tMax),
        )

        fun exactly(
            t: Time,
        ) = closed(tMin = t, tMax = t)

        val unbounded = TimeInterval(
            start = UnboundedTimeEndpoint,
            end = UnboundedTimeEndpoint,
        )
    }
}
