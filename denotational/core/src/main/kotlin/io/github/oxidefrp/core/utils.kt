package io.github.oxidefrp.core

import io.github.oxidefrp.core.shared.MomentState

fun <S, A> MomentState<S, A>.pullEnterDirectly(t: Time, oldState: S): Pair<S, A> =
    enterDirectly(oldState).pullDirectly(t)
