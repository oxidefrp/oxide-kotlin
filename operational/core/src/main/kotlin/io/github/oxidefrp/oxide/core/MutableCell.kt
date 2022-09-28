package io.github.oxidefrp.oxide.core

import io.github.oxidefrp.oxide.core.impl.cell.MutableCellVertex

class MutableCell<A>(initialValue: A) : Cell<A>() {
    override val vertex = MutableCellVertex(initialValue = initialValue)

    fun setValueExternally(newValue: A) {
        vertex.setValue(newValue)
    }
}
