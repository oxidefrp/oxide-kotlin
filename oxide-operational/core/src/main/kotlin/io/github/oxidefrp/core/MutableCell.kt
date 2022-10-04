package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.Transaction

class MutableCell<A>(initialValue: A) : Cell<A>() {
    private val newValuesEmitter = EventEmitter<A>();

    private var storedValue = initialValue

    fun setValueExternally(newValue: A) {
        newValuesEmitter.emitExternally(newValue)

        storedValue = newValue
    }

    override val currentValue: Moment<A> = object : Moment<A>() {
        override fun pullCurrentValue(transaction: Transaction): A = storedValue
    }

    override val newValues: EventStream<A> = newValuesEmitter
}
