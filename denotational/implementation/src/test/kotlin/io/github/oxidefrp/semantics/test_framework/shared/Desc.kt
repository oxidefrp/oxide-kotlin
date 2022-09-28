package io.github.oxidefrp.semantics.test_framework.shared

internal data class EventOccurrenceDesc<out A>(
    /**
     * The tick this event occurs at
     */
    val tick: Tick,
    /**
     * The event payload
     */
    val event: A,
)

internal data class CellValueDesc<out A>(
    // Idea: Make this an old value, which can imply a change indirectly
    /**
     * The tick this value starts to be exposed (as the new value)
     */
    val tick: Tick,
    // TODO: Rename to `newValue`?
    /**
     * The exposed value
     */
    val value: A,
) {
    val newValueDesc: EventOccurrenceDesc<A>
        get() = EventOccurrenceDesc(
            tick = tick,
            event = value,
        )
}
