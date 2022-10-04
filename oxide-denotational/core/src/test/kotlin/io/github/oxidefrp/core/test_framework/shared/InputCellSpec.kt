package io.github.oxidefrp.core.test_framework.shared

import io.github.oxidefrp.core.EventOccurrence

// Idea: Rename to `CellSegmentDesc`?
// Thought: Maybe re-merge with `CellValueDesc`?
internal data class CellValueSpec<out A>(
    // Thought: Shouldn't this be named `fromTick`? And `atTick` in `EventOccurrenceDesc`?
    val tick: Tick,
    val newValue: A,
)

internal abstract class InputCellSpec<A> {
    abstract val newValuesSpec: InputStreamSpec<A>

    abstract val currentValueSpec: InputMomentSpec<A>
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

    override val newValuesSpec: FiniteInputStreamSpec<A> = FiniteInputStreamSpec(
        events = innerValues.map {
            EventOccurrenceDesc(tick = it.tick, event = it.newValue)
        },
    )

    private val lastTick =
        innerValues.lastOrNull()?.tick ?: throw IllegalArgumentException("Input inner values list should not be empty")

    override val currentValueSpec: InputMomentSpec<A> = object : InputMomentSpec<A>() {
        override fun getValue(tick: Tick): A =
            innerValues.filter { it.tick < tick }.maxByOrNull { it.tick }?.newValue ?: initialValue
    }

    private fun withTail(
        tailSpec: InfiniteInputCellSpec<A>,
    ): InputCellSpec<A> = object : InputCellSpec<A>() {
        override val newValuesSpec: InputStreamSpec<A> =
            this@FiniteInputCellSpec.newValuesSpec.withTail(tailSpec.newValuesSpec)

        override val currentValueSpec: InputMomentSpec<A> = object : InputMomentSpec<A>() {
            override fun getValue(tick: Tick): A = when {
                tick <= lastTick || tick == lastTick.next -> this@FiniteInputCellSpec.currentValueSpec.getValue(tick = tick)
                else -> tailSpec.currentValueSpec.getValue(tick = tick)
            }
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
    private fun getOldValue(tick: Tick): A {
        val oldValue = tick.previous?.let(buildNewValue) ?: initialValue
        return oldValue ?: buildNewValue(tick)
    }

    override val newValuesSpec: InputStreamSpec<A> = object : InputStreamSpec<A>() {
        override fun getOccurrence(tick: Tick): EventOccurrence<A>? {
            val oldValue = tick.previous?.let(this@InfiniteInputCellSpec::getOldValue)
            val newValue = buildNewValue(tick)

            return if (oldValue != newValue) EventOccurrence(event = newValue) else null
        }
    }

    override val currentValueSpec: InputMomentSpec<A> = object : InputMomentSpec<A>() {
        override fun getValue(tick: Tick): A = getOldValue(tick = tick)
    }
}
