package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.Io
import io.github.oxidefrp.core.shared.MomentIo

object IoUtils {
    fun <A> wrap(executeExternally: () -> A): Io<A> = object : Io<A>() {
        override fun enterDirectly(
            oldState: RealWorld,
        ): Pair<RealWorld, A> = Pair(RealWorld(), executeExternally())
    }
}

fun <A> Io<A>.executeExternally(): A {
    val (_, result) = this.enterDirectly(RealWorld())
    return result
}

fun <A> MomentIo<A>.executeExternally(): A {
    val (_, result) = this.enterDirectly(RealWorld()).pullExternally()
    return result
}
