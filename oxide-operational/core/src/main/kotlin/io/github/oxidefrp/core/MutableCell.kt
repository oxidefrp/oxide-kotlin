package io.github.oxidefrp.core

import io.github.oxidefrp.core.impl.cell.MutableCellVertex

class MutableCell<A>(initialValue: A) : Cell<A>() {
    override val vertex = MutableCellVertex(initialValue = initialValue)

    fun setValueExternally(newValue: A) {
        vertex.setValue(newValue)
    }
}
