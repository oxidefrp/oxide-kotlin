package io.github.oxidefrp.core.test_framework.shared

internal abstract class InputMomentSpec<A> {
    abstract fun getValue(tick: Tick): A
}
