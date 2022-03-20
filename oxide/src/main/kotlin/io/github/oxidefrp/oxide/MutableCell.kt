package io.github.oxidefrp.oxide

import io.github.oxidefrp.oxide.cell.MutableCellVertex

class MutableCell<A>(initialValue: A) : Cell<A>() {
    override val vertex = MutableCellVertex(initialValue = initialValue)

    fun setValueExternally(newValue: A) {
        vertex.setValue(newValue)
    }
}
