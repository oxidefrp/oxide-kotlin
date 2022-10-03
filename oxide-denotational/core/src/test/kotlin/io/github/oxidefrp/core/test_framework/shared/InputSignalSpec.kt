package io.github.oxidefrp.core.test_framework.shared

internal class InputSignalSpec<A>(
    private val provideValue: (tick: Tick) -> A,
) {
    fun getValue(tick: Tick): A = this.provideValue(tick)
}
