package io.github.oxidefrp.semantics.test_framework.shared

internal data class Tick(
    val t: Int,
) : Comparable<Tick> {
    val previous: Tick?
        get() = fromT(t = this.t - 1)

    val next: Tick?
        get() = fromT(t = this.t + 1)

    companion object {
        fun fromT(t: Int): Tick? = t.takeIf { it < maxT }?.let {
            Tick(t = it)
        }

        // The highest t-value
        const val maxT: Int = 1000

        val maxTick = Tick(t = maxT)

        val Zero = Tick(t = 0)
    }

    init {
        if (t !in 0..maxT) {
            throw IllegalArgumentException("Ticks must be in range 0..$maxT")
        }
    }

    override fun compareTo(other: Tick) = compareValuesBy(this, other) { it.t }
}
