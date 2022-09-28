package io.github.oxidefrp.semantics.test_framework

import io.github.oxidefrp.semantics.Time
import io.github.oxidefrp.semantics.test_framework.shared.Tick

internal val Time.asTick: Tick
    get() {
        if (t % 1 != 0.0) throw UnsupportedOperationException("Only times with whole t-values can be converted to ticks")
        return Tick(t = this.t.toInt())
    }

internal val Tick.asTime: Time
    get() = Time(t = this.t.toDouble())
