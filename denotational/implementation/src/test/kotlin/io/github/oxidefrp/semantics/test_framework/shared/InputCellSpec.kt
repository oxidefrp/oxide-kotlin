package io.github.oxidefrp.semantics.test_framework.shared

import io.github.oxidefrp.semantics.EventOccurrence

// Idea: Rename to `CellSegmentDesc`?
// Thought: Maybe re-merge with `CellValueDesc`?
internal data class CellValueSpec<out A>(
    // Thought: Shouldn't this be named `fromTick`? And `atTick` in `EventOccurrenceDesc`?
    val tick: Tick,
    val newValue: A,
)

internal abstract class InputCellSpec<E> {
    abstract fun getOldValue(tick: Tick): E

    abstract fun getNewValueOccurrence(tick: Tick): EventOccurrence<E>?
}

internal class FiniteInputCellSpec<A> private constructor(
    /**
     * The actual semantics initial value of the cell, i.e. the value exposed
     * by the cell before the first change.
     */
    private val initialValue: A,
    /**
     * The specification of all inner values of the cell.
     */
    private val innerValues: List<CellValueSpec<A>>,
) : InputCellSpec<A>() {
    init {
        if (!innerValues.toList().isMonotonicallyIncreasingBy { it.tick }) {
            throw IllegalArgumentException("Input values ticks need to be monotonically increasing")
        }
    }

    constructor(
        initialValue: A,
        vararg innerValues: CellValueSpec<A>,
    ) : this(
        initialValue = initialValue,
        innerValues = innerValues.toList(),
    )

    private val newValuesSpec = FiniteInputStreamSpec(
        events = innerValues.map {
            EventOccurrenceDesc(tick = it.tick, event = it.newValue)
        },
    )

    private val lastTick =
        innerValues.lastOrNull()?.tick ?: throw IllegalArgumentException("Input inner values list should not be empty")

    override fun getOldValue(tick: Tick): A =
        innerValues.filter { it.tick < tick }.maxByOrNull { it.tick }?.newValue ?: initialValue

    override fun getNewValueOccurrence(tick: Tick): EventOccurrence<A>? = newValuesSpec.getOccurrence(tick = tick)

    private fun withTail(
        tailSpec: InfiniteInputCellSpec<A>,
    ): InputCellSpec<A> = object : InputCellSpec<A>() {
        override fun getOldValue(tick: Tick): A = when {
            tick <= lastTick || tick == lastTick.next -> this@FiniteInputCellSpec.getOldValue(tick = tick)
            else -> tailSpec.getOldValue(tick = tick)
        }

        override fun getNewValueOccurrence(tick: Tick): EventOccurrence<A>? = when {
            tick <= lastTick -> this@FiniteInputCellSpec.getNewValueOccurrence(tick = tick)
            else -> tailSpec.getNewValueOccurrence(tick = tick)
        }
    }

    fun withTail(
        buildNewValue: (Tick) -> A,
    ): InputCellSpec<A> = withTail(
        tailSpec = InfiniteInputCellSpec(buildNewValue = buildNewValue),
    )
}

internal class InfiniteInputCellSpec<A>(
    /**
     * The initial value of the cell. If not provided, the initial value will
     * be the new value built at tick zero.
     */
    private val initialValue: A? = null,
    /**
     * A function building the new value at the given tick. The actual input
     * cell will only emit a new value if new values built for two consecutive
     * ticks are not equal.
     */
    private val buildNewValue: (Tick) -> A,
) : InputCellSpec<A>() {
    override fun getOldValue(tick: Tick): A {
        val oldValue = tick.previous?.let(buildNewValue) ?: initialValue
        return oldValue ?: buildNewValue(tick)
    }

    override fun getNewValueOccurrence(tick: Tick): EventOccurrence<A>? {
        val oldValue = tick.previous?.let(this::getOldValue)
        val newValue = buildNewValue(tick)

        return if (oldValue != newValue) EventOccurrence(event = newValue) else null
    }
}
