package io.github.oxidefrp.oxide.core.test_framework.shared

import io.github.oxidefrp.oxide.core.EventOccurrence

internal abstract class InputStreamSpec<out E> {
    abstract fun getOccurrence(tick: Tick): EventOccurrence<E>?

    fun hasOccurrence(tick: Tick): Boolean = getOccurrence(tick = tick) != null
}

internal class FiniteInputStreamSpec<out E>(
    val events: List<EventOccurrenceDesc<E>>,
) : InputStreamSpec<E>() {
    init {
        if (!events.toList().isMonotonicallyIncreasingBy { it.tick }) {
            throw IllegalArgumentException("Input stream events ticks need to be monotonically increasing")
        }
    }

    constructor(
        vararg events: EventOccurrenceDesc<E>,
    ) : this(
        events = events.toList(),
    )

    private val eventByTick: Map<Tick, EventOccurrence<E>> = events.associate {
        it.tick to EventOccurrence(it.event)
    }

    val lastTick =
        events.lastOrNull()?.tick ?: throw IllegalArgumentException("Input events list should not be empty")

    override fun getOccurrence(tick: Tick): EventOccurrence<E>? = eventByTick[tick]
}

internal fun <E> FiniteInputStreamSpec<E>.withTail(
    tailSpec: InputStreamSpec<E>,
): InputStreamSpec<E> = object : InputStreamSpec<E>() {
    override fun getOccurrence(tick: Tick): EventOccurrence<E>? =
        if (tick <= lastTick) this@withTail.getOccurrence(tick = tick)
        else tailSpec.getOccurrence(tick)
}

internal fun <E : Any> FiniteInputStreamSpec<E>.withTail(
    build: (Tick) -> E?,
): InputStreamSpec<E> = withTail(
    tailSpec = InfiniteInputStreamSpec(build),
)

internal class InfiniteInputStreamSpec<E : Any>(
    private val build: (Tick) -> E?,
) : InputStreamSpec<E>() {
    override fun getOccurrence(tick: Tick): EventOccurrence<E>? =
        build(tick)?.let(::EventOccurrence)
}
