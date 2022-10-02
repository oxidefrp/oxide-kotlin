package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState

fun momentState(n: Int) = object : MomentState<S, String>() {
    override fun enterDirectly(oldState: S): Moment<Pair<S, String>> =
        object : Moment<Pair<S, String>>() {
            override fun pullDirectly(t: Time): Pair<S, String> {
                val a = "${oldState.sum}@${t.t}/$n"
                val newState = S(sum = oldState.sum + n)
                return Pair(newState, a)
            }
        }
}

// TODO: De-duplicate
data class S(
    val sum: Int,
)
