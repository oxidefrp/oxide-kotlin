package io.github.oxidefrp.core.test_framework

import io.github.oxidefrp.core.impl.Transaction
import io.github.oxidefrp.core.test_framework.shared.Tick

internal abstract class Validator(
    private val startTick: Tick,
) {
    fun spawn(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex {
        val currentTick = tickProvider.currentTick

        if (currentTick != startTick) {
            throw IllegalStateException("Spawned at tick ${currentTick.t}, expected at ${startTick.t}")
        }

        return spawnDirectly(
            tickProvider = tickProvider,
            transaction = transaction,
        )
    }

    protected abstract fun spawnDirectly(
        tickProvider: TickProvider,
        transaction: Transaction,
    ): TestVertex
}
