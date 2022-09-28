package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.impl.Vertex
import io.github.oxidefrp.core.test_framework.shared.Issue

internal abstract class TestVertex : Vertex() {
    override fun getDependents(): Iterable<Vertex> = emptyList()

    override fun process(transaction: Transaction) {}

    abstract fun validateRecord(): Issue?
}
